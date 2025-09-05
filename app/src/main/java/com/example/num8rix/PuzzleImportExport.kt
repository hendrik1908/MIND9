package com.example.num8rix

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.num8rix.ui.screens.MyDatabaseViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.text.SimpleDateFormat
import java.util.*

data class ExportablePuzzle(
    val unsolvedString: String,
    val layoutString: String,
    val solution: String,
    val difficulty: String, // "EASY", "MEDIUM", "HARD"
    val id: String? = null
)

data class PuzzleExportFile(
    val version: String = "1.0",
    val exportDate: String,
    val puzzles: List<ExportablePuzzle>,
    val totalCount: Int
)

sealed class ImportResult {
    data class Success(val stats: ImportStats) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

data class ImportStats(
    var successful: Int = 0,
    var duplicates: Int = 0,
    var errors: Int = 0
) {
    val total: Int get() = successful + duplicates + errors
}

enum class ImportSingleResult {
    SUCCESS, DUPLICATE, ERROR
}

class PuzzleImportExportManager(
    private val context: Context,
    private val viewModel: MyDatabaseViewModel
) {
    
    companion object {
        private const val EXPORT_FILE_EXTENSION = ".mind9"
        private const val MIME_TYPE = "application/json"
    }
    
    // ==================== EXPORT ====================
    
    suspend fun exportAllPuzzles(): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val allPuzzles = collectAllPuzzles()
                val exportFile = PuzzleExportFile(
                    exportDate = getCurrentDateString(),
                    puzzles = allPuzzles,
                    totalCount = allPuzzles.size
                )
                
                saveExportFile(exportFile, "mind9_all_puzzles_${getCurrentDateString()}")
            } catch (e: Exception) {
                Log.e("PuzzleExport", "Export fehlgeschlagen", e)
                null
            }
        }
    }
    
    suspend fun exportPuzzlesByDifficulty(difficulty: DifficultyLevel): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val puzzles = collectPuzzlesByDifficulty(difficulty)
                val exportFile = PuzzleExportFile(
                    exportDate = getCurrentDateString(),
                    puzzles = puzzles,
                    totalCount = puzzles.size
                )
                
                val difficultyName = when(difficulty) {
                    DifficultyLevel.EASY -> "easy"
                    DifficultyLevel.MEDIUM -> "medium" 
                    DifficultyLevel.HARD -> "hard"
                }
                
                saveExportFile(exportFile, "mind9_${difficultyName}_${getCurrentDateString()}")
            } catch (e: Exception) {
                Log.e("PuzzleExport", "Export fehlgeschlagen", e)
                null
            }
        }
    }
    
    private suspend fun collectAllPuzzles(): List<ExportablePuzzle> {
        val allPuzzles = mutableListOf<ExportablePuzzle>()
        
        // Easy Puzzles sammeln - warten auf Callback
        suspendCancellableCoroutine<Unit> { continuation ->
            viewModel.getAllEasyPuzzles { puzzles ->
                puzzles.forEach { puzzle ->
                    allPuzzles.add(ExportablePuzzle(
                        unsolvedString = puzzle.unsolvedString,
                        layoutString = puzzle.layoutString,
                        solution = puzzle.solutionString,
                        difficulty = "EASY",
                        id = puzzle.id.toString()
                    ))
                }
                continuation.resume(Unit)
            }
        }
        
        // Medium Puzzles sammeln - warten auf Callback
        suspendCancellableCoroutine<Unit> { continuation ->
            viewModel.getAllMediumPuzzles { puzzles ->
                puzzles.forEach { puzzle ->
                    allPuzzles.add(ExportablePuzzle(
                        unsolvedString = puzzle.unsolvedString,
                        layoutString = puzzle.layoutString,
                        solution = puzzle.solutionString,
                        difficulty = "MEDIUM",
                        id = puzzle.id.toString()
                    ))
                }
                continuation.resume(Unit)
            }
        }
        
        // Hard Puzzles sammeln - warten auf Callback
        suspendCancellableCoroutine<Unit> { continuation ->
            viewModel.getAllHardPuzzles { puzzles ->
                puzzles.forEach { puzzle ->
                    allPuzzles.add(ExportablePuzzle(
                        unsolvedString = puzzle.unsolvedString,
                        layoutString = puzzle.layoutString,
                        solution = puzzle.solutionString,
                        difficulty = "HARD",
                        id = puzzle.id.toString()
                    ))
                }
                continuation.resume(Unit)
            }
        }
        
        return allPuzzles
    }
    
    private suspend fun collectPuzzlesByDifficulty(difficulty: DifficultyLevel): List<ExportablePuzzle> {
        val puzzles = mutableListOf<ExportablePuzzle>()
        
        when(difficulty) {
            DifficultyLevel.EASY -> {
                suspendCancellableCoroutine<Unit> { continuation ->
                    viewModel.getAllEasyPuzzles { puzzleList ->
                        puzzleList.forEach { puzzle ->
                            puzzles.add(ExportablePuzzle(
                                unsolvedString = puzzle.unsolvedString,
                                layoutString = puzzle.layoutString,
                                solution = puzzle.solutionString,
                                difficulty = "EASY",
                                id = puzzle.id.toString()
                            ))
                        }
                        continuation.resume(Unit)
                    }
                }
            }
            DifficultyLevel.MEDIUM -> {
                suspendCancellableCoroutine<Unit> { continuation ->
                    viewModel.getAllMediumPuzzles { puzzleList ->
                        puzzleList.forEach { puzzle ->
                            puzzles.add(ExportablePuzzle(
                                unsolvedString = puzzle.unsolvedString,
                                layoutString = puzzle.layoutString,
                                solution = puzzle.solutionString,
                                difficulty = "MEDIUM",
                                id = puzzle.id.toString()
                            ))
                        }
                        continuation.resume(Unit)
                    }
                }
            }
            DifficultyLevel.HARD -> {
                suspendCancellableCoroutine<Unit> { continuation ->
                    viewModel.getAllHardPuzzles { puzzleList ->
                        puzzleList.forEach { puzzle ->
                            puzzles.add(ExportablePuzzle(
                                unsolvedString = puzzle.unsolvedString,
                                layoutString = puzzle.layoutString,
                                solution = puzzle.solutionString,
                                difficulty = "HARD",
                                id = puzzle.id.toString()
                            ))
                        }
                        continuation.resume(Unit)
                    }
                }
            }
        }
        
        return puzzles
    }
    
    private fun saveExportFile(exportFile: PuzzleExportFile, fileName: String): Uri? {
        return try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(exportFile)
            
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName$EXPORT_FILE_EXTENSION")
                put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { fileUri ->
                resolver.openOutputStream(fileUri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                Log.i("PuzzleExport", "Export erfolgreich: $fileName")
                fileUri
            }
        } catch (e: Exception) {
            Log.e("PuzzleExport", "Fehler beim Speichern", e)
            null
        }
    }
    
    // ==================== IMPORT ====================
    
    suspend fun importPuzzlesFromUri(uri: Uri): ImportResult {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: return@withContext ImportResult.Error("Datei konnte nicht gelesen werden")
                
                Log.d("PuzzleImport", "JSON Inhalt gelesen: ${jsonString.take(200)}...")
                
                val gson = Gson()
                val exportFile = gson.fromJson(jsonString, PuzzleExportFile::class.java)
                
                Log.d("PuzzleImport", "Parsed ${exportFile.puzzles.size} puzzles from file")
                
                if (exportFile.puzzles.isEmpty()) {
                    return@withContext ImportResult.Error("Keine Rätsel in der Datei gefunden")
                }
                
                val importStats = ImportStats()
                
                exportFile.puzzles.forEach { puzzle ->
                    try {
                        Log.d("PuzzleImport", "Importing puzzle: ${puzzle.difficulty}, ID: ${puzzle.id}")
                        Log.d("PuzzleImport", "Unsolved length: ${puzzle.unsolvedString.length}")
                        Log.d("PuzzleImport", "Solution length: ${puzzle.solution.length}")
                        Log.d("PuzzleImport", "Layout length: ${puzzle.layoutString.length}")
                        
                        val result = importSinglePuzzle(puzzle)
                        when (result) {
                            ImportSingleResult.SUCCESS -> {
                                importStats.successful++
                                Log.d("PuzzleImport", "Successfully imported puzzle ${puzzle.id}")
                            }
                            ImportSingleResult.DUPLICATE -> {
                                importStats.duplicates++
                                Log.d("PuzzleImport", "Skipped duplicate puzzle ${puzzle.id}")
                            }
                            ImportSingleResult.ERROR -> {
                                importStats.errors++
                                Log.e("PuzzleImport", "Failed to import puzzle ${puzzle.id}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PuzzleImport", "Exception importing puzzle ${puzzle.id}", e)
                        importStats.errors++
                    }
                }
                
                Log.i("PuzzleImport", "Import abgeschlossen: ${importStats.successful} erfolgreich, ${importStats.duplicates} Duplikate, ${importStats.errors} Fehler")
                ImportResult.Success(importStats)
                
            } catch (e: Exception) {
                Log.e("PuzzleImport", "Import fehlgeschlagen", e)
                ImportResult.Error("Import fehlgeschlagen: ${e.message}")
            }
        }
    }
    
    private suspend fun importSinglePuzzle(puzzle: ExportablePuzzle): ImportSingleResult {
        // Validiere Rätsel-Format
        if (!validatePuzzleFormat(puzzle)) {
            return ImportSingleResult.ERROR
        }
        
        // Prüfe auf Duplikate und importiere
        return try {
            when (puzzle.difficulty) {
                "EASY" -> {
                    // Prüfe Duplikat über callback-basierte Methode
                    val isDuplicate = suspendCancellableCoroutine<Boolean> { continuation ->
                        viewModel.checkEasyPuzzleExists(puzzle.unsolvedString, puzzle.layoutString) { exists ->
                            continuation.resume(exists)
                        }
                    }
                    
                    if (isDuplicate) {
                        ImportSingleResult.DUPLICATE
                    } else {
                        viewModel.addEinfachEntry(
                            unsolved = puzzle.unsolvedString,
                            layout = puzzle.layoutString,
                            solution = puzzle.solution,
                            solved = false
                        )
                        ImportSingleResult.SUCCESS
                    }
                }
                "MEDIUM" -> {
                    val isDuplicate = suspendCancellableCoroutine<Boolean> { continuation ->
                        viewModel.checkMediumPuzzleExists(puzzle.unsolvedString, puzzle.layoutString) { exists ->
                            continuation.resume(exists)
                        }
                    }
                    
                    if (isDuplicate) {
                        ImportSingleResult.DUPLICATE
                    } else {
                        viewModel.addMittelEntry(
                            unsolved = puzzle.unsolvedString,
                            layout = puzzle.layoutString,
                            solution = puzzle.solution,
                            solved = false
                        )
                        ImportSingleResult.SUCCESS
                    }
                }
                "HARD" -> {
                    val isDuplicate = suspendCancellableCoroutine<Boolean> { continuation ->
                        viewModel.checkHardPuzzleExists(puzzle.unsolvedString, puzzle.layoutString) { exists ->
                            continuation.resume(exists)
                        }
                    }
                    
                    if (isDuplicate) {
                        ImportSingleResult.DUPLICATE
                    } else {
                        viewModel.addSchwerEntry(
                            unsolved = puzzle.unsolvedString,
                            layout = puzzle.layoutString,
                            solution = puzzle.solution,
                            solved = false
                        )
                        ImportSingleResult.SUCCESS
                    }
                }
                else -> ImportSingleResult.ERROR
            }
        } catch (e: Exception) {
            Log.e("PuzzleImport", "Fehler beim Einfügen in DB", e)
            ImportSingleResult.ERROR
        }
    }
    
    private fun validatePuzzleFormat(puzzle: ExportablePuzzle): Boolean {
        // Grundlegende Validierung
        if (puzzle.unsolvedString.isBlank() || puzzle.solution.isBlank()) {
            Log.e("PuzzleImport", "Validation failed: Blank unsolved or solution string")
            return false
        }
        
        // Prüfe String-Format (9 Zeilen mit ; getrennt)
        val unsolvedRows = puzzle.unsolvedString.split(";")
        if (unsolvedRows.size != 9) {
            Log.e("PuzzleImport", "Validation failed: Unsolved string has ${unsolvedRows.size} rows, expected 9")
            return false
        }
        
        // Jede Zeile muss 9 Zeichen haben
        if (!unsolvedRows.all { it.length == 9 }) {
            Log.e("PuzzleImport", "Validation failed: Not all unsolved rows have 9 characters")
            unsolvedRows.forEachIndexed { index, row ->
                if (row.length != 9) {
                    Log.e("PuzzleImport", "Row $index has ${row.length} characters: '$row'")
                }
            }
            return false
        }
        
        // KORREKTUR: Solution kann entweder 81 Zeichen (full grid) oder weniger (nur weiße Felder) haben
        // Wir akzeptieren beide Formate
        if (puzzle.solution.length < 20) {
            Log.e("PuzzleImport", "Validation failed: Solution too short (${puzzle.solution.length} characters)")
            return false
        }
        
        // Layout-String validieren (falls vorhanden)
        if (puzzle.layoutString.isNotBlank()) {
            val layoutRows = puzzle.layoutString.split(";")
            if (layoutRows.size != 9 || !layoutRows.all { it.length == 9 }) {
                Log.e("PuzzleImport", "Validation failed: Invalid layout string format")
                return false
            }
        }
        
        Log.d("PuzzleImport", "Puzzle validation passed for ${puzzle.difficulty} puzzle")
        return true
    }
    
    // ==================== PREGENERATED IMPORT ====================
    
    suspend fun importPregeneratedPuzzles(): ImportResult {
        return try {
            val inputStream = context.assets.open("pregenerated_puzzles.mind9")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            val gson = Gson()
            val exportFile = gson.fromJson(jsonString, PuzzleExportFile::class.java)
            
            val importStats = ImportStats()
            
            exportFile.puzzles.forEach { puzzle ->
                val result = importSinglePuzzle(puzzle)
                when (result) {
                    ImportSingleResult.SUCCESS -> importStats.successful++
                    ImportSingleResult.DUPLICATE -> importStats.duplicates++
                    ImportSingleResult.ERROR -> importStats.errors++
                }
            }
            
            Log.i("PuzzleImport", "Vorgenerierte Rätsel importiert: ${importStats.successful} erfolgreich")
            ImportResult.Success(importStats)
        } catch (e: Exception) {
            Log.e("PuzzleImport", "Vorgenerierte Rätsel konnten nicht geladen werden", e)
            ImportResult.Error("Vorgenerierte Rätsel konnten nicht geladen werden: ${e.message}")
        }
    }
    
    // ==================== HELPER ====================
    
    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
