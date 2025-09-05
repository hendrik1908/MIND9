package com.example.num8rix.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.num8rix.DifficultyLevel

@Entity(tableName = "game_cache")
data class GameCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Index-Spalte für die UNDO-Funktion

    val currentGridString: String, // Speichert den aktuellen Spielstand
    val notesGridString: String, // Speichert die Notizen
    val originalGridString: String, // Speichert Vorgegebene Zahlen des Algotithmus
    val originalLayoutString: String = "", // Speichert das ursprüngliche Layout mit schwarzen Hinweisen
    val difficulty: DifficultyLevel, // Speichert die Schwirigkietsstufe ab
    val puzzleId: Int                // Verweis auf das Rätsel aus Schirigkeitstabelle
)