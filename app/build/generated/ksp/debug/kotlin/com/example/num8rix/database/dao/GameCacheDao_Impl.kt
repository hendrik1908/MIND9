package com.example.num8rix.database.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.num8rix.DifficultyLevel
import com.example.num8rix.database.entity.GameCache
import javax.`annotation`.processing.Generated
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class GameCacheDao_Impl(
  __db: RoomDatabase,
) : GameCacheDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfGameCache: EntityInsertAdapter<GameCache>
  init {
    this.__db = __db
    this.__insertAdapterOfGameCache = object : EntityInsertAdapter<GameCache>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `game_cache` (`id`,`currentGridString`,`notesGridString`,`originalGridString`,`originalLayoutString`,`difficulty`,`puzzleId`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GameCache) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.currentGridString)
        statement.bindText(3, entity.notesGridString)
        statement.bindText(4, entity.originalGridString)
        statement.bindText(5, entity.originalLayoutString)
        statement.bindText(6, __DifficultyLevel_enumToString(entity.difficulty))
        statement.bindLong(7, entity.puzzleId.toLong())
      }
    }
  }

  public override suspend fun insert(gameCache: GameCache): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfGameCache.insert(_connection, gameCache)
  }

  public override suspend fun getLatestEntry(): GameCache? {
    val _sql: String = "SELECT * FROM game_cache ORDER BY id DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfCurrentGridString: Int = getColumnIndexOrThrow(_stmt, "currentGridString")
        val _cursorIndexOfNotesGridString: Int = getColumnIndexOrThrow(_stmt, "notesGridString")
        val _cursorIndexOfOriginalGridString: Int = getColumnIndexOrThrow(_stmt,
            "originalGridString")
        val _cursorIndexOfOriginalLayoutString: Int = getColumnIndexOrThrow(_stmt,
            "originalLayoutString")
        val _cursorIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _cursorIndexOfPuzzleId: Int = getColumnIndexOrThrow(_stmt, "puzzleId")
        val _result: GameCache?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          val _tmpCurrentGridString: String
          _tmpCurrentGridString = _stmt.getText(_cursorIndexOfCurrentGridString)
          val _tmpNotesGridString: String
          _tmpNotesGridString = _stmt.getText(_cursorIndexOfNotesGridString)
          val _tmpOriginalGridString: String
          _tmpOriginalGridString = _stmt.getText(_cursorIndexOfOriginalGridString)
          val _tmpOriginalLayoutString: String
          _tmpOriginalLayoutString = _stmt.getText(_cursorIndexOfOriginalLayoutString)
          val _tmpDifficulty: DifficultyLevel
          _tmpDifficulty = __DifficultyLevel_stringToEnum(_stmt.getText(_cursorIndexOfDifficulty))
          val _tmpPuzzleId: Int
          _tmpPuzzleId = _stmt.getLong(_cursorIndexOfPuzzleId).toInt()
          _result =
              GameCache(_tmpId,_tmpCurrentGridString,_tmpNotesGridString,_tmpOriginalGridString,_tmpOriginalLayoutString,_tmpDifficulty,_tmpPuzzleId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLatestEntryByDifficulty(difficulty: DifficultyLevel): GameCache? {
    val _sql: String = "SELECT * FROM game_cache WHERE difficulty = ? ORDER BY id DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, __DifficultyLevel_enumToString(difficulty))
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfCurrentGridString: Int = getColumnIndexOrThrow(_stmt, "currentGridString")
        val _cursorIndexOfNotesGridString: Int = getColumnIndexOrThrow(_stmt, "notesGridString")
        val _cursorIndexOfOriginalGridString: Int = getColumnIndexOrThrow(_stmt,
            "originalGridString")
        val _cursorIndexOfOriginalLayoutString: Int = getColumnIndexOrThrow(_stmt,
            "originalLayoutString")
        val _cursorIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _cursorIndexOfPuzzleId: Int = getColumnIndexOrThrow(_stmt, "puzzleId")
        val _result: GameCache?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          val _tmpCurrentGridString: String
          _tmpCurrentGridString = _stmt.getText(_cursorIndexOfCurrentGridString)
          val _tmpNotesGridString: String
          _tmpNotesGridString = _stmt.getText(_cursorIndexOfNotesGridString)
          val _tmpOriginalGridString: String
          _tmpOriginalGridString = _stmt.getText(_cursorIndexOfOriginalGridString)
          val _tmpOriginalLayoutString: String
          _tmpOriginalLayoutString = _stmt.getText(_cursorIndexOfOriginalLayoutString)
          val _tmpDifficulty: DifficultyLevel
          _tmpDifficulty = __DifficultyLevel_stringToEnum(_stmt.getText(_cursorIndexOfDifficulty))
          val _tmpPuzzleId: Int
          _tmpPuzzleId = _stmt.getLong(_cursorIndexOfPuzzleId).toInt()
          _result =
              GameCache(_tmpId,_tmpCurrentGridString,_tmpNotesGridString,_tmpOriginalGridString,_tmpOriginalLayoutString,_tmpDifficulty,_tmpPuzzleId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearCache() {
    val _sql: String = "DELETE FROM game_cache"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteLatestEntryByDifficulty(difficulty: DifficultyLevel) {
    val _sql: String =
        "DELETE FROM game_cache WHERE id = (SELECT id FROM game_cache WHERE difficulty = ? ORDER BY id DESC LIMIT 1)"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, __DifficultyLevel_enumToString(difficulty))
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCacheByDifficulty(difficulty: DifficultyLevel) {
    val _sql: String = "DELETE FROM game_cache WHERE difficulty = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, __DifficultyLevel_enumToString(difficulty))
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __DifficultyLevel_enumToString(_value: DifficultyLevel): String = when (_value) {
    DifficultyLevel.EASY -> "EASY"
    DifficultyLevel.MEDIUM -> "MEDIUM"
    DifficultyLevel.HARD -> "HARD"
  }

  private fun __DifficultyLevel_stringToEnum(_value: String): DifficultyLevel = when (_value) {
    "EASY" -> DifficultyLevel.EASY
    "MEDIUM" -> DifficultyLevel.MEDIUM
    "HARD" -> DifficultyLevel.HARD
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
