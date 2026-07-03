package com.instagramclone.core.network

import com.instagramclone.core.session.SessionTokenProvider
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val tokenProvider: SessionTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.tag(AuthRequired::class.java) == null) return chain.proceed(request)
        val token = tokenProvider.currentToken()
        if (token.isNullOrBlank()) return chain.proceed(request)
        return chain.proceed(
            request.newBuilder()
                .header(AUTHORIZATION_HEADER, "Bearer $token")
                .build(),
        )
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}
