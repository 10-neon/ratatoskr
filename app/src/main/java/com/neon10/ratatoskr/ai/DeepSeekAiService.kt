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
 * DeepSeek AI Service implementation.
 * Uses OpenAI-compatible API format.
 */
class DeepSeekAiService : AiService {
    
    companion object {
        private const val TAG = "DeepSeekAiService"
        private const val API_URL = "https://api.deepseek.com/chat/completions"
        private const val MODEL = "deepseek-chat"
    }
    
    // Read API key from settings store
    private val apiKey: String
        get() = AiSettingsStore.apiKey
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    override suspend fun generateReplies(
        context: String?,
        limit: Int,
        prefs: StylePrefs
    ): List<ReplyOption> = withContext(Dispatchers.IO) {
        
        if (context.isNullOrBlank()) {
            Log.w(TAG, "Context is empty, returning default replies")
            return@withContext getDefaultReplies()
        }
        
        try {
            val systemPrompt = buildSystemPrompt(prefs)
            val userPrompt = buildUserPrompt(context, limit)
            
            val request = ChatRequest(
                model = MODEL,
                messages = listOf(
                    Message(role = "system", content = systemPrompt),
                    Message(role = "user", content = userPrompt)
                ),
                temperature = 0.8,
                maxTokens = 1024
            )
            
            val jsonBody = gson.toJson(request)
            Log.d(TAG, "Sending request to DeepSeek API...")
            
            val httpRequest = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()
            
            if (!response.isSuccessful) {
                Log.e(TAG, "API error: ${response.code} - $responseBody")
                return@withContext getDefaultReplies()
            }
            
            Log.d(TAG, "Response received, parsing...")
            val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)
            val content = chatResponse.choices?.firstOrNull()?.message?.content
            
            if (content.isNullOrBlank()) {
                Log.w(TAG, "Empty response content")
                return@withContext getDefaultReplies()
            }
            
            parseReplies(content)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error calling DeepSeek API: ${e.message}", e)
            getDefaultReplies()
        }
    }
    
    private fun buildSystemPrompt(prefs: StylePrefs): String {
        return """你是一个聊天回复助手。根据用户提供的聊天上下文，生成3条回复建议。

每条回复需要标注风格类型：
1. 【保守】- 安全、礼貌、不容易出错的回复
2. 【激进】- 积极推进关系或话题的回复
3. 【出其不意】- 有趣、幽默或创意的回复

请按以下格式输出，每条回复一行：
【保守】回复内容
【激进】回复内容
【出其不意】回复内容

注意：
- 回复要自然、口语化
- 符合聊天场景的语境
- 每条回复控制在50字以内"""
    }
    
    private fun buildUserPrompt(context: String, limit: Int): String {
        return """以下是聊天上下文：

$context

请根据以上对话，生成${limit}条回复建议。"""
    }
    
    private fun parseReplies(content: String): List<ReplyOption> {
        val replies = mutableListOf<ReplyOption>()
        val lines = content.trim().split("\n").filter { it.isNotBlank() }
        
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("【保守】") -> {
                    replies.add(ReplyOption("保守", trimmedLine.removePrefix("【保守】").trim()))
                }
                trimmedLine.startsWith("【激进】") -> {
                    replies.add(ReplyOption("激进", trimmedLine.removePrefix("【激进】").trim()))
                }
                trimmedLine.startsWith("【出其不意】") -> {
                    replies.add(ReplyOption("出其不意", trimmedLine.removePrefix("【出其不意】").trim()))
                }
            }
        }
        
        // If parsing failed, return as generic replies
        if (replies.isEmpty()) {
            Log.w(TAG, "Failed to parse replies, using raw content")
            return listOf(
                ReplyOption("建议", content.take(100))
            )
        }
        
        return replies
    }
    
    private fun getDefaultReplies(): List<ReplyOption> {
        return listOf(
            ReplyOption("保守", "好的，我理解了"),
            ReplyOption("激进", "没问题，我们现在就开始吧！"),
            ReplyOption("出其不意", "哈哈，你说得太对了～")
        )
    }
    
    // API Request/Response Models
    
    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7,
        @SerializedName("max_tokens")
        val maxTokens: Int = 1024
    )
    
    data class Message(
        val role: String,
        val content: String
    )
    
    data class ChatResponse(
        val choices: List<Choice>?
    )
    
    data class Choice(
        val message: Message?
    )
}
