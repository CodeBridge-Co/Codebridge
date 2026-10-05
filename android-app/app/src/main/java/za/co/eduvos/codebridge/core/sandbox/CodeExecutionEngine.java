package za.co.eduvos.codebridge.core.sandbox;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Runs user JavaScript against a problem's test suite using Rhino in interpreted mode
 * (no bytecode generation, which Android can't load). A hard time budget is enforced by the
 * instruction observer, so infinite loops are aborted rather than hanging the worker.
 *
 * execute() blocks the calling thread; executeAsync() runs on a private worker thread and
 * invokes the callback on that thread, so UI code must post back to the main thread.
 */
public class CodeExecutionEngine {

    public interface Callback {
        void onResult(ExecutionResult result);
    }

    public static final long DEFAULT_BUDGET_MS = 2000;

    static final String TIMER_KEY = "codebridge.timer";
    private static final int OBSERVER_THRESHOLD = 10_000;
    private static final int MAX_STACK_DEPTH = 400;
    private static final long WORKER_STACK_BYTES = 4L * 1024 * 1024;

    private static class SandboxContextFactory extends ContextFactory {
        @Override
        protected Context makeContext() {
            Context cx = super.makeContext();
            cx.setOptimizationLevel(-1);
            cx.setLanguageVersion(Context.VERSION_ES6);
            cx.setInstructionObserverThreshold(OBSERVER_THRESHOLD);
            cx.setMaximumInterpreterStackDepth(MAX_STACK_DEPTH);
            return cx;
        }

        @Override
        protected void observeInstructionCount(Context cx, int instructionCount) {
            ExecutionTimer timer = (ExecutionTimer) cx.getThreadLocal(TIMER_KEY);
            if (timer != null && timer.isExpired()) {
                throw new TestCaseRunner.SandboxTimeoutError();
            }
        }
    }

    private final ContextFactory factory = new SandboxContextFactory();
    private final TestCaseRunner runner = new TestCaseRunner();
    private final long budgetMs;
    private final ExecutorService worker;

    public CodeExecutionEngine() {
        this(DEFAULT_BUDGET_MS);
    }

    public CodeExecutionEngine(long budgetMs) {
        this.budgetMs = budgetMs;
        this.worker = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(null, r, "codebridge-sandbox", WORKER_STACK_BYTES);
                t.setDaemon(true);
                return t;
            }
        });
    }

    public void executeAsync(final String code, final String testCasesJson, final Callback callback) {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                callback.onResult(execute(code, testCasesJson));
            }
        });
    }

    public void shutdown() {
        worker.shutdownNow();
    }

    public ExecutionResult execute(final String code, final String testCasesJson) {
        final ExecutionTimer timer = new ExecutionTimer(budgetMs);

        final TestSuite suite;
        try {
            suite = TestSuiteParser.parse(testCasesJson);
        } catch (TestSuiteParser.ParseException e) {
            return ExecutionResult.fatal(e.getMessage(), timer.elapsedMs());
        }
        if (code == null || code.trim().isEmpty()) {
            return ExecutionResult.fatal("No code to run", timer.elapsedMs());
        }

        return factory.call(cx -> {
            cx.putThreadLocal(TIMER_KEY, timer);
            Script script;
            try {
                script = cx.compileString(code, "solution.js", 1, null);
            } catch (RhinoException e) {
                return ExecutionResult.fatal("Syntax error: " + e.getMessage(), timer.elapsedMs());
            }

            OutputCapture out = new OutputCapture();
            List<TestResult> results = new ArrayList<>();
            boolean timedOut = false;
            for (int i = 0; i < suite.cases.size(); i++) {
                TestSuite.Case tc = suite.cases.get(i);
                if (timedOut || timer.isExpired()) {
                    timedOut = true;
                    results.add(TestCaseRunner.skipped(i, tc));
                    continue;
                }
                TestResult r = runner.run(cx, script, tc, i, suite.entryPoint, out);
                results.add(r);
                if (r.status == TestResult.Status.TIMEOUT) timedOut = true;
            }
            return new ExecutionResult(results, null, timedOut, timer.elapsedMs());
        });
    }
}