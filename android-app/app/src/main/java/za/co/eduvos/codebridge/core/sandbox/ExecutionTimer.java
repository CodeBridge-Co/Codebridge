package za.co.eduvos.codebridge.core.sandbox;

import java.util.function.LongSupplier;

public class ExecutionTimer {

    private final LongSupplier nanoClock;
    private final long budgetNanos;
    private long startNanos;

    public ExecutionTimer(long budgetMs) {
        this(budgetMs, System::nanoTime);
    }

    public ExecutionTimer(long budgetMs, LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
        this.budgetNanos = budgetMs * 1_000_000L;
        this.startNanos = nanoClock.getAsLong();
    }

    public void restart() {
        startNanos = nanoClock.getAsLong();
    }

    public long elapsedMs() {
        return (nanoClock.getAsLong() - startNanos) / 1_000_000L;
    }

    public boolean isExpired() {
        return nanoClock.getAsLong() - startNanos >= budgetNanos;
    }
}