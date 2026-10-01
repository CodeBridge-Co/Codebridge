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
public final class ProblemDao_Impl implements ProblemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProblemEntity> __insertionAdapterOfProblemEntity;

  public ProblemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProblemEntity = new EntityInsertionAdapter<ProblemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `problems` (`problemId`,`title`,`difficultyLevel`,`testCasesJson`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ProblemEntity entity) {
        statement.bindLong(1, entity.problemId);
        if (entity.title == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.title);
        }
        if (entity.difficultyLevel == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.difficultyLevel);
        }
        if (entity.testCasesJson == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.testCasesJson);
        }
      }
    };
  }

  @Override
  public void insert(final ProblemEntity problem) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfProblemEntity.insert(problem);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<ProblemEntity> getAll() {
    final String _sql = "SELECT * FROM problems";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProblemId = CursorUtil.getColumnIndexOrThrow(_cursor, "problemId");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
      final int _cursorIndexOfTestCasesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "testCasesJson");
      final List<ProblemEntity> _result = new ArrayList<ProblemEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ProblemEntity _item;
        _item = new ProblemEntity();
        _item.problemId = _cursor.getInt(_cursorIndexOfProblemId);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _item.title = null;
        } else {
          _item.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfDifficultyLevel)) {
          _item.difficultyLevel = null;
        } else {
          _item.difficultyLevel = _cursor.getString(_cursorIndexOfDifficultyLevel);
        }
        if (_cursor.isNull(_cursorIndexOfTestCasesJson)) {
          _item.testCasesJson = null;
        } else {
          _item.testCasesJson = _cursor.getString(_cursorIndexOfTestCasesJson);
        }
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
