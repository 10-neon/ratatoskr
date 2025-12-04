package com.neon10.ratatoskr.service

import kotlinx.coroutines.delay

object AiActionService {
    suspend fun fetchActions(): List<String> {
        return listOf("选项一", "选项二", "选项三", "选项四")
    }
}

