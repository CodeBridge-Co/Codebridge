package za.co.eduvos.codebridge.core.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TelemetryDao {
    @Insert
    void insert(TelemetryLogEntity log);

    @Query("SELECT * FROM telemetry_logs WHERE sessionId = :sessionId")
    List<TelemetryLogEntity> getBySession(int sessionId);
}