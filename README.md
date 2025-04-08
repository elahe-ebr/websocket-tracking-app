# 📍 WebSocket Tracking App

A simple Android application that uses **WebSocket** to send **device location (GPS)** data to a server. The location is obtained in the background using a **foreground service**, and transmitted to the server every few seconds via WebSocket.

---

## ✨ Features

- Real-time location tracking using WebSocket
- Sends location every 5 seconds (customizable)
- Automatically reconnects if connection is lost
- Uses `StateFlow` to emit incoming messages from the server
- Clean MVVM architecture with `ViewModel` and `Hilt`
- Manual message sending via UI
- Displays connection status (connected/disconnected)

---

## ⚙️ Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM + Hilt (Dependency Injection)
- **WebSocket:** [OkHttp WebSocket](https://square.github.io/okhttp/)
- **Concurrency:** Kotlin Coroutines + StateFlow
- **Location:** FusedLocationProviderClient

---

## 🛰️ How it Works

1. When the user taps "Start", the `LocationService` foreground service is launched.
2. The device’s current location is retrieved every 5 seconds.
3. The location is sent to the WebSocket server as a simple message.
4. If the WebSocket connection fails, it will automatically attempt to reconnect.
5. The user can stop tracking by pressing the "Stop" button.

---

## 🔧 Configuration

You can change the WebSocket URL in the `WebSocketManager.kt` file:

```kotlin
val request = Request.Builder()
    .url("wss://echo.websocket.events") // Replace with your WebSocket server
    .build()
