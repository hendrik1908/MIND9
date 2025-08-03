package com.example.num8rix.generator

import com.example.num8rix.DifficultyLevel

class PuzzleGeneratorService {

    /**
     * Generiert ein neues Str8ts-Puzzle
     * @param difficulty App-Schwierigkeit (EASY, MEDIUM, HARD)
     * @param debug Ob Debug-Ausgaben angezeigt werden sollen
     * @return Pair von (unsolvedString, solutionString) oder null bei Fehler
     */
    fun generateNewPuzzle(difficulty: DifficultyLevel, debug: Boolean = false): PuzzleResult? {
        val generator = Str8tsGenerator()

        val generatorDifficulty = mapDifficulty(difficulty)
        val puzzleGrid = generator.generate(generatorDifficulty, debug)

        return if (puzzleGrid != null) {
            PuzzleResult(
                unsolved = convertToVisualString(generator.grid, generator.layout),
                solution = convertSolutionToFlatString(generator.solution),
                difficulty = difficulty
            )
        } else null
    }

    /**
     * Mappt App-Schwierigkeit auf Generator-Schwierigkeit
     */
    private fun mapDifficulty(appDifficulty: DifficultyLevel): GeneratorDifficulty {
        return when(appDifficulty) {
            DifficultyLevel.EASY -> GeneratorDifficulty.EASY
            DifficultyLevel.MEDIUM -> GeneratorDifficulty.MEDIUM
            DifficultyLevel.HARD -> GeneratorDifficulty.HARD
        }
    }

    /**
     * Konvertiert Grid + Layout zu Visual String Format
     * Format: "1········;·5·······;···███···;..."
     */
    private fun convertToVisualString(grid: Array<IntArray>, layout: Array<Array<Int?>>): String {
        return Array(9) { row ->
            Array(9) { col ->
                when (layout[row][col]) {
                    null -> if (grid[row][col] == 0) '·' else grid[row][col].toString()[0]
                    0 -> '█'
                    else -> layout[row][col].toString()[0]
                }
            }.joinToString("")
        }.joinToString(";")
    }

    /**
     * Konvertiert Lösung zu kompaktem String
     * Format: "534678912672195348..." (81 Zeichen)
     */
    private fun convertSolutionToFlatString(solution: Array<IntArray>): String {
        return solution.joinToString("") { row ->
            row.joinToString("")
        }
    }
}

/**
 * Ergebnis der Puzzle-Generierung
 */
data class PuzzleResult(
    val unsolved: String,
    val solution: String,
    val difficulty: DifficultyLevel
)