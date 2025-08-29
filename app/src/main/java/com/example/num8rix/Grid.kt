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
    fun copy(): Grid {
        val newGrid = Grid()
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val originalField = this.getField(row, col)
                val newField = Field(originalField.color, originalField.isInitial).apply {
                    value = originalField.value
                    notes.clear()
                    notes.addAll(originalField.notes)
                }
                newGrid.setField(row, col, newField)
            }
        }
        return newGrid
    }

    fun placeBlackFields() {
        // Beispiel: zufällige schwarze Felder setzen
    }

    fun isValid(): Boolean {
        // Spielregeln prüfen
        return true
    }

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
    fun updateGridFromVisualString(gridString: String) {
        val rows = gridString.split(";")
        if (rows.size != 9) throw IllegalArgumentException("Grid must have 9 rows")

        for (row in 0 until 9) {
            val line = rows[row]
            if (line.length != 9) throw IllegalArgumentException("Each row must have 9 characters")

            for (col in 0 until 9) {
                val char = line[col]
                val field = getField(row, col)

                when {
                    char in '1'..'9' -> {
                        field.value = char.digitToInt()
                        // isInitial NICHT ändern - bleibt wie es war
                    }
                    char == '·' -> {
                        field.value = 0
                        // Nur Wert löschen, wenn es nicht initial war
                        if (!field.isInitial) {
                            field.value = 0
                        }
                    }
                    // Schwarze Felder bleiben unverändert
                }
            }
        }
    }

    fun notesToString(): String {
        return (0 until GRID_SIZE).joinToString(";") { row ->
            (0 until GRID_SIZE).joinToString(",") { col ->
                val notes = getField(row, col).notes
                if (notes.isEmpty()) "-" else notes.sorted().joinToString("")
            }
        }
    }

    fun generateNotesFromString(notesString: String) {
        val rows = notesString.split(";")
        if (rows.size != GRID_SIZE) throw IllegalArgumentException("Notes grid must have 9 rows")

        for (row in 0 until GRID_SIZE) {
            val cells = rows[row].split(",")
            if (cells.size != GRID_SIZE) throw IllegalArgumentException("Each row must have 9 cells")

            for (col in 0 until GRID_SIZE) {
                val cellNotes = cells[col]
                val field = getField(row, col)
                field.notes.clear()
                if (cellNotes != "-") {
                    cellNotes.forEach { c ->
                        if (c in '1'..'9') {
                            field.notes.add(c.digitToInt())
                        }
                    }
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

    fun generateGridFromFlatString(flat: String) {
        if (flat.length != 81) throw IllegalArgumentException("Solution string must have 81 characters")
        for (i in flat.indices) {
            val row = i / 9
            val col = i % 9
            val value = flat[i].toString().toInt()
            this.getField(row, col).value = value
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
