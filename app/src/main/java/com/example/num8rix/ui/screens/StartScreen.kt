package com.example.num8rix.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.R

@Composable
fun StartScreen(
    viewModel: MyDatabaseViewModel,
    onInfoClick: () -> Unit = {},
    onGameStart: (DifficultyLevel) -> Unit = {},
    onGeneratorClick: () -> Unit = {}
) {
    var easyCounts by remember { mutableStateOf(Pair(0, 0)) }
    var mediumCounts by remember { mutableStateOf(Pair(0, 0)) }
    var hardCounts by remember { mutableStateOf(Pair(0, 0)) }

    LaunchedEffect(Unit) {
        viewModel.getSolvedAndTotalCounts { easy, medium, hard ->
            easyCounts = easy
            mediumCounts = medium
            hardCounts = hard
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo an oberster Stelle
        Spacer(modifier = Modifier.height(64.dp))

        Image(
            painter = painterResource(id = R.drawable.mind9_full_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
        )

        // Flexible Spacer für die vertikale Zentrierung des Hauptinhalts
        Spacer(modifier = Modifier.weight(1f))

        // Hauptinhalt: Texte und Schwierigkeits-Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Starte ein neues Spiel",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Wähle einen Schwierigkeitsgrad aus, um zu beginnen",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Schwierigkeitsgrad Buttons
            DifficultyButton(
                text = "Einfach",
                progressText = "${easyCounts.first}/${easyCounts.second}",
                isPrimary = false,
                onClick = { onGameStart(DifficultyLevel.EASY) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DifficultyButton(
                text = "Mittel",
                progressText = "${mediumCounts.first}/${mediumCounts.second}",
                isPrimary = false,
                onClick = { onGameStart(DifficultyLevel.MEDIUM) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DifficultyButton(
                text = "Schwer",
                progressText = "${hardCounts.first}/${hardCounts.second}",
                isPrimary = false,
                onClick = { onGameStart(DifficultyLevel.HARD) }
            )
        }

        // Flexibler Spacer für den Abstand zum Generator-Button
        Spacer(modifier = Modifier.weight(1.5f))

        // Rätsel Generieren Button
        DifficultyButton(
            text = "Rätsel Generieren",
            isPrimary = false,
            onClick = onGeneratorClick,
            widthFraction = 0.7f,
        )

        // Spacer, um die Bottom Navigation am unteren Rand zu fixieren
        Spacer(modifier = Modifier.height(32.dp))

        // Bottom Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Bereits auf Home */ }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Startseite",
                    tint = Color.Black
                )
            }
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DifficultyButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    widthFraction: Float = 1f,
    containerColor: Color? = null,
    contentColor: Color? = null,
    progressText: String? = null
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor ?: if (isPrimary) Color.Black else Color(0xFFEEEEEE),
            contentColor = contentColor ?: if (isPrimary) Color.White else Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp
            )
            if (progressText != null) {
                Text(
                    text = progressText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}