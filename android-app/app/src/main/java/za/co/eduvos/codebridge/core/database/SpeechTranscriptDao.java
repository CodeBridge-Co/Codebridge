package za.co.eduvos.codebridge.core.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface SpeechTranscriptDao {
    @Insert
    void insert(SpeechTranscriptEntity transcript);

    @Query("SELECT * FROM speech_transcripts WHERE sessionId = :sessionId")
    List<SpeechTranscriptEntity> getBySession(int sessionId);
}