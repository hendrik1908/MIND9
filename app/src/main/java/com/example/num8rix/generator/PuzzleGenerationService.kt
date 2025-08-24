package com.example.num8rix.generator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.MyApplication
import com.example.num8rix.database.entity.Einfach
import com.example.num8rix.database.entity.Mittel
import com.example.num8rix.database.entity.Schwer
import kotlinx.coroutines.*

class PuzzleGenerationService : Service() {

    companion object {
        const val CHANNEL_ID = "puzzle_generation_channel"
        const val NOTIFICATION_ID = 1

        // Intent Extras
        const val EXTRA_EASY_COUNT = "easy_count"
        const val EXTRA_MEDIUM_COUNT = "medium_count"
        const val EXTRA_HARD_COUNT = "hard_count"

        // Broadcast Actions
        const val ACTION_PROGRESS_UPDATE = "com.example.num8rix.PROGRESS_UPDATE"
        const val ACTION_GENERATION_COMPLETE = "com.example.num8rix.GENERATION_COMPLETE"
        const val ACTION_GENERATION_ERROR = "com.example.num8rix.GENERATION_ERROR"

        // Broadcast Extras
        const val EXTRA_CURRENT_PROGRESS = "current_progress"
        const val EXTRA_TOTAL_PUZZLES = "total_puzzles"
        const val EXTRA_CURRENT_DIFFICULTY = "current_difficulty"
        const val EXTRA_ERROR_MESSAGE = "error_message"

        fun startGeneration(
            context: Context,
            easyCount: Int,
            mediumCount: Int,
            hardCount: Int
        ) {
            val intent = Intent(context, PuzzleGenerationService::class.java).apply {
                putExtra(EXTRA_EASY_COUNT, easyCount)
                putExtra(EXTRA_MEDIUM_COUNT, mediumCount)
                putExtra(EXTRA_HARD_COUNT, hardCount)
            }
            context.startForegroundService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val puzzleGenerator = PuzzleGeneratorService()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val easyCount = intent?.getIntExtra(EXTRA_EASY_COUNT, 0) ?: 0
        val mediumCount = intent?.getIntExtra(EXTRA_MEDIUM_COUNT, 0) ?: 0
        val hardCount = intent?.getIntExtra(EXTRA_HARD_COUNT, 0) ?: 0

        val totalPuzzles = easyCount + mediumCount + hardCount

        if (totalPuzzles == 0) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Foreground Service mit Notification starten
        val initialNotification = createInitialNotification(totalPuzzles)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, initialNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC) // Verwende den gleichen Typ wie im Manifest
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        // Generierung in Coroutine starten
        serviceScope.launch {
            try {
                generatePuzzles(easyCount, mediumCount, hardCount, totalPuzzles)
            } catch (e: Exception) {
                sendErrorBroadcast(e.message ?: "Unbekannter Fehler")
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun generatePuzzles(
        easyCount: Int,
        mediumCount: Int,
        hardCount: Int,
        totalPuzzles: Int
    ) {
        var currentProgress = 0
        val database = (application as MyApplication).database

// Einfache Rätsel generieren
        if (easyCount > 0) {
            sendProgressBroadcast(currentProgress, totalPuzzles, "Einfach")
            for (i in 1..easyCount) {
                val puzzle = generateUniquePuzzle(DifficultyLevel.EASY)
                if (puzzle != null) {
                    val entity = Einfach(
                        unsolvedString = puzzle.unsolved,
                        solutionString = puzzle.solution,
                        alreadySolved = false
                    )
                    database.einfachDao().insert(entity)
                } else {
                    throw Exception("Konnte kein einzigartiges einfaches Rätsel generieren")
                }
                currentProgress++
                sendProgressBroadcast(currentProgress, totalPuzzles, "Einfach")
                updateNotification(currentProgress, totalPuzzles, "Einfach")
            }
        }

// Mittlere Rätsel generieren
        if (mediumCount > 0) {
            sendProgressBroadcast(currentProgress, totalPuzzles, "Mittel")
            for (i in 1..mediumCount) {
                val puzzle = generateUniquePuzzle(DifficultyLevel.MEDIUM)
                if (puzzle != null) {
                    val entity = Mittel(
                        unsolvedString = puzzle.unsolved,
                        solutionString = puzzle.solution,
                        alreadySolved = false
                    )
                    database.mittelDao().insert(entity)
                } else {
                    throw Exception("Konnte kein einzigartiges mittleres Rätsel generieren")
                }
                currentProgress++
                sendProgressBroadcast(currentProgress, totalPuzzles, "Mittel")
                updateNotification(currentProgress, totalPuzzles, "Mittel")
            }
        }

// Schwere Rätsel generieren
        if (hardCount > 0) {
            sendProgressBroadcast(currentProgress, totalPuzzles, "Schwer")
            for (i in 1..hardCount) {
                val puzzle = generateUniquePuzzle(DifficultyLevel.HARD)
                if (puzzle != null) {
                    val entity = Schwer(
                        unsolvedString = puzzle.unsolved,
                        solutionString = puzzle.solution,
                        alreadySolved = false
                    )
                    database.schwerDao().insert(entity)
                } else {
                    throw Exception("Konnte kein einzigartiges schweres Rätsel generieren")
                }
                currentProgress++
                sendProgressBroadcast(currentProgress, totalPuzzles, "Schwer")
                updateNotification(currentProgress, totalPuzzles, "Schwer")
            }
        }

// Erfolgsmeldung senden
        sendCompleteBroadcast()
    }

    private suspend fun generateUniquePuzzle(difficulty: DifficultyLevel): PuzzleResult? {
        val database = (application as MyApplication).database
        val maxAttempts = 10 // Maximal 10 Versuche pro Rätsel

        repeat(maxAttempts) { attempt ->
            val puzzle = puzzleGenerator.generateNewPuzzle(difficulty, debug = false)
            if (puzzle != null) {
// Prüfe auf Duplikate in der entsprechenden Tabelle
                val exists = when (difficulty) {
                    DifficultyLevel.EASY -> database.einfachDao().puzzleExists(puzzle.unsolved)
                    DifficultyLevel.MEDIUM -> database.mittelDao().puzzleExists(puzzle.unsolved)
                    DifficultyLevel.HARD -> database.schwerDao().puzzleExists(puzzle.unsolved)
                }

                if (!exists) {
                    return puzzle // Einzigartiges Rätsel gefunden
                }
// Andernfalls nächsten Versuch
            }
        }

        return null // Kein einzigartiges Rätsel nach maxAttempts Versuchen
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rätsel Generierung",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Zeigt den Fortschritt der Rätsel-Generierung an"
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createInitialNotification(totalPuzzles: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rätsel werden generiert")
            .setContentText("0 von $totalPuzzles Rätseln erstellt")
            .setProgress(totalPuzzles, 0, false)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    private fun updateNotification(current: Int, total: Int, difficulty: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rätsel werden generiert")
            .setContentText("$current von $total Rätseln erstellt ($difficulty)")
            .setProgress(total, current, false)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun sendProgressBroadcast(current: Int, total: Int, difficulty: String) {
        val intent = Intent(ACTION_PROGRESS_UPDATE).apply {
            putExtra(EXTRA_CURRENT_PROGRESS, current)
            putExtra(EXTRA_TOTAL_PUZZLES, total)
            putExtra(EXTRA_CURRENT_DIFFICULTY, difficulty)
        }
        sendBroadcast(intent)
    }

    private fun sendCompleteBroadcast() {
        val intent = Intent(ACTION_GENERATION_COMPLETE)
        sendBroadcast(intent)
    }

    private fun sendErrorBroadcast(error: String) {
        val intent = Intent(ACTION_GENERATION_ERROR).apply {
            putExtra(EXTRA_ERROR_MESSAGE, error)
        }
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}