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
import com.example.num8rix.ui.screens.BottomNavigation // <-- WICHTIG: Importiere die neue Datei

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        val context = LocalContext.current
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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
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


            Spacer(modifier = Modifier.height(24.dp))

            InfoSection(
                title = "Spielregeln",
                content = buildSpielregelnText()
            )

            Spacer(modifier = Modifier.height(24.dp))

            InfoSection(
                title = "Tipps",
                content = buildTippsText()
            )

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Rufe die geteilte Komponente auf
        BottomNavigation(
            onHomeClick = onHomeClick,
            onInfoClick = { /* Bereits auf Info */ },
            currentScreen = "info"
        )
    }
}

// HIER SIND DIE HILFSFUNKTIONEN, DIE IN DIESER DATEI BLEIBEN
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

fun buildSpielregelnText(): String {
    return """In MIND9 trägst du die Zahlen von 1 bis 9 in die weißen Felder eines 9×9-Gitters ein. Dabei gilt: Jede Zahl darf in jeder Zeile und jeder Spalte nur einmal vorkommen.
Zusätzlich enthält das Gitter schwarze Felder, die eine besondere Rolle spielen. Sie unterteilen das Spielfeld in sogenannte „Minds“ – das sind zusammenhängende horizontale oder vertikale Gruppen weißer Felder. Schwarze Felder dürfen nicht befüllt werden, können aber Zahlen enthalten. Diese blockieren die entsprechende Zahl für die gesamte Zeile und Spalte, in der sie stehen – sie darf dort also nicht mehr verwendet werden. Zahlen in schwarzen Feldern sind nicht Teil der „Minds“.
Ein „Mind“ besteht aus einer lückenlosen Zahlenfolge, zum Beispiel 3-2-4 oder 6-8-7. Die Reihenfolge innerhalb eines „Mind“ ist beliebig, solange die Zahlenfolge keine Lücke enthält (z.B. 1-4-5 ist nicht zulässig).
Ziel ist es, alle weißen Felder korrekt zu befüllen, ohne gegen die Regeln zu verstoßen."""
}

fun buildTippsText(): String {
    return """Für Einsteiger empfiehlt es sich mit dem Schwierigkeitsgrad „leicht“ zu starten und sich dann nach und nach an die schwierigeren Level zu wagen.
Beginne mit dem Eintragen von Zahlen bei den kleinsten „Minds“, die bestenfalls nur aus zwei bis drei Kästchen bestehen. Hier ist die Lösung meist eindeutig.
Im Vergleich zum beliebten Ratespiel Sudoku kommen bei MIND9 nicht alle Zahlen in jeder Zeile und Spalte vor, da die schwarzen Kästchen auch leer sein können.
Nutze den Notizmodus, um dir potenzielle Zahlenkandidaten für bestimmte Kästchen zu merken, somit siehst du schnell, wenn nur eine Zahl in einem Kästchen möglich ist.
Falls du nicht weiterkommst, kannst du die Hinweisfunktion nutzen. Diese deckt entweder die Lösung für ein Feld auf oder zeigt dir eine Zahl in rot an falls du einen Fehler gemacht hast. Letzteres kannst du auch über den Prüfen-Button selbst initiieren."""
}