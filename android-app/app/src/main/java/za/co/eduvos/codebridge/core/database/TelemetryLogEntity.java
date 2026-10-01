package za.co.eduvos.codebridge.core.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "telemetry_logs")
public class TelemetryLogEntity {
    @PrimaryKey(autoGenerate = true)
    public int logId;

    public int sessionId;
    public int executionSpeedMs;
    public float correctnessScore;
    public int gitErrorCount;
    public float speechKeywordDensity;
    public int aiAccessAttempts;
}