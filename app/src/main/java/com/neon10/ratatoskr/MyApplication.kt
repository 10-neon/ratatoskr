package com.neon10.ratatoskr

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FxComposeSimple.install(this)
    }
}