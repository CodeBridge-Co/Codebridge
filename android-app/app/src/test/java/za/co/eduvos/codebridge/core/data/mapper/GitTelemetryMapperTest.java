package za.co.eduvos.codebridge.core.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import za.co.eduvos.codebridge.core.git.CommandLogger;
import za.co.eduvos.codebridge.core.git.GitSimulationManager;
import za.co.eduvos.codebridge.core.git.GitTelemetryCollector;
import za.co.eduvos.codebridge.core.git.MergeConflictGenerator;
import za.co.eduvos.codebridge.core.network.dto.TelemetryDto;

public class GitTelemetryMapperTest {

    private static GitTelemetryCollector.Summary playSession() {
        AtomicLong t = new AtomicLong(1_000_000L);
        GitTelemetryCollector telemetry = new GitTelemetryCollector(() -> t.addAndGet(1000));
        GitSimulationManager git = new GitSimulationManager(
                MergeConflictGenerator.generate(0, () -> t.get()),
                new CommandLogger((id, cmd, err, ts) -> { }, 1, () -> t.addAndGet(1000)),
                telemetry, () -> t.addAndGet(1000));
        git.execute("git merge main");
        git.execute("git commit -m \"too early\"");        // 1 error
        git.execute("git frobnicate");                     // 2 errors
        git.execute("git checkout --theirs src/solution.js");
        git.execute("git add .");
        git.execute("git commit -m \"merge\"");
        git.execute("git push");
        assertTrue(git.isTaskComplete());
        return telemetry.snapshot();
    }

    @Test
    public void copiesSessionAndGitErrorCount() {
        TelemetryDto dto = GitTelemetryMapper.toDto(5, playSession());
        assertEquals(5, dto.getSessionId());
        assertEquals(2, dto.getGitErrorCount());
    }

    @Test
    public void jsonUsesTheBackendFieldNamesAndLeavesOtherStagesAtZero() {
        JsonObject json = new Gson().toJsonTree(GitTelemetryMapper.toDto(9, playSession())).getAsJsonObject();
        assertEquals(9, json.get("session_id").getAsInt());
        assertEquals(2, json.get("git_error_count").getAsInt());
        assertEquals(0, json.get("execution_speed_ms").getAsInt());
        assertEquals(0f, json.get("speech_keyword_density").getAsFloat(), 0f);
        assertFalse("null log_id is omitted", json.has("log_id"));
    }
}
