package com.example.num8rix.ui.screens

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.num8rix.MyApplication // Importiere deine Application-Klasse
import com.example.num8rix.database.dao.EinfachDao // Passe den Import an
import com.example.num8rix.database.dao.MittelDao
import com.example.num8rix.database.dao.SchwerDao
import com.example.num8rix.database.entity.Einfach // Passe den Import an
import kotlinx.coroutines.launch
import com.example.num8rix.DifficultyLevel

// Beispiel ViewModel, das die DAOs verwendet
class MyDatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val einfachDao: EinfachDao
    private val mittelDao: MittelDao
    private val schwerDao: SchwerDao
    val currentPuzzle = mutableStateOf<String?>(null)

    init {
        // Zugriff auf die Singleton-Datenbankinstanz
        val appDatabase = (application as MyApplication).database
        einfachDao = appDatabase.einfachDao()
        mittelDao = appDatabase.mittelDao()
        schwerDao = appDatabase.schwerDao()
    }

    // Beispiel: Eine neue "Einfach"-Zeile hinzufügen
    fun addEinfachEntry(unsolved: String, solution: String, solved: Boolean) {
        viewModelScope.launch {
            val newEntry = Einfach(unsolvedString = unsolved, solutionString = solution, alreadySolved = solved)
            einfachDao.insert(newEntry)
        }
    }

    // Beispiel: Lösung für eine bestimmte ID abfragen
    fun getSolutionForEinfach(id: Int) {
        viewModelScope.launch {
            val solution = einfachDao.getSolutionStringById(id)
            println("Solution for Einfach ID $id: $solution")
            // Hier könntest du die Lösung über LiveData oder State in die UI zurückgeben
        }
    }

    // Beispiel: Alle gelösten "Mittel"-Einträge abfragen
    fun getSolvedMittelEntries() {
        viewModelScope.launch {
            val solvedEntries = mittelDao.getBySolvedStatus(true)
            println("Solved Mittel Entries: $solvedEntries")
            // Hier könntest du die Liste über LiveData oder State in die UI zurückgeben
        }
    }
    //Gibt je nach Schwirigkeit ein zufällig gelöstes Rätsel zurück
    fun getRandomUnsolvedByDifficulty(
        difficulty: DifficultyLevel,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = when (difficulty) {
                DifficultyLevel.EASY -> einfachDao.getRandomUnsolved()?.unsolvedString
                DifficultyLevel.MEDIUM -> mittelDao.getRandomUnsolved()?.unsolvedString
                DifficultyLevel.HARD -> schwerDao.getRandomUnsolved()?.unsolvedString
            }
            onResult(result)
        }
    }
    // Füge hier weitere Funktionen für deine Datenbankoperationen hinzu
}
