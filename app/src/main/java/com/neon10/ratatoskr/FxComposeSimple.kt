package com.neon10.ratatoskr
import android.app.Application
import androidx.compose.ui.platform.ComposeView
import com.neon10.ratatoskr.ui.fx.FxFloatingPanel
import com.petterp.floatingx.FloatingX
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.assist.FxScopeType
import com.petterp.floatingx.compose.enableComposeSupport

object FxComposeSimple {
    fun install(context: Application) {
        FloatingX.install {
            setContext(context)
            setTag("compose")
            enableComposeSupport()
            setScopeType(FxScopeType.SYSTEM)
            setGravity(FxGravity.CENTER)
            setOffsetXY(0f, 0f)
            setLayoutView(
                ComposeView(context).apply {
                    setContent { FxFloatingPanel() }
                }
            )
            setEnableLog(true)
            setEdgeOffset(0f)
            setEnableEdgeAdsorption(false)
        }.show()
    }
}
