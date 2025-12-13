package com.neon10.ratatoskr.data

import android.view.accessibility.AccessibilityNodeInfo
import com.ven.assists.service.AssistsService

/**
 * Collects chat messages from QQ/WeChat using Assists accessibility API.
 * Only collects when explicitly triggered by user clicking the floating button.
 */
object ChatMessageCollector {

    data class ChatMessage(
        val sender: String?,
        val content: String,
        val isFromSelf: Boolean = false
    )

    data class CollectionResult(
        val messages: List<ChatMessage>,
        val rawContext: String,
        val appName: String?
    )

    /**
     * Collect messages from the current screen.
     * Should be called when user clicks the floating button.
     */
    fun collect(): CollectionResult {
        val messages = mutableListOf<ChatMessage>()
        val textParts = mutableListOf<String>()
        var appName: String? = null

        try {
            // Get the accessibility service instance
            val service = AssistsService.instance
            
            // Get the current active window's root node
            val rootNode = service?.rootInActiveWindow
            appName = rootNode?.packageName?.toString()

            if (rootNode != null) {
                // Recursively traverse all nodes to collect text content
                collectTextsFromNode(rootNode, textParts, messages)
                rootNode.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Clean and deduplicate
        val cleanedTexts = textParts
            .filter { it.isNotBlank() }
            .distinct()
            .takeLast(50) // Keep last 50 messages for context

        // Build context string (max 2000 chars as per design)
        val rawContext = cleanedTexts.joinToString("\n").take(2000)

        // Store to ChatContextStore for AI processing
        ChatContextStore.setLast(rawContext)

        return CollectionResult(
            messages = messages.takeLast(20), // Keep last 20 for display
            rawContext = rawContext,
            appName = appName
        )
    }

    private fun collectTextsFromNode(
        node: AccessibilityNodeInfo,
        textParts: MutableList<String>,
        messages: MutableList<ChatMessage>
    ) {
        try {
            // Get text from this node
            val text = node.text?.toString()
            val contentDesc = node.contentDescription?.toString()

            // Add non-empty text
            if (!text.isNullOrBlank()) {
                textParts.add(text)
                // Try to parse as a chat message
                parseAsMessage(text)?.let { messages.add(it) }
            }

            // Also check contentDescription
            if (!contentDesc.isNullOrBlank() && contentDesc != text) {
                textParts.add(contentDesc)
            }

            // Recursively process children
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    collectTextsFromNode(child, textParts, messages)
                    child.recycle()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseAsMessage(text: String): ChatMessage? {
        // Skip very short texts (likely UI elements)
        if (text.length < 2) return null

        // Skip common UI elements
        val skipPatterns = listOf(
            "发送", "返回", "更多", "语音", "表情", "相册", "拍摄",
            "视频通话", "语音通话", "转账", "红包", "位置",
            "Send", "Back", "More", "Voice", "Photo"
        )
        if (skipPatterns.any { text.contains(it, ignoreCase = true) && text.length < 10 }) {
            return null
        }

        // Skip time-only texts
        val timePattern = Regex("^\\d{1,2}:\\d{2}(:\\d{2})?$")
        if (timePattern.matches(text.trim())) return null

        // For now, we can't reliably determine sender without more context
        // Just return as a generic message
        return ChatMessage(
            sender = null,
            content = text,
            isFromSelf = false
        )
    }

    /**
     * Check if accessibility service is running
     */
    fun isAccessibilityEnabled(): Boolean {
        return AssistsService.instance != null
    }
}

