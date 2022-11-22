package com.synervoz.agorasampleapp

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        context =getApplicationContext()
        handler = Handler(Looper.getMainLooper())
    }

    companion object {
        private var context: Context? = null
        val appContext: Context?
            get() = context

        internal lateinit var handler: Handler

        var userId = ""
        var clientId = "Playground"

        // SECRET, please fill in
        val clientSecret = "" // needed for api request, please check the code for more info
        val agoraAppId = ""

    }
}