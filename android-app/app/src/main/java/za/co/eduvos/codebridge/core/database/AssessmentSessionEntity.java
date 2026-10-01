package za.co.eduvos.codebridge.core.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "assessment_sessions")
public class AssessmentSessionEntity {
    @PrimaryKey(autoGenerate = true)
    public int sessionId;

    public String studentHashId;
    public int problemId;
    public long startTime;
    public long endTime;
    public boolean isOffline;
}