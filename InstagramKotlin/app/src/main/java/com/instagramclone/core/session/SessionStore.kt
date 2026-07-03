package com.instagramclone.core.session

interface SessionStore {
    suspend fun readToken(): String?
    suspend fun saveToken(token: String)
    suspend fun clear()
}

fun interface SessionCleanup {
    suspend fun onSessionCleared()
}
