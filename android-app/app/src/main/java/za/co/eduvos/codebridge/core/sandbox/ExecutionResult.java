package za.co.eduvos.codebridge.core.sandbox;

import java.util.Collections;
import java.util.List;

/** Everything the UI needs after a Run/Submit. */
public class ExecutionResult {

    public final List<TestResult> results;
    /** Set when the code failed to compile or the suite was unusable; results is then empty. */
    public final String fatalError;
    public final boolean timedOut;
    public final long totalMs;

    public ExecutionResult(List<TestResult> results, String fatalError, boolean timedOut, long totalMs) {
        this.results = Collections.unmodifiableList(results);
        this.fatalError = fatalError;
        this.timedOut = timedOut;
        this.totalMs = totalMs;
    }

    public static ExecutionResult fatal(String message, long totalMs) {
        return new ExecutionResult(Collections.<TestResult>emptyList(), message, false, totalMs);
    }

    public int passedCount() {
        int n = 0;
        for (TestResult r : results) {
            if (r.isPassed()) n++;
        }
        return n;
    }

    public int totalCount() {
        return results.size();
    }

    public boolean allPassed() {
        return fatalError == null && !results.isEmpty() && passedCount() == results.size();
    }
}
