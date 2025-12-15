package com.neon10.ratatoskr.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun copyPlainText(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("reply", text))
}
