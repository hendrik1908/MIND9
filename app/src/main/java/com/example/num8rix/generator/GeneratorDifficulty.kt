package com.example.num8rix.generator

enum class GeneratorDifficulty(
    val blackCellCount: IntRange,
    val symmetricLayout: Boolean,
    val blackClueRatio: Double,
    val minWhiteClues: Int,
    val description: String
) {
    EASY(22..34, true, 0.4, 20, "Große Kompartments, viele Hinweise"),
    MEDIUM(14..25, true, 0.3, 15, "Mittlere Kompartments, moderate Hinweise"),
    HARD(26..38, true, 0.2, 10, "Kleine Kompartments, wenige Hinweise"),
    EXPERT(5..15, false, 0.1, 8, "Sehr kleine Kompartments, minimale Hinweise")
}