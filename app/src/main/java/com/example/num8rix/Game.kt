package com.example.num8rix

class Game(val difficulty: DifficultyLevel) {
    val grid = Grid()

    fun startGame() {
        generateGame()
    }


    fun generateGame() {
        val generator = Grid()
        generator.generateGrid()
    }
}