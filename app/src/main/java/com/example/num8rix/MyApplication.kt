package com.example.num8rix

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.num8rix.database.AppDatabase

class MyApplication : Application() {

    lateinit var database: AppDatabase
        private set

    // Migration von Version 3 zu 4: Neue DAO-Methoden sind nur Kotlin-Funktionen, keine Schema-Änderungen
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Keine SQL-Änderungen nötig - nur neue DAO-Methoden hinzugefügt
            // Diese existieren nur in Kotlin-Code, nicht in der Datenbankstruktur
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "num8rix_database"
        )
            .addMigrations(MIGRATION_3_4) // Saubere Migration hinzugefügt
            .fallbackToDestructiveMigration() // Fallback für unerwartete Fälle
            .build()
    }
}