package com.example.num8rix.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.num8rix.database.entity.Schwer

@Dao
interface SchwerDao {
    @Insert
    suspend fun insert(schwer: Schwer)

    @Query("SELECT solutionString FROM schwer WHERE id = :itemId")
    suspend fun getSolutionStringById(itemId: Int): String?

    @Query("SELECT * FROM schwer WHERE alreadySolved = :solvedStatus")
    suspend fun getBySolvedStatus(solvedStatus: Boolean): List<Schwer>

    @Query("UPDATE schwer SET alreadySolved = :newStatus WHERE id = :itemId")
    suspend fun updateSolvedStatus(itemId: Int, newStatus: Boolean)
}