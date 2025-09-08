package com.example.num8rix.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.example.num8rix.database.dao.EinfachDao
import com.example.num8rix.database.dao.EinfachDao_Impl
import com.example.num8rix.database.dao.GameCacheDao
import com.example.num8rix.database.dao.GameCacheDao_Impl
import com.example.num8rix.database.dao.MittelDao
import com.example.num8rix.database.dao.MittelDao_Impl
import com.example.num8rix.database.dao.SchwerDao
import com.example.num8rix.database.dao.SchwerDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Any
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _einfachDao: Lazy<EinfachDao> = lazy {
    EinfachDao_Impl(this)
  }


  private val _mittelDao: Lazy<MittelDao> = lazy {
    MittelDao_Impl(this)
  }


  private val _schwerDao: Lazy<SchwerDao> = lazy {
    SchwerDao_Impl(this)
  }


  private val _gameCacheDao: Lazy<GameCacheDao> = lazy {
    GameCacheDao_Impl(this)
  }


  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(7,
        "3f68c428e487a338d75f3265a5ba3665", "450a1f76ade1771bcfa36fd74ef1004a") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `einfach` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `unsolvedString` TEXT NOT NULL, `layoutString` TEXT NOT NULL, `solutionString` TEXT NOT NULL, `alreadySolved` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `mittel` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `unsolvedString` TEXT NOT NULL, `layoutString` TEXT NOT NULL, `solutionString` TEXT NOT NULL, `alreadySolved` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `schwer` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `unsolvedString` TEXT NOT NULL, `layoutString` TEXT NOT NULL, `solutionString` TEXT NOT NULL, `alreadySolved` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `game_cache` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `currentGridString` TEXT NOT NULL, `notesGridString` TEXT NOT NULL, `originalGridString` TEXT NOT NULL, `originalLayoutString` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `puzzleId` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3f68c428e487a338d75f3265a5ba3665')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `einfach`")
        connection.execSQL("DROP TABLE IF EXISTS `mittel`")
        connection.execSQL("DROP TABLE IF EXISTS `schwer`")
        connection.execSQL("DROP TABLE IF EXISTS `game_cache`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsEinfach: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsEinfach.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEinfach.put("unsolvedString", TableInfo.Column("unsolvedString", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEinfach.put("layoutString", TableInfo.Column("layoutString", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEinfach.put("solutionString", TableInfo.Column("solutionString", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEinfach.put("alreadySolved", TableInfo.Column("alreadySolved", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysEinfach: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesEinfach: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoEinfach: TableInfo = TableInfo("einfach", _columnsEinfach, _foreignKeysEinfach,
            _indicesEinfach)
        val _existingEinfach: TableInfo = read(connection, "einfach")
        if (!_infoEinfach.equals(_existingEinfach)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |einfach(com.example.num8rix.database.entity.Einfach).
              | Expected:
              |""".trimMargin() + _infoEinfach + """
              |
              | Found:
              |""".trimMargin() + _existingEinfach)
        }
        val _columnsMittel: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMittel.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMittel.put("unsolvedString", TableInfo.Column("unsolvedString", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMittel.put("layoutString", TableInfo.Column("layoutString", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMittel.put("solutionString", TableInfo.Column("solutionString", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMittel.put("alreadySolved", TableInfo.Column("alreadySolved", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMittel: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMittel: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMittel: TableInfo = TableInfo("mittel", _columnsMittel, _foreignKeysMittel,
            _indicesMittel)
        val _existingMittel: TableInfo = read(connection, "mittel")
        if (!_infoMittel.equals(_existingMittel)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |mittel(com.example.num8rix.database.entity.Mittel).
              | Expected:
              |""".trimMargin() + _infoMittel + """
              |
              | Found:
              |""".trimMargin() + _existingMittel)
        }
        val _columnsSchwer: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSchwer.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSchwer.put("unsolvedString", TableInfo.Column("unsolvedString", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSchwer.put("layoutString", TableInfo.Column("layoutString", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSchwer.put("solutionString", TableInfo.Column("solutionString", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSchwer.put("alreadySolved", TableInfo.Column("alreadySolved", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSchwer: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSchwer: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSchwer: TableInfo = TableInfo("schwer", _columnsSchwer, _foreignKeysSchwer,
            _indicesSchwer)
        val _existingSchwer: TableInfo = read(connection, "schwer")
        if (!_infoSchwer.equals(_existingSchwer)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |schwer(com.example.num8rix.database.entity.Schwer).
              | Expected:
              |""".trimMargin() + _infoSchwer + """
              |
              | Found:
              |""".trimMargin() + _existingSchwer)
        }
        val _columnsGameCache: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGameCache.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGameCache.put("currentGridString", TableInfo.Column("currentGridString", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameCache.put("notesGridString", TableInfo.Column("notesGridString", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameCache.put("originalGridString", TableInfo.Column("originalGridString", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameCache.put("originalLayoutString", TableInfo.Column("originalLayoutString",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameCache.put("difficulty", TableInfo.Column("difficulty", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGameCache.put("puzzleId", TableInfo.Column("puzzleId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGameCache: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGameCache: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoGameCache: TableInfo = TableInfo("game_cache", _columnsGameCache,
            _foreignKeysGameCache, _indicesGameCache)
        val _existingGameCache: TableInfo = read(connection, "game_cache")
        if (!_infoGameCache.equals(_existingGameCache)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |game_cache(com.example.num8rix.database.entity.GameCache).
              | Expected:
              |""".trimMargin() + _infoGameCache + """
              |
              | Found:
              |""".trimMargin() + _existingGameCache)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "einfach", "mittel", "schwer",
        "game_cache")
  }

  public override fun clearAllTables() {
    super.performClear(false, "einfach", "mittel", "schwer", "game_cache")
  }

  protected override fun getRequiredTypeConverterClasses():
      Map<KClass<out Any>, List<KClass<out Any>>> {
    val _typeConvertersMap: MutableMap<KClass<out Any>, List<KClass<out Any>>> = mutableMapOf()
    _typeConvertersMap.put(EinfachDao::class, EinfachDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MittelDao::class, MittelDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SchwerDao::class, SchwerDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(GameCacheDao::class, GameCacheDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun einfachDao(): EinfachDao = _einfachDao.value

  public override fun mittelDao(): MittelDao = _mittelDao.value

  public override fun schwerDao(): SchwerDao = _schwerDao.value

  public override fun gameCacheDao(): GameCacheDao = _gameCacheDao.value
}
