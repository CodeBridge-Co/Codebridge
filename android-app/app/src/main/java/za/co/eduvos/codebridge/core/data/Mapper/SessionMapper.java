package za.co.eduvos.codebridge.core.data.mapper;

import za.co.eduvos.codebridge.core.database.AssessmentSessionEntity;
import za.co.eduvos.codebridge.core.network.dto.SessionDto;

public class SessionMapper {

    public static AssessmentSessionEntity toEntity(SessionDto dto) {
        AssessmentSessionEntity entity = new AssessmentSessionEntity();
        if (dto.getSessionId() != null) {
            entity.sessionId = String.valueOf(dto.getSessionId());
        }
        entity.studentHashId = dto.getStudentHashId();
        entity.problemId = dto.getProblemId();
        entity.startTime = dto.getStartTime();
        entity.endTime = dto.getEndTime();
        entity.isOffline = dto.isOffline();
        return entity;
    }
}
