package za.co.eduvos.codebridge.core.sandbox;

/** Outcome of a single test case. */
public class TestResult {

    public enum Status { PASSED, FAILED, ERROR, TIMEOUT, SKIPPED }

    public final int index;
    public final Status status;
    public final String input;
    public final String expected;
    public final String actual;
    public final String error;
    public final String stdout;
    public final long durationMs;

    public TestResult(int index, Status status, String input, String expected,
                      String actual, String error, String stdout, long durationMs) {
        this.index = index;
        this.status = status;
        this.input = input;
        this.expected = expected;
        this.actual = actual;
        this.error = error;
        this.stdout = stdout;
        this.durationMs = durationMs;
    }

    public boolean isPassed() {
        return status == Status.PASSED;
    }
}