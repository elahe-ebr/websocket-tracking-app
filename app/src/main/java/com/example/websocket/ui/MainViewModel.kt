package com.example.websocket.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.websocket.data.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow("")
    val messages: StateFlow<String> = _messages

    init {
        WebSocketManager.connect()
        observeIncomingMessages()
    }

    private fun observeIncomingMessages() {
        viewModelScope.launch {
            WebSocketManager.messages.collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(text: String) {
        if (!WebSocketManager.isConnected.value) {
            _messages.value = "❌Connection lost! Trying to reconnect..."
        } else {
            val success = WebSocketManager.sendMessage(text)
            if (!success) {
                _messages.value = "❌Sending message failed."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        WebSocketManager.close()
    }
}