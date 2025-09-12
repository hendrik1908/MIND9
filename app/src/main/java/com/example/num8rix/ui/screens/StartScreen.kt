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
import com.example.num8rix.ui.screens.BottomNavigation // <-- IMPORTANT: Import the new file

@Composable
fun StartScreen(
    viewModel: MyDatabaseViewModel,
    onInfoClick: () -> Unit = {},
    onGameStart: (DifficultyLevel) -> Unit = {},
    onGeneratorClick: () -> Unit = {}
){
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
        Spacer(modifier = Modifier.height(64.dp))

        Image(
            painter = painterResource(id = R.drawable.mind9_full_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

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

        Spacer(modifier = Modifier.weight(1.5f))

        DifficultyButton(
            text = "Rätsel generieren",
            isPrimary = false,
            onClick = onGeneratorClick,
            widthFraction = 0.7f,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // This will now use the function from the separate file
        BottomNavigation(
            onHomeClick = { /* Already on Home */ },
            onInfoClick = onInfoClick,
            currentScreen = "home"
        )
    }
}

// HIER SIND DIE HILFSFUNKTIONEN, DIE IN DIESER DATEI BLEIBEN
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