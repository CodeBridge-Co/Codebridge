package za.co.eduvos.codebridge.core.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "speech_transcripts")
public class SpeechTranscriptEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;
    public String transcriptText;
    public float keywordDensity;
    public long durationMs;
    public long timestamp;
}