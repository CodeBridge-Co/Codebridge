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
public final class SpeechTranscriptDao_Impl implements SpeechTranscriptDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SpeechTranscriptEntity> __insertionAdapterOfSpeechTranscriptEntity;

  public SpeechTranscriptDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSpeechTranscriptEntity = new EntityInsertionAdapter<SpeechTranscriptEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `speech_transcripts` (`id`,`sessionId`,`transcriptText`,`keywordDensity`,`durationMs`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SpeechTranscriptEntity entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.sessionId);
        if (entity.transcriptText == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.transcriptText);
        }
        statement.bindDouble(4, entity.keywordDensity);
        statement.bindLong(5, entity.durationMs);
        statement.bindLong(6, entity.timestamp);
      }
    };
  }

  @Override
  public void insert(final SpeechTranscriptEntity transcript) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSpeechTranscriptEntity.insert(transcript);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<SpeechTranscriptEntity> getBySession(final int sessionId) {
    final String _sql = "SELECT * FROM speech_transcripts WHERE sessionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sessionId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfTranscriptText = CursorUtil.getColumnIndexOrThrow(_cursor, "transcriptText");
      final int _cursorIndexOfKeywordDensity = CursorUtil.getColumnIndexOrThrow(_cursor, "keywordDensity");
      final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<SpeechTranscriptEntity> _result = new ArrayList<SpeechTranscriptEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final SpeechTranscriptEntity _item;
        _item = new SpeechTranscriptEntity();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.sessionId = _cursor.getInt(_cursorIndexOfSessionId);
        if (_cursor.isNull(_cursorIndexOfTranscriptText)) {
          _item.transcriptText = null;
        } else {
          _item.transcriptText = _cursor.getString(_cursorIndexOfTranscriptText);
        }
        _item.keywordDensity = _cursor.getFloat(_cursorIndexOfKeywordDensity);
        _item.durationMs = _cursor.getLong(_cursorIndexOfDurationMs);
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
