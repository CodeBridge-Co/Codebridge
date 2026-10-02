package za.co.eduvos.codebridge.core.data.mapper;

import za.co.eduvos.codebridge.core.database.TelemetryLogEntity;
import za.co.eduvos.codebridge.core.network.dto.TelemetryDto;

public class TelemetryMapper {

    public static TelemetryLogEntity toEntity(TelemetryDto dto) {
        TelemetryLogEntity entity = new TelemetryLogEntity();
        entity.sessionId = String.valueOf(dto.getSessionId());
        entity.executionSpeedMs = dto.getExecutionSpeedMs();
        entity.correctnessScore = dto.getCorrectnessScore();
        entity.gitErrorCount = dto.getGitErrorCount();
        entity.speechKeywordDensity = dto.getSpeechKeywordDensity();
        entity.aiAccessAttempts = dto.getAiAccessAttempts();
        return entity;
    }
}
