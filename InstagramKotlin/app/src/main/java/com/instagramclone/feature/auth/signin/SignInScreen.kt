package com.instagramclone.feature.auth.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.instagramclone.feature.auth.domain.AuthField

@Composable
fun SignInScreen(
    state: SignInUiState,
    onEvent: (SignInEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .testTag("sign-in-screen-container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Instagram", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.padding(top = 28.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(SignInEvent.EmailChanged(it)) },
            label = { Text("Email") },
            singleLine = true,
            isError = AuthField.EMAIL in state.fieldErrors,
            supportingText = { state.fieldErrors[AuthField.EMAIL]?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth().testTag("sign-in-email-input"),
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(SignInEvent.PasswordChanged(it)) },
            label = { Text("Password") },
            singleLine = true,
            isError = AuthField.PASSWORD in state.fieldErrors,
            supportingText = { state.fieldErrors[AuthField.PASSWORD]?.let { Text(it) } },
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = { onEvent(SignInEvent.TogglePassword) },
                    modifier = Modifier.testTag("sign-in-password-visibility-button"),
                ) {
                    Icon(
                        imageVector = if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (state.passwordVisible) "Hide password" else "Show password",
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onEvent(SignInEvent.Submit)
            }),
            modifier = Modifier.fillMaxWidth().testTag("sign-in-password-input"),
        )
        state.formMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        }
        Button(
            onClick = { onEvent(SignInEvent.Submit) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().testTag("sign-in-submit-button"),
        ) {
            if (state.isSubmitting) CircularProgressIndicator() else Text("Log in")
        }
        TextButton(
            onClick = { onEvent(SignInEvent.OpenRegistration) },
            enabled = !state.isSubmitting,
            modifier = Modifier.testTag("sign-in-create-account-button"),
        ) {
            Text("Create new account")
        }
    }
}
