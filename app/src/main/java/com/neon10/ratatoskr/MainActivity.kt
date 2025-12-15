package com.neon10.ratatoskr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.neon10.ratatoskr.ui.theme.RatatoskrTheme
import com.neon10.ratatoskr.ui.AppPage
import com.neon10.ratatoskr.ui.components.AppNavigationBar
import com.neon10.ratatoskr.ui.pages.HomeScreen
import com.neon10.ratatoskr.ui.pages.AiSettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RatatoskrTheme {
                var page by remember { mutableStateOf(AppPage.HOME) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { AppNavigationBar(page) { page = it } }
                ) { innerPadding ->
                    when (page) {
                        AppPage.HOME -> HomeScreen(Modifier.padding(innerPadding))
                        AppPage.AI_SETTINGS -> AiSettingsScreen(Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RatatoskrTheme {
        Greeting("Android")
    }
}
