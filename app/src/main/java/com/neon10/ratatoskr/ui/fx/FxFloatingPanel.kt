package com.neon10.ratatoskr.ui.fx

import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import com.neon10.ratatoskr.service.AiActionService

data class FxAction(val label: String, val onClick: () -> Unit = {})
@Composable
fun FxFloatingPanel(actions: List<FxAction> = listOf(
    FxAction("A"), FxAction("B"), FxAction("C"), FxAction("D")
)) {
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var panelActions by remember { mutableStateOf(actions) }
    var closingByAction by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<FxAction?>(null) }
    var boxW by remember { mutableStateOf(0f) }
    var boxH by remember { mutableStateOf(0f) }
    var boxX by remember { mutableStateOf(0f) }
    var boxY by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val bubbleSize = 56.dp

    val containerSize = bubbleSize
    val gap = 12.dp

    Box(
        modifier = Modifier
            .size(containerSize)
            .onGloballyPositioned {
                val b = it.boundsInWindow()
                boxX = b.left
                boxY = b.top
                boxW = b.width
                boxH = b.height
            }
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(bubbleSize)
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    if (expanded) {
                        closingByAction = false
                        expanded = false
                    } else if (!isLoading) {
                        isLoading = true
                    }
                }
                .align(Alignment.TopStart)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(28.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = if (expanded) "−" else "+",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (expanded) {
            val screenW = config.screenWidthDp.dp
            val screenH = config.screenHeightDp.dp
            val screenWpx = with(density) { screenW.toPx() }
            val screenHpx = with(density) { screenH.toPx() }
            val bubblePx = with(density) { bubbleSize.toPx() }
            val centerX = screenWpx / 2f
            val centerY = screenHpx / 2f
            val ballCX = boxX + bubblePx / 2f
            val ballCY = boxY + bubblePx / 2f
            val near = with(density) { 100.dp.toPx() }
            val shift = with(density) { (bubbleSize / 2 + 24.dp).toPx() }.toInt()
            val yAdjust = if (kotlin.math.abs(ballCX - centerX) < near && kotlin.math.abs(ballCY - centerY) < near) {
                if (ballCY <= centerY) shift else -shift
            } else 0
            Popup(alignment = Alignment.TopStart) {
                Box(
                    modifier = Modifier
                        .size(screenW, screenH)
                        .background(Color.Transparent)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { closingByAction = false; pendingAction = null; expanded = false },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(0.dp)
                            .width(240.dp)
                            .offset(y = yAdjust.dp)
                    ) {
                        AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.95f),
                            exit = if (closingByAction) {
                                fadeOut(tween(280)) + scaleOut(tween(280), targetScale = 0.92f)
                            } else {
                                fadeOut(tween(160))
                            }
                        ) {
                            ChoicePanel(panelActions) { a ->
                                closingByAction = true
                                pendingAction = a
                                expanded = false
                            }
                        }
                    }
                }
            }
        }
        LaunchedEffect(isLoading) {
            if (isLoading) {
                val labels = AiActionService.fetchActions()
                panelActions = labels.map { FxAction(it) }
                isLoading = false
                expanded = true
            }
        }
        LaunchedEffect(expanded, closingByAction, pendingAction) {
            if (!expanded && closingByAction && pendingAction != null) {
                delay(280)
                pendingAction?.onClick()
                pendingAction = null
                closingByAction = false
            }
        }
    }
}

@Composable
private fun ChoicePanel(actions: List<FxAction>, onSelect: (FxAction) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        actions.forEach { action ->
            ChoiceButton(text = action.label) { onSelect(action) }
        }
    }
}

@Composable
private fun ChoiceButton(text: String, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
        )
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
