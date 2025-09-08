package com.example.num8rix.generator

enum class GeneratorDifficulty(
    val blackCellCount: IntRange,
    val symmetricLayout: Boolean,
    val blackClueRatio: Double,
    val minWhiteClues: Int,
    val description: String
) {
    EASY(4..12, true, 0.4, 20, "Große Kompartments, viele Hinweise"),
    MEDIUM(13..25, true, 0.3, 15, "Mittlere Kompartments, moderate Hinweise"),
    HARD(26..38, true, 0.2, 10, "Kleine Kompartments, wenige Hinweise"),
    EXPERT(10..22, false, 0.1, 8, "Sehr kleine Kompartments, minimale Hinweise")
}