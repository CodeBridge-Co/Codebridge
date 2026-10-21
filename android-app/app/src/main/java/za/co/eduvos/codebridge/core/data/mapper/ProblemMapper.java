package za.co.eduvos.codebridge.core.data.mapper;

import za.co.eduvos.codebridge.core.database.ProblemEntity;
import za.co.eduvos.codebridge.core.network.dto.ProblemDto;

public class ProblemMapper {

    public static ProblemEntity toEntity(ProblemDto dto) {
        ProblemEntity entity = new ProblemEntity();
        entity.problemId = dto.getProblemId();
        entity.title = dto.getTitle();
        entity.difficultyLevel = dto.getDifficultyLevel();
        entity.testCasesJson = dto.getTestCasesJson();
        return entity;
    }

    public static ProblemDto toDto(ProblemEntity entity) {
        ProblemDto dto = new ProblemDto();
        dto.setProblemId(entity.problemId);
        dto.setTitle(entity.title);
        dto.setDifficultyLevel(entity.difficultyLevel);
        dto.setTestCasesJson(entity.testCasesJson);
        return dto;
    }
}
