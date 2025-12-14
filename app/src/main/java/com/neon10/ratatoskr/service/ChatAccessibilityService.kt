package com.neon10.ratatoskr.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Native Android AccessibilityService for collecting chat messages.
 * Using native service instead of Assists library may work better with WeChat.
 */
class ChatAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "ChatA11yService"
        
        @Volatile
        var instance: ChatAccessibilityService? = null
            private set
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Service connected")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We only need on-demand collection, not continuous monitoring
        // So we don't need to handle events here
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Service destroyed")
    }
    
    /**
     * Get the root node of the current active window.
     * This uses the native Android API directly.
     */
    fun getRootNode(): AccessibilityNodeInfo? {
        return try {
            rootInActiveWindow
        } catch (e: Exception) {
            Log.e(TAG, "Error getting root node: ${e.message}")
            null
        }
    }
    
    /**
     * Get all windows (requires API 21+)
     */
    fun getAllWindows() = windows
}
