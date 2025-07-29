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
import com.example.num8rix.ui.screens.GameScreen
import com.example.num8rix.ui.screens.MyDatabaseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


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
    var selectedDifficulty by remember { mutableStateOf<DifficultyLevel?>(null) }

    // Instanzierung des ViewModel und Aufruf der enthaltenen Funktion
    val databaseViewModel: MyDatabaseViewModel = viewModel()

    when (currentScreen) {
        "start" -> StartScreen(
            onInfoClick = { currentScreen = "info" },
            onSettingsClick = { /* z.B. später */ },
            onGameStart = { difficulty ->
                selectedDifficulty = difficulty
                currentScreen = "game"
            }
        )

        "info" -> InfoScreen(
            onBackClick = { currentScreen = "start" },
            onHomeClick = { currentScreen = "start" }
        )

        "game" -> {
            // ⚠️ Warten bis Difficulty gesetzt ist
            selectedDifficulty?.let { difficulty ->
                val game = remember(difficulty) {
                    Game(difficulty).apply { generateGame() }
                }
                val grid = game.grid

                GameScreen(
                    grid = grid,
                    onBackClick = { currentScreen = "start" }
                )
            }
        }
    }
}

    @Preview(showBackground = true)
    @Composable
    fun Num8rixAppPreview() {
        Num8rixTheme {
            Num8rixApp()
        }
    }