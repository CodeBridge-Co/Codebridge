package za.co.eduvos.codebridge.core.sandbox;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Runs one test case against pre-compiled user code in a fresh scope, so state never leaks between cases.
 * Must be called on a thread that already has a Rhino Context entered (see CodeExecutionEngine).
 */
public class TestCaseRunner {

    /** Thrown from the instruction observer to abort runaway code. Extends Error so JS try/catch can't swallow it. */
    public static class SandboxTimeoutError extends Error {
        public SandboxTimeoutError() {
            super("Execution time limit exceeded", null, false, false);
        }
    }

    private static final String HELPERS =
            "function __cbArgs(s){try{return JSON.parse('['+s+']');}catch(e){return [s];}}\n"
          + "function __cbCanon(v){if(typeof v==='string')return v;if(v===undefined)return 'undefined';"
          + "try{var j=JSON.stringify(v);return j===undefined?String(v):j;}catch(e){return String(v);}}\n"
          + "function __cbExpected(s){try{return __cbCanon(JSON.parse(s));}catch(e){return s;}}\n";

    public TestResult run(Context cx, Script userScript, TestSuite.Case testCase, int index,
                          String entryPoint, OutputCapture out) {
        long start = System.nanoTime();
        String actual = null;
        try {
            ScriptableObject scope = cx.initSafeStandardObjects();
            installGlobals(cx, scope, out);
            Set<Object> before = idsOf(scope);

            userScript.exec(cx, scope);

            Function fn = resolveEntryPoint(scope, before, entryPoint);
            Object[] args = ((NativeArray) callHelper(cx, scope, "__cbArgs", testCase.input)).toArray();
            Object result = fn.call(cx, scope, scope, args);

            actual = Context.toString(callHelper(cx, scope, "__cbCanon", result));
            String expected = Context.toString(callHelper(cx, scope, "__cbExpected", testCase.expected));
            TestResult.Status status = actual.equals(expected)
                    ? TestResult.Status.PASSED : TestResult.Status.FAILED;
            return result(index, status, testCase, actual, null, out, start);
        } catch (TestSuiteSetupException e) {
            return result(index, TestResult.Status.ERROR, testCase, null, e.getMessage(), out, start);
        } catch (SandboxTimeoutError e) {
            return result(index, TestResult.Status.TIMEOUT, testCase, null, e.getMessage(), out, start);
        } catch (RhinoException e) {
            return result(index, TestResult.Status.ERROR, testCase, null, e.getMessage(), out, start);
        } catch (StackOverflowError e) {
            return result(index, TestResult.Status.ERROR, testCase, null, "Maximum call stack size exceeded", out, start);
        }
    }

    public static TestResult skipped(int index, TestSuite.Case testCase) {
        return new TestResult(index, TestResult.Status.SKIPPED, testCase.input, testCase.expected,
                null, null, "", 0);
    }

    private static TestResult result(int index, TestResult.Status status, TestSuite.Case tc,
                                     String actual, String error, OutputCapture out, long startNanos) {
        long ms = (System.nanoTime() - startNanos) / 1_000_000L;
        return new TestResult(index, status, tc.input, tc.expected, actual, error, out.drain(), ms);
    }

    private static void installGlobals(Context cx, ScriptableObject scope, final OutputCapture out) {
        BaseFunction print = new BaseFunction() {
            @Override
            public Object call(Context c, Scriptable s, Scriptable thisObj, Object[] args) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) sb.append(' ');
                    sb.append(Context.toString(args[i]));
                }
                out.println(sb.toString());
                return Context.getUndefinedValue();
            }
        };
        ScriptableObject.putProperty(scope, "print", print);

        cx.evaluateString(scope, "var console = {};", "setup", 1, null);
        Scriptable console = (Scriptable) scope.get("console", scope);
        ScriptableObject.putProperty(console, "log", print);
        ScriptableObject.putProperty(console, "error", print);
        ScriptableObject.putProperty(console, "warn", print);

        cx.evaluateString(scope, HELPERS, "helpers", 1, null);
    }

    private static Set<Object> idsOf(ScriptableObject scope) {
        Set<Object> ids = new HashSet<>();
        for (Object id : scope.getIds()) ids.add(id);
        return ids;
    }

    private static Function resolveEntryPoint(ScriptableObject scope, Set<Object> before, String entryPoint)
            throws TestSuiteSetupException {
        if (entryPoint != null) {
            Object f = scope.get(entryPoint, scope);
            if (f instanceof Function) return (Function) f;
            throw new TestSuiteSetupException("Function '" + entryPoint + "' is not defined");
        }
        for (String name : new String[]{"solve", "solution"}) {
            Object f = scope.get(name, scope);
            if (f instanceof Function) return (Function) f;
        }
        for (Object id : scope.getIds()) {
            if (before.contains(id) || !(id instanceof String)) continue;
            Object f = scope.get((String) id, scope);
            if (f instanceof Function) return (Function) f;
        }
        throw new TestSuiteSetupException("No function found. Define a function such as solve(...)");
    }

    private static Object callHelper(Context cx, Scriptable scope, String name, Object arg) {
        Function h = (Function) scope.get(name, scope);
        return h.call(cx, scope, scope, new Object[]{arg});
    }

    private static class TestSuiteSetupException extends Exception {
        TestSuiteSetupException(String message) {
            super(message);
        }
    }
}
