package com.example.websocket.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        @get:Synchronized
        var instance: MyApplication? = null
    }

    init {
        instance = this
    }
}