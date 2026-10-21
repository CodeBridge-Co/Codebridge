package za.co.eduvos.codebridge.core.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

import za.co.eduvos.codebridge.core.data.mapper.VideoTelemetryMapper;
import za.co.eduvos.codebridge.core.network.dto.VideoTelemetryDto;

public class VideoTelemetryMapperTest {

    private static AttentionTracker.Summary generateMockSummary() {
        AttentionTracker monitor = new AttentionTracker();
        monitor.onSample(FaceSample.createTracked(0, 0.0f, 0.0f, 0.0f, 0.9f, 0.9f));
        monitor.onSample(FaceSample.createTracked(1000, 40.0f, 0.0f, 0.0f, 0.9f, 0.9f));
        monitor.onSample(FaceSample.createTracked(2000, 40.0f, 0.0f, 0.0f, 0.9f, 0.9f));
        monitor.onSample(FaceSample.createTracked(3000, 0.0f, 0.0f, 0.0f, 0.9f, 0.9f));
        monitor.onSample(FaceSample.createTracked(4000, 0.0f, 0.0f, 0.0f, 0.9f, 0.9f));
        return monitor.summary();
    }

    @Test
    public void validatesDtoMapping() {
        AttentionTracker.Summary result = generateMockSummary();
        VideoTelemetryDto payload = VideoTelemetryMapper.mapToDto(7, result);

        assertEquals(7, payload.getSessionId());
        assertEquals(result.totalDurationMs, payload.getTotalDurationMs());
        assertEquals(result.focusRatio, payload.getFocusRatio(), 0.0f);
        assertEquals(result.distractedMs, payload.getDistractedMs());
        assertEquals(result.distractedCount, payload.getDistractedCount());
        assertEquals(result.maxDistractionTimeMs, payload.getMaxDistractionTimeMs());

        assertEquals(4000, payload.getTotalDurationMs());
        assertEquals(1, payload.getDistractedCount());
    }

    @Test
    public void verifiesSnakeCaseSerialization() {
        VideoTelemetryDto payload = VideoTelemetryMapper.mapToDto(3, generateMockSummary());
        JsonObject jsonOutput = new Gson().toJsonTree(payload).getAsJsonObject();

        String[] requiredKeys = {
                "session_id", "duration_ms", "attention_ratio", "looking_away_ms", "eyes_closed_ms",
                "no_face_ms", "multiple_faces_ms", "look_away_episodes", "no_face_episodes",
                "multiple_face_episodes", "longest_look_away_ms"
        };

        for (String key : requiredKeys) {
            assertTrue("JSON payload is missing required key: " + key, jsonOutput.has(key));
        }

        assertEquals(requiredKeys.length, jsonOutput.size());
        assertEquals(3, jsonOutput.get("session_id").getAsInt());
    }
}