package com.neon10.ratatoskr.ui.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.neon10.ratatoskr.data.AiSettingsStore

@Composable
fun AiSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Load states
    var token by remember { mutableStateOf(AiSettingsStore.apiKey) }
    var baseUrl by remember { mutableStateOf(AiSettingsStore.baseUrl) }
    var modelName by remember { mutableStateOf(AiSettingsStore.model) }
    var prompt by remember { mutableStateOf(AiSettingsStore.systemPrompt) }
    var timeout by remember { mutableStateOf(AiSettingsStore.timeout.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState), // 增加滚动支持
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI 配置", style = MaterialTheme.typography.headlineSmall)

        // 1. 基础连接设置
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("连接设置", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("API Base URL") },
                    placeholder = { Text("https://api.deepseek.com") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Model Name") },
                    placeholder = { Text("deepseek-chat / gpt-3.5-turbo") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timeout,
                    onValueChange = { timeout = it },
                    label = { Text("超时时间 (ms)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. Prompt 设置
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("系统提示词 (System Prompt)", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { prompt = AiSettingsStore.DEFAULT_PROMPT }) {
                        Text("重置默认")
                    }
                }

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("自定义 AI 的角色和回复格式") },
                    modifier = Modifier.fillMaxWidth().height(180.dp), // 增加高度
                    maxLines = 10
                )

                Text(
                    "提示：保持【保守】【激进】等标签格式，以便应用正确解析并展示分类。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 保存按钮
        Button(
            onClick = {
                AiSettingsStore.apiKey = token
                AiSettingsStore.baseUrl = baseUrl
                AiSettingsStore.model = modelName
                AiSettingsStore.systemPrompt = prompt
                AiSettingsStore.timeout = timeout.toIntOrNull() ?: 30000
                Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存所有配置")
        }

        Spacer(Modifier.height(32.dp))
    }
}
