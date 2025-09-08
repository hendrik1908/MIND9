package com.example.num8rix

class Game(val unsolvedString: String, val layoutString: String = "") {
    var grid = Grid()

   // Hard Coded Game zum test
//    private fun loadVisualGridFromDatabase(difficulty: DifficultyLevel): String {
//        // Platzhalter – hier später Datenbankzugriff
//        return "1········;·5·······;···███···;·········;·········;·········;·███·····;·········;·········"
//    }

    fun generateGame() {
        grid.generateGridFromVisualAndLayout(unsolvedString, layoutString)
    }

    fun startGame() {
        grid.printGrid()
    }
    companion object {
        fun fromGrid(grid: Grid): Game {
            return Game("").apply { this.grid = grid }
        }
    }

}