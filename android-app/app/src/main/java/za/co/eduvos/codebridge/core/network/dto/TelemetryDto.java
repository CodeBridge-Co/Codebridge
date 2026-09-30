package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class TelemetryDto {
    @SerializedName("log_id")
    private Integer logId;
    @SerializedName("session_id")
    private int sessionId;
    @SerializedName("execution_speed_ms")
    private int executionSpeedMs;
    @SerializedName("correctness_score")
    private float correctnessScore;
    @SerializedName("git_error_count")
    private int gitErrorCount;
    @SerializedName("speech_keyword_density")
    private float speechKeywordDensity;
    @SerializedName("ai_access_attempts")
    private int aiAccessAttempts;

    // Getters and Setters
    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public int getExecutionSpeedMs() { return executionSpeedMs; }
    public void setExecutionSpeedMs(int executionSpeedMs) { this.executionSpeedMs = executionSpeedMs; }
    public float getCorrectnessScore() { return correctnessScore; }
    public void setCorrectnessScore(float correctnessScore) { this.correctnessScore = correctnessScore; }
    public int getGitErrorCount() { return gitErrorCount; }
    public void setGitErrorCount(int gitErrorCount) { this.gitErrorCount = gitErrorCount; }
    public float getSpeechKeywordDensity() { return speechKeywordDensity; }
    public void setSpeechKeywordDensity(float speechKeywordDensity) { this.speechKeywordDensity = speechKeywordDensity; }
    public int getAiAccessAttempts() { return aiAccessAttempts; }
    public void setAiAccessAttempts(int aiAccessAttempts) { this.aiAccessAttempts = aiAccessAttempts; }
}
