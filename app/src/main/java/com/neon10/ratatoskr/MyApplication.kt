package com.neon10.ratatoskr

import android.app.Application
import com.neon10.ratatoskr.data.AppData
import com.neon10.ratatoskr.data.AiSettingsStore

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppData.init(this)
        AiSettingsStore.init(this)
    }
}
