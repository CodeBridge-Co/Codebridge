package za.co.eduvos.codebridge.core.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Test;

/** Compare modes (exact / unordered / checker) and hidden test cases. */
public class SolutionComparisonTest {

    private final CodeExecutionEngine engine = new CodeExecutionEngine();

    @After
    public void tearDown() {
        engine.shutdown();
    }

    private static final String REVERSED_PAIR = "function solve(a, t){ return [1, 0]; }";

    private static String suite(String compare, String extra, String cases) {
        return "{\"compare\":\"" + compare + "\"" + extra + ",\"cases\":" + cases + "}";
    }

    @Test
    public void exactModeRejectsDifferentOrder() {
        String s = "{\"cases\":[{\"input\":\"[2,7], 9\",\"expected\":\"[0,1]\"}]}";
        assertFalse(engine.execute(REVERSED_PAIR, s).allPassed());
    }

    @Test
    public void exactModeIgnoresWhitespaceAndNumberFormatting() {
        String s = "{\"cases\":[{\"input\":\"1\",\"expected\":\"[ 1 , 2.0 ]\"}]}";
        assertTrue(engine.execute("function solve(){ return [1,2]; }", s).allPassed());
    }

    // ---- unordered -------------------------------------------------------

    @Test
    public void unorderedAcceptsAnyOrder() {
        String s = suite("unordered", "", "[{\"input\":\"[2,7], 9\",\"expected\":\"[0,1]\"}]");
        assertTrue(engine.execute(REVERSED_PAIR, s).allPassed());
    }

    @Test
    public void unorderedStillRejectsWrongContents() {
        String s = suite("unordered", "", "[{\"input\":\"[2,7], 9\",\"expected\":\"[0,1]\"}]");
        assertFalse(engine.execute("function solve(){ return [0, 2]; }", s).allPassed());
    }

    @Test
    public void unorderedStillCountsDuplicates() {
        String s = suite("unordered", "", "[{\"input\":\"1\",\"expected\":\"[1,1,2]\"}]");
        assertFalse(engine.execute("function solve(){ return [1,2,2]; }", s).allPassed());
        assertTrue(engine.execute("function solve(){ return [2,1,1]; }", s).allPassed());
    }

    @Test
    public void unorderedLeavesNonArraysAlone() {
        String s = suite("unordered", "", "[{\"input\":\"3\",\"expected\":\"6\"}]");
        assertTrue(engine.execute("function solve(n){ return n * 2; }", s).allPassed());
    }

    // ---- checker ---------------------------------------------------------

    private static final String PAIR_CHECKER =
            ",\"checker\":\"function check(args, out){ var n=args[0], t=args[1];"
                    + " return out.length===2 && out[0]!==out[1] && n[out[0]]+n[out[1]]===t; }\"";
    private static final String PAIR_CASES = "[{\"input\":\"[3,3,4], 6\",\"expected\":\"[0,1]\"}]";

    @Test
    public void checkerAcceptsAnyValidAnswer() {
        String s = suite("checker", PAIR_CHECKER, PAIR_CASES);
        assertTrue(engine.execute("function solve(){ return [1,0]; }", s).allPassed());
        assertTrue(engine.execute("function solve(){ return [0,1]; }", s).allPassed());
    }

    @Test
    public void checkerRejectsInvalidAnswer() {
        String s = suite("checker", PAIR_CHECKER, PAIR_CASES);
        assertFalse(engine.execute("function solve(){ return [0,2]; }", s).allPassed());
    }

    @Test
    public void checkerThatThrowsIsAnErrorNotAPass() {
        String s = suite("checker", ",\"checker\":\"function check(a,o){ throw new Error('bad'); }\"", PAIR_CASES);
        TestResult r = engine.execute("function solve(){ return [0,1]; }", s).results.get(0);
        assertEquals(TestResult.Status.ERROR, r.status);
    }

    @Test
    public void checkerWithoutCheckFunctionIsAnError() {
        String s = suite("checker", ",\"checker\":\"var x = 1;\"", PAIR_CASES);
        TestResult r = engine.execute("function solve(){ return [0,1]; }", s).results.get(0);
        assertEquals(TestResult.Status.ERROR, r.status);
    }

    @Test
    public void infiniteLoopInCheckerTimesOut() {
        CodeExecutionEngine quick = new CodeExecutionEngine(500);
        try {
            String s = suite("checker", ",\"checker\":\"function check(a,o){ while(true){} }\"", PAIR_CASES);
            ExecutionResult r = quick.execute("function solve(){ return [0,1]; }", s);
            assertTrue(r.timedOut);
            assertFalse(r.allPassed());
        } finally {
            quick.shutdown();
        }
    }

    @Test
    public void studentCannotOverrideTheChecker() {
        String s = suite("checker", PAIR_CHECKER, PAIR_CASES);
        String cheat = "function check(){ return true; } function solve(){ return [9,9]; }";
        assertFalse(engine.execute(cheat, s).allPassed());
    }

    // ---- hidden cases ----------------------------------------------------

    private static final String WITH_HIDDEN =
            "[{\"input\":\"2\",\"expected\":\"4\"},{\"input\":\"7\",\"expected\":\"14\",\"hidden\":true}]";

    @Test
    public void hiddenCasesStillCount() {
        ExecutionResult r = engine.execute("function solve(n){ return n === 2 ? 4 : 0; }",
                "{\"cases\":" + WITH_HIDDEN + "}");
        assertEquals(2, r.totalCount());
        assertEquals(1, r.passedCount());
        assertFalse(r.allPassed());
    }

    @Test
    public void hiddenFailureRevealsNothing() {
        ExecutionResult r = engine.execute("function solve(n){ return 12345; }",
                "{\"cases\":" + WITH_HIDDEN + "}");
        TestResult t = r.results.get(1);
        assertTrue(t.hidden);
        assertEquals(TestResult.Status.FAILED, t.status);
        assertEquals("", t.input);
        assertEquals("", t.expected);
        assertNull(t.actual);
        assertEquals("", t.stdout);
        String all = t.input + t.expected + t.actual + t.error + t.stdout;
        assertFalse(all.contains("14"));
        assertFalse(all.contains("12345"));
    }

    @Test
    public void hiddenErrorMessageIsGeneric() {
        ExecutionResult r = engine.execute(
                "function solve(n){ if (n===7) throw new Error('secret-detail'); return 4; }",
                "{\"cases\":" + WITH_HIDDEN + "}");
        TestResult t = r.results.get(1);
        assertEquals(TestResult.Status.ERROR, t.status);
        assertFalse(t.error.contains("secret-detail"));
        assertEquals("", t.stdout);
    }

    @Test
    public void hiddenPassStaysPassed() {
        ExecutionResult r = engine.execute("function solve(n){ return n * 2; }", "{\"cases\":" + WITH_HIDDEN + "}");
        assertTrue(r.allPassed());
        assertTrue(r.results.get(1).hidden);
        assertFalse(r.results.get(0).hidden);
    }

    @Test
    public void visibleResultsStillShowDetails() {
        ExecutionResult r = engine.execute("function solve(n){ return 5; }", "{\"cases\":" + WITH_HIDDEN + "}");
        TestResult t = r.results.get(0);
        assertEquals("2", t.input);
        assertEquals("4", t.expected);
        assertEquals("5", t.actual);
    }

    // ---- parser ----------------------------------------------------------

    @Test
    public void parserReadsModeHiddenAndChecker() throws Exception {
        TestSuite s = TestSuiteParser.parse(suite("checker", PAIR_CHECKER,
                "[{\"input\":\"1\",\"expected\":\"1\"},{\"input\":\"2\",\"expected\":\"2\",\"hidden\":true}]"));
        assertEquals(TestSuite.Mode.CHECKER, s.mode);
        assertTrue(s.checkerSource.contains("function check"));
        assertFalse(s.cases.get(0).hidden);
        assertTrue(s.cases.get(1).hidden);
        assertEquals(1, s.visibleCases().size());
        assertEquals(1, s.hiddenCount());
    }

    @Test
    public void parserDefaultsToExactAndVisible() throws Exception {
        TestSuite s = TestSuiteParser.parse("[{\"input\":\"1\",\"expected\":\"1\"}]");
        assertEquals(TestSuite.Mode.EXACT, s.mode);
        assertFalse(s.cases.get(0).hidden);
    }

    @Test
    public void parserRejectsUnknownModeAndCheckerWithoutFunction() {
        String[] bad = {
                "{\"compare\":\"fuzzy\",\"cases\":[{\"input\":\"1\",\"expected\":\"1\"}]}",
                "{\"compare\":\"checker\",\"cases\":[{\"input\":\"1\",\"expected\":\"1\"}]}"
        };
        for (String json : bad) {
            try {
                TestSuiteParser.parse(json);
                fail("should reject: " + json);
            } catch (TestSuiteParser.ParseException expected) {
                // ok
            }
        }
    }
}
