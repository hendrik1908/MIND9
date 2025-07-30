package com.example.num8rix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.num8rix.ui.screens.InfoScreen
import com.example.num8rix.ui.screens.StartScreen
import com.example.num8rix.ui.theme.Num8rixTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    var unsolvedString by remember { mutableStateOf<String?>(null) }

    // ViewModel
    val databaseViewModel: MyDatabaseViewModel = viewModel()

    when (currentScreen) {
        "start" -> StartScreen(
            onInfoClick = { currentScreen = "info" },
            onSettingsClick = { /* optional */ },
            onGameStart = { difficulty ->
                selectedDifficulty = difficulty
                // Lade einen zufälligen unsolvedString aus der Datenbank
                databaseViewModel.getRandomUnsolvedByDifficulty(difficulty) { result ->
                    unsolvedString = result
                    currentScreen = "game"
                }
            }
        )

        "info" -> InfoScreen(
            onBackClick = { currentScreen = "start" },
            onHomeClick = { currentScreen = "start" }
        )

        "game" -> {
            val difficulty = selectedDifficulty
            val puzzle = unsolvedString

            if (difficulty != null && puzzle != null) {
                val game = remember(puzzle) {
                    Game(puzzle).apply { generateGame() }
                }

                GameScreen(
                    difficulty = difficulty,
                    viewModel = databaseViewModel,
                    grid = game.grid,
                    onBackClick = {
                        currentScreen = "start"
                        selectedDifficulty = null
                        unsolvedString = null
                    }
                )
            } else {
                // Noch kein Rätsel geladen → Ladeanzeige
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
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