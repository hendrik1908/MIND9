package com.example.num8rix.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.num8rix.database.entity.Einfach

@Dao
interface EinfachDao {

    //hinzufügen einer neuen Zeile
    @Insert
    suspend fun insert(einfach: Einfach)

    //Abfrage des Lösungs-String anhand der ID des Rätsels
    @Query("SELECT solutionString FROM einfach WHERE id = :itemId")
    suspend fun getSolutionStringById(itemId: Int): String?

    //Abfrage ob das Rätsel bereits gelöst wurde, anhand des Boolean Wertes
    @Query("SELECT * FROM einfach WHERE alreadySolved = :solvedStatus")
    suspend fun getBySolvedStatus(solvedStatus: Boolean): List<Einfach>

    //Abfrage um den Boolean Wert auf true zu ändern, wenn das Rätsel erfolgreich gelöst wurde
    @Query("UPDATE einfach SET alreadySolved = :newStatus WHERE id = :itemId")
    suspend fun updateSolvedStatus(itemId: Int, newStatus: Boolean)

    @Query("SELECT * FROM einfach WHERE alreadySolved = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomUnsolved(): Einfach?
}