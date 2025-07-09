package com.example.num8rix

class Grid {
    var fields: Array<Array<Field>> = Array(9) { Array(9) { Field(FieldColor.WHITE) } }

    fun getField(row: Int, col: Int): Field = fields[row][col]

    fun setField(row: Int, col: Int, field: Field) {
        fields[row][col] = field
    }

    fun clearFields() {
        for (row in fields) {
            for (field in row) {
                field.value = 0
                field.color = FieldColor.WHITE
            }
        }
    }

    fun placeBlackFields() {
        // Beispiel: zufällige schwarze Felder setzen
    }

    fun isValid(): Boolean {
        // Spielregeln prüfen
        return true
    }
    fun generateGrid() {
        // Spielfeld aufbauen je nach Schwierigkeit
    }
}
