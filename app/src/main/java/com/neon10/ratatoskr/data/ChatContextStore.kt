package com.neon10.ratatoskr.data

import java.util.concurrent.atomic.AtomicReference

object ChatContextStore {
    private val lastRef = AtomicReference<String?>(null)
    fun setLast(text: String?) {
        lastRef.set(text?.take(2000))
    }
    fun getLast(): String? = lastRef.get()
}
