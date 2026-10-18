package za.co.eduvos.codebridge.core.speech;

import java.util.function.LongSupplier;

/**
 * One answer's recording session: state, elapsed time (pauses excluded), microphone level and the
 * transcript built from the recognizer's results. It holds no Android types, so it is unit-testable;
 * SpeechRecognizerManager feeds it and the screen reads it.
 */
public class SpeechRecorder {

    public enum State { IDLE, RECORDING, PAUSED, STOPPED }

    /** Android's RMS callback reports roughly -2 dB (silence) to 10 dB (loud speech). */
    private static final float RMS_FLOOR_DB = -2f;
    private static final float RMS_RANGE_DB = 12f;

    private final LongSupplier clockMs;
    private State state = State.IDLE;
    private long accumulatedMs;
    private long segmentStartMs;
    private float level;

    private final StringBuilder committed = new StringBuilder();
    private String partial = "";

    public SpeechRecorder(LongSupplier clockMs) {
        this.clockMs = clockMs;
    }

    public synchronized State getState() { return state; }

    // ---- state transitions: return false (and do nothing) when not allowed from the current state ----

    public synchronized boolean start() {
        if (state != State.IDLE) return false;
        beginSegment();
        return true;
    }

    public synchronized boolean pause() {
        if (state != State.RECORDING) return false;
        accumulatedMs += clockMs.getAsLong() - segmentStartMs;
        state = State.PAUSED;
        level = 0f;
        return true;
    }

    public synchronized boolean resume() {
        if (state != State.PAUSED) return false;
        beginSegment();
        return true;
    }

    public synchronized boolean stop() {
        if (state == State.RECORDING) accumulatedMs += clockMs.getAsLong() - segmentStartMs;
        else if (state != State.PAUSED) return false;
        state = State.STOPPED;
        level = 0f;
        return true;
    }

    /** Back to a blank IDLE session, ready for the next answer. */
    public synchronized void reset() {
        state = State.IDLE;
        accumulatedMs = 0;
        level = 0f;
        committed.setLength(0);
        partial = "";
    }

    private void beginSegment() {
        segmentStartMs = clockMs.getAsLong();
        state = State.RECORDING;
    }

    // ---- timing and level ----

    public synchronized long elapsedMs() {
        return state == State.RECORDING ? accumulatedMs + (clockMs.getAsLong() - segmentStartMs) : accumulatedMs;
    }

    /** Converts the recognizer's RMS value (dB) to a 0..1 bar height. */
    public static float normalizeRms(float rmsDb) {
        return Math.max(0f, Math.min(1f, (rmsDb - RMS_FLOOR_DB) / RMS_RANGE_DB));
    }

    public synchronized void onRms(float rmsDb) {
        if (state == State.RECORDING) level = normalizeRms(rmsDb);
    }

    /** Latest microphone level, 0..1; 0 whenever not recording. */
    public synchronized float level() { return level; }

    // ---- transcript ----

    /** Words recognised so far in the current utterance; replaces the previous partial. Ignored unless recording. */
    public synchronized void onPartialResult(String text) {
        if (state == State.RECORDING) partial = text == null ? "" : text.trim();
    }

    /**
     * A finished utterance. Accepted while recording, paused or stopped, because the recognizer
     * delivers its last result just after the user taps pause or stop.
     */
    public synchronized void onFinalResult(String text) {
        if (state == State.IDLE) return;
        partial = "";
        if (text == null || text.trim().isEmpty()) return;
        if (committed.length() > 0) committed.append(' ');
        committed.append(text.trim());
    }

    /** Everything said so far, including the utterance still being recognised. */
    public synchronized String transcript() {
        if (partial.isEmpty()) return committed.toString();
        return committed.length() == 0 ? partial : committed + " " + partial;
    }

    public synchronized int wordCount() {
        String t = transcript();
        return t.isEmpty() ? 0 : t.split("\\s+").length;
    }
}
