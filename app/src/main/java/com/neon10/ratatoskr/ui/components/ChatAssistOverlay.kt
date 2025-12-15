package com.neon10.ratatoskr.ui.components

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import com.petterp.floatingx.FloatingX
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.assist.FxScopeType
import com.petterp.floatingx.compose.enableComposeSupport

object ChatAssistOverlay {
    private var installed = false
    private var enabled = mutableStateOf(false)
    fun install(context: Application) {
        if (installed) return
        FloatingX.install {
            setContext(context)
            setTag("chat_assist_overlay")
            enableComposeSupport()
            setScopeType(FxScopeType.SYSTEM)
            setGravity(FxGravity.CENTER)
            setOffsetXY(0f, 0f)
            setLayoutView(
                ComposeView(context).apply {
                    setContent {
                        var on by remember { enabled }
                        if (on) {
                            ChatAssistPanel()
                        } else {
                            Box(modifier = androidx.compose.ui.Modifier.size(1.dp))
                        }
                    }
                }
            )
            setEnableLog(true)
            setEdgeOffset(0f)
            setEnableEdgeAdsorption(false)
        }.show()
        installed = true
    }
    fun enable() { enabled.value = true }
    fun disable() { enabled.value = false }
}
