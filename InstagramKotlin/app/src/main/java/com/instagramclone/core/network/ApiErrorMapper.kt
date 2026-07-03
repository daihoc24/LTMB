package com.instagramclone.core.network

import com.instagramclone.BuildConfig
import com.squareup.moshi.JsonDataException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
class ApiErrorMapper @Inject constructor() {
    fun <T : Any> fromResponse(
        response: Response<ApiEnvelope<T>>,
        context: SafeErrorContext,
    ): ApiResult<T> {
        if (response.code() == 401 || response.code() == 403) {
            return ApiResult.Unauthorized()
        }
        if (!response.isSuccessful) {
            return if (response.code() >= 500) {
                ApiResult.ServerError(response.code())
            } else {
                ApiResult.ClientError(response.code(), "The request could not be completed.")
            }
        }

        val envelope = response.body() ?: return ApiResult.DataError()
        if (envelope.code == 9999) {
            return ApiResult.UncategorizedError(safeContext = context.copy(httpStatus = response.code()))
        }
        if (envelope.code != 200) {
            return ApiResult.BusinessError(
                code = envelope.code,
                userMessage = envelope.message?.takeIf(String::isNotBlank)
                    ?: "The request could not be completed.",
            )
        }
        return envelope.result?.let { ApiResult.Success(it) } ?: ApiResult.DataError()
    }

    fun fromThrowable(throwable: Throwable): ApiResult<Nothing> = when (throwable) {
        is IOException -> ApiResult.NetworkError(
            userMessage = if (BuildConfig.DEBUG) {
                "Network ${throwable.javaClass.simpleName}: ${throwable.message ?: "unknown error"}"
            } else {
                "Unable to connect. Check your connection and try again."
            },
        )
        is JsonDataException -> ApiResult.DataError()
        else -> ApiResult.UncategorizedError()
    }
}
