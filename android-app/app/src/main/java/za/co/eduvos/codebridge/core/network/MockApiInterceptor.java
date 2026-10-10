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
 * MockInterceptor for Person B to build UI without the real backend.
 * Enable by setting USE_MOCK_API = true in app/build.gradle.
 *
 * Responses match docs/api-contracts/openapi.json and the
 * test_cases_json format defined by Person A's sandbox:
 *   {"cases":[{"input":"...","expected":"..."}], "entryPoint":"solve"}
 */
public class MockApiInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson GSON = new Gson();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();
        String method = request.method();

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

    private Response json(Request request, int code, String body) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("Mock")
                .body(ResponseBody.create(JSON, body))
                .build();
    }

    private String buildProblemsListJson() {
        List<Map<String, Object>> problems = new ArrayList<>();
        problems.add(buildProblemMap(1, "Two Sum", "Easy",
                "[2,7,11,15], 9", "[0,1]", "twoSum"));
        problems.add(buildProblemMap(2, "Reverse String", "Easy",
                "hello", "olleh", "reverseString"));
        problems.add(buildProblemMap(3, "Merge Intervals", "Medium",
                "[[1,3],[2,6]]", "[[1,6]]", "mergeIntervals"));
        return GSON.toJson(problems);
    }

    private String buildProblemJson(int problemId) {
        switch (problemId) {
            case 1:
                return GSON.toJson(buildProblemMap(1, "Two Sum", "Easy",
                        "[2,7,11,15], 9", "[0,1]", "twoSum"));
            case 2:
                return GSON.toJson(buildProblemMap(2, "Reverse String", "Easy",
                        "hello", "olleh", "reverseString"));
            case 3:
                return GSON.toJson(buildProblemMap(3, "Merge Intervals", "Medium",
                        "[[1,3],[2,6]]", "[[1,6]]", "mergeIntervals"));
            default:
                return GSON.toJson(buildProblemMap(1, "Two Sum", "Easy",
                        "[2,7,11,15], 9", "[0,1]", "twoSum"));
        }
    }

    private Map<String, Object> buildProblemMap(int id, String title, String difficulty,
                                                String input, String expected, String entryPoint) {
        Map<String, Object> caseMap = new HashMap<>();
        caseMap.put("input", input);
        caseMap.put("expected", expected);

        List<Map<String, Object>> casesList = new ArrayList<>();
        casesList.add(caseMap);

        Map<String, Object> suite = new HashMap<>();
        suite.put("cases", casesList);
        suite.put("entryPoint", entryPoint);

        String testCasesJson = GSON.toJson(suite);

        Map<String, Object> problem = new HashMap<>();
        problem.put("problem_id", id);
        problem.put("title", title);
        problem.put("difficulty_level", difficulty);
        problem.put("test_cases_json", testCasesJson);
        return problem;
    }
}
