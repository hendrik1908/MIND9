package com.example.num8rix
/**
 * Serializer-Klasse für Str8ts 9x9 Grid mit hard-codiertem Array
 */
class Str8tsGridSerializer {

    companion object {
        private const val GRID_SIZE = 9
        private const val EMPTY_WHITE = '·'
        private const val BLACK_CELL = '█'
    }

    // Dein hard-codiertes Spielfeld als zweidimensionales Array
   private val hardCodedGrid = arrayOf(
        arrayOf('1', '·', '·', '·', '·', '·', '·', '·', '·'),
        arrayOf('·', '5', '·', '·', '·', '·', '·', '·', '·'),
        arrayOf('·', '·', '·', '█', '█', '█', '·', '·', '·'),
        arrayOf('·', '·', '·', '·', '·', '·', '·', '·', '·'),
        arrayOf('·', '·', '·', '·', '·', '·', '·', '·', '·'),
        arrayOf('·', '·', '·', '·', '·', '·', '·', '·', '·'),
        arrayOf('2', '█', '█', '█', '·', '·', '·', '·', '·'),
        arrayOf('·', '·', '·', '·', '·', '·', '·', '·', '·'),
        arrayOf('·', '·', '·', '·', '·', '·', '·', '·', '·')
    )


    /**
     * Konvertiert das hard-codierte Grid in einen String
     * Format: Einfacher String mit Zeichen direkt hintereinander, Zeilen getrennt durch Semikolon
     */
    fun gridToString(): String {
        return hardCodedGrid.joinToString(";") { row ->
            row.joinToString("")
        }
    }


    /**
     * Zeigt das Grid formatiert in der Konsole an
     */
    fun printGrid() {
        println("Str8ts Grid:")
        println("=" * 19)
        hardCodedGrid.forEach { row ->
            row.forEach { cell ->
                print("$cell ")
            }
            println()
        }
        println("=" * 19)
    }

    /**
     * Gibt das hard-codierte Grid zurück (falls du es anderswo verwenden möchtest)
     */
    fun getGrid(): Array<Array<Char>> {
        return hardCodedGrid
    }

    /**
     * Konvertiert einen String zurück in ein Grid (für Database-Retrieval)
     */
    fun stringToGrid(gridString: String): Array<Array<Char>> {
        val rows = gridString.split(";")
        require(rows.size == GRID_SIZE) { "String muss 9 Zeilen repräsentieren" }

        return Array(GRID_SIZE) { rowIndex ->
            val rowString = rows[rowIndex]
            require(rowString.length == GRID_SIZE) { "Jede Zeile muss 9 Zeichen haben" }

            Array(GRID_SIZE) { colIndex ->
                rowString[colIndex]
            }
        }
    }
}

// Beispiel für die Nutzung
fun main() {
    val serializer = Str8tsGridSerializer()

    // Zeige das Grid an
    serializer.printGrid()

    // Konvertiere zu verschiedenen String-Formaten
    println("\n1. Standard String (mit Semikolon):")
    val standardString = serializer.gridToString()
    println(standardString)

    
    // Test: String zurück zu Grid konvertieren
    println("\n5. Test: String zurück zu Grid:")
    val reconstructedGrid = serializer.stringToGrid(standardString)
    println("Rekonstruktion erfolgreich: ${reconstructedGrid.contentDeepEquals(serializer.getGrid())}")


}

// Extension function für String-Wiederholung
private operator fun String.times(n: Int): String = this.repeat(n)