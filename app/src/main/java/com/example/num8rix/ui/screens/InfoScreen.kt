package com.example.num8rix.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
)
{
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        val context = LocalContext.current
        // Header mit Zurück-Button und Titel
        TopAppBar(
            title = {
                Text(
                    text = "Regeln",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Zurück"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF5F5F5),
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black
            )
        )

        // Scrollbarer Inhalt
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Neuer Link zu str8ts.de ---
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Quelle: str8ts.de",
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.str8ts.de"))
                        context.startActivity(intent)
                    }
            )

            // Ziel Section
            InfoSection(
                title = "Ziel",
                content = """Das Ziel von MIND9 ist es, alle Zahlen von 1 bis 9 in das 9x9-Gitter zu vervollständigen, wobei jede Zahl nur einmal in jeder Zeile und Spalte vorkommen darf. Die Zahlen müssen in aufeinander folgenden Sequenzen angeordnet sein, die als 'MIND9' bezeichnet werden."""
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Spielregeln Section
            InfoSection(
                title = "Spielregeln",
                content = buildSpielregelnText()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tipps Section
            InfoSection(
                title = "Tipps",
                content = buildTippsText()
            )

            Spacer(modifier = Modifier.height(80.dp)) // Platz für Bottom Navigation
        }

        // Bottom Navigation (nur Home und Info)
        BottomNavigation(
            onHomeClick = onHomeClick,
            onInfoClick = { /* Bereits auf Info */ },
            currentScreen = "info"
        )
    }
}

@Composable
fun InfoSection(
    title: String,
    content: String
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = content,
            fontSize = 14.sp,
            color = Color(0xFF333333),
            lineHeight = 20.sp,
            textAlign = TextAlign.Left
        )
    }
}

@Composable
fun BottomNavigation(
    onHomeClick: () -> Unit,
    onInfoClick: () -> Unit,
    currentScreen: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onHomeClick) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Startseite",
                tint = if (currentScreen == "home") Color.Black else Color.Gray
            )
        }

        IconButton(onClick = onInfoClick) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = if (currentScreen == "info") Color.Black else Color.Gray
            )
        }
    }
}

fun buildSpielregelnText(): String {
    return """1. Das Gitter muss aus schwarzen und weißen Zellen. Schwarze Zellen sind gesperrt und können keine Zahlen enthalten.

2. Weiße Zellen müssen mit Zahlen von 1 bis 9 gefüllt werden.

3. Jede Zeile und Spalte muss alle Zahlen von 1 bis 9 genau einmal enthalten.

4. Zahlen in weißen Zellen müssen in aufeinanderfolgenden Sequenzen angeordnet sein, die als 'MIND9' bezeichnet werden.

5. MIND9-Sequenzen sind horizontal oder vertikal verlaufen und müssen mindestens zwei aufeinanderfolgende Zahlen enthalten.

6. MIND9 dürfen nicht durch schwarze Zellen unterbrochen werden.

7. Zahlen in MIND9 müssen in aufsteigender oder absteigender Reihenfolge angeordnet sein.

8. Es gibt immer nur eine eindeutige Lösung für jedes MIND9-Puzzle."""
}

fun buildTippsText(): String {
    return """1. Beginnen Sie mit Zeilen und Spalten, die bereits viele Zahlen enthalten.

2. Achten Sie auf schwarze Zellen, da diese die möglichen Positionen für MIND9 einschränken.

3. Suchen Sie nach MIND9, die nur wenige mögliche Positionen haben.

4. Verwenden Sie die Logik, um unmögliche Zahlen in Zellen zu eliminieren.

5. Wenn Sie nicht weiterkommen, versuchen Sie, mit einer Zelle zu beginnen und alle Konsequenzen zu prüfen."""
}

@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    InfoScreen()
}