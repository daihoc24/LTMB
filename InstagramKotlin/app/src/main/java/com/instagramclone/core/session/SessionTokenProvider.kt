package com.instagramclone.core.session

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTokenProvider @Inject constructor() {
    @Volatile private var token: String? = null

    fun currentToken(): String? = token

    fun update(token: String?) {
        this.token = token
    }
}
