package za.co.eduvos.codebridge.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import za.co.eduvos.codebridge.core.network.ApiService;
import za.co.eduvos.codebridge.core.network.MockApiInterceptor;
import za.co.eduvos.codebridge.core.network.dto.LoginRequest;
import za.co.eduvos.codebridge.core.network.dto.LoginResponse;
import za.co.eduvos.codebridge.core.network.dto.ProblemDto;
import za.co.eduvos.codebridge.core.sandbox.CodeExecutionEngine;
import za.co.eduvos.codebridge.core.sandbox.ExecutionResult;

/**
 * Exercises the three wired paths on the JVM using the real Retrofit interface and the
 * mock interceptor: login, problem list, and running a solution against the delivered suite.
 */
public class MockPipelineTest {

    private ApiService api;
    private CodeExecutionEngine engine;

    private static final Map<String, String> SOLUTIONS = new HashMap<>();

    static {
        SOLUTIONS.put("twoSum", "function twoSum(nums, target) { var m = {};"
                + " for (var i = 0; i < nums.length; i++) {"
                + " var j = m[target - nums[i]]; if (j !== undefined) return [j, i]; m[nums[i]] = i; } }");
        SOLUTIONS.put("reverseString", "function reverseString(s) { return s.split('').reverse().join(''); }");
        SOLUTIONS.put("mergeIntervals", "function mergeIntervals(iv) {"
                + " iv = iv.slice().sort(function(a, b) { return a[0] - b[0]; }); var out = [];"
                + " for (var i = 0; i < iv.length; i++) {"
                + " var last = out[out.length - 1];"
                + " if (last && iv[i][0] <= last[1]) last[1] = Math.max(last[1], iv[i][1]);"
                + " else out.push([iv[i][0], iv[i][1]]); } return out; }");
    }

    @Before
    public void setUp() {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new MockApiInterceptor()).build();
        api = new Retrofit.Builder()
                .baseUrl("http://localhost/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
        engine = new CodeExecutionEngine();
    }

    @After
    public void tearDown() {
        engine.shutdown();
    }

    @Test
    public void loginReturnsTokenAndHash() throws IOException {
        Response<LoginResponse> r = api.login(new LoginRequest("some-hash", "inst-1")).execute();
        assertTrue(r.isSuccessful());
        assertNotNull(r.body());
        assertEquals("mock_jwt_token", r.body().getToken());
        assertFalse(r.body().getStudentHashId().isEmpty());
    }

    @Test
    public void problemsListParsesAndEachSuiteRunsInTheSandbox() throws IOException {
        Response<List<ProblemDto>> r = api.getProblems().execute();
        assertTrue(r.isSuccessful());
        List<ProblemDto> dtos = r.body();
        assertNotNull(dtos);
        assertFalse(dtos.isEmpty());

        List<ProblemDetail> details = new ArrayList<>();
        for (ProblemDto dto : dtos) details.add(ProblemDetailFactory.INSTANCE.fromDto(dto));

        for (ProblemDetail p : details) {
            String solution = SOLUTIONS.get(p.getEntryPoint());
            assertNotNull("no reference solution for " + p.getEntryPoint(), solution);

            // Starter code must name the right function and must not already pass.
            assertTrue(p.getInitialCode().startsWith("function " + p.getEntryPoint() + "("));
            assertFalse(engine.execute(p.getInitialCode(), p.getTestCasesJson()).allPassed());

            ExecutionResult result = engine.execute(solution, p.getTestCasesJson());
            assertEquals(p.getTitle() + ": " + result.fatalError, null, result.fatalError);
            assertTrue(p.getTitle() + " should pass", result.allPassed());
        }
    }

    @Test
    public void starterCodeMatchesArgumentCount() throws IOException {
        List<ProblemDto> dtos = api.getProblems().execute().body();
        assertNotNull(dtos);
        for (ProblemDto dto : dtos) {
            ProblemDetail p = ProblemDetailFactory.INSTANCE.fromDto(dto);
            if (p.getEntryPoint().equals("twoSum")) {
                assertTrue(p.getInitialCode().startsWith("function twoSum(arg1, arg2)"));
            }
            if (p.getEntryPoint().equals("mergeIntervals")) {
                assertTrue(p.getInitialCode().startsWith("function mergeIntervals(arg1)"));
            }
        }
    }

    @Test
    public void unusableSuiteDoesNotCrashTheFactory() {
        ProblemDetail p = ProblemDetailFactory.INSTANCE.create(9, null, null, "{}");
        assertEquals("solve", p.getEntryPoint());
        assertEquals("Problem 9", p.getTitle());
        assertEquals("-", p.getExample1Input());
    }
}
