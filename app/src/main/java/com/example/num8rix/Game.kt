package com.example.num8rix

class Game(val difficulty: DifficultyLevel) {
    val grid = Grid()

    private fun loadVisualGridFromDatabase(difficulty: DifficultyLevel): String {
        // Platzhalter – hier später Datenbankzugriff
        return "1········;·5·······;···███···;·········;·········;·········;·███·····;·········;·········"
    }

    fun generateGame() {
        val visualString = loadVisualGridFromDatabase(difficulty)
        grid.generateGridFromVisualString(visualString)
        grid.printGrid()
    }

    fun startGame() {
        grid.printGrid()
    }


}