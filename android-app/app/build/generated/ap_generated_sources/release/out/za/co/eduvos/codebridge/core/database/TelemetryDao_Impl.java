package za.co.eduvos.codebridge.core.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TelemetryDao_Impl implements TelemetryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TelemetryLogEntity> __insertionAdapterOfTelemetryLogEntity;

  public TelemetryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTelemetryLogEntity = new EntityInsertionAdapter<TelemetryLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `telemetry_logs` (`logId`,`sessionId`,`executionSpeedMs`,`correctnessScore`,`gitErrorCount`,`speechKeywordDensity`,`aiAccessAttempts`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final TelemetryLogEntity entity) {
        statement.bindLong(1, entity.logId);
        statement.bindLong(2, entity.sessionId);
        statement.bindLong(3, entity.executionSpeedMs);
        statement.bindDouble(4, entity.correctnessScore);
        statement.bindLong(5, entity.gitErrorCount);
        statement.bindDouble(6, entity.speechKeywordDensity);
        statement.bindLong(7, entity.aiAccessAttempts);
      }
    };
  }

  @Override
  public void insert(final TelemetryLogEntity log) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfTelemetryLogEntity.insert(log);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<TelemetryLogEntity> getBySession(final int sessionId) {
    final String _sql = "SELECT * FROM telemetry_logs WHERE sessionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfLogId = CursorUtil.getColumnIndexOrThrow(_cursor, "logId");
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfExecutionSpeedMs = CursorUtil.getColumnIndexOrThrow(_cursor, "executionSpeedMs");
      final int _cursorIndexOfCorrectnessScore = CursorUtil.getColumnIndexOrThrow(_cursor, "correctnessScore");
      final int _cursorIndexOfGitErrorCount = CursorUtil.getColumnIndexOrThrow(_cursor, "gitErrorCount");
      final int _cursorIndexOfSpeechKeywordDensity = CursorUtil.getColumnIndexOrThrow(_cursor, "speechKeywordDensity");
      final int _cursorIndexOfAiAccessAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "aiAccessAttempts");
      final List<TelemetryLogEntity> _result = new ArrayList<TelemetryLogEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final TelemetryLogEntity _item;
        _item = new TelemetryLogEntity();
        _item.logId = _cursor.getInt(_cursorIndexOfLogId);
        _item.sessionId = _cursor.getInt(_cursorIndexOfSessionId);
        _item.executionSpeedMs = _cursor.getInt(_cursorIndexOfExecutionSpeedMs);
        _item.correctnessScore = _cursor.getFloat(_cursorIndexOfCorrectnessScore);
        _item.gitErrorCount = _cursor.getInt(_cursorIndexOfGitErrorCount);
        _item.speechKeywordDensity = _cursor.getFloat(_cursorIndexOfSpeechKeywordDensity);
        _item.aiAccessAttempts = _cursor.getInt(_cursorIndexOfAiAccessAttempts);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
