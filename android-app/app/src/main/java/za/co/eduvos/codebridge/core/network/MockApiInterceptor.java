package za.co.eduvos.codebridge.core.network;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * MockInterceptor for Person B to build UI without the real backend.
 * Enable in RetrofitClient when BuildConfig.USE_MOCK_API is true.
 */
public class MockApiInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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
            String body = "[" +
                "{\"problem_id\":1,\"title\":\"Two Sum\",\"difficulty_level\":\"Easy\",\"test_cases_json\":\"{}\"}," +
                "{\"problem_id\":2,\"title\":\"Reverse String\",\"difficulty_level\":\"Easy\",\"test_cases_json\":\"{}\"}," +
                "{\"problem_id\":3,\"title\":\"Merge Intervals\",\"difficulty_level\":\"Medium\",\"test_cases_json\":\"{}\"}" +
                "]";
            return json(request, 200, body);
        }
        if (path.startsWith("/problems/")) {
            return json(request, 200,
                "{\"problem_id\":1,\"title\":\"Two Sum\",\"difficulty_level\":\"Easy\",\"test_cases_json\":\"{}\"}");
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
                "{\"event_id\":1,\"company_id\":1,\"title\":\"CodeBridge Hackathon\",\"event_date\":\"2025-06-15T09:00:00Z\"}," +
                "{\"event_id\":2,\"company_id\":1,\"title\":\"Tech Career Fair\",\"event_date\":\"2025-07-20T10:00:00Z\"}" +
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

        // Fallback
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
}
