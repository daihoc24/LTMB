package com.instagramclone.data.repository

import com.instagramclone.core.network.ApiErrorMapper
import com.instagramclone.core.network.ApiResult
import com.instagramclone.core.network.SafeErrorContext
import com.instagramclone.core.session.SessionRepository
import com.instagramclone.core.session.SessionState
import com.instagramclone.data.remote.auth.AuthApi
import com.instagramclone.data.remote.auth.AuthenticationRequestDto
import com.instagramclone.data.remote.auth.RegistrationApi
import com.instagramclone.data.remote.auth.UserCreateRequestDto
import com.instagramclone.data.remote.auth.VerificationApi
import com.instagramclone.data.remote.auth.VerifyEmailRequestDto
import com.instagramclone.feature.auth.domain.AuthOperationResult
import com.instagramclone.feature.auth.domain.AuthRepository
import com.instagramclone.feature.auth.domain.RegistrationDraft
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val verificationApi: VerificationApi,
    private val registrationApi: RegistrationApi,
    private val errorMapper: ApiErrorMapper,
    private val sessionRepository: SessionRepository,
) : AuthRepository {
    override suspend fun login(email: String, password: String): AuthOperationResult = apiCall {
        when (val result = errorMapper.fromResponse(
            authApi.login(AuthenticationRequestDto(normalizeEmail(email), password)),
            SafeErrorContext("POST", "v1/auth/token"),
        )) {
            is ApiResult.Success -> {
                val candidate = result.value
                if (!candidate.authenticated || candidate.token.isNullOrBlank()) {
                    AuthOperationResult.Failure("Email or password is incorrect.", recoverable = false)
                } else {
                    when (val session = sessionRepository.establishSession(candidate.token)) {
                        is SessionState.Authenticated -> AuthOperationResult.Success
                        is SessionState.RecoverableError -> AuthOperationResult.Failure(session.message)
                        else -> AuthOperationResult.Failure("Unable to start your session. Please try again.")
                    }
                }
            }
            else -> result.toAuthFailure()
        }
    }

    override suspend fun sendOtp(email: String): AuthOperationResult = apiCall {
        when (val result = errorMapper.fromResponse(
            verificationApi.sendCode(VerifyEmailRequestDto(normalizeEmail(email))),
            SafeErrorContext("POST", "v1/verification/send-code"),
        )) {
            is ApiResult.Success -> AuthOperationResult.Success
            else -> result.toAuthFailure()
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): AuthOperationResult = apiCall {
        when (val result = errorMapper.fromResponse(
            verificationApi.verifyCode(otp.trim(), normalizeEmail(email)),
            SafeErrorContext("GET", "v1/verification/verify"),
        )) {
            is ApiResult.Success -> if (result.value) {
                AuthOperationResult.Success
            } else {
                AuthOperationResult.Failure("The verification code is incorrect.", recoverable = false)
            }
            else -> result.toAuthFailure()
        }
    }

    override suspend fun register(draft: RegistrationDraft): AuthOperationResult = apiCall {
        val birthday = draft.birthday
            ?: return@apiCall AuthOperationResult.Failure("Birthday is required.", recoverable = false)
        when (val result = errorMapper.fromResponse(
            registrationApi.createUser(
                UserCreateRequestDto(
                    username = draft.username.trim(),
                    email = normalizeEmail(draft.email),
                    password = draft.password,
                    birthday = birthday.toString(),
                ),
            ),
            SafeErrorContext("POST", "v1/users"),
        )) {
            is ApiResult.Success -> AuthOperationResult.Success
            else -> result.toAuthFailure()
        }
    }

    private suspend fun apiCall(block: suspend () -> AuthOperationResult): AuthOperationResult = try {
        block()
    } catch (throwable: Throwable) {
        errorMapper.fromThrowable(throwable).toAuthFailure()
    }

    private fun ApiResult<*>.toAuthFailure(): AuthOperationResult.Failure = when (this) {
        is ApiResult.BusinessError -> AuthOperationResult.Failure(userMessage, recoverable = false)
        is ApiResult.ClientError -> AuthOperationResult.Failure(userMessage, recoverable = false)
        is ApiResult.DataError -> AuthOperationResult.Failure(userMessage)
        is ApiResult.NetworkError -> AuthOperationResult.Failure(userMessage)
        is ApiResult.ServerError -> AuthOperationResult.Failure(userMessage)
        is ApiResult.Unauthorized -> AuthOperationResult.Failure(userMessage, recoverable = false)
        is ApiResult.UncategorizedError -> AuthOperationResult.Failure(userMessage)
        is ApiResult.Success -> error("Success cannot be converted to failure")
    }

    private fun normalizeEmail(value: String): String = value.trim().lowercase(Locale.ROOT)
}
