package za.co.eduvos.codebridge.core.data.mapper;

import za.co.eduvos.codebridge.core.network.dto.VideoTelemetryDto;
import za.co.eduvos.codebridge.core.vision.AttentionTracker;

/**
 * Maps internal tracking summaries to network-ready Data Transfer Objects.
 */
public class VideoTelemetryMapper {

    private VideoTelemetryMapper() {
        // Prevent instantiation of utility class
    }

    public static VideoTelemetryDto mapToDto(int currentSessionId, AttentionTracker.Summary sessionSummary) {
        VideoTelemetryDto payload = new VideoTelemetryDto();
        payload.setSessionId(currentSessionId);
        payload.setTotalDurationMs(sessionSummary.totalDurationMs);
        payload.setFocusRatio(sessionSummary.focusRatio);
        payload.setDistractedMs(sessionSummary.distractedMs);
        payload.setClosedEyesMs(sessionSummary.closedEyesMs);
        payload.setMissingFaceMs(sessionSummary.missingFaceMs);
        payload.setMultipleFacesMs(sessionSummary.multipleFacesMs);
        payload.setDistractedCount(sessionSummary.distractedCount);
        payload.setMissingFaceCount(sessionSummary.missingFaceCount);
        payload.setMultipleFacesCount(sessionSummary.multipleFacesCount);
        payload.setMaxDistractionTimeMs(sessionSummary.maxDistractionTimeMs);

        return payload;
    }
}