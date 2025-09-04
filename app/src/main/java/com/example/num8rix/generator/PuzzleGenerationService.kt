package com.example.num8rix.generator

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.MainActivity
import com.example.num8rix.MyApplication
import com.example.num8rix.database.entity.Einfach
import com.example.num8rix.database.entity.Mittel
import com.example.num8rix.database.entity.Schwer
import kotlinx.coroutines.*

class PuzzleGenerationService : Service() {

    companion object {
        const val CHANNEL_ID = "puzzle_generation_channel"
        const val COMPLETION_CHANNEL_ID = "puzzle_completion_channel"
        const val NOTIFICATION_ID = 1
        const val COMPLETION_NOTIFICATION_ID = 2

        // Intent Extras
        const val EXTRA_EASY_COUNT = "easy_count"
        const val EXTRA_MEDIUM_COUNT = "medium_count"
        const val EXTRA_HARD_COUNT = "hard_count"

        // Broadcast Actions
        const val ACTION_PROGRESS_UPDATE = "com.example.num8rix.PROGRESS_UPDATE"
        const val ACTION_GENERATION_COMPLETE = "com.example.num8rix.GENERATION_COMPLETE"
        const val ACTION_GENERATION_ERROR = "com.example.num8rix.GENERATION_ERROR"
        const val ACTION_SERVICE_STATUS = "com.example.num8rix.SERVICE_STATUS"

        // Broadcast Extras
        const val EXTRA_CURRENT_PROGRESS = "current_progress"
        const val EXTRA_TOTAL_PUZZLES = "total_puzzles"
        const val EXTRA_CURRENT_DIFFICULTY = "current_difficulty"
        const val EXTRA_ERROR_MESSAGE = "error_message"
        const val EXTRA_SERVICE_RUNNING = "service_running"

        // Static variables für Service State
        private var currentServiceProgress = 0
        private var currentServiceTotal = 0
        private var currentServiceDifficulty = ""
        private var serviceInstance: PuzzleGenerationService? = null

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

        // Methode zum Prüfen ob Service läuft
        fun isServiceRunning(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (PuzzleGenerationService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        // Methode zum Abrufen des aktuellen Status
        fun requestCurrentStatus(context: Context) {
            if (serviceInstance != null) {
                val intent = Intent(ACTION_SERVICE_STATUS).apply {
                    putExtra(EXTRA_SERVICE_RUNNING, true)
                    putExtra(EXTRA_CURRENT_PROGRESS, currentServiceProgress)
                    putExtra(EXTRA_TOTAL_PUZZLES, currentServiceTotal)
                    putExtra(EXTRA_CURRENT_DIFFICULTY, currentServiceDifficulty)
                }
                context.sendBroadcast(intent)
            } else {
                val intent = Intent(ACTION_SERVICE_STATUS).apply {
                    putExtra(EXTRA_SERVICE_RUNNING, false)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val puzzleGenerator = PuzzleGeneratorService()

    override fun onCreate() {
        super.onCreate()
        serviceInstance = this
        createNotificationChannels()
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

        // Service State initialisieren
        currentServiceTotal = totalPuzzles
        currentServiceProgress = 0
        currentServiceDifficulty = "Wird gestartet..."

        // Foreground Service mit Notification starten
        val initialNotification = createInitialNotification(totalPuzzles)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, initialNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
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
            currentServiceDifficulty = "Einfach"
            sendProgressBroadcast(currentProgress, totalPuzzles, "Einfach")
            updateNotification(currentProgress, totalPuzzles, "Einfach")
            
            for (i in 1..easyCount) {
                val puzzle = generateUniquePuzzle(DifficultyLevel.EASY)
                if (puzzle != null) {
                    val entity = Einfach(
                        unsolvedString = puzzle.unsolved,
                        layoutString = puzzle.layout,
                        solutionString = puzzle.solution,
                        alreadySolved = false
                    )
                    database.einfachDao().insert(entity)
                } else {
                    throw Exception("Konnte kein einzigartiges einfaches Rätsel generieren")
                }
                currentProgress++
                
                // Service State aktualisieren
                currentServiceProgress = currentProgress
                sendProgressBroadcast(currentProgress, totalPuzzles, "Einfach")
                updateNotification(currentProgress, totalPuzzles, "Einfach")
                
                // Kurze Pause für bessere UI-Updates
                delay(100)
            }
        }

        // Mittlere Rätsel generieren
        if (mediumCount > 0) {
            currentServiceDifficulty = "Mittel"
            sendProgressBroadcast(currentProgress, totalPuzzles, "Mittel")
            updateNotification(currentProgress, totalPuzzles, "Mittel")
            
            for (i in 1..mediumCount) {
                val puzzle = generateUniquePuzzle(DifficultyLevel.MEDIUM)
                if (puzzle != null) {
                    val entity = Mittel(
                        unsolvedString = puzzle.unsolved,
                        layoutString = puzzle.layout,
                        solutionString = puzzle.solution,
                        alreadySolved = false
                    )
                    database.mittelDao().insert(entity)
                } else {
                    throw Exception("Konnte kein einzigartiges mittleres Rätsel generieren")
                }
                currentProgress++
                
                // Service State aktualisieren
                currentServiceProgress = currentProgress
                sendProgressBroadcast(currentProgress, totalPuzzles, "Mittel")
                updateNotification(currentProgress, totalPuzzles, "Mittel")
                
                // Kurze Pause für bessere UI-Updates
                delay(100)
            }
        }

        // Schwere Rätsel generieren
        if (hardCount > 0) {
            currentServiceDifficulty = "Schwer"
            sendProgressBroadcast(currentProgress, totalPuzzles, "Schwer")
            updateNotification(currentProgress, totalPuzzles, "Schwer")
            
            for (i in 1..hardCount) {
                val puzzle = generateUniquePuzzle(DifficultyLevel.HARD)
                if (puzzle != null) {
                    val entity = Schwer(
                        unsolvedString = puzzle.unsolved,
                        layoutString = puzzle.layout,
                        solutionString = puzzle.solution,
                        alreadySolved = false
                    )
                    database.schwerDao().insert(entity)
                } else {
                    throw Exception("Konnte kein einzigartiges schweres Rätsel generieren")
                }
                currentProgress++
                
                // Service State aktualisieren
                currentServiceProgress = currentProgress
                sendProgressBroadcast(currentProgress, totalPuzzles, "Schwer")
                updateNotification(currentProgress, totalPuzzles, "Schwer")
                
                // Kurze Pause für bessere UI-Updates
                delay(100)
            }
        }

        // Completion Notification immer anzeigen
        showCompletionNotification(totalPuzzles)
        sendCompleteBroadcast()
    }

    private suspend fun generateUniquePuzzle(difficulty: DifficultyLevel): PuzzleResult? {
        val database = (application as MyApplication).database
        val maxAttempts = 10

        repeat(maxAttempts) { attempt ->
            val puzzle = puzzleGenerator.generateNewPuzzle(difficulty, debug = false) // DEBUG AUS!
            if (puzzle != null) {
                val exists = when (difficulty) {
                    DifficultyLevel.EASY -> database.einfachDao().puzzleExists(puzzle.unsolved)
                    DifficultyLevel.MEDIUM -> database.mittelDao().puzzleExists(puzzle.unsolved)
                    DifficultyLevel.HARD -> database.schwerDao().puzzleExists(puzzle.unsolved)
                }

                if (!exists) {
                    return puzzle
                }
            }
        }

        return null
    }

    private fun createNotificationChannels() {
        // Channel für Fortschritts-Benachrichtigungen (Low Priority)
        val progressChannel = NotificationChannel(
            CHANNEL_ID,
            "Rätsel Generierung",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Zeigt den Fortschritt der Rätsel-Generierung an"
            setShowBadge(false)
        }

        // Separate Channel für Fertigstellungs-Benachrichtigungen (Higher Priority)
        val completionChannel = NotificationChannel(
            COMPLETION_CHANNEL_ID,
            "Generierung Abgeschlossen",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Benachrichtigt Sie, wenn neue Rätsel fertig generiert wurden"
            setShowBadge(true)
            enableVibration(true)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(progressChannel)
        notificationManager.createNotificationChannel(completionChannel)
    }

    private fun createInitialNotification(totalPuzzles: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rätsel werden generiert")
            .setContentText("0 von $totalPuzzles Rätseln erstellt")
            .setProgress(totalPuzzles, 0, false)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setSound(null)
            .setVibrate(null)
            .build()
    }

    private fun updateNotification(current: Int, total: Int, difficulty: String) {
        val percentage = ((current.toFloat() / total.toFloat()) * 100).toInt()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rätsel werden generiert")
            .setContentText("$current von $total erstellt ($percentage%) - $difficulty")
            .setProgress(total, current, false)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setSound(null)
            .setVibrate(null)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // Separate Fertigstellungs-Benachrichtigung
    private fun showCompletionNotification(totalPuzzles: Int) {
        // Intent zum Öffnen der App
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completionNotification = NotificationCompat.Builder(this, COMPLETION_CHANNEL_ID)
            .setContentTitle("✅ Rätsel generiert!")
            .setContentText("$totalPuzzles neue Rätsel wurden erfolgreich erstellt")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$totalPuzzles neue Rätsel wurden erfolgreich erstellt und stehen zum Spielen bereit. Tippen Sie hier, um die App zu öffnen."))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(COMPLETION_NOTIFICATION_ID, completionNotification)
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
        // Service State zurücksetzen
        serviceInstance = null
        currentServiceProgress = 0
        currentServiceTotal = 0
        currentServiceDifficulty = ""
        
        serviceScope.cancel()
        super.onDestroy()
    }
}
