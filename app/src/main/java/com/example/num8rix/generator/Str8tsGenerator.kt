package com.example.num8rix.generator

import com.example.num8rix.generator.GeneratorDifficulty.MEDIUM as MEDIUM1

class Str8tsGenerator {
    val grid = Array(9) { IntArray(9) }
    val solution = Array(9) { IntArray(9) }

    val layout: Array<Array<Int?>> = Array(9) { Array(9) { null } }

    private val compartments = mutableListOf<List<Pair<Int, Int>>>()
    private val cellToCompartmentMap = mutableMapOf<Pair<Int, Int>, List<List<Pair<Int, Int>>>>()
    private var debugMode = false
    private var currentGeneratorDifficulty = MEDIUM1

    fun isDebugMode() = debugMode

    fun generate(
        GeneratorDifficulty: GeneratorDifficulty = MEDIUM1,
        debug: Boolean = false
    ): Array<IntArray>? {
        debugMode = debug
        currentGeneratorDifficulty = GeneratorDifficulty

        if (debugMode) {
            println("Generiere ${GeneratorDifficulty.description} Puzzle...")
        }

        return generateRandomLayoutUntilSuccess()
    }

    private fun generateRandomLayoutUntilSuccess(): Array<IntArray>? {
        var layoutAttempts = 0
        // Wir geben ihm viele Versuche, ein Layout zu finden, das überhaupt eine Lösung zulässt.
        val maxLayoutAttempts = 200

        while (layoutAttempts < maxLayoutAttempts) {
            layoutAttempts++
            if (debugMode) println("Layout-Versuch $layoutAttempts/$maxLayoutAttempts...")

            // SCHRITT 1: Erzeuge ein gültiges Muster aus schwarzen und weißen Feldern.
            if (!createRandomLayout()) {
                continue // Das Layout selbst war fehlerhaft (z.B. nicht verbunden).
            }

            // Finde die Kompartments für dieses Layout.
            findCompartments()

            // SCHRITT 2: Finde EINE GÜLTIGE LÖSUNG für dieses leere Layout.
            resetGridValues() // Stelle sicher, dass das Gitter leer ist.

            if (solveWithTimeLimit(15000)) { // Gib dem Solver 15 Sekunden Zeit pro Versuch.

                if (debugMode) println("Erfolg! Lösbares Layout nach $layoutAttempts Versuchen gefunden.")

                // Die aktuelle 'grid'-Variable enthält jetzt eine vollständige, korrekte Lösung.
                // Speichere sie im 'solution'-Array.
                for (r in 0 until 9) {
                    for (c in 0 until 9) {
                        solution[r][c] = grid[r][c]
                    }
                }

                // SCHRITT 3: Platziere die Hinweise in den schwarzen Feldern.
                // Diese Funktion muss jetzt die Zahlen aus der 'solution' nehmen.
                placeBlackCellCluesFromSolution()

                // SCHRITT 4: Baue das finale Puzzle, indem du Zahlen aus der Lösung entfernst.
                extractPuzzle()

                // Fertig! Wir haben ein garantiert lösbares Puzzle mit einer einzigartigen Lösung.
                return grid
            }
            // Wenn der Solver hier fehlschlägt, war das Layout + Clues unlösbar. Nächster Versuch.
        }

        if (debugMode) println("Alle $maxLayoutAttempts Versuche fehlgeschlagen, ein lösbares Layout zu finden.")
        return null
    }

    private fun placeBlackCellCluesFromSolution() {
        val blackCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                // Finde alle Zellen, die als schwarz markiert sind (layout[r][c] == 0)
                if (layout[r][c] == 0) {
                    blackCells.add(r to c)
                }
            }
        }
        blackCells.shuffle()

        val numCluesToPlace = (blackCells.size * currentGeneratorDifficulty.blackClueRatio).toInt()

        for (i in 0 until numCluesToPlace) {
            val (r, c) = blackCells[i]
            // Nimm die Zahl für den Hinweis direkt aus der zuvor generierten Lösung!
            layout[r][c] = solution[r][c]
        }
    }

    private fun printLayoutDebug() {
        println("Layout (B=black, W=white, 1-9=clues):")
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                when (layout[r][c]) {
                    null -> print("W ")
                    0 -> print("B ")
                    else -> print("${layout[r][c]} ")
                }
            }
            println()
        }
    }

    private fun createRandomLayout(): Boolean {
        // 1. Setze das Layout zurück auf "komplett weiß" (null)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                layout[r][c] = null
            }
        }

        val desiredBlackCellCount = currentGeneratorDifficulty.blackCellCount.random()
        val placedBlackCells = mutableSetOf<Pair<Int, Int>>()

        // Erstelle eine Liste aller Zellen und mische sie für eine zufällige Reihenfolge
        val allCells = (0..80).map { it / 9 to it % 9 }.toMutableList()
        allCells.shuffle()

        for (cell in allCells) {
            // Wenn die Zielanzahl erreicht ist, sind wir fertig.
            if (placedBlackCells.size >= desiredBlackCellCount) break

            val (r, c) = cell

            // Überspringe, falls die Zelle schon schwarz ist (z.B. als symmetrisches Paar)
            if (layout[r][c] != null) continue

            // Temporär die Zelle(n) als schwarz markieren
            val cellsToTry = mutableSetOf(r to c)
            if (currentGeneratorDifficulty.symmetricLayout) {
                val symmetricCell = (8 - r) to (8 - c)
                if (layout[symmetricCell.first][symmetricCell.second] == null) {
                    cellsToTry.add(symmetricCell)
                }
            }

            // Markiere die Zellen im Layout temporär als schwarz
            cellsToTry.forEach { (tr, tc) -> layout[tr][tc] = 0 }

            // PRÜFUNG: Bleiben die weißen Felder nach dieser Änderung noch verbunden?
            if (isWhiteCellsConnected(layout)) {
                // Ja, die Änderung ist gültig. Behalte sie bei.
                placedBlackCells.addAll(cellsToTry)
            } else {
                // Nein, die Änderung würde das Gitter zerteilen. Mache sie rückgängig.
                cellsToTry.forEach { (tr, tc) -> layout[tr][tc] = null }
            }
        }

        // Am Ende sollte das Layout immer gültig sein, da wir es schrittweise aufgebaut haben.
        // Ein finaler Check schadet aber nie.
        if (placedBlackCells.size < currentGeneratorDifficulty.blackCellCount.first) {
            if(debugMode) println("KONSTRUKTION FEHLGESCHLAGEN: Konnte nicht genug schwarze Felder platzieren (${placedBlackCells.size}).")
            return false
        }

        return isLayoutValid()
    }

    private fun placeBlackCellClues() {
        val blackCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (layout[r][c] != null) {
                    blackCells.add(r to c)
                }
            }
        }
        blackCells.shuffle()

        val numCluesToPlace = (blackCells.size * currentGeneratorDifficulty.blackClueRatio).toInt()
        var cluesPlaced = 0

        for (cell in blackCells) {
            if (cluesPlaced >= numCluesToPlace) break

            val (r, c) = cell
            val possibleNumbers = (1..9).shuffled()

            for (num in possibleNumbers) {
                // HIER IST DIE VERBESSERUNG:
                // Prüfe zuerst, ob der Hinweis Sudoku-konform ist (isClueValid)
                // UND DANN, ob er logisch möglich ist (isCluePlacementPossible)
                if (isClueValid(r, c, num) && isCluePlacementPossible(r, c, num)) {
                    layout[r][c] = num
                    cluesPlaced++
                    break
                }
            }
        }
    }

    /**
     * Führt eine schnelle Vorab-Prüfung durch, um festzustellen, ob ein Hinweis in einem
     * schwarzen Feld einen sofortigen, offensichtlichen Widerspruch in den angrenzenden
     * weißen Kompartments erzeugt.
     */
    private fun isCluePlacementPossible(r: Int, c: Int, num: Int): Boolean {
        // Finde alle weißen Kompartments, die direkt an die Zelle (r, c) angrenzen.
        val adjacentCompartments = compartments.filter { comp ->
            // Ein Kompartment ist angrenzend, wenn es ein weißes Feld enthält,
            // das ein direkter Nachbar der schwarzen Zelle (r,c) ist.
            comp.any { (cr, cc) ->
                (cr == r && (cc == c - 1 || cc == c + 1)) || (cc == c && (cr == r - 1 || cr == r + 1))
            }
        }

        // Wenn es keine angrenzenden Kompartments gibt, gibt es auch keinen Konflikt.
        if (adjacentCompartments.isEmpty()) {
            return true
        }

        // Prüfe jedes angrenzende Kompartment
        for (comp in adjacentCompartments) {
            val compSize = comp.size

            // EINE STRAIGHT KANN EINE ZAHL 'num' NUR ENTHALTEN, WENN 'num' KLEINER-GLEICH 9 IST
            // UND GRÖSSER-GLEICH der Kompartmentgröße.
            // Bsp: Eine 5er-Straight (z.B. 1-2-3-4-5) kann keine 6,7,8,9 enthalten.
            // Bsp: Eine 8er-Straight (z.B. 2-3-4-5-6-7-8-9) kann keine 1 enthalten.

            // Die niedrigstmögliche Zahl in einer Straight der Größe 'compSize', die 'num' enthält.
            val lowestPossibleValue = maxOf(1, num - compSize + 1)
            // Die höchstmögliche Zahl in einer Straight der Größe 'compSize', die 'num' enthält.
            val highestPossibleValue = minOf(9, num)

            // Wenn die höchste mögliche Zahl kleiner ist als die Kompartmentgröße,
            // kann die Straight 'num' nicht enthalten.
            // Beispiel: Kompartment Größe 4, Hinweis ist 3. Die Straight wäre [1,2,3,4].
            // lowestPossible = 1, highestPossible=3. 3 < 4 -> UNMÖGLICH, eine 4er-Straight mit 3 zu bilden.
            // Korrektur: Die Bedingung muss prüfen, ob das Intervall der möglichen Zahlen überhaupt existiert.
            // Die höchste Zahl im Straight muss mindestens `compSize` sein.

            var canContainNum = false
            // Iteriere durch alle möglichen Straights der Größe compSize, die es gibt
            for (start in 1..(10 - compSize)) {
                val straight = (start until start + compSize)
                if (num in straight) {
                    canContainNum = true
                    break // Es gibt mindestens eine mögliche Straight, das reicht.
                }
            }

            // Wenn für DIESES eine Kompartment keine einzige gültige Straight gefunden wurde,
            // die den Hinweis 'num' aufnehmen kann, dann ist die Platzierung unmöglich.
            if (!canContainNum) {
                return false // Fail Fast!
            }
        }

        // Alle angrenzenden Kompartments können den Hinweis theoretisch aufnehmen.
        return true
    }



    /**
     * Hilfsfunktion, die prüft, ob ein Hinweis in einem schwarzen Feld
     * mit anderen, bereits gesetzten Hinweisen in Konflikt steht.
     */
    private fun isClueValid(r: Int, c: Int, num: Int): Boolean {
        // Prüfe die Zeile auf Konflikte mit anderen Hinweisen
        for (col in 0 until 9) {
            if (c == col) continue
            // layout[r][col] ist > 0, wenn dort bereits ein Hinweis steht
            if (layout[r][col] == num) {
                return false
            }
        }

        // Prüfe die Spalte auf Konflikte mit anderen Hinweisen
        for (row in 0 until 9) {
            if (r == row) continue
            if (layout[row][c] == num) {
                return false
            }
        }

        // Kein Konflikt mit anderen Hinweisen gefunden
        return true
    }



    private fun isWhiteCellsConnected(currentGrid: Array<Array<Int?>>): Boolean {
        val whiteCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (currentGrid[r][c] == null) { // Null bedeutet weiße Zelle
                    whiteCells.add(r to c)
                }
            }
        }

        if (whiteCells.isEmpty()) {
            return true // Keine weißen Zellen, trivialerweise verbunden
        }

        val visited = Array(9) { BooleanArray(9) }
        val stack = mutableListOf<Pair<Int, Int>>()

        // Starte den Flood Fill von der ersten gefundenen weißen Zelle
        stack.add(whiteCells.first())
        visited[whiteCells.first().first][whiteCells.first().second] = true
        var count = 0

        while (stack.isNotEmpty()) {
            val (r, c) = stack.removeAt(stack.size - 1)
            count++

            // Nachbarn prüfen (oben, unten, links, rechts)
            val neighbors = listOf(r - 1 to c, r + 1 to c, r to c - 1, r to c + 1)
            for ((n_r, n_c) in neighbors) {
                if (n_r in 0..8 && n_c in 0..8 && currentGrid[n_r][n_c] == null && !visited[n_r][n_c]) {
                    visited[n_r][n_c] = true
                    stack.add(n_r to n_c)
                }
            }
        }

        // Wenn die Anzahl der erreichten weißen Zellen ungleich der Gesamtzahl der weißen Zellen ist,
        // sind sie nicht alle verbunden (oder es gibt isolierte Felder)
        return count == whiteCells.size
    }

    private fun resetGridValues() {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                grid[r][c] = 0
                solution[r][c] = 0
            }
        }
    }

    private fun resetGrid() {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                grid[r][c] = 0
                solution[r][c] = 0
                // Wichtig: Zu Beginn ist alles weiß (null), damit der Solver eine Basislösung finden kann
                layout[r][c] = null
            }
        }
    }

    private fun isLayoutValid(): Boolean {
        val whiteCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (layout[r][c] == null) {
                    whiteCells.add(r to c)
                }
            }
        }

        if (whiteCells.size < 30) {
            if (debugMode) println("Validierung fehlgeschlagen: Zu wenig weiße Felder (${whiteCells.size})")
            return false
        }

        for ((r, c) in whiteCells) {
            val hasHorizontalNeighbor = (c > 0 && layout[r][c - 1] == null) || (c < 8 && layout[r][c + 1] == null)
            val hasVerticalNeighbor = (r > 0 && layout[r - 1][c] == null) || (r < 8 && layout[r + 1][c] == null)

            if (!hasHorizontalNeighbor && !hasVerticalNeighbor) {
                if (debugMode) println("Validierung fehlgeschlagen: Isoliertes Feld bei ($r, $c)")
                return false
            }
        }

        if (whiteCells.isEmpty()) return true

        val visited = Array(9) { BooleanArray(9) }
        val stack = mutableListOf(whiteCells.first())
        visited[whiteCells.first().first][whiteCells.first().second] = true
        var count = 0

        while (stack.isNotEmpty()) {
            val (r, c) = stack.removeAt(stack.size - 1)
            count++

            listOf(r - 1 to c, r + 1 to c, r to c - 1, r to c + 1).forEach { (nr, nc) ->
                if (nr in 0..8 && nc in 0..8 && layout[nr][nc] == null && !visited[nr][nc]) {
                    visited[nr][nc] = true
                    stack.add(nr to nc)
                }
            }
        }

        if (count != whiteCells.size) {
            if (debugMode) println("Validierung fehlgeschlagen: Weiße Felder sind nicht verbunden ($count vs ${whiteCells.size})")
            return false
        }

        return true
    }

    private fun findCompartments() {
        compartments.clear()
        cellToCompartmentMap.clear()

        // Horizontale Kompartiments finden (Reihen durchsuchen)
        val horizontalCompartments = mutableListOf<List<Pair<Int, Int>>>()
        for (r in 0 until 9) {
            var start = -1
            for (c in 0 until 9) {
                if (layout[r][c] == null) {
                    if (start == -1) start = c
                } else {
                    if (start != -1) {
                        // Kompartment hinzufügen, auch wenn es nur eine Zelle ist
                        val comp = (start until c).map { col -> r to col }
                        horizontalCompartments.add(comp)
                    }
                    start = -1
                }
            }
            // Am Ende der Reihe prüfen, ob ein Kompartment offen ist
            if (start != -1) {
                val comp = (start until 9).map { col -> r to col }
                horizontalCompartments.add(comp)
            }
        }

        // Vertikale Kompartiments finden (Spalten durchsuchen)
        val verticalCompartments = mutableListOf<List<Pair<Int, Int>>>()
        for (c in 0 until 9) {
            var start = -1
            for (r in 0 until 9) {
                if (layout[r][c] == null) {
                    if (start == -1) start = r
                } else {
                    if (start != -1) {
                        // Kompartment hinzufügen, auch wenn es nur eine Zelle ist
                        val comp = (start until r).map { row -> row to c }
                        verticalCompartments.add(comp)
                    }
                    start = -1
                }
            }
            // Am Ende der Spalte prüfen, ob ein Kompartment offen ist
            if (start != -1) {
                val comp = (start until 9).map { row -> row to c }
                verticalCompartments.add(comp)
            }
        }

        // Jede weiße Zelle einem oder mehreren Kompartiments zuordnen
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (layout[r][c] == null) {
                    val hComp = horizontalCompartments.find { it.contains(r to c) }
                    val vComp = verticalCompartments.find { it.contains(r to c) }
                    val compsForCell = mutableListOf<List<Pair<Int, Int>>>()
                    if (hComp != null) compsForCell.add(hComp)
                    if (vComp != null) compsForCell.add(vComp)
                    // Falls keine Kompartiments gefunden wurden, erstelle ein Kompartment nur für diese Zelle
                    if (compsForCell.isEmpty()) {
                        compsForCell.add(listOf(r to c))
                        // Füge dieses Einzelzellen-Kompartment auch zu compartments hinzu
                        compartments.add(listOf(r to c))
                    }
                    // Weise die Kompartiments der Zelle zu
                    cellToCompartmentMap[r to c] = compsForCell
                }
            }
        }

        // Füge alle horizontalen und vertikalen Kompartiments zur Gesamtliste hinzu (ohne Duplikate)
        compartments.addAll(horizontalCompartments.distinct())
        compartments.addAll(verticalCompartments.distinct())

        // Debug-Ausgaben zur Überprüfung
        if (debugMode) {
            println("DEBUG: Gefundene Kompartments: ${compartments.size}")
            println("DEBUG: Zellen in cellToCompartmentMap: ${cellToCompartmentMap.size}")

            // Zeige alle weißen Zellen ohne Kompartments (sollte jetzt keine mehr geben)
            for (r in 0 until 9) {
                for (c in 0 until 9) {
                    if (layout[r][c] == null && cellToCompartmentMap[r to c] == null) {
                        //println("DEBUG: Weiße Zelle ($r, $c) hat KEIN Kompartment")
                    } else if (layout[r][c] == null) {
                        //println("DEBUG: Zelle ($r, $c) hat ${cellToCompartmentMap[r to c]?.size ?: 0} Kompartment(s)")
                    }
                }
            }
        }
    }

    /**
     * Zählt, für wie viele leere Nachbarzellen die Zahl 'num' ebenfalls ein gültiger Zug wäre.
     * Je NIEDRIGER das Ergebnis, desto weniger stört 'num' die Nachbarn und desto besser ist die Wahl.
     * Diese Version ist deutlich schneller als die vorherige.
     */
    private fun countConstraints(r: Int, c: Int, num: Int): Int {
        var constraints = 0

        // Finde alle einzigartigen, leeren, weißen Nachbarzellen in der gleichen Zeile und Spalte.
        val neighbors = mutableSetOf<Pair<Int, Int>>()
        for (i in 0 until 9) {
            // Horizontale Nachbarn
            if (i != c && layout[r][i] == null && grid[r][i] == 0) {
                neighbors.add(r to i)
            }
            // Vertikale Nachbarn
            if (i != r && layout[i][c] == null && grid[i][c] == 0) {
                neighbors.add(i to c)
            }
        }

        // Prüfe für jede Nachbarzelle, ob 'num' dort auch hätte platziert werden können.
        for (neighbor in neighbors) {
            if (isValidMove(grid, neighbor.first, neighbor.second, num)) {
                constraints++
            }
        }

        return constraints
    }


    private fun extractPuzzle() {
        // SCHRITT 1: Beginne mit der vollständigen Lösung.
        // Das 'grid' wird unsere Arbeitskopie, die wir zum Puzzle reduzieren.
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                grid[r][c] = solution[r][c]
            }
        }

        // SCHRITT 2: Sammle alle weißen Felder, die potenzielle Hinweise sind.
        val whiteCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (layout[r][c] == null) { // layout[r][c] == null bedeutet, es ist ein weißes Feld
                    whiteCells.add(r to c)
                }
            }
        }
        // Mische die Liste, um bei jedem Durchlauf unterschiedliche Rätsel zu erzeugen.
        whiteCells.shuffle()

        if (debugMode) {
            println("Beginne das Entfernen von Hinweisen (${whiteCells.size} Kandidaten). Ziel: Eindeutigkeit wahren.")
        }
        var removedCount = 0

        // SCHRITT 3: Gehe jeden potenziellen Hinweis durch und versuche, ihn zu entfernen.
        for ((r, c) in whiteCells) {
            // Überprüfe, ob die minimale Anzahl an Hinweisen bereits erreicht ist.
            val currentWhiteClues = whiteCells.size - removedCount
            if (currentWhiteClues <= currentGeneratorDifficulty.minWhiteClues) {
                if(debugMode) println("Minimale Anzahl von ${currentGeneratorDifficulty.minWhiteClues} Hinweisen erreicht. Stoppe Entfernung.")
                break // Ziel erreicht, nicht weiter entfernen.
            }

            // Merke dir den ursprünglichen Wert der Zelle.
            val originalValue = grid[r][c]
            if (originalValue == 0) continue // Sollte nicht passieren, aber sicher ist sicher

            // Versuchsweise den Hinweis entfernen (auf 0 setzen).
            grid[r][c] = 0

            // Erstelle eine Testkopie des aktuellen Rätsels.
            // Dies ist SEHR WICHTIG, damit der Lösungszähler das 'grid' nicht verändert.
            val testGrid = Array(9) { row -> grid[row].clone() }

            // SCHRITT 4: Überprüfe, ob das Rätsel noch EINE EINZIGE Lösung hat.
            // Wir nutzen maxCount=2 als Optimierung: uns interessiert nur, ob es 0, 1 oder >1 Lösungen gibt.
            val solutionCount = countSolutions(testGrid, 0, 0, 2)

            if (solutionCount == 1) {
                // ERFOLG: Die Entfernung war sicher, die Eindeutigkeit bleibt gewahrt.
                // Behalte die Änderung bei (grid[r][c] bleibt 0).
                removedCount++
            } else {
                // FEHLSCHLAG: Die Entfernung würde zu 0 oder >1 Lösungen führen.
                // Mache die Änderung rückgängig.
                grid[r][c] = originalValue
            }
        }

        if (debugMode) {
            val finalClueCount = whiteCells.size - removedCount
            println("Entfernungsprozess abgeschlossen. $removedCount Zahlen entfernt. Verbleibende Hinweise in weißen Feldern: $finalClueCount.")
        }
    }


    private fun hasUniqueSolution(): Boolean {
        val testGrid = Array(9) { r -> IntArray(9) { c -> grid[r][c] } }
        return countSolutions(testGrid, 0, 0, 2) == 1
    }

    private fun countSolutions(testGrid: Array<IntArray>, startR: Int, startC: Int, maxCount: Int): Int {
        var r = startR
        var c = startC

        while (r < 9) {
            while (c < 9) {
                // Prüfe, ob es eine leere, weiße Zelle ist
                if (layout[r][c] == null && testGrid[r][c] == 0) {
                    var count = 0
                    for (num in 1..9) {
                        // Nutze hier die ZENTRALE isValidMove-Funktion mit dem testGrid
                        if (isValidMove(testGrid, r, c, num)) {
                            testGrid[r][c] = num
                            count += countSolutions(testGrid, r, c + 1, maxCount - count)
                            testGrid[r][c] = 0 // Backtrack
                            if (count >= maxCount) return count
                        }
                    }
                    return count
                }
                c++
            }
            c = 0
            r++
        }
        return 1 // Eine vollständige Lösung wurde gefunden
    }

    private fun solveWithTimeLimit(timeoutMs: Long): Boolean {
        val startTime = System.currentTimeMillis()
        return solveWithBacktrackAndTimeout(startTime, timeoutMs)
    }

    private fun solveWithBacktrackAndTimeout(startTime: Long, timeoutMs: Long): Boolean {
        if (System.currentTimeMillis() - startTime > timeoutMs) {
            if(debugMode) println("Solver Timeout!")
            return false
        }

        // Wählt die Zelle mit den wenigsten Optionen (Most Constrained Variable)
        val emptyCell = findBestEmptyCell()

        if (emptyCell == null) {
            return true // Alle Zellen sind gefüllt, Erfolg!
        }

        val (r, c) = emptyCell

        // *** DAS IST DIE KORREKTUR ***
        // Ruft die intelligente Funktion auf, die die Zahlen nach der LCV-Heuristik sortiert.
        val validNumbers = getValidNumbers(r, c)

        // Wenn es für diese Zelle keine gültigen Züge gibt, ist es eine Sackgasse.
        if (validNumbers.isEmpty()) {
            return false
        }

        // Gehe die intelligent sortierten Zahlen durch
        for (num in validNumbers) {
            // Wir müssen hier NICHT erneut isValidMove prüfen, da getValidNumbers
            // uns bereits eine Liste garantiert gültiger Zahlen liefert.
            grid[r][c] = num

            if (solveWithBacktrackAndTimeout(startTime, timeoutMs)) {
                return true
            }

            grid[r][c] = 0 // Backtrack
        }

        return false
    }

    /**
     * Prüft, ob alle weißen Zellen im Grid gefüllt sind.
     */
    private fun areAllWhiteCellsFilled(): Boolean {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (layout[r][c] == null && grid[r][c] == 0) {
                    return false // Noch eine leere weiße Zelle gefunden
                }
            }
        }
        return true // Alle weißen Zellen sind gefüllt
    }

    // In der Str8tsGenerator Klasse

    private fun findBestEmptyCell(): Pair<Int, Int>? {
        var bestCell: Pair<Int, Int>? = null
        var minOptions = 10 // Startwert > 9

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                // Finde eine leere, weiße Zelle
                if (layout[r][c] == null && grid[r][c] == 0) {
                    // Zähle die gültigen Optionen für diese Zelle
                    val options = (1..9).count { num -> isValidMove(grid, r, c, num) }

                    // WICHTIGE ÄNDERUNG:
                    // Behandle eine Zelle mit 0 Optionen nicht mehr als Sonderfall.
                    // Eine Zelle mit 0 Optionen ist eine exzellente Wahl,
                    // da der Solver sofort merkt, dass dies eine Sackgasse ist und zurückspringt (Backtrack).
                    // Der `if (options == 0) continue` Befehl wird entfernt.

                    if (options < minOptions) {
                        minOptions = options
                        bestCell = r to c
                        // Optimierung: Wenn wir eine Zelle mit 0 oder 1 Option finden,
                        // ist sie die beste Wahl, um den Suchbaum schnell zu reduzieren.
                        if (minOptions <= 1) {
                            return bestCell
                        }
                    }
                }
            }
        }

        // Wenn nach der Schleife keine Zelle gefunden wurde, ist das Gitter wirklich voll.
        return bestCell
    }

    private fun getValidNumbers(r: Int, c: Int): List<Int> {
        val validMoves = (1..9).filter { num -> isValidMove(grid, r, c, num) }

        // Sortiere die gültigen Züge. Die mit dem GERINGSTEN Störfaktor (Constraints) kommen zuerst.
        return validMoves.sortedBy { num -> countConstraints(r, c, num) }
    }


    /**
     * Dies ist die ZENTRALE und EINZIGE Funktion zur Überprüfung eines Zuges.
     * Sie wird vom Haupt-Solver UND vom Lösungszähler (`countSolutions`) verwendet.
     * @param grid Das Gitter, auf dem der Zug geprüft wird (kann das Haupt-Grid oder ein Test-Grid sein).
     * @param r Die Zeile der zu prüfenden Zelle.
     * @param c Die Spalte der zu prüfenden Zelle.
     * @param num Die Zahl, die versuchsweise platziert wird.
     * @return true, wenn der Zug den Str8ts-Regeln entspricht.
     */
    // ERSETZE DEINE AKTUELLE isValidMove-FUNKTION VOLLSTÄNDIG HIERMIT

    private fun isValidMove(grid: Array<IntArray>, r: Int, c: Int, num: Int): Boolean {
        // === 1. SUDOKU-REGEL ===
        // Prüft, ob 'num' bereits in einer anderen Zelle der gleichen Zeile oder Spalte existiert.
        for (i in 0 until 9) {
            // Prüfe Zeile r (ignoriere die Spalte c, die wir gerade füllen)
            if (i != c) {
                val valueInRow = if (layout[r][i] == null) grid[r][i] else layout[r][i]
                if (valueInRow == num) return false
            }
            // Prüfe Spalte c (ignoriere die Zeile r, die wir gerade füllen)
            if (i != r) {
                val valueInCol = if (layout[i][c] == null) grid[i][c] else layout[i][c]
                if (valueInCol == num) return false
            }
        }

        // === 2. STR8TS-REGEL ===
        val relevantCompartments = cellToCompartmentMap[r to c] ?: return true

        for (compartment in relevantCompartments) {
            val currentNumbers = mutableListOf<Int>()
            // Sammle alle Zahlen im Kompartment, INKLUSIVE der neuen Test-Zahl.
            for ((cr, cc) in compartment) {
                val valueInCell = when {
                    cr == r && cc == c -> num
                    layout[cr][cc] == null -> grid[cr][cc]
                    else -> layout[cr][cc]
                }
                if (valueInCell != null && valueInCell != 0) {
                    currentNumbers.add(valueInCell)
                }
            }

            if (currentNumbers.size <= 1) continue

            // REGEL A: Keine Duplikate im Kompartment
            if (currentNumbers.size != currentNumbers.toSet().size) return false

            val minVal = currentNumbers.minOrNull()!!
            val maxVal = currentNumbers.maxOrNull()!!
            val compartmentSize = compartment.size

            // REGEL B: Die Spannweite der Zahlen darf NIE größer sein als die Kompartmentgröße.
            // DIES IST DER ENTSCHEIDENDE FEHLENDE CHECK!
            if (maxVal - minVal + 1 > compartmentSize) {
                return false
            }

            // REGEL C: Wenn das Kompartment VOLL ist, MUSS es eine lückenlose Straße sein.
            if (currentNumbers.size == compartmentSize) {
                if (maxVal - minVal + 1 != compartmentSize) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Findet das horizontale Kompartment, das die Zelle (r, c) enthält.
     * @return Liste der Koordinaten aller Zellen im Kompartment (oder leere Liste, wenn es keine weiße Zelle ist)
     */
    private fun findHorizontalCompartmentAt(r: Int, c: Int): List<Pair<Int, Int>> {
        // Nur weiße Zellen können Teil eines Kompartments sein
        if (layout[r][c] != null) return emptyList()

        // Finde den Startpunkt des Kompartments (gehe nach links bis zum schwarzen Feld oder Rand)
        var start = c
        while (start > 0 && layout[r][start - 1] == null) {
            start--
        }

        // Finde das Ende des Kompartments (gehe nach rechts bis zum schwarzen Feld oder Rand)
        var end = c
        while (end < 8 && layout[r][end + 1] == null) {
            end++
        }

        // Keine Mindestgröße mehr: Auch einzelne Zellen sind ein Kompartment
        return (start..end).map { col -> r to col }
    }


    /**
     * Findet das vertikale Kompartment, das die Zelle (r, c) enthält.
     * @return Liste der Koordinaten aller Zellen im Kompartment (oder leere Liste, wenn es keine weiße Zelle ist)
     */
    private fun findVerticalCompartmentAt(r: Int, c: Int): List<Pair<Int, Int>> {
        // Nur weiße Zellen können Teil eines Kompartments sein
        if (layout[r][c] != null) return emptyList()

        // Finde den Startpunkt des Kompartments (gehe nach oben bis zum schwarzen Feld oder Rand)
        var start = r
        while (start > 0 && layout[start - 1][c] == null) {
            start--
        }

        // Finde das Ende des Kompartments (gehe nach unten bis zum schwarzen Feld oder Rand)
        var end = r
        while (end < 8 && layout[end + 1][c] == null) {
            end++
        }

        // Keine Mindestgröße mehr: Auch einzelne Zellen sind ein Kompartment
        return (start..end).map { row -> row to c }
    }


    /**
     * Prüft, ob ein Kompartment gültig bleibt, wenn 'num' an Position (r, c) gesetzt wird.
     * @param grid Das aktuelle Grid
     * @param compartment Liste aller Zellen im Kompartment
     * @param r Zeile der zu setzenden Zelle
     * @param c Spalte der zu setzenden Zelle
     * @param num Die zu setzende Zahl
     * @return true, wenn das Kompartment gültig bleibt
     */
    private fun isCompartmentValidWithMove(
        grid: Array<IntArray>,
        compartment: List<Pair<Int, Int>>,
        r: Int,
        c: Int,
        num: Int
    ): Boolean {
        // Sammle alle Zahlen im Kompartment nach dem hypothetischen Zug
        val currentNumbers = mutableListOf<Int>()

        for ((cr, cc) in compartment) {
            val valueInCell = when {
                cr == r && cc == c -> num // Dies ist die Zahl, die wir gerade testen
                layout[cr][cc] == null -> grid[cr][cc] // Eine andere weiße Zelle
                else -> layout[cr][cc] // Eine schwarze Zelle mit Hinweis (sollte nicht vorkommen in Kompartments)
            }

            // Nur Zahlen > 0 berücksichtigen (0 = leere Zelle)
            if (valueInCell != null && valueInCell > 0) {
                currentNumbers.add(valueInCell)
            }
        }

        // Wenn weniger als 2 Zahlen im Kompartment sind, kann es keinen Konflikt geben
        if (currentNumbers.size <= 1) return true

        val compartmentSize = compartment.size

        // REGEL A: Keine Duplikate im Kompartment
        if (currentNumbers.size != currentNumbers.toSet().size) return false

        // Werte für die nächsten Prüfungen berechnen
        val minVal = currentNumbers.minOrNull()!!
        val maxVal = currentNumbers.maxOrNull()!!

        // REGEL B: Die Spannweite der Zahlen darf nie größer sein als die Größe des Kompartments
        if (maxVal - minVal + 1 > compartmentSize) {
            return false
        }

        // REGEL C: Wenn das Kompartment VOLL ist, MUSS es eine lückenlose Straße sein.
        if (currentNumbers.size == compartmentSize) {
            val sortedNumbers = currentNumbers.sorted()
            // Prüfe, ob alle Zahlen lückenlos aufeinanderfolgend sind
            for (i in 1 until sortedNumbers.size) {
                if (sortedNumbers[i] != sortedNumbers[i-1] + 1) {
                    return false // Lücke gefunden!
                }
            }
        }

        return true
    }


    private fun printLayoutRaw() {
        println("Raw Layout (null=white, 0=black_no_clue, 1-9=black_clue):")
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                when (layout[r][c]) {
                    null -> print("W ")
                    0 -> print("B0 ")
                    else -> print("B${layout[r][c]} ")
                }
            }
            println()
        }
    }

    fun printGrid() {
        println("Puzzle:")
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                when (layout[r][c]) {
                    null -> {
                        if (grid[r][c] == 0) {
                            print("· ")
                        } else {
                            print("${grid[r][c]} ")
                        }
                    }
                    0 -> print("■ ")
                    else -> print("${layout[r][c]} ")
                }
            }
            println()
        }
    }

    fun printSolution() {
        println("Lösung:")
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                when (layout[r][c]) {
                    null -> print("${solution[r][c]} ")
                    0 -> print("■ ")
                    else -> print("${layout[r][c]} ")
                }
            }
            println()
        }
    }

    fun validateSolution(): Boolean {
        if (debugMode) println("*** validateSolution() wird aufgerufen ***")

        // WICHTIG: Wir validieren die SOLUTION, nicht das GRID!
        // Das GRID ist das Puzzle (mit leeren Zellen), die SOLUTION ist die vollständige Lösung.

        // 1. Prüfe, ob alle weißen Zellen in der LÖSUNG gefüllt sind
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (layout[r][c] == null && solution[r][c] == 0) {
                    if (debugMode) {
                        println("Ungefüllte Zelle in LÖSUNG bei ($r, $c)")
                    }
                    return false
                }
            }
        }

        // 2. Prüfe Sudoku-Regeln (Zeilen) in der LÖSUNG
        for (r in 0 until 9) {
            val rowNumbers = mutableSetOf<Int>()
            for (c in 0 until 9) {
                val value = when (layout[r][c]) {
                    null -> solution[r][c]  // ← SOLUTION statt GRID
                    0 -> continue
                    else -> layout[r][c]!!
                }
                if (value != 0) {
                    if (value in rowNumbers) {
                        if (debugMode) {
                            println("Duplikat in Zeile $r: $value")
                        }
                        return false
                    }
                    rowNumbers.add(value)
                }
            }
        }

        // 3. Prüfe Sudoku-Regeln (Spalten) in der LÖSUNG
        for (c in 0 until 9) {
            val colNumbers = mutableSetOf<Int>()
            for (r in 0 until 9) {
                val value = when (layout[r][c]) {
                    null -> solution[r][c]  // ← SOLUTION statt GRID
                    0 -> continue
                    else -> layout[r][c]!!
                }
                if (value != 0) {
                    if (value in colNumbers) {
                        if (debugMode) {
                            println("Duplikat in Spalte $c: $value")
                        }
                        return false
                    }
                    colNumbers.add(value)
                }
            }
        }

        // 4. Prüfe Str8ts-Regeln - verwende DYNAMISCHE Kompartment-Erkennung auf der LÖSUNG
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (layout[r][c] == null) { // Nur weiße Zellen prüfen
                    // Prüfe horizontales Kompartment
                    val hComp = findHorizontalCompartmentAt(r, c)
                    if (hComp.isNotEmpty() && !isCompartmentFullyValidInSolution(hComp)) {
                        if (debugMode) {
                            val numbers = hComp.map { (hr, hc) -> solution[hr][hc] }
                            println("Ungültige horizontale Straight bei ($r, $c): $numbers")
                        }
                        return false
                    }

                    // Prüfe vertikales Kompartment
                    val vComp = findVerticalCompartmentAt(r, c)
                    if (vComp.isNotEmpty() && !isCompartmentFullyValidInSolution(vComp)) {
                        if (debugMode) {
                            val numbers = vComp.map { (vr, vc) -> solution[vr][vc] }
                            println("Ungültige vertikale Straight bei ($r, $c): $numbers")
                        }
                        return false
                    }
                }
            }
        }

        return true
    }

    /**
     * Prüft, ob ein vollständig gefülltes Kompartment in der LÖSUNG eine gültige Straight bildet.
     */
    private fun isCompartmentFullyValidInSolution(compartment: List<Pair<Int, Int>>): Boolean {
        val numbers = mutableListOf<Int>()

        for ((r, c) in compartment) {
            if (layout[r][c] != null) {
                // Schwarze Zelle in Kompartment = Fehler
                return false
            }
            val value = solution[r][c]  // ← SOLUTION statt GRID
            if (value <= 0) {
                // Leere Zelle in Kompartment = Fehler (sollte bei vollständiger Lösung nicht vorkommen)
                return false
            }
            numbers.add(value)
        }

        // Prüfe, ob es eine gültige Straight ist
        val sortedNumbers = numbers.sorted()
        for (i in 1 until sortedNumbers.size) {
            if (sortedNumbers[i] != sortedNumbers[i-1] + 1) {
                return false // Lücke gefunden
            }
        }

        return true
    }

    fun validatePuzzle(): Boolean {
        for (r in 0 until 9) {
            val rowNumbers = mutableSetOf<Int>()
            for (c in 0 until 9) {
                val value = when (layout[r][c]) {
                    null -> grid[r][c]
                    0 -> continue
                    else -> layout[r][c]!!
                }
                if (value != 0) {
                    if (value in rowNumbers) {
                        return false
                    }
                    rowNumbers.add(value)
                }
            }
        }

        for (c in 0 until 9) {
            val colNumbers = mutableSetOf<Int>()
            for (r in 0 until 9) {
                val value = when (layout[r][c]) {
                    null -> grid[r][c]
                    0 -> continue
                    else -> layout[r][c]!!
                }
                if (value != 0) {
                    if (value in colNumbers) {
                        return false
                    }
                    colNumbers.add(value)
                }
            }
        }
        return true
    }
}
