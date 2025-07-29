package com.example.num8rix.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schwer")
data class Schwer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val unsolvedString: String,
    val solutionString: String,
    val alreadySolved: Boolean
)