package com.instagramclone.feature.auth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.instagramclone.feature.auth.domain.RegistrationStep
import com.instagramclone.feature.auth.registration.BirthdayScreen
import com.instagramclone.feature.auth.registration.EmailScreen
import com.instagramclone.feature.auth.registration.OtpScreen
import com.instagramclone.feature.auth.registration.PasswordScreen
import com.instagramclone.feature.auth.registration.RegistrationEffect
import com.instagramclone.feature.auth.registration.RegistrationViewModel
import com.instagramclone.feature.auth.registration.TermsScreen
import com.instagramclone.feature.auth.registration.UsernameScreen
import com.instagramclone.feature.auth.signin.SignInEffect
import com.instagramclone.feature.auth.signin.SignInScreen
import com.instagramclone.feature.auth.signin.SignInViewModel
import kotlinx.coroutines.flow.collectLatest

private const val SIGN_IN_ROUTE = "auth/sign-in"
private const val REGISTRATION_GRAPH = "auth/registration"

@Composable
fun AuthNavigation(
    showMessage: (String) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SIGN_IN_ROUTE) {
        composable(SIGN_IN_ROUTE) {
            val viewModel = hiltViewModel<SignInViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(viewModel) {
                viewModel.effects.collectLatest { effect ->
                    if (effect == SignInEffect.OpenRegistration) {
                        navController.navigate(REGISTRATION_GRAPH) { launchSingleTop = true }
                    }
                }
            }
            SignInScreen(state = state, onEvent = viewModel::onEvent)
        }
        navigation(
            route = REGISTRATION_GRAPH,
            startDestination = RegistrationStep.EMAIL.route,
        ) {
            composable(RegistrationStep.EMAIL.route) { RegistrationRoute(it, navController, showMessage) { state, event -> EmailScreen(state, event) } }
            composable(RegistrationStep.OTP.route) { RegistrationRoute(it, navController, showMessage) { state, event -> OtpScreen(state, event) } }
            composable(RegistrationStep.PASSWORD.route) { RegistrationRoute(it, navController, showMessage) { state, event -> PasswordScreen(state, event) } }
            composable(RegistrationStep.BIRTHDAY.route) { RegistrationRoute(it, navController, showMessage) { state, event -> BirthdayScreen(state, event) } }
            composable(RegistrationStep.USERNAME.route) { RegistrationRoute(it, navController, showMessage) { state, event -> UsernameScreen(state, event) } }
            composable(RegistrationStep.TERMS.route) { RegistrationRoute(it, navController, showMessage) { state, event -> TermsScreen(state, event) } }
        }
    }
}

private val RegistrationStep.route: String get() = "auth/registration/${name.lowercase()}"

@Composable
private fun RegistrationRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    showMessage: (String) -> Unit,
    content: @Composable (
        com.instagramclone.feature.auth.registration.RegistrationUiState,
        (com.instagramclone.feature.auth.registration.RegistrationEvent) -> Unit,
    ) -> Unit,
) {
    val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(REGISTRATION_GRAPH) }
    val viewModel = hiltViewModel<RegistrationViewModel>(parentEntry)
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is RegistrationEffect.Navigate -> navController.navigate(effect.step.route) { launchSingleTop = true }
                is RegistrationEffect.BackTo -> {
                    if (!navController.popBackStack(effect.step.route, inclusive = false)) {
                        navController.navigate(effect.step.route) { launchSingleTop = true }
                    }
                }
                RegistrationEffect.BackToSignIn -> navController.popBackStack(SIGN_IN_ROUTE, inclusive = false)
                is RegistrationEffect.Completed -> {
                    navController.popBackStack(SIGN_IN_ROUTE, inclusive = false)
                    showMessage(effect.message)
                }
            }
        }
    }
    content(state, viewModel::onEvent)
}
