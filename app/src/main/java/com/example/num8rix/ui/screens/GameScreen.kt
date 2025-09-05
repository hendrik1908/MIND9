package com.example.num8rix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.Game
import com.example.num8rix.Grid
import kotlinx.coroutines.launch


@Composable
fun GameScreen(
    difficulty: DifficultyLevel,
    viewModel: MyDatabaseViewModel,
    onBackClick: () -> Unit = {}
)
{
    var game by remember { mutableStateOf<Game?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null)}
    var isNoteMode by remember { mutableStateOf(false) }
    var grid by remember { mutableStateOf<Grid?>(null) }
    var incorrectCells by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var correctCells by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var showExitDialog by remember { mutableStateOf(false) }

    // CoroutineScope früh definieren - HIER verschoben!
    val coroutineScope = rememberCoroutineScope()


    // Ruft die Datenbank nur einmal beim ersten Composable-Aufbau auf, Game wird asynchron aufgebaut
    LaunchedEffect(difficulty) {
        viewModel.getLatestGameStateAsGrid(difficulty) { cachedGrid ->
            if (cachedGrid != null) {
                grid = cachedGrid
                game = Game.fromGrid(cachedGrid)
                isLoading = false
            } else {
                viewModel.getRandomUnsolvedByDifficulty(difficulty) { entry ->
                    if (entry != null) {
                        val newGame = Game(entry.unsolvedString, entry.layoutString).apply { generateGame() }
                        grid = newGame.grid
                        game = newGame
                        isLoading = false

                        // EINMALIG: Erstes Speichern inkl. puzzleId
                        viewModel.saveGameState(
                            currentGridString = newGame.grid.toVisualString(),
                            notesGridString = newGame.grid.notesToString(),
                            difficulty = difficulty,
                            originalGridString = newGame.grid.toVisualString(),
                            originalLayoutString = newGame.grid.toLayoutString(),
                            puzzleId = entry.id   // <-- WICHTIG: puzzleId speichern
                        )
                    }
                }
            }
        }
    }

    // Ladeanzeige, wenn Spiel noch nicht geladen ist
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (game == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Kein passendes Rätsel gefunden.")
        }
        return
    }

    val currentGrid = grid
    if (currentGrid == null) {
        // Ladeanzeige oder return
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Bar mit Zurück-Button, Titel und X-Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)

        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Zurück"
                )
            }
            Text(
                text = "MIND9",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            // X-Button oben rechts (Kreis mit X)
            IconButton(
                onClick = { showExitDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, Color.Black, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Schließen",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Zeit + Züge Anzeige


        // 9x9 Gitter – jetzt mit echtem Grid!
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .border(2.dp, Color.Black)
                .padding(2.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (row in 0 until 9) {
                    Row(modifier = Modifier.weight(1f)) {
                        for (col in 0 until 9) {
                            val field = currentGrid.getField(row, col)
                            val isSelected = selectedCell == row to col

                            SudokuCell(
                                value = if (field.value == 0) "" else field.value.toString(),
                                isBlack = field.isBlack(),
                                isSelected = isSelected,
                                notes = field.notes,
                                isInitial = field.isInitial,
                                isIncorrect = incorrectCells.contains(row to col),
                                isCorrect = correctCells.contains(row to col),
                                blackCellHint = field.blackCellValue, // NEU: Hinweiszahl übergeben
                                //Nur weiße & nicht-initiale Felder dürfen ausgewählt werden
                                onClick = {
                                    if (!field.isBlack() && !field.isInitial) {
                                        selectedCell = row to col
                                        incorrectCells = emptySet()
                                        correctCells = emptySet()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Zahlenfeld 1-9
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 1..9) {
                Button(
                    onClick = {
                        // Nur setzen, wenn eine Zelle ausgewählt ist
                        selectedCell?.let { (row, col) ->
                            val field = currentGrid.getField(row, col)

                            //Prüfen: nur weiße, nicht-initiale Felder dürfen geändert werden
                            if (field.isWhite() && !field.isInitial) {
                                if (isNoteMode) {
                                    // NEU: Note einfügen oder entfernen
                                    if (field.notes.contains(i)) {
                                        field.notes.remove(i)
                                    } else {
                                        field.notes.add(i)
                                    }
                                    field.value = 0 // Nur Notizen, kein Wert
                                } else {
                                    // Normalmodus → Wert setzen & Notizen löschen
                                    field.value = i
                                    field.notes.clear()
                                }
                                grid = currentGrid.copy()
                                //falsche Markierungen zurücksetzen
                                incorrectCells = emptySet()
                                // Aktuellen Spielstand speichern inkl. Notizen und pro Schwirigkeitslevel
                                coroutineScope.launch {
                                    val latest = viewModel.getLatestCacheEntry(difficulty)
                                    latest?.let { cache ->
                                        viewModel.saveGameState(
                                            currentGridString = currentGrid.toVisualString(),
                                            notesGridString = currentGrid.notesToString(),
                                            difficulty = difficulty,
                                            puzzleId = cache.puzzleId
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = if (isNoteMode) {  //dynamische Farbe grau bei aktiven Notizmodus
                        ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                        )
                    } else {
                        ButtonDefaults.buttonColors() // Standard-Theme-Farben UI5
                    },
                ) {
                    Text(i.toString())
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))


        // Buttons unten
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        )  {
            ActionButton("Löschen") {
                selectedCell?.let { (row, col) ->
                    val field = currentGrid.getField(row, col)
                    if (!field.isBlack() && !field.isInitial) {
                        field.value = 0 // Zahl löschen
                        field.notes.clear()
                        grid = currentGrid.copy()
                        // Spielstand in GameCache speichern nach Löschen
                        coroutineScope.launch {
                            val latest = viewModel.getLatestCacheEntry(difficulty)
                            latest?.let { cache ->
                                viewModel.saveGameState(
                                    currentGridString = currentGrid.toVisualString(),
                                    notesGridString = currentGrid.notesToString(),
                                    difficulty = difficulty,
                                    puzzleId = cache.puzzleId
                                )
                            }
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = { isNoteMode = !isNoteMode },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isNoteMode) Color(0xFF1976D2) else Color.Transparent,
                    contentColor = if (isNoteMode) Color.White else Color.Unspecified
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isNoteMode) Color(0xFF1976D2) else Color(0xFF79747E)
                ),
                shape = CircleShape,
                modifier = Modifier.height(36.dp),
                elevation = if (isNoteMode) ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp) else null
            ) {
                Text(
                    text = "Notizen",
                    fontSize = 12.sp,
                    fontWeight = if (isNoteMode) FontWeight.Bold else FontWeight.Normal
                )
            }
            ActionButton("Hinweis") { /* unverändert */ }
            ActionButton("Prüfen") {
                coroutineScope.launch {
                    val latest = viewModel.getLatestCacheEntry(difficulty)
                    latest?.let { cache ->
                        viewModel.checkCurrentGridWithHighlights(
                            difficulty,
                            currentGrid,
                            cache.puzzleId
                        ) { correct, incorrect ->
                            correctCells = correct
                            incorrectCells = incorrect
                        }
                    }
                }
            }
            ActionButton("Lösen") { /* unverändert */ }
            // Zurück-Button für Undo
            ActionButton("Zurück") {
                viewModel.undoLastMove(difficulty) { restoredGrid, restoredNotes ->
                    grid?.let {
                        it.updateGridFromVisualString(restoredGrid)
                        it.generateNotesFromString(restoredNotes)
                        grid = it.copy() // löst Recompose aus
                    }
                }
            }
        }
    }

    // Bestätigungsdialog für das Verlassen des Spiels
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Spiel verlassen",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Wollen Sie das Spiel endgültig verwerfen?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false

                        // Erst das Spiel in der DB auf solved setzen und Cache leeren
                        coroutineScope.launch {
                            val latest = viewModel.getLatestCacheEntry(difficulty)
                            latest?.let { cache ->
                                // 1. Spiel als gelöst markieren
                                viewModel.markPuzzleAsSolved(difficulty, cache.puzzleId)

                                // 2. Cache für dieses Schwierigkeitslevel leeren
                                viewModel.clearCacheForDifficulty(difficulty)
                            }

                            // 3. Dann erst zum Startscreen zurückkehren
                            onBackClick()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Ja")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    Text("Nein")
                }
            }
        )
    }
}

@Composable
fun SudokuCell(
    value: String,
    isBlack: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    notes: Set<Int> = emptySet(),
    isInitial: Boolean = false,
    isIncorrect: Boolean = false,
    isCorrect: Boolean = false,
    blackCellHint: Int? = null // NEU: Hinweiszahl für schwarze Felder
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .border(0.5.dp, Color.Black)
            .background(
                when {
                    isBlack -> Color.Black
                    isSelected -> Color(0xFFB3E5FC)
                    else -> Color.White
                }
            )
            .clickable { if (!isBlack) onClick() }
    ) {
        when {
            isBlack && blackCellHint != null && blackCellHint > 0 -> {
                // Schwarze Zelle mit Hinweiszahl
                Text(
                    text = blackCellHint.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            value.isNotEmpty() -> { // normale Zahl
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    // Schwarz für initiale, Blau für Spieler-Zahlen, Rot für falsche bei Prüfung
                    color = when {
                        isInitial -> Color.Black
                        isIncorrect -> Color.Red
                        isCorrect -> Color.Green
                        else -> Color.Blue
                    }

                )
            }
            notes.isNotEmpty() -> { // Notizen im 3x3 Grid
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp), // Minimaler Abstand
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (r in 0 until 3) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (c in 0 until 3) {
                                val num = r * 3 + c + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (notes.contains(num)) num.toString() else "",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Light,
                                        color = Color.Blue,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 10.sp, // Verhindert Abschneiden
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 12.sp)
    }
}