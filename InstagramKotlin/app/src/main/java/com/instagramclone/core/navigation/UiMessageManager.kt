package com.instagramclone.core.navigation

import com.instagramclone.core.model.UiMessage
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class UiMessageManager @Inject constructor() {
    private val ids = AtomicLong(0)
    private val _message = MutableStateFlow<UiMessage?>(null)
    val message: StateFlow<UiMessage?> = _message.asStateFlow()

    fun show(text: String) {
        _message.value = UiMessage(ids.incrementAndGet(), text)
    }

    fun consume(id: Long) {
        if (_message.value?.id == id) _message.value = null
    }
}
