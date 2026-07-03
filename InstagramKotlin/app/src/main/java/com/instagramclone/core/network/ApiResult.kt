package com.instagramclone.core.network

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class BusinessError(val code: Int, val userMessage: String) : ApiResult<Nothing>
    data class Unauthorized(val userMessage: String = "Your session has expired. Please sign in again.") : ApiResult<Nothing>
    data class ClientError(val httpCode: Int, val userMessage: String) : ApiResult<Nothing>
    data class NetworkError(val userMessage: String = "Unable to connect. Check your connection and try again.") : ApiResult<Nothing>
    data class ServerError(val httpCode: Int, val userMessage: String = "The server is unavailable. Please try again.") : ApiResult<Nothing>
    data class UncategorizedError(
        val code: Int = 9999,
        val userMessage: String = "Something went wrong. Please try again.",
        val safeContext: SafeErrorContext? = null,
    ) : ApiResult<Nothing>
    data class DataError(val userMessage: String = "The server returned invalid data. Please try again.") : ApiResult<Nothing>
}

data class SafeErrorContext(
    val method: String? = null,
    val path: String? = null,
    val httpStatus: Int? = null,
)
