package com.example.num8rix.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.database.entity.GameCache

@Dao
interface GameCacheDao {
    @Insert
    suspend fun insert(gameCache: GameCache)

    @Query("DELETE FROM game_cache")
    suspend fun clearCache()

    // Ruft den letzten gespeicherten Spielstand ab
    @Query("SELECT * FROM game_cache ORDER BY id DESC LIMIT 1")
    suspend fun getLatestEntry(): GameCache?

    @Query("SELECT * FROM game_cache WHERE difficulty = :difficulty ORDER BY id DESC LIMIT 1")
    suspend fun getLatestEntryByDifficulty(difficulty: DifficultyLevel): GameCache?

    @Query("DELETE FROM game_cache WHERE id = (SELECT id FROM game_cache WHERE difficulty = :difficulty ORDER BY id DESC LIMIT 1)")
    suspend fun deleteLatestEntryByDifficulty(difficulty: DifficultyLevel)
}