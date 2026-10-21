package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object for stage 3 attention metrics.
 * Note: Ensure these snake_case serialized names match the backend contract before syncing.
 */
public class VideoTelemetryDto {

    @SerializedName("session_id")
    private int sessionId;

    @SerializedName("duration_ms")
    private long totalDurationMs;

    @SerializedName("attention_ratio")
    private float focusRatio;

    @SerializedName("looking_away_ms")
    private long distractedMs;

    @SerializedName("eyes_closed_ms")
    private long closedEyesMs;

    @SerializedName("no_face_ms")
    private long missingFaceMs;

    @SerializedName("multiple_faces_ms")
    private long multipleFacesMs;

    @SerializedName("look_away_episodes")
    private int distractedCount;

    @SerializedName("no_face_episodes")
    private int missingFaceCount;

    @SerializedName("multiple_face_episodes")
    private int multipleFacesCount;

    @SerializedName("longest_look_away_ms")
    private long maxDistractionTimeMs;

    public VideoTelemetryDto() {}

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public long getTotalDurationMs() { return totalDurationMs; }
    public void setTotalDurationMs(long totalDurationMs) { this.totalDurationMs = totalDurationMs; }

    public float getFocusRatio() { return focusRatio; }
    public void setFocusRatio(float focusRatio) { this.focusRatio = focusRatio; }

    public long getDistractedMs() { return distractedMs; }
    public void setDistractedMs(long distractedMs) { this.distractedMs = distractedMs; }

    public long getClosedEyesMs() { return closedEyesMs; }
    public void setClosedEyesMs(long closedEyesMs) { this.closedEyesMs = closedEyesMs; }

    public long getMissingFaceMs() { return missingFaceMs; }
    public void setMissingFaceMs(long missingFaceMs) { this.missingFaceMs = missingFaceMs; }

    public long getMultipleFacesMs() { return multipleFacesMs; }
    public void setMultipleFacesMs(long multipleFacesMs) { this.multipleFacesMs = multipleFacesMs; }

    public int getDistractedCount() { return distractedCount; }
    public void setDistractedCount(int distractedCount) { this.distractedCount = distractedCount; }

    public int getMissingFaceCount() { return missingFaceCount; }
    public void setMissingFaceCount(int missingFaceCount) { this.missingFaceCount = missingFaceCount; }

    public int getMultipleFacesCount() { return multipleFacesCount; }
    public void setMultipleFacesCount(int multipleFacesCount) { this.multipleFacesCount = multipleFacesCount; }

    public long getMaxDistractionTimeMs() { return maxDistractionTimeMs; }
    public void setMaxDistractionTimeMs(long maxDistractionTimeMs) { this.maxDistractionTimeMs = maxDistractionTimeMs; }
}