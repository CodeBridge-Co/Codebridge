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
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AssessmentSessionEntity> __insertionAdapterOfAssessmentSessionEntity;

  public SessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAssessmentSessionEntity = new EntityInsertionAdapter<AssessmentSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `assessment_sessions` (`sessionId`,`studentHashId`,`problemId`,`startTime`,`endTime`,`isOffline`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final AssessmentSessionEntity entity) {
        statement.bindLong(1, entity.sessionId);
        if (entity.studentHashId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.studentHashId);
        }
        statement.bindLong(3, entity.problemId);
        statement.bindLong(4, entity.startTime);
        statement.bindLong(5, entity.endTime);
        final int _tmp = entity.isOffline ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
  }

  @Override
  public long insert(final AssessmentSessionEntity session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfAssessmentSessionEntity.insertAndReturnId(session);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public AssessmentSessionEntity getById(final int id) {
    final String _sql = "SELECT * FROM assessment_sessions WHERE sessionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfStudentHashId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentHashId");
      final int _cursorIndexOfProblemId = CursorUtil.getColumnIndexOrThrow(_cursor, "problemId");
      final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
      final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
      final int _cursorIndexOfIsOffline = CursorUtil.getColumnIndexOrThrow(_cursor, "isOffline");
      final AssessmentSessionEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new AssessmentSessionEntity();
        _result.sessionId = _cursor.getInt(_cursorIndexOfSessionId);
        if (_cursor.isNull(_cursorIndexOfStudentHashId)) {
          _result.studentHashId = null;
        } else {
          _result.studentHashId = _cursor.getString(_cursorIndexOfStudentHashId);
        }
        _result.problemId = _cursor.getInt(_cursorIndexOfProblemId);
        _result.startTime = _cursor.getLong(_cursorIndexOfStartTime);
        _result.endTime = _cursor.getLong(_cursorIndexOfEndTime);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsOffline);
        _result.isOffline = _tmp != 0;
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<AssessmentSessionEntity> getByStudent(final String hashId) {
    final String _sql = "SELECT * FROM assessment_sessions WHERE studentHashId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (hashId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, hashId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfStudentHashId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentHashId");
      final int _cursorIndexOfProblemId = CursorUtil.getColumnIndexOrThrow(_cursor, "problemId");
      final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
      final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
      final int _cursorIndexOfIsOffline = CursorUtil.getColumnIndexOrThrow(_cursor, "isOffline");
      final List<AssessmentSessionEntity> _result = new ArrayList<AssessmentSessionEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AssessmentSessionEntity _item;
        _item = new AssessmentSessionEntity();
        _item.sessionId = _cursor.getInt(_cursorIndexOfSessionId);
        if (_cursor.isNull(_cursorIndexOfStudentHashId)) {
          _item.studentHashId = null;
        } else {
          _item.studentHashId = _cursor.getString(_cursorIndexOfStudentHashId);
        }
        _item.problemId = _cursor.getInt(_cursorIndexOfProblemId);
        _item.startTime = _cursor.getLong(_cursorIndexOfStartTime);
        _item.endTime = _cursor.getLong(_cursorIndexOfEndTime);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsOffline);
        _item.isOffline = _tmp != 0;
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
