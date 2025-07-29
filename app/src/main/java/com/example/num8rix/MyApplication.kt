package com.example.num8rix

import android.app.Application
import androidx.room.Room
import com.example.num8rix.database.AppDatabase // WICHTIG: Pfad anpassen!

class MyApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "num8rix_database" // Wähle einen Namen für deine Datenbankdatei
        )
            .fallbackToDestructiveMigration() // Optional: Für Entwicklungszwecke, falls sich das Schema ändert
            .build()
    }
}