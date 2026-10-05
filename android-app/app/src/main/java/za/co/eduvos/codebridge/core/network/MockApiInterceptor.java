package za.co.eduvos.codebridge.core.network;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * MockInterceptor for UI/UX to build UI without the real backend.
 * Enable by setting USE_MOCK_API = true in app/build.gradle.
 *
 * Responses match docs/api-contracts/openapi.json.
 * The /problems and /problems/{id} endpoints return realistic test_cases_json
 * identical to what Backend Lead's backend seed data returns.
 */
public class MockApiInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson GSON = new Gson();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();
        String method = request.method();

        // Simulate network latency
        try { TimeUnit.MILLISECONDS.sleep(300); } catch (InterruptedException ignored) {}

        if (path.endsWith("/health")) {
            return json(request, 200, "{\"data\":\"OK\",\"error\":null,\"status\":\"success\"}");
        }
        if (path.endsWith("/auth/register")) {
            return json(request, 200, "{\"student_hash_id\":\"mock-student-001\",\"token\":\"mock_jwt_token\"}");
        }
        if (path.endsWith("/auth/login")) {
            return json(request, 200, "{\"student_hash_id\":\"mock-student-001\",\"token\":\"mock_jwt_token\"}");
        }
        if (path.equals("/problems")) {
            return json(request, 200, buildProblemsListJson());
        }
        if (path.startsWith("/problems/")) {
            // Extract problem_id from the path
            int problemId = 1;
            try {
                problemId = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
            } catch (NumberFormatException ignored) {}
            return json(request, 200, buildProblemJson(problemId));
        }
        if (path.endsWith("/sessions") && method.equals("POST")) {
            return json(request, 200, "{\"session_id\":\"sess-mock-001\",\"status\":\"created\"}");
        }
        if (path.endsWith("/sync")) {
            return json(request, 200, "{\"status\":\"success\",\"synced_items\":3}");
        }
        if (path.endsWith("/leaderboard")) {
            String body = "[" +
                    "{\"hash_id\":\"mock-student-001\",\"xp\":1200,\"streak\":5}," +
                    "{\"hash_id\":\"mock-student-002\",\"xp\":950,\"streak\":3}," +
                    "{\"hash_id\":\"mock-student-003\",\"xp\":700,\"streak\":2}" +
                    "]";
            return json(request, 200, body);
        }
        if (path.endsWith("/events")) {
            String body = "[" +
                    "{\"event_id\":1,\"company_id\":1,\"title\":\"CodeBridge Hackathon 2026\",\"event_date\":\"2026-06-15T09:00:00Z\"}," +
                    "{\"event_id\":2,\"company_id\":1,\"title\":\"Tech Career Fair\",\"event_date\":\"2026-07-20T10:00:00Z\"}" +
                    "]";
            return json(request, 200, body);
        }
        if (path.endsWith("/ai-assist")) {
            return json(request, 200, "{\"allowed\":true,\"remaining_uses\":2}");
        }
        if (path.endsWith("/speech/transcribe")) {
            return json(request, 200,
                    "{\"transcript\":\"Mock transcript of the student explaining their solution.\",\"keyword_density\":0.4}");
        }

        return json(request, 404, "{\"error\":\"Mock not implemented for " + method + " " + path + "\"}");
    }

    // ---------- Helpers ----------

    private Response json(Request request, int code, String body) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("Mock")
                .body(ResponseBody.create(JSON, body))
                .build();
    }

    /** Builds the full /problems list with realistic test_cases_json. */
    private String buildProblemsListJson() {
        List<Map<String, Object>> problems = new ArrayList<>();
        problems.add(buildProblemMap(1, "Two Sum", "Easy",
                "[2,7,11,15], 9", "[0,1]"));
        problems.add(buildProblemMap(2, "Reverse String", "Easy",
                "hello", "olleh"));
        problems.add(buildProblemMap(3, "Merge Intervals", "Medium",
                "[[1,3],[2,6]]", "[[1,6]]"));
        return GSON.toJson(problems);
    }

    /** Builds a single /problems/{id} response. Falls back to Two Sum if id not found. */
    private String buildProblemJson(int problemId) {
        switch (problemId) {
            case 1:
                return GSON.toJson(buildProblemMap(1, "Two Sum", "Easy",
                        "[2,7,11,15], 9", "[0,1]"));
            case 2:
                return GSON.toJson(buildProblemMap(2, "Reverse String", "Easy",
                        "hello", "olleh"));
            case 3:
                return GSON.toJson(buildProblemMap(3, "Merge Intervals", "Medium",
                        "[[1,3],[2,6]]", "[[1,6]]"));
            default:
                return GSON.toJson(buildProblemMap(1, "Two Sum", "Easy",
                        "[2,7,11,15], 9", "[0,1]"));
        }
    }

    /**
     * Builds a ProblemDto-shaped map. The test_cases_json field is a properly
     * escaped JSON string, exactly matching what Person C's backend returns.
     */
    private Map<String, Object> buildProblemMap(int id, String title, String difficulty,
                                                String input, String expected) {
        // Inner JSON: {"cases":[{"input":"...","expected":"..."}]}
        Map<String, Object> caseMap = new HashMap<>();
        caseMap.put("input", input);
        caseMap.put("expected", expected);

        List<Map<String, Object>> casesList = new ArrayList<>();
        casesList.add(caseMap);

        Map<String, Object> casesWrapper = new HashMap<>();
        casesWrapper.put("cases", casesList);

        String testCasesJson = GSON.toJson(casesWrapper);

        Map<String, Object> problem = new HashMap<>();
        problem.put("problem_id", id);
        problem.put("title", title);
        problem.put("difficulty_level", difficulty);
        problem.put("test_cases_json", testCasesJson);
        return problem;
    }
}
