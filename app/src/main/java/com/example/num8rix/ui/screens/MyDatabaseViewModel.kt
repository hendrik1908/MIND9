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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Enum für die Schwierigkeitsstufen (falls du es noch nicht hast)
enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}

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
    fun saveGameState(currentGridString: String, notesGridString: String) {
        viewModelScope.launch {
            val newEntry = GameCache(
                currentGridString = currentGridString,
                notesGridString = notesGridString
            )
            gameCacheDao.insert(newEntry)
            println("Spielstand wurde in der Datenbank gespeichert.")
        }
    }

    /**
     * Löscht den letzten gespeicherten Spielstand, um die UNDO-Funktion zu realisieren.
     */
    fun undoLastMove() {
        viewModelScope.launch {
            gameCacheDao.deleteLatestEntry()
            println("Letzter Spielstand wurde gelöscht.")
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
    fun getLatestGameState(onResult: (GameCache?) -> Unit) {
        viewModelScope.launch {
            val latestState = gameCacheDao.getLatestEntry()
            onResult(latestState)
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