package za.co.eduvos.codebridge.core.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
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
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RewardDao_Impl implements RewardDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RewardProgressEntity> __insertionAdapterOfRewardProgressEntity;

  private final EntityDeletionOrUpdateAdapter<RewardProgressEntity> __updateAdapterOfRewardProgressEntity;

  public RewardDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRewardProgressEntity = new EntityInsertionAdapter<RewardProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `reward_progress` (`rewardId`,`studentHashId`,`streakCount`,`xpPoints`,`badgesJson`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final RewardProgressEntity entity) {
        statement.bindLong(1, entity.rewardId);
        if (entity.studentHashId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.studentHashId);
        }
        statement.bindLong(3, entity.streakCount);
        statement.bindLong(4, entity.xpPoints);
        if (entity.badgesJson == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.badgesJson);
        }
      }
    };
    this.__updateAdapterOfRewardProgressEntity = new EntityDeletionOrUpdateAdapter<RewardProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reward_progress` SET `rewardId` = ?,`studentHashId` = ?,`streakCount` = ?,`xpPoints` = ?,`badgesJson` = ? WHERE `rewardId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final RewardProgressEntity entity) {
        statement.bindLong(1, entity.rewardId);
        if (entity.studentHashId == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.studentHashId);
        }
        statement.bindLong(3, entity.streakCount);
        statement.bindLong(4, entity.xpPoints);
        if (entity.badgesJson == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.badgesJson);
        }
        statement.bindLong(6, entity.rewardId);
      }
    };
  }

  @Override
  public void insert(final RewardProgressEntity reward) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfRewardProgressEntity.insert(reward);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final RewardProgressEntity reward) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfRewardProgressEntity.handle(reward);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public RewardProgressEntity getByStudent(final String hashId) {
    final String _sql = "SELECT * FROM reward_progress WHERE studentHashId = ? LIMIT 1";
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
      final int _cursorIndexOfRewardId = CursorUtil.getColumnIndexOrThrow(_cursor, "rewardId");
      final int _cursorIndexOfStudentHashId = CursorUtil.getColumnIndexOrThrow(_cursor, "studentHashId");
      final int _cursorIndexOfStreakCount = CursorUtil.getColumnIndexOrThrow(_cursor, "streakCount");
      final int _cursorIndexOfXpPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "xpPoints");
      final int _cursorIndexOfBadgesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "badgesJson");
      final RewardProgressEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new RewardProgressEntity();
        _result.rewardId = _cursor.getInt(_cursorIndexOfRewardId);
        if (_cursor.isNull(_cursorIndexOfStudentHashId)) {
          _result.studentHashId = null;
        } else {
          _result.studentHashId = _cursor.getString(_cursorIndexOfStudentHashId);
        }
        _result.streakCount = _cursor.getInt(_cursorIndexOfStreakCount);
        _result.xpPoints = _cursor.getInt(_cursorIndexOfXpPoints);
        if (_cursor.isNull(_cursorIndexOfBadgesJson)) {
          _result.badgesJson = null;
        } else {
          _result.badgesJson = _cursor.getString(_cursorIndexOfBadgesJson);
        }
      } else {
        _result = null;
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
