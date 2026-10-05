package za.co.eduvos.codebridge.core.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CodeExecutionEngineTest {

    private static final String TWO_SUM =
            "{\"cases\":[{\"input\":\"[2,7,11,15], 9\",\"expected\":\"[0,1]\"},"
          + "{\"input\":\"[3,2,4], 6\",\"expected\":\"[1,2]\"}]}";
    private static final String REVERSE =
            "{\"cases\":[{\"input\":\"hello\",\"expected\":\"olleh\"}]}";

    private final CodeExecutionEngine engine = new CodeExecutionEngine();

    @After
    public void tearDown() {
        engine.shutdown();
    }

    @Test
    public void correctSolutionPassesAllCases() {
        String code = "function twoSum(nums, target) {"
                + " var m = {};"
                + " for (var i = 0; i < nums.length; i++) {"
                + "   if (m[target - nums[i]] !== undefined) return [m[target - nums[i]], i];"
                + "   m[nums[i]] = i; } }";
        ExecutionResult r = engine.execute(code, TWO_SUM);
        assertNull(r.fatalError);
        assertEquals(2, r.totalCount());
        assertTrue(r.allPassed());
    }

    @Test
    public void bareStringInputAndExpectedWork() {
        ExecutionResult r = engine.execute("function solve(s){ return s.split('').reverse().join(''); }", REVERSE);
        assertTrue(r.allPassed());
    }

    @Test
    public void wrongAnswerIsFailedWithActualValue() {
        ExecutionResult r = engine.execute("function solve(s){ return s; }", REVERSE);
        TestResult t = r.results.get(0);
        assertEquals(TestResult.Status.FAILED, t.status);
        assertEquals("hello", t.actual);
        assertEquals("olleh", t.expected);
        assertFalse(r.allPassed());
    }

    @Test
    public void syntaxErrorIsFatal() {
        ExecutionResult r = engine.execute("function solve( {", REVERSE);
        assertNotNull(r.fatalError);
        assertTrue(r.fatalError.startsWith("Syntax error"));
        assertEquals(0, r.totalCount());
    }

    @Test
    public void runtimeErrorIsReportedPerCase() {
        ExecutionResult r = engine.execute("function solve(s){ return s.nope.x; }", REVERSE);
        TestResult t = r.results.get(0);
        assertEquals(TestResult.Status.ERROR, t.status);
        assertNotNull(t.error);
    }

    @Test
    public void missingFunctionIsAnError() {
        ExecutionResult r = engine.execute("var x = 1;", REVERSE);
        assertEquals(TestResult.Status.ERROR, r.results.get(0).status);
    }

    @Test
    public void capturesConsoleLogAndPrint() {
        ExecutionResult r = engine.execute(
                "function solve(s){ console.log('a', 1); print('b'); return 'olleh'; }", REVERSE);
        assertEquals("a 1\nb\n", r.results.get(0).stdout);
        assertTrue(r.allPassed());
    }

    @Test
    public void infiniteLoopTimesOutWithinBudgetAndSkipsRest() {
        long start = System.nanoTime();
        ExecutionResult r = engine.execute("function twoSum(a,t){ while(true){} }", TWO_SUM);
        long ms = (System.nanoTime() - start) / 1_000_000L;
        assertTrue(r.timedOut);
        assertEquals(TestResult.Status.TIMEOUT, r.results.get(0).status);
        assertEquals(TestResult.Status.SKIPPED, r.results.get(1).status);
        assertTrue("took " + ms + "ms", ms < 3500);
    }

    @Test
    public void jsTryCatchCannotSwallowTimeout() {
        ExecutionResult r = engine.execute(
                "function solve(s){ for(;;){ try { while(true){} } catch(e) {} } }", REVERSE);
        assertTrue(r.timedOut);
    }

    @Test
    public void runawayRecursionIsAnErrorNotACrash() {
        ExecutionResult r = engine.execute("function solve(s){ return solve(s); }", REVERSE);
        assertEquals(TestResult.Status.ERROR, r.results.get(0).status);
    }

    @Test
    public void javaClassesAreNotReachable() {
        ExecutionResult r = engine.execute(
                "function solve(s){ return java.lang.System.getProperty('user.dir'); }", REVERSE);
        assertEquals(TestResult.Status.ERROR, r.results.get(0).status);
    }

    @Test
    public void stateDoesNotLeakBetweenCases() {
        String suite = "{\"cases\":[{\"input\":\"1\",\"expected\":\"1\"},{\"input\":\"1\",\"expected\":\"1\"}]}";
        ExecutionResult r = engine.execute("var n = 0; function solve(x){ n++; return n; }", suite);
        assertTrue(r.allPassed());
    }

    @Test
    public void emptySuiteAndEmptyCodeAreFatal() {
        assertNotNull(engine.execute("function solve(){}", "{}").fatalError);
        assertNotNull(engine.execute("  ", REVERSE).fatalError);
    }

    @Test
    public void asyncDeliversResult() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ExecutionResult> ref = new AtomicReference<>();
        engine.executeAsync("function solve(s){ return 'olleh'; }", REVERSE, result -> {
            ref.set(result);
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(ref.get().allPassed());
    }

    @Test
    public void standardSuiteRunsUnderTwoSeconds() {
        StringBuilder cases = new StringBuilder("{\"cases\":[");
        for (int i = 0; i < 10; i++) {
            if (i > 0) cases.append(',');
            cases.append("{\"input\":\"").append(i).append("\",\"expected\":\"").append(i * 2).append("\"}");
        }
        cases.append("]}");
        ExecutionResult r = engine.execute("function solve(x){ return x * 2; }", cases.toString());
        assertTrue(r.allPassed());
        assertTrue("took " + r.totalMs + "ms", r.totalMs < 2000);
    }
}
