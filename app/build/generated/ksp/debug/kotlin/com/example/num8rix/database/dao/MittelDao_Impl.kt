package com.example.num8rix.database.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.num8rix.database.entity.Mittel
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MittelDao_Impl(
  __db: RoomDatabase,
) : MittelDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMittel: EntityInsertAdapter<Mittel>
  init {
    this.__db = __db
    this.__insertAdapterOfMittel = object : EntityInsertAdapter<Mittel>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `mittel` (`id`,`unsolvedString`,`layoutString`,`solutionString`,`alreadySolved`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Mittel) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.unsolvedString)
        statement.bindText(3, entity.layoutString)
        statement.bindText(4, entity.solutionString)
        val _tmp: Int = if (entity.alreadySolved) 1 else 0
        statement.bindLong(5, _tmp.toLong())
      }
    }
  }

  public override suspend fun insert(mittel: Mittel): Unit = performSuspending(__db, false, true) {
      _connection ->
    __insertAdapterOfMittel.insert(_connection, mittel)
  }

  public override suspend fun getSolutionStringById(itemId: Int): String? {
    val _sql: String = "SELECT solutionString FROM mittel WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, itemId.toLong())
        val _result: String?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getText(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBySolvedStatus(solvedStatus: Boolean): List<Mittel> {
    val _sql: String = "SELECT * FROM mittel WHERE alreadySolved = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (solvedStatus) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfUnsolvedString: Int = getColumnIndexOrThrow(_stmt, "unsolvedString")
        val _cursorIndexOfLayoutString: Int = getColumnIndexOrThrow(_stmt, "layoutString")
        val _cursorIndexOfSolutionString: Int = getColumnIndexOrThrow(_stmt, "solutionString")
        val _cursorIndexOfAlreadySolved: Int = getColumnIndexOrThrow(_stmt, "alreadySolved")
        val _result: MutableList<Mittel> = mutableListOf()
        while (_stmt.step()) {
          val _item: Mittel
          val _tmpId: Int
          _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          val _tmpUnsolvedString: String
          _tmpUnsolvedString = _stmt.getText(_cursorIndexOfUnsolvedString)
          val _tmpLayoutString: String
          _tmpLayoutString = _stmt.getText(_cursorIndexOfLayoutString)
          val _tmpSolutionString: String
          _tmpSolutionString = _stmt.getText(_cursorIndexOfSolutionString)
          val _tmpAlreadySolved: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_cursorIndexOfAlreadySolved).toInt()
          _tmpAlreadySolved = _tmp_1 != 0
          _item =
              Mittel(_tmpId,_tmpUnsolvedString,_tmpLayoutString,_tmpSolutionString,_tmpAlreadySolved)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRandomUnsolved(): Mittel? {
    val _sql: String = "SELECT * FROM mittel WHERE alreadySolved = 0 ORDER BY RANDOM() LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfUnsolvedString: Int = getColumnIndexOrThrow(_stmt, "unsolvedString")
        val _cursorIndexOfLayoutString: Int = getColumnIndexOrThrow(_stmt, "layoutString")
        val _cursorIndexOfSolutionString: Int = getColumnIndexOrThrow(_stmt, "solutionString")
        val _cursorIndexOfAlreadySolved: Int = getColumnIndexOrThrow(_stmt, "alreadySolved")
        val _result: Mittel?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          val _tmpUnsolvedString: String
          _tmpUnsolvedString = _stmt.getText(_cursorIndexOfUnsolvedString)
          val _tmpLayoutString: String
          _tmpLayoutString = _stmt.getText(_cursorIndexOfLayoutString)
          val _tmpSolutionString: String
          _tmpSolutionString = _stmt.getText(_cursorIndexOfSolutionString)
          val _tmpAlreadySolved: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_cursorIndexOfAlreadySolved).toInt()
          _tmpAlreadySolved = _tmp != 0
          _result =
              Mittel(_tmpId,_tmpUnsolvedString,_tmpLayoutString,_tmpSolutionString,_tmpAlreadySolved)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun puzzleExists(unsolvedString: String): Boolean {
    val _sql: String = "SELECT COUNT(*) > 0 FROM mittel WHERE unsolvedString = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, unsolvedString)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM mittel"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSolvedCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM mittel WHERE alreadySolved = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(itemId: Int): Mittel? {
    val _sql: String = "SELECT * FROM mittel WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, itemId.toLong())
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfUnsolvedString: Int = getColumnIndexOrThrow(_stmt, "unsolvedString")
        val _cursorIndexOfLayoutString: Int = getColumnIndexOrThrow(_stmt, "layoutString")
        val _cursorIndexOfSolutionString: Int = getColumnIndexOrThrow(_stmt, "solutionString")
        val _cursorIndexOfAlreadySolved: Int = getColumnIndexOrThrow(_stmt, "alreadySolved")
        val _result: Mittel?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          val _tmpUnsolvedString: String
          _tmpUnsolvedString = _stmt.getText(_cursorIndexOfUnsolvedString)
          val _tmpLayoutString: String
          _tmpLayoutString = _stmt.getText(_cursorIndexOfLayoutString)
          val _tmpSolutionString: String
          _tmpSolutionString = _stmt.getText(_cursorIndexOfSolutionString)
          val _tmpAlreadySolved: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_cursorIndexOfAlreadySolved).toInt()
          _tmpAlreadySolved = _tmp != 0
          _result =
              Mittel(_tmpId,_tmpUnsolvedString,_tmpLayoutString,_tmpSolutionString,_tmpAlreadySolved)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllPuzzles(): List<Mittel> {
    val _sql: String = "SELECT * FROM mittel"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfUnsolvedString: Int = getColumnIndexOrThrow(_stmt, "unsolvedString")
        val _cursorIndexOfLayoutString: Int = getColumnIndexOrThrow(_stmt, "layoutString")
        val _cursorIndexOfSolutionString: Int = getColumnIndexOrThrow(_stmt, "solutionString")
        val _cursorIndexOfAlreadySolved: Int = getColumnIndexOrThrow(_stmt, "alreadySolved")
        val _result: MutableList<Mittel> = mutableListOf()
        while (_stmt.step()) {
          val _item: Mittel
          val _tmpId: Int
          _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          val _tmpUnsolvedString: String
          _tmpUnsolvedString = _stmt.getText(_cursorIndexOfUnsolvedString)
          val _tmpLayoutString: String
          _tmpLayoutString = _stmt.getText(_cursorIndexOfLayoutString)
          val _tmpSolutionString: String
          _tmpSolutionString = _stmt.getText(_cursorIndexOfSolutionString)
          val _tmpAlreadySolved: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_cursorIndexOfAlreadySolved).toInt()
          _tmpAlreadySolved = _tmp != 0
          _item =
              Mittel(_tmpId,_tmpUnsolvedString,_tmpLayoutString,_tmpSolutionString,_tmpAlreadySolved)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun checkPuzzleExists(unsolvedString: String, layoutString: String):
      Boolean {
    val _sql: String =
        "SELECT COUNT(*) > 0 FROM mittel WHERE unsolvedString = ? AND layoutString = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, unsolvedString)
        _argIndex = 2
        _stmt.bindText(_argIndex, layoutString)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateSolvedStatus(itemId: Int, newStatus: Boolean) {
    val _sql: String = "UPDATE mittel SET alreadySolved = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (newStatus) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, itemId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
