package com.example.num8rix.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.generator.PuzzleGenerationService
import kotlin.math.max

@Composable
fun GeneratorScreen(
    viewModel: MyDatabaseViewModel,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    var easyCount by remember { mutableStateOf(5) }
    var mediumCount by remember { mutableStateOf(5) }
    var hardCount by remember { mutableStateOf(5) }
    
    var isGenerating by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0) }
    var totalPuzzles by remember { mutableStateOf(0) }
    var currentDifficulty by remember { mutableStateOf("") }
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    var totalCounts by remember { mutableStateOf(Triple(0, 0, 0)) }
    
    // Lade aktuelle Rätsel-Anzahlen
    LaunchedEffect(Unit) {
        viewModel.getTotalPuzzleCounts { easy, medium, hard ->
            totalCounts = Triple(easy, medium, hard)
        }
    }
    
    // Broadcast Receiver für Service Updates
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    PuzzleGenerationService.ACTION_PROGRESS_UPDATE -> {
                        currentProgress = intent.getIntExtra(PuzzleGenerationService.EXTRA_CURRENT_PROGRESS, 0)
                        totalPuzzles = intent.getIntExtra(PuzzleGenerationService.EXTRA_TOTAL_PUZZLES, 0)
                        currentDifficulty = intent.getStringExtra(PuzzleGenerationService.EXTRA_CURRENT_DIFFICULTY) ?: ""
                    }
                    PuzzleGenerationService.ACTION_GENERATION_COMPLETE -> {
                        isGenerating = false
                        showCompleteDialog = true
                        // Aktualisiere Rätsel-Anzahlen
                        viewModel.getTotalPuzzleCounts { easy, medium, hard ->
                            totalCounts = Triple(easy, medium, hard)
                        }
                    }
                    PuzzleGenerationService.ACTION_GENERATION_ERROR -> {
                        isGenerating = false
                        errorMessage = intent.getStringExtra(PuzzleGenerationService.EXTRA_ERROR_MESSAGE) ?: "Unbekannter Fehler"
                        showErrorDialog = true
                    }
                }
            }
        }
        
        val intentFilter = IntentFilter().apply {
            addAction(PuzzleGenerationService.ACTION_PROGRESS_UPDATE)
            addAction(PuzzleGenerationService.ACTION_GENERATION_COMPLETE)
            addAction(PuzzleGenerationService.ACTION_GENERATION_ERROR)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Header
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
                text = "Rätsel Generieren",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Status-Anzeige der vorhandenen Rätsel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Vorhandene Rätsel in der Datenbank",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PuzzleCountDisplay("Einfach", totalCounts.first)
                    PuzzleCountDisplay("Mittel", totalCounts.second)
                    PuzzleCountDisplay("Schwer", totalCounts.third)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isGenerating) {
            // Fortschrittsanzeige
            GenerationProgressCard(currentProgress, totalPuzzles, currentDifficulty)
        } else {
            // Eingabeformular
            Text(
                text = "Neue Rätsel generieren",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            DifficultyInputCard(
                title = "Einfach",
                count = easyCount,
                onCountChange = { easyCount = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DifficultyInputCard(
                title = "Mittel",
                count = mediumCount,
                onCountChange = { mediumCount = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DifficultyInputCard(
                title = "Schwer",
                count = hardCount,
                onCountChange = { hardCount = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
    // Generieren Button
            Button(
                onClick = {
                    val total = easyCount + mediumCount + hardCount
                    if (total > 0) {
                        // Prüfe Notification Permission (Android 13+)
                        try {
                            isGenerating = true
                            currentProgress = 0
                            totalPuzzles = total
                            PuzzleGenerationService.startGeneration(
                                context,
                                easyCount,
                                mediumCount,
                                hardCount
                            )
                        } catch (e: Exception) {
                            isGenerating = false
                            errorMessage = "Fehler beim Starten: ${e.message}"
                            showErrorDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isGenerating && (easyCount + mediumCount + hardCount) > 0
            ) {
                Text(
                    text = "Generierung starten",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Hinweise
        Spacer(modifier = Modifier.weight(1f))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "💡 Hinweise:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF856404)
                )
                Text(
                    text = "• Die Generierung läuft im Hintergrund\n• Sie können die App währenddessen nutzen\n• Duplikate werden automatisch vermieden\n• Schwere Rätsel nutzen EXPERT-Algorithmus",
                    fontSize = 12.sp,
                    color = Color(0xFF856404),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
    
    // Success Dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            confirmButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("✅ Erfolgreich!") },
            text = { Text("Alle Rätsel wurden erfolgreich generiert und in die Datenbank eingefügt.") }
        )
    }
    
    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("❌ Fehler") },
            text = { Text("Fehler bei der Generierung: $errorMessage") }
        )
    }
}

@Composable
fun PuzzleCountDisplay(title: String, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun DifficultyInputCard(
    title: String,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { onCountChange(max(0, count - 1)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(
                        text = "-",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                Text(
                    text = count.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.widthIn(min = 32.dp),
                    textAlign = TextAlign.Center
                )
                
                IconButton(
                    onClick = { onCountChange(count + 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun GenerationProgressCard(
    currentProgress: Int,
    totalPuzzles: Int,
    currentDifficulty: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rätsel werden generiert...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LinearProgressIndicator(
                progress = if (totalPuzzles > 0) currentProgress.toFloat() / totalPuzzles else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color.Black,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "$currentProgress von $totalPuzzles erstellt",
                fontSize = 16.sp,
                color = Color.Black
            )
            
            if (currentDifficulty.isNotEmpty()) {
                Text(
                    text = "Aktuell: $currentDifficulty",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Die Generierung läuft im Hintergrund. Sie können die App weiter nutzen.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
