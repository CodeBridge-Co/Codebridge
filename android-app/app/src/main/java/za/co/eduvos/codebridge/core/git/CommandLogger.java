package za.co.eduvos.codebridge.core.git;

import java.util.function.LongSupplier;

/** Records every terminal command for a session through a pluggable sink (Room in the app, a list in tests). */
public class CommandLogger {

    public interface Sink {
        void record(int sessionId, String command, boolean isError, long timestamp);
    }

    private final Sink sink;
    private final int sessionId;
    private final LongSupplier clock;

    public CommandLogger(Sink sink, int sessionId, LongSupplier clock) {
        this.sink = sink;
        this.sessionId = sessionId;
        this.clock = clock;
    }

    public void log(String command, boolean isError) {
        sink.record(sessionId, command, isError, clock.getAsLong());
    }
}
