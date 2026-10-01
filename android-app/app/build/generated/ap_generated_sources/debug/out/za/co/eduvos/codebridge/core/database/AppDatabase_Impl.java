package za.co.eduvos.codebridge.core.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ProblemDao _problemDao;

  private volatile SessionDao _sessionDao;

  private volatile TelemetryDao _telemetryDao;

  private volatile RewardDao _rewardDao;

  private volatile GitCommandDao _gitCommandDao;

  private volatile SpeechTranscriptDao _speechTranscriptDao;

  private volatile SyncQueueDao _syncQueueDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `problems` (`problemId` INTEGER NOT NULL, `title` TEXT NOT NULL, `difficultyLevel` TEXT, `testCasesJson` TEXT, PRIMARY KEY(`problemId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `assessment_sessions` (`sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `studentHashId` TEXT, `problemId` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `isOffline` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `telemetry_logs` (`logId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `executionSpeedMs` INTEGER NOT NULL, `correctnessScore` REAL NOT NULL, `gitErrorCount` INTEGER NOT NULL, `speechKeywordDensity` REAL NOT NULL, `aiAccessAttempts` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reward_progress` (`rewardId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `studentHashId` TEXT, `streakCount` INTEGER NOT NULL, `xpPoints` INTEGER NOT NULL, `badgesJson` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `git_command_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `command` TEXT, `isError` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `speech_transcripts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `transcriptText` TEXT, `keywordDensity` REAL NOT NULL, `durationMs` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_queue` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entityType` TEXT, `payloadJson` TEXT, `createdAt` INTEGER NOT NULL, `retryCount` INTEGER NOT NULL, `status` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6b381b47cb03ebe8924557e9bd5ec97b')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `problems`");
        db.execSQL("DROP TABLE IF EXISTS `assessment_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `telemetry_logs`");
        db.execSQL("DROP TABLE IF EXISTS `reward_progress`");
        db.execSQL("DROP TABLE IF EXISTS `git_command_logs`");
        db.execSQL("DROP TABLE IF EXISTS `speech_transcripts`");
        db.execSQL("DROP TABLE IF EXISTS `sync_queue`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsProblems = new HashMap<String, TableInfo.Column>(4);
        _columnsProblems.put("problemId", new TableInfo.Column("problemId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProblems.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProblems.put("difficultyLevel", new TableInfo.Column("difficultyLevel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProblems.put("testCasesJson", new TableInfo.Column("testCasesJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProblems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProblems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProblems = new TableInfo("problems", _columnsProblems, _foreignKeysProblems, _indicesProblems);
        final TableInfo _existingProblems = TableInfo.read(db, "problems");
        if (!_infoProblems.equals(_existingProblems)) {
          return new RoomOpenHelper.ValidationResult(false, "problems(za.co.eduvos.codebridge.core.database.ProblemEntity).\n"
                  + " Expected:\n" + _infoProblems + "\n"
                  + " Found:\n" + _existingProblems);
        }
        final HashMap<String, TableInfo.Column> _columnsAssessmentSessions = new HashMap<String, TableInfo.Column>(6);
        _columnsAssessmentSessions.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAssessmentSessions.put("studentHashId", new TableInfo.Column("studentHashId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAssessmentSessions.put("problemId", new TableInfo.Column("problemId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAssessmentSessions.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAssessmentSessions.put("endTime", new TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAssessmentSessions.put("isOffline", new TableInfo.Column("isOffline", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAssessmentSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAssessmentSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAssessmentSessions = new TableInfo("assessment_sessions", _columnsAssessmentSessions, _foreignKeysAssessmentSessions, _indicesAssessmentSessions);
        final TableInfo _existingAssessmentSessions = TableInfo.read(db, "assessment_sessions");
        if (!_infoAssessmentSessions.equals(_existingAssessmentSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "assessment_sessions(za.co.eduvos.codebridge.core.database.AssessmentSessionEntity).\n"
                  + " Expected:\n" + _infoAssessmentSessions + "\n"
                  + " Found:\n" + _existingAssessmentSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsTelemetryLogs = new HashMap<String, TableInfo.Column>(7);
        _columnsTelemetryLogs.put("logId", new TableInfo.Column("logId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetryLogs.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetryLogs.put("executionSpeedMs", new TableInfo.Column("executionSpeedMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetryLogs.put("correctnessScore", new TableInfo.Column("correctnessScore", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetryLogs.put("gitErrorCount", new TableInfo.Column("gitErrorCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetryLogs.put("speechKeywordDensity", new TableInfo.Column("speechKeywordDensity", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetryLogs.put("aiAccessAttempts", new TableInfo.Column("aiAccessAttempts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTelemetryLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTelemetryLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTelemetryLogs = new TableInfo("telemetry_logs", _columnsTelemetryLogs, _foreignKeysTelemetryLogs, _indicesTelemetryLogs);
        final TableInfo _existingTelemetryLogs = TableInfo.read(db, "telemetry_logs");
        if (!_infoTelemetryLogs.equals(_existingTelemetryLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "telemetry_logs(za.co.eduvos.codebridge.core.database.TelemetryLogEntity).\n"
                  + " Expected:\n" + _infoTelemetryLogs + "\n"
                  + " Found:\n" + _existingTelemetryLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsRewardProgress = new HashMap<String, TableInfo.Column>(5);
        _columnsRewardProgress.put("rewardId", new TableInfo.Column("rewardId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardProgress.put("studentHashId", new TableInfo.Column("studentHashId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardProgress.put("streakCount", new TableInfo.Column("streakCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardProgress.put("xpPoints", new TableInfo.Column("xpPoints", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRewardProgress.put("badgesJson", new TableInfo.Column("badgesJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRewardProgress = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRewardProgress = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRewardProgress = new TableInfo("reward_progress", _columnsRewardProgress, _foreignKeysRewardProgress, _indicesRewardProgress);
        final TableInfo _existingRewardProgress = TableInfo.read(db, "reward_progress");
        if (!_infoRewardProgress.equals(_existingRewardProgress)) {
          return new RoomOpenHelper.ValidationResult(false, "reward_progress(za.co.eduvos.codebridge.core.database.RewardProgressEntity).\n"
                  + " Expected:\n" + _infoRewardProgress + "\n"
                  + " Found:\n" + _existingRewardProgress);
        }
        final HashMap<String, TableInfo.Column> _columnsGitCommandLogs = new HashMap<String, TableInfo.Column>(5);
        _columnsGitCommandLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGitCommandLogs.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGitCommandLogs.put("command", new TableInfo.Column("command", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGitCommandLogs.put("isError", new TableInfo.Column("isError", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGitCommandLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGitCommandLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGitCommandLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGitCommandLogs = new TableInfo("git_command_logs", _columnsGitCommandLogs, _foreignKeysGitCommandLogs, _indicesGitCommandLogs);
        final TableInfo _existingGitCommandLogs = TableInfo.read(db, "git_command_logs");
        if (!_infoGitCommandLogs.equals(_existingGitCommandLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "git_command_logs(za.co.eduvos.codebridge.core.database.GitCommandLogEntity).\n"
                  + " Expected:\n" + _infoGitCommandLogs + "\n"
                  + " Found:\n" + _existingGitCommandLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsSpeechTranscripts = new HashMap<String, TableInfo.Column>(6);
        _columnsSpeechTranscripts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeechTranscripts.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeechTranscripts.put("transcriptText", new TableInfo.Column("transcriptText", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeechTranscripts.put("keywordDensity", new TableInfo.Column("keywordDensity", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeechTranscripts.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSpeechTranscripts.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSpeechTranscripts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSpeechTranscripts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSpeechTranscripts = new TableInfo("speech_transcripts", _columnsSpeechTranscripts, _foreignKeysSpeechTranscripts, _indicesSpeechTranscripts);
        final TableInfo _existingSpeechTranscripts = TableInfo.read(db, "speech_transcripts");
        if (!_infoSpeechTranscripts.equals(_existingSpeechTranscripts)) {
          return new RoomOpenHelper.ValidationResult(false, "speech_transcripts(za.co.eduvos.codebridge.core.database.SpeechTranscriptEntity).\n"
                  + " Expected:\n" + _infoSpeechTranscripts + "\n"
                  + " Found:\n" + _existingSpeechTranscripts);
        }
        final HashMap<String, TableInfo.Column> _columnsSyncQueue = new HashMap<String, TableInfo.Column>(6);
        _columnsSyncQueue.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("entityType", new TableInfo.Column("entityType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("payloadJson", new TableInfo.Column("payloadJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("retryCount", new TableInfo.Column("retryCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncQueue.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSyncQueue = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSyncQueue = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSyncQueue = new TableInfo("sync_queue", _columnsSyncQueue, _foreignKeysSyncQueue, _indicesSyncQueue);
        final TableInfo _existingSyncQueue = TableInfo.read(db, "sync_queue");
        if (!_infoSyncQueue.equals(_existingSyncQueue)) {
          return new RoomOpenHelper.ValidationResult(false, "sync_queue(za.co.eduvos.codebridge.core.database.SyncQueueEntity).\n"
                  + " Expected:\n" + _infoSyncQueue + "\n"
                  + " Found:\n" + _existingSyncQueue);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6b381b47cb03ebe8924557e9bd5ec97b", "de0326e62182c074c510fdc05f9ee7c1");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "problems","assessment_sessions","telemetry_logs","reward_progress","git_command_logs","speech_transcripts","sync_queue");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `problems`");
      _db.execSQL("DELETE FROM `assessment_sessions`");
      _db.execSQL("DELETE FROM `telemetry_logs`");
      _db.execSQL("DELETE FROM `reward_progress`");
      _db.execSQL("DELETE FROM `git_command_logs`");
      _db.execSQL("DELETE FROM `speech_transcripts`");
      _db.execSQL("DELETE FROM `sync_queue`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ProblemDao.class, ProblemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SessionDao.class, SessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TelemetryDao.class, TelemetryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RewardDao.class, RewardDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GitCommandDao.class, GitCommandDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SpeechTranscriptDao.class, SpeechTranscriptDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SyncQueueDao.class, SyncQueueDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ProblemDao problemDao() {
    if (_problemDao != null) {
      return _problemDao;
    } else {
      synchronized(this) {
        if(_problemDao == null) {
          _problemDao = new ProblemDao_Impl(this);
        }
        return _problemDao;
      }
    }
  }

  @Override
  public SessionDao sessionDao() {
    if (_sessionDao != null) {
      return _sessionDao;
    } else {
      synchronized(this) {
        if(_sessionDao == null) {
          _sessionDao = new SessionDao_Impl(this);
        }
        return _sessionDao;
      }
    }
  }

  @Override
  public TelemetryDao telemetryDao() {
    if (_telemetryDao != null) {
      return _telemetryDao;
    } else {
      synchronized(this) {
        if(_telemetryDao == null) {
          _telemetryDao = new TelemetryDao_Impl(this);
        }
        return _telemetryDao;
      }
    }
  }

  @Override
  public RewardDao rewardDao() {
    if (_rewardDao != null) {
      return _rewardDao;
    } else {
      synchronized(this) {
        if(_rewardDao == null) {
          _rewardDao = new RewardDao_Impl(this);
        }
        return _rewardDao;
      }
    }
  }

  @Override
  public GitCommandDao gitCommandDao() {
    if (_gitCommandDao != null) {
      return _gitCommandDao;
    } else {
      synchronized(this) {
        if(_gitCommandDao == null) {
          _gitCommandDao = new GitCommandDao_Impl(this);
        }
        return _gitCommandDao;
      }
    }
  }

  @Override
  public SpeechTranscriptDao speechTranscriptDao() {
    if (_speechTranscriptDao != null) {
      return _speechTranscriptDao;
    } else {
      synchronized(this) {
        if(_speechTranscriptDao == null) {
          _speechTranscriptDao = new SpeechTranscriptDao_Impl(this);
        }
        return _speechTranscriptDao;
      }
    }
  }

  @Override
  public SyncQueueDao syncQueueDao() {
    if (_syncQueueDao != null) {
      return _syncQueueDao;
    } else {
      synchronized(this) {
        if(_syncQueueDao == null) {
          _syncQueueDao = new SyncQueueDao_Impl(this);
        }
        return _syncQueueDao;
      }
    }
  }
}
