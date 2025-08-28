package com.example.num8rix.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.num8rix.database.entity.Mittel

@Dao
interface MittelDao {
    @Insert
    suspend fun insert(mittel: Mittel)

    @Query("SELECT solutionString FROM mittel WHERE id = :itemId")
    suspend fun getSolutionStringById(itemId: Int): String?

    @Query("SELECT * FROM mittel WHERE alreadySolved = :solvedStatus")
    suspend fun getBySolvedStatus(solvedStatus: Boolean): List<Mittel>

    @Query("UPDATE mittel SET alreadySolved = :newStatus WHERE id = :itemId")
    suspend fun updateSolvedStatus(itemId: Int, newStatus: Boolean)

    @Query("SELECT * FROM mittel WHERE alreadySolved = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomUnsolved(): Mittel?


    // NEU: Duplikatsprüfung für die Generierung
    @Query("SELECT COUNT(*) > 0 FROM mittel WHERE unsolvedString = :unsolvedString")
    suspend fun puzzleExists(unsolvedString: String): Boolean

    // NEU: Zählt alle Rätsel dieser Schwierigkeit
    @Query("SELECT COUNT(*) FROM mittel")
    suspend fun getTotalCount(): Int
    @Query("SELECT COUNT(*) FROM mittel WHERE alreadySolved = 1")
    suspend fun getSolvedCount(): Int
}