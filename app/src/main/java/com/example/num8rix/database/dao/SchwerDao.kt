package com.example.num8rix.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.num8rix.database.entity.Einfach
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

    @Query("SELECT * FROM schwer WHERE alreadySolved = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomUnsolved(): Schwer?

    // NEU: Duplikatsprüfung für die Generierung
    @Query("SELECT COUNT(*) > 0 FROM schwer WHERE unsolvedString = :unsolvedString")
    suspend fun puzzleExists(unsolvedString: String): Boolean

    // NEU: Zählt alle Rätsel dieser Schwierigkeit
    @Query("SELECT COUNT(*) FROM schwer")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM schwer WHERE alreadySolved = 1")
    suspend fun getSolvedCount(): Int

    @Query("SELECT * FROM schwer WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: Int): Schwer?
}