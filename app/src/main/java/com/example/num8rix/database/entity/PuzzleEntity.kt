package com.example.num8rix.database.entity


interface PuzzleEntity {
    val id: Int
    val unsolvedString: String
    val layoutString: String
    val solutionString: String
    val alreadySolved: Boolean
}