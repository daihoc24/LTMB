package com.instagramclone.feature.auth.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramclone.feature.auth.domain.AuthField
import com.instagramclone.feature.auth.domain.AuthOperationResult
import com.instagramclone.feature.auth.domain.AuthRepository
import com.instagramclone.feature.auth.domain.AuthValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val fieldErrors: Map<AuthField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val formMessage: String? = null,
)

sealed interface SignInEvent {
    data class EmailChanged(val value: String) : SignInEvent
    data class PasswordChanged(val value: String) : SignInEvent
    data object TogglePassword : SignInEvent
    data object Submit : SignInEvent
    data object OpenRegistration : SignInEvent
}

sealed interface SignInEffect {
    data object OpenRegistration : SignInEffect
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()
    private val _effects = Channel<SignInEffect>(Channel.BUFFERED)
    val effects: Flow<SignInEffect> = _effects.receiveAsFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.EmailChanged -> _state.update {
                it.copy(email = event.value, fieldErrors = it.fieldErrors - AuthField.EMAIL, formMessage = null)
            }
            is SignInEvent.PasswordChanged -> _state.update {
                it.copy(password = event.value, fieldErrors = it.fieldErrors - AuthField.PASSWORD, formMessage = null)
            }
            SignInEvent.TogglePassword -> _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            SignInEvent.Submit -> submit()
            SignInEvent.OpenRegistration -> viewModelScope.launch { _effects.send(SignInEffect.OpenRegistration) }
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isSubmitting) return
        val errors = listOfNotNull(
            AuthValidators.email(current.email),
            AuthValidators.password(current.password),
        ).associate { it.field to it.message }
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, fieldErrors = emptyMap(), formMessage = null) }
            when (val result = repository.login(current.email, current.password)) {
                AuthOperationResult.Success -> Unit // U-02 session state replaces the root graph.
                is AuthOperationResult.Failure -> _state.update { it.copy(formMessage = result.message) }
                is AuthOperationResult.ValidationError -> _state.update {
                    it.copy(fieldErrors = result.errors.associate { error -> error.field to error.message })
                }
            }
            _state.update { it.copy(isSubmitting = false) }
        }
    }
}
