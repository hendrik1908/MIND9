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
import com.example.num8rix.game.Str8tsGridSerializer // <- Den Import für deine Serializer-Klasse hinzufügen!
import com.example.num8rix.game.DifficultyLevel // <- Import für das DifficultyLevel Enum

// Enum für die Schwierigkeitsstufen (falls du es noch nicht hast)
enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
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
}

// Str8tsGridSerializer.kt (deine Datei aus dem Upload)
// ... der Inhalt deines Cells2DB Files ...