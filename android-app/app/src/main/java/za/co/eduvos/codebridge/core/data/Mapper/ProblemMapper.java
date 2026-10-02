package za.co.eduvos.codebridge.core.data.mapper;

import za.co.eduvos.codebridge.core.database.ProblemEntity;
import za.co.eduvos.codebridge.core.network.dto.ProblemDto;

public class ProblemMapper {

    public static ProblemEntity toEntity(ProblemDto dto) {
        ProblemEntity entity = new ProblemEntity();
        entity.setProblemId(dto.getProblemId());
        entity.setTitle(dto.getTitle());
        entity.setDifficultyLevel(dto.getDifficultyLevel());
        entity.setTestCasesJson(dto.getTestCasesJson());
        return entity;
    }

    public static ProblemDto toDto(ProblemEntity entity) {
        ProblemDto dto = new ProblemDto();
        dto.setProblemId(entity.getProblemId());
        dto.setTitle(entity.getTitle());
        dto.setDifficultyLevel(entity.getDifficultyLevel());
        dto.setTestCasesJson(entity.getTestCasesJson());
        return dto;
    }
}
