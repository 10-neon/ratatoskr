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

    private const val KEY_BASE_URL = "base_url"
    private const val KEY_MODEL = "model"
    private const val KEY_SYSTEM_PROMPT = "system_prompt"

    const val DEFAULT_BASE_URL = "https://api.deepseek.com" // 或者 https://api.openai.com/v1
    const val DEFAULT_MODEL = "deepseek-chat" // 或者 gpt-3.5-turbo
    const val DEFAULT_PROMPT = """你是一个聊天回复助手。根据用户提供的聊天上下文，生成3条回复建议。
每条回复需要标注风格类型：
1. 【保守】- 安全、礼貌、不容易出错的回复
2. 【激进】- 积极推进关系或话题的回复
3. 【出其不意】- 有趣、幽默或创意的回复

请按以下格式输出，每条回复一行：
【保守】回复内容
【激进】回复内容
【出其不意】回复内容

注意：回复要自然、口语化，每条控制在50字以内。"""

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
        get() = prefs?.getInt(KEY_TIMEOUT, 10000) ?: 10000
        set(value) {
            prefs?.edit()?.putInt(KEY_TIMEOUT, value)?.apply()
        }

    var limit: Int
        get() = prefs?.getInt(KEY_LIMIT, 3) ?: 3
        set(value) {
            prefs?.edit()?.putInt(KEY_LIMIT, value)?.apply()
        }

    var baseUrl: String
        get() = prefs?.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) {
            val cleanUrl = value.removeSuffix("/chat/completions").removeSuffix("/")
            prefs?.edit()?.putString(KEY_BASE_URL, cleanUrl)?.apply()
        }

    var model: String
        get() = prefs?.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs?.edit()?.putString(KEY_MODEL, value)?.apply()!!

    var systemPrompt: String
        get() = prefs?.getString(KEY_SYSTEM_PROMPT, DEFAULT_PROMPT) ?: DEFAULT_PROMPT
        set(value) = prefs?.edit()?.putString(KEY_SYSTEM_PROMPT, value)?.apply()!!
}
