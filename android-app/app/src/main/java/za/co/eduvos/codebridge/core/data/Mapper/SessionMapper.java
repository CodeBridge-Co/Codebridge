package za.co.eduvos.codebridge.core.data.mapper;

import za.co.eduvos.codebridge.core.database.AssessmentSessionEntity;
import za.co.eduvos.codebridge.core.network.dto.SessionDto;

public class SessionMapper {

    public static AssessmentSessionEntity toEntity(SessionDto dto) {
        AssessmentSessionEntity entity = new AssessmentSessionEntity();
        if (dto.getSessionId() != null) {
            entity.setSessionId(String.valueOf(dto.getSessionId()));
        }
        entity.setStudentHashId(dto.getStudentHashId());
        entity.setProblemId(dto.getProblemId());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setOffline(dto.isOffline());
        return entity;
    }
}
