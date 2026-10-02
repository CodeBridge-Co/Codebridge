package za.co.eduvos.codebridge.core.data.mapper;

import za.co.eduvos.codebridge.core.database.AssessmentSessionEntity;
import za.co.eduvos.codebridge.core.network.dto.SessionDto;

public class SessionMapper {

    public static AssessmentSessionEntity toEntity(SessionDto dto) {
        AssessmentSessionEntity entity = new AssessmentSessionEntity();
        if (dto.getSessionId() != null) {
            entity.sessionId = dto.getSessionId() != null ? dto.getSessionId() : 0;
        }
        entity.studentHashId = dto.getStudentHashId();
        entity.problemId = dto.getProblemId();
        entity.startTime = java.time.Instant.parse(dto.getStartTime()).toEpochMilli();
        entity.endTime = java.time.Instant.parse(dto.getEndTime()).toEpochMilli();
        entity.isOffline = dto.isOffline();
        return entity;
    }
}
