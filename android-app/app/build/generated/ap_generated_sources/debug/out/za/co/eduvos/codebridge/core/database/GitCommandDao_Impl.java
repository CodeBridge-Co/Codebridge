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
public final class GitCommandDao_Impl implements GitCommandDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GitCommandLogEntity> __insertionAdapterOfGitCommandLogEntity;

  public GitCommandDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGitCommandLogEntity = new EntityInsertionAdapter<GitCommandLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `git_command_logs` (`id`,`sessionId`,`command`,`isError`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final GitCommandLogEntity entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.sessionId);
        if (entity.command == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.command);
        }
        final int _tmp = entity.isError ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.timestamp);
      }
    };
  }

  @Override
  public void insert(final GitCommandLogEntity log) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfGitCommandLogEntity.insert(log);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<GitCommandLogEntity> getBySession(final int sessionId) {
    final String _sql = "SELECT * FROM git_command_logs WHERE sessionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
      final int _cursorIndexOfIsError = CursorUtil.getColumnIndexOrThrow(_cursor, "isError");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<GitCommandLogEntity> _result = new ArrayList<GitCommandLogEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final GitCommandLogEntity _item;
        _item = new GitCommandLogEntity();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.sessionId = _cursor.getInt(_cursorIndexOfSessionId);
        if (_cursor.isNull(_cursorIndexOfCommand)) {
          _item.command = null;
        } else {
          _item.command = _cursor.getString(_cursorIndexOfCommand);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsError);
        _item.isError = _tmp != 0;
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
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
