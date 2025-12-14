package com.neon10.ratatoskr.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Storage for AI settings using SharedPreferences.
 */
object AiSettingsStore {
    private const val PREFS_NAME = "ai_settings"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_TIMEOUT = "timeout"
    private const val KEY_LIMIT = "limit"
    
    private var prefs: SharedPreferences? = null
    
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    
    var apiKey: String
        get() = prefs?.getString(KEY_API_KEY, "") ?: ""
        set(value) {
            prefs?.edit()?.putString(KEY_API_KEY, value)?.apply()
        }
    
    var timeout: Int
        get() = prefs?.getInt(KEY_TIMEOUT, 1500) ?: 1500
        set(value) {
            prefs?.edit()?.putInt(KEY_TIMEOUT, value)?.apply()
        }
    
    var limit: Int
        get() = prefs?.getInt(KEY_LIMIT, 3) ?: 3
        set(value) {
            prefs?.edit()?.putInt(KEY_LIMIT, value)?.apply()
        }
}
