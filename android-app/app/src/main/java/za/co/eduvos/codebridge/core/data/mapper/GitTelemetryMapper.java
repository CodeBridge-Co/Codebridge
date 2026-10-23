package za.co.eduvos.codebridge.core.data.mapper;

import za.co.eduvos.codebridge.core.git.GitTelemetryCollector;
import za.co.eduvos.codebridge.core.network.dto.TelemetryDto;

/** Converts the Git stage summary into the telemetry DTO the sync queue sends. */
public class GitTelemetryMapper {

    private GitTelemetryMapper() {}

    /**
     * Fills the Git field of TelemetryDto (git_error_count = commands that failed or were unsupported).
     * Other fields belong to other stages and are left at their defaults.
     */
    public static TelemetryDto toDto(int sessionId, GitTelemetryCollector.Summary summary) {
        TelemetryDto dto = new TelemetryDto();
        dto.setSessionId(sessionId);
        dto.setGitErrorCount(summary.errorCommands);
        return dto;
    }
}
