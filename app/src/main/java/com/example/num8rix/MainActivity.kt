package com.example.num8rix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.num8rix.ui.screens.InfoScreen
import com.example.num8rix.ui.screens.StartScreen
import com.example.num8rix.ui.screens.GeneratorScreen // Originaler Generator mit Simple Service
import com.example.num8rix.ui.theme.Num8rixTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.example.num8rix.ui.screens.GameScreen
import com.example.num8rix.ui.screens.MyDatabaseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.example.num8rix.ui.screens.MyDatabaseViewModelFactory



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        // Eintrag in DB zum Testen, lädt Daten beim Start des Spiels in DB
//        val viewModel: MyDatabaseViewModel = ViewModelProvider(
//            this,
//            MyDatabaseViewModelFactory(application)
//        )[MyDatabaseViewModel::class.java]
//
//        // TEST: Rätsel mit schwarzen Hinweisen hinzufügen
//        viewModel.addEinfachEntry(
//            unsolved  = "1········;·5·······;···███···;·········;·····9···;·········;·███·····;·····3···;·····2···",
//            layout = "1········;·5·······;···705···;·········;·····9···;·········;·403·····;·····3···;·····2···",
//            solution = "534678912672195348198342567859761423426853791713924856961537284387419625245286179",
//            solved = false
//        )

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
    var currentPuzzleId by remember { mutableStateOf<Int?>(null) }

    // ViewModel
    val databaseViewModel: MyDatabaseViewModel = viewModel()

    // Back Button Handler - navigiert zwischen Screens statt App zu schließen
    BackHandler(enabled = currentScreen != "start") {
        when (currentScreen) {
            "info" -> currentScreen = "start"
            "generator" -> currentScreen = "start" 
            "game" -> {
                // Bei Game Screen zurück zum Start und States zurücksetzen
                currentScreen = "start"
                selectedDifficulty = null
                unsolvedString = null
            }
        }
    }

    when (currentScreen) {
        "start" -> StartScreen(
            viewModel = databaseViewModel,
            onInfoClick = { currentScreen = "info" },
            onGameStart = { difficulty ->
                selectedDifficulty = difficulty
                // Lade einen zufälligen unsolvedString aus der Datenbank
                databaseViewModel.getRandomUnsolvedByDifficulty(difficulty) { result ->
                    if (result != null) {
                        unsolvedString = result.unsolvedString
                        currentPuzzleId = result.id
                        currentScreen = "game"
                    }
                }
            },
            onGeneratorClick = { currentScreen = "generator" } // NEU: Navigation zum Generator
        )

        "info" -> InfoScreen(
            onBackClick = { currentScreen = "start" },
            onHomeClick = { currentScreen = "start" }
        )

        // NEU: Generator Screen (Simple Service)
        "generator" -> GeneratorScreen(
            viewModel = databaseViewModel,
            onBackClick = { currentScreen = "start" }
        )

        "game" -> {
            val difficulty = selectedDifficulty
            val puzzle = unsolvedString

            if (difficulty != null && puzzle != null) {
                val game = remember(puzzle) {
                    // Hier müssen wir das Layout aus der Datenbank holen
                    // Für jetzt verwenden wir einen leeren Layout-String als Fallback
                    Game(puzzle, "").apply { generateGame() }
                }

                GameScreen(
                    difficulty = difficulty,
                    viewModel = databaseViewModel,
                    onBackClick = {
                        currentScreen = "start"
                        selectedDifficulty = null
                        unsolvedString = null
                        currentPuzzleId = null
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