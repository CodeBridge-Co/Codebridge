package za.co.eduvos.codebridge.core.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
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
public final class SyncQueueDao_Impl implements SyncQueueDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SyncQueueEntity> __insertionAdapterOfSyncQueueEntity;

  private final EntityDeletionOrUpdateAdapter<SyncQueueEntity> __updateAdapterOfSyncQueueEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  private final SharedSQLiteStatement __preparedStmtOfIncrementRetry;

  public SyncQueueDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSyncQueueEntity = new EntityInsertionAdapter<SyncQueueEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sync_queue` (`id`,`entityType`,`payloadJson`,`createdAt`,`retryCount`,`status`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SyncQueueEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getEntityType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getEntityType());
        }
        if (entity.getPayloadJson() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPayloadJson());
        }
        statement.bindLong(4, entity.getCreatedAt());
        statement.bindLong(5, entity.getRetryCount());
        if (entity.getStatus() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStatus());
        }
      }
    };
    this.__updateAdapterOfSyncQueueEntity = new EntityDeletionOrUpdateAdapter<SyncQueueEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sync_queue` SET `id` = ?,`entityType` = ?,`payloadJson` = ?,`createdAt` = ?,`retryCount` = ?,`status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SyncQueueEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getEntityType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getEntityType());
        }
        if (entity.getPayloadJson() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPayloadJson());
        }
        statement.bindLong(4, entity.getCreatedAt());
        statement.bindLong(5, entity.getRetryCount());
        if (entity.getStatus() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStatus());
        }
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sync_queue SET status = 'SYNCED' WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementRetry = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE sync_queue SET retryCount = retryCount + 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public long insert(final SyncQueueEntity entity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfSyncQueueEntity.insertAndReturnId(entity);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final SyncQueueEntity entity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfSyncQueueEntity.handle(entity);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void markSynced(final int id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSynced.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfMarkSynced.release(_stmt);
    }
  }

  @Override
  public void incrementRetry(final int id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementRetry.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfIncrementRetry.release(_stmt);
    }
  }

  @Override
  public List<SyncQueueEntity> fetchPending() {
    final String _sql = "SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfEntityType = CursorUtil.getColumnIndexOrThrow(_cursor, "entityType");
      final int _cursorIndexOfPayloadJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payloadJson");
      final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
      final int _cursorIndexOfRetryCount = CursorUtil.getColumnIndexOrThrow(_cursor, "retryCount");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final List<SyncQueueEntity> _result = new ArrayList<SyncQueueEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final SyncQueueEntity _item;
        final String _tmpEntityType;
        if (_cursor.isNull(_cursorIndexOfEntityType)) {
          _tmpEntityType = null;
        } else {
          _tmpEntityType = _cursor.getString(_cursorIndexOfEntityType);
        }
        final String _tmpPayloadJson;
        if (_cursor.isNull(_cursorIndexOfPayloadJson)) {
          _tmpPayloadJson = null;
        } else {
          _tmpPayloadJson = _cursor.getString(_cursorIndexOfPayloadJson);
        }
        final long _tmpCreatedAt;
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
        _item = new SyncQueueEntity(_tmpEntityType,_tmpPayloadJson,_tmpCreatedAt);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final int _tmpRetryCount;
        _tmpRetryCount = _cursor.getInt(_cursorIndexOfRetryCount);
        _item.setRetryCount(_tmpRetryCount);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
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
