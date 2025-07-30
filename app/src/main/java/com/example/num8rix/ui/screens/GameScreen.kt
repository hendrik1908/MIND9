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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.Game
import com.example.num8rix.Grid


@Composable
fun GameScreen(
    difficulty: DifficultyLevel,
    viewModel: MyDatabaseViewModel,
    grid: Grid,
    onBackClick: () -> Unit = {}
)
{
    var game by remember { mutableStateOf<Game?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null)}

    // Ruft die Datenbank nur einmal beim ersten Composable-Aufbau auf, Game wird asynchron aufgebaut
    LaunchedEffect(difficulty) {
        viewModel.getRandomUnsolvedByDifficulty(difficulty) { unsolvedString ->
            if (unsolvedString != null) {
                game = Game(unsolvedString).apply { generateGame() }
            }
            isLoading = false
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

    val grid = game!!.grid
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Bar mit Zurück-Button und Titel
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Zurück"
                )
            }
            Text(
                text = "Num8rix",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // Platzhalter rechts
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Zeit + Züge Anzeige
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBox(label = "Zeit", value = "00:00")
            StatBox(label = "Züge", value = "0")
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                            val field = grid.getField(row, col)
                            val isSelected = selectedCell == row to col

                            SudokuCell(
                                value = if (field.value == 0) "" else field.value.toString(),
                                isBlack = field.isBlack(),
                                isSelected = isSelected,
                                onClick = { selectedCell = row to col },
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
                    onClick = { /* Wert setzen */ },
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(i.toString())
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))


        // Buttons unten
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton("Löschen") { /* Löschen */ }
            ActionButton("Notizen") { /* Notizen */ }
            ActionButton("Hinweis") { /* Hinweis */ }
            ActionButton("Prüfen") { /* Prüfen */ }
            ActionButton("Lösen") { /* Lösen */ }
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
    modifier: Modifier = Modifier
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
        if (!isBlack && value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
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
