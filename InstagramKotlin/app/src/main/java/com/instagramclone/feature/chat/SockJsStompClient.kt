package com.instagramclone.feature.chat

import com.instagramclone.BuildConfig
import com.instagramclone.data.remote.chat.SendMessageDto
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import android.util.Log

@Singleton
class SockJsStompClient @Inject constructor(private val client: OkHttpClient, moshi: Moshi) {
    private val adapter = moshi.adapter(SendMessageDto::class.java)
    private var socket: WebSocket? = null
    private var onSignal: (() -> Unit)? = null
    private var connected = false

    fun connect(onSignal: () -> Unit, onState: (Boolean) -> Unit) {
        close(); this.onSignal = onSignal
        socket = client.newWebSocket(Request.Builder().url(BuildConfig.CHAT_WS_URL).build(), object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send("CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\u0000")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                when {
                    text.startsWith("CONNECTED") -> {
                        connected = true
                        onState(true)
                        sendFrame("SUBSCRIBE\nid:chat-0\ndestination:/topic/messages\nack:auto\n\n\u0000")
                    }
                    text.startsWith("MESSAGE") -> onSignal()
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatWebSocket", "Connection failed: HTTP ${response?.code}", t)
                connected = false
                onState(false)
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { connected = false; onState(false) }
        })
    }

    fun send(message: SendMessageDto): Boolean {
        if (!connected) return false
        val body = adapter.toJson(message)
        return sendFrame("SEND\ndestination:/app/chat\ncontent-type:application/json\ncontent-length:${body.toByteArray().size}\n\n$body\u0000")
    }

    fun close() { socket?.close(1000, "screen closed"); socket = null; connected = false }
    private fun sendFrame(frame: String): Boolean = socket?.send(frame) == true
}
