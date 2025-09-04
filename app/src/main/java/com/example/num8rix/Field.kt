package com.example.num8rix

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

data class Field(
    var color: FieldColor,
    val isInitial: Boolean = false,
    var notes: MutableSet<Int> = mutableSetOf(),
    var blackCellValue: Int? = null // Hinweiszahl für schwarze Felder
) {
    var value by mutableIntStateOf(0) // observable Int State
    fun isWhite() = color == FieldColor.WHITE
    fun isBlack() = color == FieldColor.BLACK
    fun hasBlackCellHint() = isBlack() && blackCellValue != null && blackCellValue!! > 0

    fun clearNotes() {
        notes.clear()
    }
}