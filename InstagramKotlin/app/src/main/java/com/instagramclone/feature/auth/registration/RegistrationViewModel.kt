package com.instagramclone.feature.auth.registration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramclone.feature.auth.domain.AuthField
import com.instagramclone.feature.auth.domain.AuthOperationResult
import com.instagramclone.feature.auth.domain.AuthRepository
import com.instagramclone.feature.auth.domain.AuthValidators
import com.instagramclone.feature.auth.domain.RegistrationDraft
import com.instagramclone.feature.auth.domain.RegistrationStep
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationUiState(
    val draft: RegistrationDraft = RegistrationDraft(),
    val currentStep: RegistrationStep = RegistrationStep.EMAIL,
    val passwordVisible: Boolean = false,
    val fieldErrors: Map<AuthField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val formMessage: String? = null,
    val resendSeconds: Int = 0,
    val birthdayInput: String = "",
)

sealed interface RegistrationEvent {
    data class EmailChanged(val value: String) : RegistrationEvent
    data class OtpChanged(val value: String) : RegistrationEvent
    data class PasswordChanged(val value: String) : RegistrationEvent
    data class BirthdayChanged(val value: String) : RegistrationEvent
    data class UsernameChanged(val value: String) : RegistrationEvent
    data object TogglePassword : RegistrationEvent
    data object Next : RegistrationEvent
    data object VerifyOtp : RegistrationEvent
    data object ResendOtp : RegistrationEvent
    data object AgreeAndCreate : RegistrationEvent
    data object Back : RegistrationEvent
    data object Cancel : RegistrationEvent
}

sealed interface RegistrationEffect {
    data class Navigate(val step: RegistrationStep) : RegistrationEffect
    data class BackTo(val step: RegistrationStep) : RegistrationEffect
    data class Completed(val message: String) : RegistrationEffect
    data object BackToSignIn : RegistrationEffect
}

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val initialStep = savedStateHandle.get<String>(KEY_STEP)
        ?.let { runCatching { RegistrationStep.valueOf(it) }.getOrNull() }
        ?: RegistrationStep.EMAIL
    private val initialBirthday = savedStateHandle.get<String>(KEY_BIRTHDAY)
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    private val _state = MutableStateFlow(
        RegistrationUiState(
            currentStep = initialStep,
            draft = RegistrationDraft(
                email = savedStateHandle[KEY_EMAIL] ?: "",
                username = savedStateHandle[KEY_USERNAME] ?: "",
                birthday = initialBirthday,
                otpVerified = savedStateHandle[KEY_VERIFIED] ?: false,
            ),
            birthdayInput = savedStateHandle[KEY_BIRTHDAY] ?: "",
        ),
    )
    val state: StateFlow<RegistrationUiState> = _state.asStateFlow()
    private val _effects = Channel<RegistrationEffect>(Channel.BUFFERED)
    val effects: Flow<RegistrationEffect> = _effects.receiveAsFlow()
    private var cooldownJob: Job? = null

    fun onEvent(event: RegistrationEvent) {
        when (event) {
            is RegistrationEvent.EmailChanged -> updateDraft(AuthField.EMAIL) { copy(email = event.value) }
            is RegistrationEvent.OtpChanged -> updateDraft(AuthField.OTP) { copy(otp = event.value) }
            is RegistrationEvent.PasswordChanged -> updateDraft(AuthField.PASSWORD) { copy(password = event.value) }
            is RegistrationEvent.BirthdayChanged -> {
                val parsed = runCatching { LocalDate.parse(event.value) }.getOrNull()
                _state.update {
                    it.copy(
                        birthdayInput = event.value,
                        draft = it.draft.copy(birthday = parsed),
                        fieldErrors = it.fieldErrors - AuthField.BIRTHDAY,
                        formMessage = null,
                    )
                }
                savedStateHandle[KEY_BIRTHDAY] = event.value
            }
            is RegistrationEvent.UsernameChanged -> updateDraft(AuthField.USERNAME) { copy(username = event.value) }
            RegistrationEvent.TogglePassword -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            RegistrationEvent.Next -> next()
            RegistrationEvent.VerifyOtp -> verifyOtp()
            RegistrationEvent.ResendOtp -> resendOtp()
            RegistrationEvent.AgreeAndCreate -> createAccount()
            RegistrationEvent.Back -> back()
            RegistrationEvent.Cancel -> clearAndReturn()
        }
    }

    private fun next() {
        when (_state.value.currentStep) {
            RegistrationStep.EMAIL -> sendOtp()
            RegistrationStep.PASSWORD -> validateAndMove(AuthValidators.password(_state.value.draft.password), RegistrationStep.BIRTHDAY)
            RegistrationStep.BIRTHDAY -> validateAndMove(AuthValidators.birthday(_state.value.draft.birthday), RegistrationStep.USERNAME)
            RegistrationStep.USERNAME -> validateAndMove(AuthValidators.username(_state.value.draft.username), RegistrationStep.TERMS)
            else -> Unit
        }
    }

    private fun sendOtp() {
        val error = AuthValidators.email(_state.value.draft.email)
        if (error != null) return showFieldError(error.field, error.message)
        submit(
            operation = { repository.sendOtp(_state.value.draft.email) },
            onSuccess = {
                startCooldown()
                moveTo(RegistrationStep.OTP)
            },
        )
    }

    private fun verifyOtp() {
        if (_state.value.currentStep != RegistrationStep.OTP) return
        val error = AuthValidators.otp(_state.value.draft.otp)
        if (error != null) return showFieldError(error.field, error.message)
        submit(
            operation = { repository.verifyOtp(_state.value.draft.email, _state.value.draft.otp) },
            onSuccess = {
                _state.update { it.copy(draft = it.draft.copy(otpVerified = true, otp = "")) }
                savedStateHandle[KEY_VERIFIED] = true
                moveTo(RegistrationStep.PASSWORD)
            },
        )
    }

    private fun resendOtp() {
        if (_state.value.currentStep != RegistrationStep.OTP || _state.value.resendSeconds > 0) return
        submit(
            operation = { repository.sendOtp(_state.value.draft.email) },
            onSuccess = ::startCooldown,
        )
    }

    private fun createAccount() {
        if (_state.value.currentStep != RegistrationStep.TERMS) return
        submit(
            operation = { repository.register(_state.value.draft) },
            onSuccess = {
                clearSensitiveAndSavedState()
                viewModelScope.launch {
                    _effects.send(RegistrationEffect.Completed("Account created. You can now log in."))
                }
            },
        )
    }

    private fun submit(operation: suspend () -> AuthOperationResult, onSuccess: () -> Unit) {
        if (_state.value.isSubmitting) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, formMessage = null, fieldErrors = emptyMap()) }
            when (val result = operation()) {
                AuthOperationResult.Success -> onSuccess()
                is AuthOperationResult.Failure -> _state.update { it.copy(formMessage = result.message) }
                is AuthOperationResult.ValidationError -> _state.update {
                    it.copy(fieldErrors = result.errors.associate { error -> error.field to error.message })
                }
            }
            _state.update { it.copy(isSubmitting = false) }
        }
    }

    private fun validateAndMove(error: com.instagramclone.feature.auth.domain.FieldError?, next: RegistrationStep) {
        if (error != null) showFieldError(error.field, error.message) else moveTo(next)
    }

    private fun moveTo(step: RegistrationStep) {
        _state.update { it.copy(currentStep = step, fieldErrors = emptyMap(), formMessage = null) }
        savedStateHandle[KEY_STEP] = step.name
        saveNonSensitiveDraft()
        viewModelScope.launch { _effects.send(RegistrationEffect.Navigate(step)) }
    }

    private fun back() {
        val previous = when (_state.value.currentStep) {
            RegistrationStep.EMAIL -> null
            RegistrationStep.OTP -> RegistrationStep.EMAIL
            RegistrationStep.PASSWORD -> RegistrationStep.OTP
            RegistrationStep.BIRTHDAY -> RegistrationStep.PASSWORD
            RegistrationStep.USERNAME -> RegistrationStep.BIRTHDAY
            RegistrationStep.TERMS -> RegistrationStep.USERNAME
        }
        if (previous == null) {
            clearAndReturn()
        } else {
            _state.update { it.copy(currentStep = previous, fieldErrors = emptyMap(), formMessage = null) }
            savedStateHandle[KEY_STEP] = previous.name
            viewModelScope.launch { _effects.send(RegistrationEffect.BackTo(previous)) }
        }
    }

    private fun clearAndReturn() {
        clearSensitiveAndSavedState()
        viewModelScope.launch { _effects.send(RegistrationEffect.BackToSignIn) }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (seconds in RESEND_COOLDOWN_SECONDS downTo 0) {
                _state.update { it.copy(resendSeconds = seconds) }
                if (seconds > 0) delay(1_000)
            }
        }
    }

    private fun updateDraft(field: AuthField, update: RegistrationDraft.() -> RegistrationDraft) {
        _state.update { it.copy(draft = it.draft.update(), fieldErrors = it.fieldErrors - field, formMessage = null) }
        if (field !in setOf(AuthField.PASSWORD, AuthField.OTP)) saveNonSensitiveDraft()
    }

    private fun showFieldError(field: AuthField, message: String) {
        _state.update { it.copy(fieldErrors = mapOf(field to message)) }
    }

    private fun saveNonSensitiveDraft() {
        val draft = _state.value.draft
        savedStateHandle[KEY_EMAIL] = draft.email
        savedStateHandle[KEY_USERNAME] = draft.username
        savedStateHandle[KEY_BIRTHDAY] = _state.value.birthdayInput
        savedStateHandle[KEY_VERIFIED] = draft.otpVerified
    }

    private fun clearSensitiveAndSavedState() {
        cooldownJob?.cancel()
        listOf(KEY_EMAIL, KEY_USERNAME, KEY_BIRTHDAY, KEY_VERIFIED, KEY_STEP)
            .forEach { key -> savedStateHandle.remove<Any?>(key) }
        _state.value = RegistrationUiState()
    }

    private companion object {
        const val RESEND_COOLDOWN_SECONDS = 60
        const val KEY_EMAIL = "registration.email"
        const val KEY_USERNAME = "registration.username"
        const val KEY_BIRTHDAY = "registration.birthday"
        const val KEY_VERIFIED = "registration.verified"
        const val KEY_STEP = "registration.step"
    }
}
