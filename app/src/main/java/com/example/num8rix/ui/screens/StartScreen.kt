package com.example.num8rix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StartScreen(
    onInfoClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onGameStart: (Difficulty) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(24.dp)) // Platzhalter für Symmetrie

            Text(
                text = "Num8rix",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Einstellungen",
                    tint = Color.Black
                )
            }
        }

        // Hauptinhalt
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Starte ein neues Spiel",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Wähle ein Schwierigkeitsgrad aus, um zu beginnen",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Schwierigkeitsgrad Buttons
            DifficultyButton(
                text = "Einfach",
                isPrimary = true,
                onClick = { onGameStart(Difficulty.EASY) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DifficultyButton(
                text = "Mittel",
                isPrimary = false,
                onClick = { onGameStart(Difficulty.MEDIUM) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DifficultyButton(
                text = "Schwer",
                isPrimary = false,
                onClick = { onGameStart(Difficulty.HARD) }
            )
        }

        // Bottom Navigation (nur Home und Info)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Color.Black else Color(0xFFEEEEEE),
            contentColor = if (isPrimary) Color.White else Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp
        )
    }
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen()
}