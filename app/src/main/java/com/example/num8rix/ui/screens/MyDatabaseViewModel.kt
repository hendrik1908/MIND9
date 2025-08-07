package com.example.num8rix.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.num8rix.MyApplication
import com.example.num8rix.database.dao.EinfachDao
import com.example.num8rix.database.dao.MittelDao
import com.example.num8rix.database.dao.SchwerDao
import com.example.num8rix.database.entity.Einfach
import com.example.num8rix.database.entity.Mittel
import com.example.num8rix.database.entity.Schwer
import kotlinx.coroutines.launch
import com.example.num8rix.Str8tsGridSerializer
import com.example.num8rix.DifficultyLevel

// Enum für die Schwierigkeitsstufen (falls du es noch nicht hast)
enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}

// Sealed Class für Check-Ergebnisse
sealed class PuzzleCheckResult {
    data class ReadyToSave(val gridString: String) : PuzzleCheckResult()
    data class AlreadyExists(val gridString: String) : PuzzleCheckResult()
    data class Error(val message: String) : PuzzleCheckResult()
}

class MyDatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val einfachDao: EinfachDao
    private val mittelDao: MittelDao
    private val schwerDao: SchwerDao

    init {
        val appDatabase = (application as MyApplication).database
        einfachDao = appDatabase.einfachDao()
        mittelDao = appDatabase.mittelDao()
        schwerDao = appDatabase.schwerDao()
    }

    /**
     * Prüft ob ein Grid-String bereits in der entsprechenden Tabelle existiert
     * @param gridString Der zu prüfende Grid-String
     * @param difficulty Die Schwierigkeitsstufe (bestimmt welche Tabelle geprüft wird)
     * @return PuzzleCheckResult mit dem Prüfergebnis
     */
    suspend fun checkPuzzleExists(gridString: String, difficulty: DifficultyLevel): PuzzleCheckResult {
        return try {
            val exists = when (difficulty) {
                DifficultyLevel.EASY -> einfachDao.existsByUnsolvedString(gridString)
                DifficultyLevel.MEDIUM -> mittelDao.existsByUnsolvedString(gridString)
                DifficultyLevel.HARD -> schwerDao.existsByUnsolvedString(gridString)
            }

            if (exists) {
                PuzzleCheckResult.AlreadyExists(gridString)
            } else {
                PuzzleCheckResult.ReadyToSave(gridString)
            }
        } catch (e: Exception) {
            PuzzleCheckResult.Error("Fehler bei DB-Prüfung: ${e.message}")
        }
    }

    /**
     * Prüft ob ein Grid bereits in der entsprechenden Tabelle existiert
     * @param grid Das zu prüfende Grid
     * @param difficulty Die Schwierigkeitsstufe
     * @return PuzzleCheckResult mit dem Prüfergebnis
     */
    suspend fun checkPuzzleExists(grid: Array<Array<Char>>, difficulty: DifficultyLevel): PuzzleCheckResult {
        val serializer = Str8tsGridSerializer()
        val gridString = serializer.gridToString(grid)
        return checkPuzzleExists(gridString, difficulty)
    }

    /**
     * Speichert ein neues Puzzle nur wenn es noch nicht existiert
     * @param gridString Der Grid-String des Puzzles
     * @param difficulty Die Schwierigkeitsstufe
     * @return PuzzleCheckResult mit dem Ergebnis der Operation
     */
    fun saveNewPuzzleIfNotExists(gridString: String, difficulty: DifficultyLevel) {
        viewModelScope.launch {
            val checkResult = checkPuzzleExists(gridString, difficulty)

            when (checkResult) {
                is PuzzleCheckResult.ReadyToSave -> {
                    // Puzzle kann gespeichert werden
                    savePuzzleToDatabase(checkResult.gridString, difficulty)
                    println("Neues Rätsel ($difficulty) in die DB eingefügt.")
                }
                is PuzzleCheckResult.AlreadyExists -> {
                    println("Rätsel ($difficulty) existiert bereits in der DB.")
                }
                is PuzzleCheckResult.Error -> {
                    println("Fehler beim Speichern: ${checkResult.message}")
                }
            }
        }
    }

    /**
     * Speichert ein neues Puzzle (Grid) nur wenn es noch nicht existiert
     * @param grid Das Grid des Puzzles
     * @param difficulty Die Schwierigkeitsstufe
     */
    fun saveNewPuzzleIfNotExists(grid: Array<Array<Char>>, difficulty: DifficultyLevel) {
        val serializer = Str8tsGridSerializer()
        val gridString = serializer.gridToString(grid)
        saveNewPuzzleIfNotExists(gridString, difficulty)
    }

    /**
     * Ursprüngliche Funktion - speichert ohne Duplikat-Check
     * @param difficulty Die Schwierigkeitsstufe, die die Zieltabelle bestimmt.
     */
    fun saveNewPuzzle(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            // 1. Instanziierung des Serializers und Generierung des Strings
            val serializer = Str8tsGridSerializer()
            val gridString = serializer.gridToString() // Holen des Strings aus deiner Klasse

            // 2. Erstellen der entsprechenden Entität und Einfügen in die DB
            savePuzzleToDatabase(gridString, difficulty)
        }
    }

    /**
     * Private Hilfsfunktion zum tatsächlichen Speichern in die DB
     */
    private suspend fun savePuzzleToDatabase(gridString: String, difficulty: DifficultyLevel) {
        when (difficulty) {
            DifficultyLevel.EASY -> {
                val newEntry = Einfach(unsolvedString = gridString, solutionString = "", alreadySolved = false)
                einfachDao.insert(newEntry)
            }
            DifficultyLevel.MEDIUM -> {
                val newEntry = Mittel(unsolvedString = gridString, solutionString = "", alreadySolved = false)
                mittelDao.insert(newEntry)
            }
            DifficultyLevel.HARD -> {
                val newEntry = Schwer(unsolvedString = gridString, solutionString = "", alreadySolved = false)
                schwerDao.insert(newEntry)
            }
        }
    }

    /**
     * Batch-Operation: Speichert mehrere Puzzles mit Duplikat-Check
     * @param grids Liste von Grids
     * @param difficulty Schwierigkeitsstufe für alle Puzzles
     * @return BatchResult mit Statistiken
     */
    fun saveMultiplePuzzlesIfNotExists(grids: List<Array<Array<Char>>>, difficulty: DifficultyLevel) {
        viewModelScope.launch {
            var savedCount = 0
            var duplicateCount = 0
            var errorCount = 0

            for (grid in grids) {
                val checkResult = checkPuzzleExists(grid, difficulty)

                when (checkResult) {
                    is PuzzleCheckResult.ReadyToSave -> {
                        savePuzzleToDatabase(checkResult.gridString, difficulty)
                        savedCount++
                    }
                    is PuzzleCheckResult.AlreadyExists -> {
                        duplicateCount++
                    }
                    is PuzzleCheckResult.Error -> {
                        errorCount++
                    }
                }
            }

            println("Batch-Ergebnis für $difficulty:")
            println("- Verarbeitet: ${grids.size}")
            println("- Gespeichert: $savedCount")
            println("- Duplikate: $duplicateCount")
            println("- Fehler: $errorCount")
        }
    }

    // ... andere bestehende Funktionen (z.B. updateSolvedStatus) ...
}