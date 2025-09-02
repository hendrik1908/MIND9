package com.example.num8rix.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mittel")
data class Mittel(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    override val unsolvedString: String,
    override val solutionString: String,
    override val alreadySolved: Boolean
) : PuzzleEntity