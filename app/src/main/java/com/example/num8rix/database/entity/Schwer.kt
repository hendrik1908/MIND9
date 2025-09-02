package com.example.num8rix.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schwer")
data class Schwer(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,
    override val unsolvedString: String,
    override val solutionString: String,
    override val alreadySolved: Boolean
) : PuzzleEntity