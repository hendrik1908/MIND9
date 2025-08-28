package com.example.num8rix.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.Game
import com.example.num8rix.Grid


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


    // Ruft die Datenbank nur einmal beim ersten Composable-Aufbau auf, Game wird asynchron aufgebaut
    LaunchedEffect(difficulty) {
        viewModel.getLatestGameStateAsGrid { cachedGrid ->
            if (cachedGrid != null) {
                grid = cachedGrid
                game = Game.fromGrid(cachedGrid)
                isLoading = false
            } else {
                viewModel.getRandomUnsolvedByDifficulty(difficulty) { unsolvedString ->
                    if (unsolvedString != null) {
                        val newGame = Game(unsolvedString).apply { generateGame() }
                        grid = newGame.grid
                        game = newGame

                        // EINMALIG: Erstes Speichern mit Original
                        viewModel.saveGameState(
                            currentGridString = newGame.grid.toVisualString(),
                            notesGridString = newGame.grid.notesToString(),
                            originalGridString = unsolvedString // NUR HIER mitgeben
                        )
                    }
                    isLoading = false
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
        // Top Bar mit Zurück-Button und Titel
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
            Spacer(modifier = Modifier.width(48.dp)) // Platzhalter rechts
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
                                isInitial = field.isInitial, // NEU hinzugefügt
                                //Nur weiße & nicht-initiale Felder dürfen ausgewählt werden
                                onClick = {
                                    if (!field.isBlack() && !field.isInitial) {
                                        selectedCell = row to col
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
                                // Spielstand speichern (jetzt inkl. Notizen)
                                viewModel.saveGameState(
                                    currentGridString = currentGrid.toVisualString(),
                                    notesGridString = currentGrid.notesToString(),
                                )
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
                        field.value = 0 //Zahl löschen
                        field.notes.clear()
                        grid = currentGrid.copy()
                        //Spielstand in GameCache speichern nach löschen
                        viewModel.saveGameState(
                            currentGridString = currentGrid.toVisualString(),
                            notesGridString = currentGrid.notesToString(),
                        )
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
            ActionButton("Prüfen") { /* unverändert */ }
            ActionButton("Lösen") { /* unverändert */ }
            // Zurück-Button für Undo
            // Zurück-Button für Undo
            ActionButton("Zurück") {
                viewModel.undoLastMove { restoredGrid, restoredNotes ->
                    grid?.let {
                        it.updateGridFromVisualString(restoredGrid)
                        it.generateNotesFromString(restoredNotes)
                        grid = it.copy() // <--- Neu: löst Recompose aus
                    }
                }
            }
            }

        }
    }

@Composable
fun StatBox(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
    isInitial: Boolean = false // NEU hinzugefügt
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
            value.isNotEmpty() -> { // normale Zahl
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isInitial) Color.Black else Color.Blue // Schwarz für initiale, Blau für Spieler-Zahlen
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
