package com.example.num8rix

class Game(val unsolvedString: String) {
    val grid = Grid()

   // Hard Coded Game zum test
//    private fun loadVisualGridFromDatabase(difficulty: DifficultyLevel): String {
//        // Platzhalter – hier später Datenbankzugriff
//        return "1········;·5·······;···███···;·········;·········;·········;·███·····;·········;·········"
//    }

    fun generateGame() {
        grid.generateGridFromVisualString(unsolvedString)
        grid.printGrid()
    }

    fun startGame() {
        grid.printGrid()
    }


}