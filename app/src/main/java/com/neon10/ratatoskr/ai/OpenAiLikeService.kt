package com.neon10.ratatoskr.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.neon10.ratatoskr.data.AiSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * 通用 OpenAI 接口兼容服务
 * 支持 OpenAI, DeepSeek, Moonshot, 本地 Ollama 等
 */
class OpenAiLikeService : AiService {

    companion object {
        private const val TAG = "OpenAiService"
    }

    // 每次调用时动态获取最新配置
    private val apiKey: String get() = AiSettingsStore.apiKey
    private val baseUrl: String get() = AiSettingsStore.baseUrl
    private val model: String get() = AiSettingsStore.model
    private val systemPrompt: String get() = AiSettingsStore.systemPrompt
    private val timeout: Int get() = AiSettingsStore.timeout

    private val gson = Gson()

    override suspend fun generateReplies(
        context: String?,
        limit: Int,
        prefs: StylePrefs
    ): List<ReplyOption> = withContext(Dispatchers.IO) {

        // 动态构建 Client 以支持超时设置的变更（如果不需要动态超时，可以将 Client 设为静态）
        val client = OkHttpClient.Builder()
            .connectTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
            .build()

        if (context.isNullOrBlank()) {
            return@withContext getDefaultReplies()
        }

        try {
            val userPrompt = "以下是聊天上下文：\n\n$context\n\n请根据以上对话，生成${limit}条回复建议。"

            val requestBody = ChatRequest(
                model = model,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = userPrompt)
                ),
                temperature = 0.8
            )

            val jsonBody = gson.toJson(requestBody)

            // 拼接完整的 URL，兼容 OpenAI 格式
            val fullUrl = "$baseUrl/chat/completions"

            Log.d(TAG, "Request: $fullUrl, Model: $model")

            val httpRequest = Request.Builder()
                .url(fullUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                Log.e(TAG, "API Error: ${response.code} - $responseBody")
                return@withContext listOf(ReplyOption("错误", "API 请求失败: ${response.code}"))
            }

            val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
            val content = chatResponse.choices?.firstOrNull()?.message?.content

            if (content.isNullOrBlank()) {
                return@withContext getDefaultReplies()
            }

            parseReplies(content)

        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            listOf(ReplyOption("错误", "发生异常: ${e.message}"))
        }
    }

    private fun parseReplies(content: String): List<ReplyOption> {
        val replies = mutableListOf<ReplyOption>()
        val lines = content.trim().split("\n").filter { it.isNotBlank() }

        for (line in lines) {
            val trimmed = line.trim()
            // 简单的解析逻辑，兼容用户可能修改 Prompt 后格式微调的情况
            // 你可以在这里增加更多鲁棒性，例如正则匹配
            when {
                trimmed.startsWith("【保守】") -> replies.add(ReplyOption("保守", trimmed.removePrefix("【保守】").trim()))
                trimmed.startsWith("【激进】") -> replies.add(ReplyOption("激进", trimmed.removePrefix("【激进】").trim()))
                trimmed.startsWith("【出其不意】") -> replies.add(ReplyOption("出其不意", trimmed.removePrefix("【出其不意】").trim()))
                // 兼容没有前缀的情况，当作普通建议
                !trimmed.startsWith("【") && trimmed.length > 2 -> replies.add(ReplyOption("建议", trimmed))
            }
        }

        return if (replies.isNotEmpty()) replies else listOf(ReplyOption("AI回复", content))
    }

    private fun getDefaultReplies() = listOf(
        ReplyOption("提示", "无法生成回复，请检查网络或配置")
    )

    // 数据类
    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7
    )

    data class Message(val role: String, val content: String)
    data class ChatResponse(val choices: List<Choice>?)
    data class Choice(val message: Message?)
}
