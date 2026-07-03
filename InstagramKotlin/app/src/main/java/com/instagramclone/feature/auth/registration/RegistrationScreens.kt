package com.instagramclone.feature.auth.registration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import com.instagramclone.feature.auth.components.AuthFormMessage
import com.instagramclone.feature.auth.components.AuthPrimaryButton
import com.instagramclone.feature.auth.components.AuthScreenScaffold
import com.instagramclone.feature.auth.domain.AuthField

@Composable
fun EmailScreen(state: RegistrationUiState, onEvent: (RegistrationEvent) -> Unit) {
    AuthScreenScaffold(
        title = "What's your email?",
        description = "Enter the email where you can be contacted.",
        onBack = { onEvent(RegistrationEvent.Back) },
    ) {
        AuthTextField(
            value = state.draft.email,
            onValueChange = { onEvent(RegistrationEvent.EmailChanged(it)) },
            label = "Email",
            error = state.fieldErrors[AuthField.EMAIL],
            keyboardType = KeyboardType.Email,
            testTag = "registration-email-input",
        )
        AuthFormMessage(state.formMessage)
        AuthPrimaryButton("Next", state.isSubmitting, { onEvent(RegistrationEvent.Next) }, "registration-email-next-button")
    }
}

@Composable
fun OtpScreen(state: RegistrationUiState, onEvent: (RegistrationEvent) -> Unit) {
    AuthScreenScaffold(
        title = "Enter your confirmation code",
        description = "We sent a code to ${state.draft.email}.",
        onBack = { onEvent(RegistrationEvent.Back) },
    ) {
        AuthTextField(
            value = state.draft.otp,
            onValueChange = { onEvent(RegistrationEvent.OtpChanged(it)) },
            label = "Confirmation code",
            error = state.fieldErrors[AuthField.OTP],
            keyboardType = KeyboardType.Number,
            testTag = "registration-otp-input",
        )
        AuthFormMessage(state.formMessage)
        AuthPrimaryButton("Next", state.isSubmitting, { onEvent(RegistrationEvent.VerifyOtp) }, "registration-otp-next-button")
        TextButton(
            onClick = { onEvent(RegistrationEvent.ResendOtp) },
            enabled = state.resendSeconds == 0 && !state.isSubmitting,
            modifier = Modifier.testTag("registration-resend-button"),
        ) {
            Text(if (state.resendSeconds > 0) "Resend in ${state.resendSeconds}s" else "Resend code")
        }
    }
}

@Composable
fun PasswordScreen(state: RegistrationUiState, onEvent: (RegistrationEvent) -> Unit) {
    AuthScreenScaffold(
        title = "Create a password",
        description = "Use at least 6 characters.",
        onBack = { onEvent(RegistrationEvent.Back) },
    ) {
        OutlinedTextField(
            value = state.draft.password,
            onValueChange = { onEvent(RegistrationEvent.PasswordChanged(it)) },
            label = { Text("Password") },
            singleLine = true,
            isError = state.fieldErrors[AuthField.PASSWORD] != null,
            supportingText = { state.fieldErrors[AuthField.PASSWORD]?.let { Text(it) } },
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = { onEvent(RegistrationEvent.TogglePassword) },
                    modifier = Modifier.testTag("registration-password-visibility-button"),
                ) {
                    Icon(
                        if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (state.passwordVisible) "Hide password" else "Show password",
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().testTag("registration-password-input"),
        )
        AuthPrimaryButton("Next", false, { onEvent(RegistrationEvent.Next) }, "registration-password-next-button")
    }
}

@Composable
fun BirthdayScreen(state: RegistrationUiState, onEvent: (RegistrationEvent) -> Unit) {
    AuthScreenScaffold(
        title = "What's your birthday?",
        description = "You must be at least 16. Use YYYY-MM-DD.",
        onBack = { onEvent(RegistrationEvent.Back) },
    ) {
        AuthTextField(
            value = state.birthdayInput,
            onValueChange = { onEvent(RegistrationEvent.BirthdayChanged(it)) },
            label = "Birthday",
            error = state.fieldErrors[AuthField.BIRTHDAY],
            keyboardType = KeyboardType.Number,
            testTag = "registration-birthday-input",
        )
        AuthPrimaryButton("Next", false, { onEvent(RegistrationEvent.Next) }, "registration-birthday-next-button")
    }
}

@Composable
fun UsernameScreen(state: RegistrationUiState, onEvent: (RegistrationEvent) -> Unit) {
    AuthScreenScaffold(
        title = "Create a username",
        description = "Use letters, numbers, dots, underscores or hyphens.",
        onBack = { onEvent(RegistrationEvent.Back) },
    ) {
        AuthTextField(
            value = state.draft.username,
            onValueChange = { onEvent(RegistrationEvent.UsernameChanged(it)) },
            label = "Username",
            error = state.fieldErrors[AuthField.USERNAME],
            testTag = "registration-username-input",
        )
        AuthPrimaryButton("Next", false, { onEvent(RegistrationEvent.Next) }, "registration-username-next-button")
    }
}

@Composable
fun TermsScreen(state: RegistrationUiState, onEvent: (RegistrationEvent) -> Unit) {
    AuthScreenScaffold(
        title = "Agree to our terms and policies",
        description = "By signing up you agree to our Terms, Privacy Policy and Cookies Policy.",
        onBack = { onEvent(RegistrationEvent.Back) },
    ) {
        AuthFormMessage(state.formMessage)
        AuthPrimaryButton(
            label = "I agree",
            loading = state.isSubmitting,
            onClick = { onEvent(RegistrationEvent.AgreeAndCreate) },
            testTag = "registration-agree-button",
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    testTag: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth().testTag(testTag),
    )
}
