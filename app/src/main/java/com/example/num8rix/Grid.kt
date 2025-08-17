package com.example.num8rix

class Grid (){
    var fields: Array<Array<Field>> = Array(9) { Array(9) { Field(FieldColor.WHITE) } }

    fun getField(row: Int, col: Int): Field = fields[row][col]

    fun setField(row: Int, col: Int, field: Field) {
        fields[row][col] = field
    }

    companion object {
        private const val GRID_SIZE = 9
        private const val EMPTY_WHITE = '·'
        private const val BLACK_CELL = '█'
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

    // NEU
    fun toVisualString(): String {
        return (0 until GRID_SIZE).joinToString(";") { row ->
            (0 until GRID_SIZE).joinToString("") { col ->
                val f = getField(row, col)
                when {
                    f.isBlack() -> BLACK_CELL.toString()
                    f.value == 0 -> EMPTY_WHITE.toString()
                    else -> f.value.toString()
                }
            }
        }
    }

    fun generateGridFromVisualString(gridString: String) {
        val rows = gridString.split(";")
        if (rows.size != 9) throw IllegalArgumentException("Grid must have 9 rows")

        for (row in 0 until 9) {
            val line = rows[row]
            if (line.length != 9) throw IllegalArgumentException("Each row must have 9 characters")

            for (col in 0 until 9) {
                val char = line[col]
                val field: Field = when {
                    char in '1'..'9' -> Field(FieldColor.WHITE, isInitial = true).apply { value = char.digitToInt() }
                    char == '·' -> Field(FieldColor.WHITE, isInitial = false) // leeres Feld
                    char == '█' -> Field(FieldColor.BLACK, isInitial = true)
                    else -> throw IllegalArgumentException("Invalid character: $char at row $row, col $col")
                }
                setField(row, col, field)
            }
        }
    }

    fun printGrid() {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val f = getField(row, col)
                print(
                    when {
                        f.isBlack() -> "█"
                        f.value == 0 -> "·"
                        else -> f.value.toString()
                    }
                )
            }
            println()
        }
    }
}
