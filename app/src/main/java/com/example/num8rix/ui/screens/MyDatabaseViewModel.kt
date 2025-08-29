package com.example.num8rix.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.num8rix.MyApplication
import com.example.num8rix.database.dao.EinfachDao
import com.example.num8rix.database.dao.MittelDao
import com.example.num8rix.database.dao.SchwerDao
import com.example.num8rix.database.dao.GameCacheDao
import com.example.num8rix.database.entity.Einfach
import com.example.num8rix.database.entity.Mittel
import com.example.num8rix.database.entity.Schwer
import com.example.num8rix.database.entity.GameCache
import kotlinx.coroutines.launch
import com.example.num8rix.Str8tsGridSerializer // <- Den Import für deine Serializer-Klasse hinzufügen!
import com.example.num8rix.DifficultyLevel // <- Import für das DifficultyLevel Enum
import com.example.num8rix.Grid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



open class MyDatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val einfachDao: EinfachDao
    private val mittelDao: MittelDao
    private val schwerDao: SchwerDao
    private val gameCacheDao: GameCacheDao

    init {
        val appDatabase = (application as MyApplication).database
        einfachDao = appDatabase.einfachDao()
        mittelDao = appDatabase.mittelDao()
        schwerDao = appDatabase.schwerDao()
        gameCacheDao = appDatabase.gameCacheDao()
    }

    // ... andere Funktionen (z.B. updateSolvedStatus) ...

    /**
     * Erstellt einen neuen Datensatz und speichert ihn in der entsprechenden Tabelle.
     * @param difficulty Die Schwierigkeitsstufe, die die Zieltabelle bestimmt.
     */
    fun saveNewPuzzle(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            // 1. Instanziierung des Serializers und Generierung des Strings
            val serializer = Str8tsGridSerializer()
            val gridString = serializer.gridToString() // Holen des Strings aus deiner Klasse

            // 2. Erstellen der entsprechenden Entität und Einfügen in die DB
            when (difficulty) {
                DifficultyLevel.EASY -> {
                    val newEntry = Einfach(unsolvedString = gridString, solutionString = "", alreadySolved = false)
                    einfachDao.insert(newEntry)
                    println("Neues Rätsel (Einfach) in die DB eingefügt.")
                }
                DifficultyLevel.MEDIUM -> {
                    val newEntry = Mittel(unsolvedString = gridString, solutionString = "", alreadySolved = false)
                    mittelDao.insert(newEntry)
                    println("Neues Rätsel (Mittel) in die DB eingefügt.")
                }
                DifficultyLevel.HARD -> {
                    val newEntry = Schwer(unsolvedString = gridString, solutionString = "", alreadySolved = false)
                    schwerDao.insert(newEntry)
                    println("Neues Rätsel (Schwer) in die DB eingefügt.")
                }
            }
        }
    }

    //Gibt je nach Schwirigkeit ein zufällig gelöstes Rätsel zurück
    open fun getRandomUnsolvedByDifficulty(
        difficulty: DifficultyLevel,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = when (difficulty) {
                DifficultyLevel.EASY -> einfachDao.getRandomUnsolved()?.unsolvedString
                DifficultyLevel.MEDIUM -> mittelDao.getRandomUnsolved()?.unsolvedString
                DifficultyLevel.HARD -> schwerDao.getRandomUnsolved()?.unsolvedString
            }
            onResult(result)
        }
    }

    /**
     * Speichert den aktuellen Spielstand (Spielfeld und Notizen) in der Datenbank.
     * Dies sollte bei jeder Eingabe aufgerufen werden.
     */
    fun saveGameState(
        currentGridString: String,
        notesGridString: String,
        difficulty: DifficultyLevel,
        originalGridString: String? = null
    ) {
        viewModelScope.launch {
            //originalGridString wird nur gesetzt, wenn noch kein Spiel für diese Schwierigkeit existiert.
            val original = originalGridString ?: run {
                // Wenn wir schon einen Spielstand für diese Schwierigkeit haben, den Originalstring NICHT überschreiben
                val existing = gameCacheDao.getLatestEntryByDifficulty(difficulty)
                existing?.originalGridString ?: currentGridString
            }

            val gameState = GameCache(
                currentGridString = currentGridString,
                notesGridString = notesGridString,
                originalGridString = original,
                difficulty = difficulty
            )
            gameCacheDao.insert(gameState)
        }
    }

    /**
     * Löscht den letzten gespeicherten Spielstand, um die UNDO-Funktion zu realisieren.
     */
    fun undoLastMove(
        difficulty: DifficultyLevel,
        onGridRestored: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            // Lösche den letzten Spielstand nur für diese Schwierigkeit
            gameCacheDao.deleteLatestEntryByDifficulty(difficulty)

            // Lade den neuen letzten Spielstand der gleichen Schwierigkeit
            val latest = gameCacheDao.getLatestEntryByDifficulty(difficulty)
            latest?.let {
                onGridRestored(it.currentGridString, it.notesGridString)
            }
        }
    }
    /**
     * Löscht alle gespeicherten Spielstände.
     * Dies sollte beim Start eines neuen Spiels oder beim erfolgreichen Lösen eines Rätsels aufgerufen werden.
     */
    fun clearCache() {
        viewModelScope.launch {
            gameCacheDao.clearCache()
            println("GameCache-Tabelle wurde geleert.")
        }
    }

    /**
     * Ruft den neuesten Spielstand aus dem Cache ab, um ein Spiel fortzusetzen.
     */
    fun getLatestGameStateAsGrid(difficulty: DifficultyLevel, onResult: (Grid?) -> Unit) {
        viewModelScope.launch {
            val latestState = gameCacheDao.getLatestEntryByDifficulty(difficulty)
            if (latestState != null) {
                val grid = Grid()

                // Erst die ursprünglichen Zahlen laden (alle als initial)
                grid.generateGridFromVisualString(latestState.originalGridString)

                // Dann den aktuellen Stand darüber laden (ohne initial zu ändern)
                grid.updateGridFromVisualString(latestState.currentGridString)
                grid.generateNotesFromString(latestState.notesGridString)
                onResult(grid)
            } else {
                onResult(null)
            }
        }
    }
    /**
     * Gibt die Gesamtzahl der Rätsel für alle Schwierigkeitsgrade zurück
     */
    fun getTotalPuzzleCounts(onResult: (Int, Int, Int) -> Unit) {
        viewModelScope.launch {
            val easyCount = einfachDao.getTotalCount()
            val mediumCount = mittelDao.getTotalCount()
            val hardCount = schwerDao.getTotalCount()
            onResult(easyCount, mediumCount, hardCount)
        }
    }
// gibt gelöste vs. ungelöste Rätsel pro Difficulty zurückgibt für StartScreen
    fun getSolvedAndTotalCounts(onResult: (easy: Pair<Int, Int>, medium: Pair<Int, Int>, hard: Pair<Int, Int>) -> Unit) {
        viewModelScope.launch {
            val easySolved = einfachDao.getSolvedCount()
            val easyTotal = einfachDao.getTotalCount()

            val mediumSolved = mittelDao.getSolvedCount()
            val mediumTotal = mittelDao.getTotalCount()

            val hardSolved = schwerDao.getSolvedCount()
            val hardTotal = schwerDao.getTotalCount()

            withContext(Dispatchers.Main) {
                onResult(
                    Pair(easyTotal - easySolved, easyTotal), // ungelöst/gesamt
                    Pair(mediumTotal - mediumSolved, mediumTotal),
                    Pair(hardTotal - hardSolved, hardTotal)
                )
            }
        }
    }

    fun checkCurrentGridWithHighlights(
        difficulty: DifficultyLevel,
        currentGrid: Grid,
        onResult: (correct: Set<Pair<Int, Int>>, incorrect: Set<Pair<Int, Int>>) -> Unit
    ) {
        viewModelScope.launch {
            val solutionString = when (difficulty) {
                DifficultyLevel.EASY -> einfachDao.getRandomUnsolved()?.solutionString
                DifficultyLevel.MEDIUM -> mittelDao.getRandomUnsolved()?.solutionString
                DifficultyLevel.HARD -> schwerDao.getRandomUnsolved()?.solutionString
            }

            if (solutionString != null) {
                val solutionGrid = Grid()
                solutionGrid.generateGridFromFlatString(solutionString)

                val correct = mutableSetOf<Pair<Int, Int>>()
                val incorrect = mutableSetOf<Pair<Int, Int>>()

                for (row in 0 until 9) {
                    for (col in 0 until 9) {
                        val userVal = currentGrid.getField(row, col).value
                        val solVal = solutionGrid.getField(row, col).value

                        if (userVal != 0) {
                            if (userVal == solVal) correct.add(row to col)
                            else incorrect.add(row to col)
                        }
                    }
                }

                onResult(correct, incorrect)
            }
        }
    }

    // Nur zum Testen des Screens, damit Eintrag in DB vorhanden ist. Kann bei funktionierendem Algorithmus entfernt werden
    fun addEinfachEntry(
        unsolved: String,
        solution: String,
        solved: Boolean
    ) {
        viewModelScope.launch {
            val entry = Einfach(
                unsolvedString = unsolved,
                solutionString = solution,
                alreadySolved = solved
            )
            einfachDao.insert(entry)
            println("Beispielrätsel (Einfach) wurde in die DB eingefügt.")
        }
    }
}

// Str8tsGridSerializer.kt (deine Datei aus dem Upload)
// ... der Inhalt deines Cells2DB Files ...