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

/** Runs one test case against the student's code in a fresh scope. */
public class TestCaseRunner {
    public static class SandboxTimeoutError extends Error {
        public SandboxTimeoutError() {
            super("Execution time limit exceeded", null, false, false);
        }
    }

    private static final String HELPERS =
            "function __cbArgs(s){try{return JSON.parse('['+s+']');}catch(e){return [s];}}\n"
                    + "function __cbCanon(v){if(typeof v==='string')return v;if(v===undefined)return 'undefined';"
                    + "try{var j=JSON.stringify(v);return j===undefined?String(v):j;}catch(e){return String(v);}}\n"
                    + "function __cbExpected(s){try{return __cbCanon(JSON.parse(s));}catch(e){return s;}}\n"
                    + "function __cbUnordered(s){var v;try{v=JSON.parse(s);}catch(e){return s;}"
                    + "if(!Array.isArray(v))return s;var p=[];for(var i=0;i<v.length;i++)p.push(__cbCanon(v[i]));"
                    + "p.sort();return '['+p.join(',')+']';}\n"
                    + "function __cbDecode(s){try{return JSON.parse(s);}catch(e){return s;}}\n";

    public TestResult run(Context cx, Script userScript, TestSuite.Case testCase, int index,
                          String entryPoint, OutputCapture out) {
        return run(cx, userScript, testCase, index, new TestSuite(java.util.Collections.singletonList(testCase), entryPoint), out);
    }

    public TestResult run(Context cx, Script userScript, TestSuite.Case testCase, int index,
                          TestSuite suite, OutputCapture out) {
        String entryPoint = suite.entryPoint;
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
            boolean ok;
            switch (suite.mode) {
                case CHECKER:
                    ok = runChecker(cx, suite, testCase, actual);
                    break;
                case UNORDERED: {
                    String expected = Context.toString(callHelper(cx, scope, "__cbExpected", testCase.expected));
                    ok = Context.toString(callHelper(cx, scope, "__cbUnordered", actual))
                            .equals(Context.toString(callHelper(cx, scope, "__cbUnordered", expected)));
                    break;
                }
                default: {
                    String expected = Context.toString(callHelper(cx, scope, "__cbExpected", testCase.expected));
                    ok = actual.equals(expected);
                }
            }
            TestResult.Status status = ok ? TestResult.Status.PASSED : TestResult.Status.FAILED;
            return result(index, status, testCase, actual, null, out, start);
        } catch (IllegalStateException e) {
            return result(index, TestResult.Status.ERROR, testCase, null, e.getMessage(), out, start);
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

    /** Runs the problem's checker in its own scope, so student code can't change it. An error is never a pass. */
    private boolean runChecker(Context cx, TestSuite suite, TestSuite.Case testCase, String actualCanon) {
        ScriptableObject scope = cx.initSafeStandardObjects();
        cx.evaluateString(scope, HELPERS, "helpers", 1, null);
        cx.evaluateString(scope, suite.checkerSource, "checker.js", 1, null);
        Object f = scope.get("check", scope);
        if (!(f instanceof Function)) {
            throw new IllegalStateException("The problem's checker does not define check(args, actual)");
        }
        Object args = callHelper(cx, scope, "__cbArgs", testCase.input);
        Object actual = callHelper(cx, scope, "__cbDecode", actualCanon);
        Object verdict = ((Function) f).call(cx, scope, scope, new Object[]{args, actual});
        return Context.toBoolean(verdict);
    }

    public static TestResult skipped(int index, TestSuite.Case testCase) {
        if (testCase.hidden) {
            return new TestResult(index, TestResult.Status.SKIPPED, "", "", null, null, "", 0, true);
        }
        return new TestResult(index, TestResult.Status.SKIPPED, testCase.input, testCase.expected,
                null, null, "", 0);
    }

    private static TestResult result(int index, TestResult.Status status, TestSuite.Case tc,
                                     String actual, String error, OutputCapture out, long startNanos) {
        long ms = (System.nanoTime() - startNanos) / 1_000_000L;
        if (tc.hidden) {
            // Hide everything about a hidden case from the student.
            return new TestResult(index, status, "", "", null,
                    error == null ? null : "Error on a hidden test", "", ms, true);
        }
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