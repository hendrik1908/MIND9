package com.example.num8rix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.num8rix.ui.screens.InfoScreen
import com.example.num8rix.ui.screens.StartScreen
import com.example.num8rix.ui.theme.Num8rixTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Num8rixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Num8rixApp()
                }
            }
        }
    }
}

@Composable
fun Num8rixApp() {
    var currentScreen by remember { mutableStateOf("start") }

    when (currentScreen) {
        "start" -> StartScreen(
            onInfoClick = { currentScreen = "info" }
        )

        "info" -> InfoScreen(
            onBackClick = { currentScreen = "start" },
            onHomeClick = { currentScreen = "start" }
        )
    }
}

    @Preview(showBackground = true)
    @Composable
    fun Num8rixAppPreview() {
        Num8rixTheme {
            Num8rixApp()
        }
    }