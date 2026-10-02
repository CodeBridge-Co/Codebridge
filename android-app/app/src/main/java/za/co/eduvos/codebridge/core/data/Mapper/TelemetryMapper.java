package za.co.eduvos.codebridge.core.data.mapper;

import za.co.eduvos.codebridge.core.database.TelemetryLogEntity;
import za.co.eduvos.codebridge.core.network.dto.TelemetryDto;

public class TelemetryMapper {

    public static TelemetryLogEntity toEntity(TelemetryDto dto) {
        TelemetryLogEntity entity = new TelemetryLogEntity();
        entity.setSessionId(String.valueOf(dto.getSessionId()));
        entity.setExecutionSpeedMs(dto.getExecutionSpeedMs());
        entity.setCorrectnessScore(dto.getCorrectnessScore());
        entity.setGitErrorCount(dto.getGitErrorCount());
        entity.setSpeechKeywordDensity(dto.getSpeechKeywordDensity());
        entity.setAiAccessAttempts(dto.getAiAccessAttempts());
        return entity;
    }
}
