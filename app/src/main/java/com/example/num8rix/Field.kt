package com.example.num8rix

data class Field(var color: FieldColor, var value: Int = 0) {
    fun isWhite() = color == FieldColor.WHITE
    fun isBlack() = color == FieldColor.BLACK
}
