package com.example.num8rix.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.generator.PuzzleGenerationService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.num8rix.PuzzleImportExportManager
import com.example.num8rix.ImportResult
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: MyDatabaseViewModel,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
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
    var availableCounts by remember { mutableStateOf(Triple(Pair(0, 0), Pair(0, 0), Pair(0, 0))) }
    
    // Import/Export States
    var showImportExportDialog by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var importResultMessage by remember { mutableStateOf("") }
    var showImportResultDialog by remember { mutableStateOf(false) }
    
    // Initialize ImportExportManager
    val importExportManager = remember { PuzzleImportExportManager(context, viewModel) }
    
    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isImporting = true
            coroutineScope.launch {
                val result = importExportManager.importPuzzlesFromUri(it)
                when (result) {
                    is ImportResult.Success -> {
                        importResultMessage = "Import erfolgreich!\n" +
                                "${result.stats.successful} Rätsel importiert\n" +
                                "${result.stats.duplicates} Duplikate übersprungen\n" +
                                "${result.stats.errors} Fehler"
                        // Aktualisiere sowohl total counts als auch available counts
                        viewModel.getTotalPuzzleCounts { easy, medium, hard ->
                            totalCounts = Triple(easy, medium, hard)
                        }
                        viewModel.getSolvedAndTotalCounts { easy, medium, hard ->
                            availableCounts = Triple(easy, medium, hard)
                        }
                    }
                    is ImportResult.Error -> {
                        importResultMessage = "Import fehlgeschlagen:\n${result.message}"
                    }
                }
                showImportResultDialog = true
                isImporting = false
            }
        }
    }
    
    // Prüfe beim Start ob Service bereits läuft
    LaunchedEffect(Unit) {
        // Lade sowohl total counts als auch solved/total counts
        viewModel.getTotalPuzzleCounts { easy, medium, hard ->
            totalCounts = Triple(easy, medium, hard)
        }
        
        viewModel.getSolvedAndTotalCounts { easy, medium, hard ->
            availableCounts = Triple(easy, medium, hard)
        }
        
        // Prüfe ob PuzzleGenerationService bereits läuft
        if (PuzzleGenerationService.isServiceRunning(context)) {
            isGenerating = true
            // Versuche aktuelle Progress-Daten zu erhalten
            PuzzleGenerationService.requestCurrentStatus(context)
        }
    }
    
    // Broadcast Receiver für Service Updates
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    PuzzleGenerationService.ACTION_PROGRESS_UPDATE -> {
                        val newProgress = intent.getIntExtra(PuzzleGenerationService.EXTRA_CURRENT_PROGRESS, 0)
                        val newTotal = intent.getIntExtra(PuzzleGenerationService.EXTRA_TOTAL_PUZZLES, 0)
                        val newDifficulty = intent.getStringExtra(PuzzleGenerationService.EXTRA_CURRENT_DIFFICULTY) ?: ""
                        
                        // Explizite State Updates für Live-Updates
                        currentProgress = newProgress
                        totalPuzzles = newTotal
                        currentDifficulty = newDifficulty
                        isGenerating = true
                    }
                    PuzzleGenerationService.ACTION_GENERATION_COMPLETE -> {
                        isGenerating = false
                        currentProgress = 0
                        totalPuzzles = 0
                        currentDifficulty = ""
                        showCompleteDialog = true
                        
                        // Aktualisiere Rätsel-Anzahlen
                        viewModel.getTotalPuzzleCounts { easy, medium, hard ->
                            totalCounts = Triple(easy, medium, hard)
                        }
                        viewModel.getSolvedAndTotalCounts { easy, medium, hard ->
                            availableCounts = Triple(easy, medium, hard)
                        }
                    }
                    PuzzleGenerationService.ACTION_GENERATION_ERROR -> {
                        isGenerating = false
                        currentProgress = 0
                        totalPuzzles = 0
                        currentDifficulty = ""
                        errorMessage = intent.getStringExtra(PuzzleGenerationService.EXTRA_ERROR_MESSAGE) ?: "Unbekannter Fehler"
                        showErrorDialog = true
                    }
                    PuzzleGenerationService.ACTION_SERVICE_STATUS -> {
                        val serviceRunning = intent.getBooleanExtra(PuzzleGenerationService.EXTRA_SERVICE_RUNNING, false)
                        
                        if (serviceRunning) {
                            val statusProgress = intent.getIntExtra(PuzzleGenerationService.EXTRA_CURRENT_PROGRESS, 0)
                            val statusTotal = intent.getIntExtra(PuzzleGenerationService.EXTRA_TOTAL_PUZZLES, 0)
                            val statusDifficulty = intent.getStringExtra(PuzzleGenerationService.EXTRA_CURRENT_DIFFICULTY) ?: ""
                            
                            isGenerating = true
                            currentProgress = statusProgress
                            totalPuzzles = statusTotal
                            currentDifficulty = statusDifficulty
                        } else {
                            isGenerating = false
                        }
                    }
                }
            }
        }
        
        // Verbesserte Registrierung mit EXPORTED Flag
        val intentFilter = IntentFilter().apply {
            addAction(PuzzleGenerationService.ACTION_PROGRESS_UPDATE)
            addAction(PuzzleGenerationService.ACTION_GENERATION_COMPLETE)
            addAction(PuzzleGenerationService.ACTION_GENERATION_ERROR)
            addAction(PuzzleGenerationService.ACTION_SERVICE_STATUS)
        }

        // Verwende RECEIVER_EXPORTED für Service Communication
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                context,
                receiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, intentFilter)
        }
        
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Receiver war bereits unregistriert
            }
        }
    }
    
    // Proper Window Insets für Punchhole/Statusbar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        
        // Header mit verbessertem Spacing
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Zurück",
                        tint = Color(0xFF2C2C2C)
                    )
                }
                Text(
                    text = "Rätsel Generieren",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF2C2C2C)
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
        
        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status-Anzeige der noch verfügbaren Rätsel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Noch verfügbare Rätsel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C2C2C),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PuzzleCountDisplay(
                            "Einfach", 
                            availableCounts.first.first, 
                            availableCounts.first.second
                        )
                        PuzzleCountDisplay(
                            "Mittel", 
                            availableCounts.second.first, 
                            availableCounts.second.second
                        )
                        PuzzleCountDisplay(
                            "Schwer", 
                            availableCounts.third.first, 
                            availableCounts.third.second
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (isGenerating) {
                // Verbesserter Fortschrittsbalken mit Live-Updates
                GenerationProgressCard(
                    currentProgress = currentProgress,
                    totalPuzzles = totalPuzzles, 
                    currentDifficulty = currentDifficulty
                )
                
            } else {
                // Eingabeformular
                Text(
                    text = "Neue Rätsel generieren",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C2C2C),
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
                            try {
                                // Sofort UI State setzen für bessere Responsivität
                                isGenerating = true
                                currentProgress = 0
                                totalPuzzles = total
                                currentDifficulty = "Wird gestartet..."
                                
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
                        containerColor = Color(0xFF2C2C2C),
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
                
                // Import/Export Buttons
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Import Button
                    Button(
                        onClick = { importLauncher.launch("application/json") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEEEEEE),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isGenerating && !isImporting && !isExporting
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black
                            )
                        } else {
                            Text(
                                text = "Importieren",
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Export Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isExporting = true
                                val uri = importExportManager.exportAllPuzzles()
                                isExporting = false
                                if (uri != null) {
                                    importResultMessage = "Export erfolgreich!\nDatei wurde im Downloads-Ordner gespeichert."
                                } else {
                                    importResultMessage = "Export fehlgeschlagen!"
                                }
                                showImportResultDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEEEEEE),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isGenerating && !isImporting && !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black
                            )
                        } else {
                            Text(
                                text = "Exportieren",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Hinweise
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 Hinweise:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8A6400)
                    )
                    Text(
                        text = "• Die Generierung läuft im Hintergrund\n" +
                               "• Sie erhalten eine Benachrichtigung bei Abschluss\n" +
                               "• Duplikate werden automatisch vermieden",
                        fontSize = 12.sp,
                        color = Color(0xFF8A6400),
                        modifier = Modifier.padding(top = 8.dp),
                        lineHeight = 16.sp
                    )
                }
            }
            
            // Extra Padding am Ende für besseres Scrolling
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    
    // Success Dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showCompleteDialog = false 
                        viewModel.getTotalPuzzleCounts { easy, medium, hard ->
                            totalCounts = Triple(easy, medium, hard)
                        }
                        viewModel.getSolvedAndTotalCounts { easy, medium, hard ->
                            availableCounts = Triple(easy, medium, hard)
                        }
                    }
                ) {
                    Text("Verstanden", color = Color(0xFF2C2C2C))
                }
            },
            title = { 
                Text(
                    "✅ Generierung abgeschlossen!",
                    color = Color(0xFF2C2C2C),
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    "Alle Rätsel wurden erfolgreich generiert und in die Datenbank eingefügt. Sie können jetzt neue Spiele starten!",
                    color = Color(0xFF454545),
                    lineHeight = 20.sp
                ) 
            },
            containerColor = Color.White
        )
    }
    
    // Import/Export Result Dialog
    if (showImportResultDialog) {
        AlertDialog(
            onDismissRequest = { showImportResultDialog = false },
            title = { 
                Text(
                    "Import/Export Ergebnis",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C)
                ) 
            },
            text = { 
                Text(
                    importResultMessage,
                    lineHeight = 20.sp,
                    color = Color(0xFF454545)
                ) 
            },
            confirmButton = {
                TextButton(onClick = { showImportResultDialog = false }) {
                    Text("OK", color = Color(0xFF2C2C2C))
                }
            },
            containerColor = Color.White
        )
    }
    
    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK", color = Color(0xFFD32F2F))
                }
            },
            title = { 
                Text(
                    "❌ Fehler bei der Generierung",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    "Fehler: $errorMessage\n\nBitte versuchen Sie es erneut.",
                    color = Color(0xFF454545),
                    lineHeight = 20.sp
                ) 
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun PuzzleCountDisplay(title: String, available: Int, total: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$available/$total",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2C2C)
        )
        Text(
            text = title,
            fontSize = 13.sp,
            color = Color(0xFF757575),
            fontWeight = FontWeight.Medium
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalIconButton(
                    onClick = { onCountChange(max(0, count - 1)) },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFF2C2C2C)
                    )
                ) {
                    Text(
                        text = "−",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = count.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C),
                    modifier = Modifier.widthIn(min = 40.dp),
                    textAlign = TextAlign.Center
                )
                
                FilledTonalIconButton(
                    onClick = { onCountChange(count + 1) },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFF2C2C2C),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rätsel werden generiert...",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C2C2C),
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // Korrekte Progress-Berechnung mit Sicherheitscheck
            val progress = remember(currentProgress, totalPuzzles) {
                if (totalPuzzles > 0) {
                    (currentProgress.toFloat() / totalPuzzles.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
            
            LinearProgressIndicator(
                progress = { progress }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fortschritt in Zahlen
            Text(
                text = "$currentProgress von $totalPuzzles erstellt",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
            
            // Prozentanzeige
            Text(
                text = "${(progress * 100).toInt()}% abgeschlossen",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            if (currentDifficulty.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF3E5F5),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Aktuell: $currentDifficulty",
                        fontSize = 14.sp,
                        color = Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Die Generierung läuft im Hintergrund. Sie können die App weiter nutzen.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
