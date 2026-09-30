package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SyncPayload {
    @SerializedName("sessions")
    private List<SessionDto> sessions;
    @SerializedName("telemetry_logs")
    private List<TelemetryDto> telemetryLogs;

    public SyncPayload(List<SessionDto> sessions, List<TelemetryDto> telemetryLogs) {
        this.sessions = sessions;
        this.telemetryLogs = telemetryLogs;
    }
    // Getters and Setters
    public List<SessionDto> getSessions() { return sessions; }
    public void setSessions(List<SessionDto> sessions) { this.sessions = sessions; }
    public List<TelemetryDto> getTelemetryLogs() { return telemetryLogs; }
    public void setTelemetryLogs(List<TelemetryDto> telemetryLogs) { this.telemetryLogs = telemetryLogs; }
}
