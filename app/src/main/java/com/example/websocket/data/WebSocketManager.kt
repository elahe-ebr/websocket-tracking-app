package com.example.websocket.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object WebSocketManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private var reconnectJob: Job? = null
    private var reconnectDelay = 1000L

    private val _messages = MutableStateFlow("")
    val messages: StateFlow<String> = _messages

    private var _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = this._isConnected

    private val request = Request.Builder()
        .url("wss://echo.websocket.events")
        .build()

    fun connect() {
        if (_isConnected.value) return

        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@WebSocketManager.webSocket = webSocket
                reconnectDelay = 5000
                _isConnected.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch { _messages.emit(text) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                scope.launch { _messages.emit("Connection error: ${t.localizedMessage}") }
                attemptReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
                attemptReconnect()
            }
        })
    }

    private fun attemptReconnect() {
        if (reconnectJob?.isActive == true) return

        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(reconnectDelay)
            connect()
        }
    }

    fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    fun close() {
        reconnectJob?.cancel()
        scope.cancel()
        webSocket?.close(1000, "Closing manually")
        _isConnected.value = false
    }

}