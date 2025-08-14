package com.example.num8rix.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_cache")
data class GameCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Index-Spalte für die UNDO-Funktion

    val currentGridString: String, // Speichert den aktuellen Spielstand
    val notesGridString: String // Speichert die Notizen
)