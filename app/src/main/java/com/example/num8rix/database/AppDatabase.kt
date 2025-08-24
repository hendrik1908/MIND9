package com.example.num8rix.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.num8rix.database.entity.Einfach
import com.example.num8rix.database.entity.Mittel
import com.example.num8rix.database.entity.Schwer
import com.example.num8rix.database.entity.GameCache
import com.example.num8rix.database.dao.EinfachDao
import com.example.num8rix.database.dao.MittelDao
import com.example.num8rix.database.dao.SchwerDao
import com.example.num8rix.database.dao.GameCacheDao

@Database(entities = [Einfach::class, Mittel::class, Schwer::class, GameCache::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun einfachDao(): EinfachDao
    abstract fun mittelDao(): MittelDao
    abstract fun schwerDao(): SchwerDao
    abstract fun gameCacheDao(): GameCacheDao
}