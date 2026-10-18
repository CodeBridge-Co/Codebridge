package za.co.eduvos.codebridge.core.git;

import java.util.function.LongSupplier;

/** Aggregates how the student handled the Git stage, for scoring and the behavioural log. */
public class GitTelemetryCollector {

    public static final class Summary {
        public final int totalCommands;
        public final int errorCommands;
        public final int unsupportedCommands;
        public final int markersStaged;
        public final boolean conflictEncountered;
        public final boolean conflictResolved;
        /** Milliseconds from the conflict appearing to the last file being marked resolved; -1 if not resolved. */
        public final long timeToResolveMs;
        public final boolean taskCompleted;
        /** Milliseconds from the first command to completion; -1 if not completed. */
        public final long timeToCompleteMs;

        Summary(int totalCommands, int errorCommands, int unsupportedCommands, int markersStaged,
                boolean conflictEncountered, boolean conflictResolved, long timeToResolveMs,
                boolean taskCompleted, long timeToCompleteMs) {
            this.totalCommands = totalCommands;
            this.errorCommands = errorCommands;
            this.unsupportedCommands = unsupportedCommands;
            this.markersStaged = markersStaged;
            this.conflictEncountered = conflictEncountered;
            this.conflictResolved = conflictResolved;
            this.timeToResolveMs = timeToResolveMs;
            this.taskCompleted = taskCompleted;
            this.timeToCompleteMs = timeToCompleteMs;
        }
    }

    private final LongSupplier clock;
    private int total, errors, unsupported, markersStaged;
    private long firstCommandAt = -1, conflictAt = -1, resolvedAt = -1, completedAt = -1;

    public GitTelemetryCollector(LongSupplier clock) {
        this.clock = clock;
    }

    synchronized void onCommand(boolean isError, boolean wasUnsupported) {
        if (firstCommandAt < 0) firstCommandAt = clock.getAsLong();
        total++;
        if (isError) errors++;
        if (wasUnsupported) unsupported++;
    }

    synchronized void onConflictEntered() {
        if (conflictAt < 0) conflictAt = clock.getAsLong();
        resolvedAt = -1;
    }

    synchronized void onConflictsCleared() {
        resolvedAt = clock.getAsLong();
    }

    synchronized void onMarkersStaged() {
        markersStaged++;
    }

    synchronized void onTaskCompleted() {
        if (completedAt < 0) completedAt = clock.getAsLong();
    }

    public synchronized Summary snapshot() {
        boolean resolved = conflictAt >= 0 && resolvedAt >= 0;
        return new Summary(total, errors, unsupported, markersStaged,
                conflictAt >= 0, resolved, resolved ? resolvedAt - conflictAt : -1,
                completedAt >= 0, completedAt >= 0 && firstCommandAt >= 0 ? completedAt - firstCommandAt : -1);
    }
}
