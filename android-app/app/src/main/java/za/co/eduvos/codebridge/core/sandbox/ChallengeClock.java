package za.co.eduvos.codebridge.core.sandbox;

import java.util.Locale;

/** Countdown timer for one challenge. It only stores the start time, so it is easy to test. */
public final class ChallengeClock {

    public enum Level { NORMAL, WARNING, CRITICAL }

    /** When the time left drops below these shares, the timer changes colour. */
    private static final double WARNING_FRACTION = 0.20;
    private static final double CRITICAL_FRACTION = 0.05;

    public static final int DEFAULT_EASY_MINUTES = 10;
    public static final int DEFAULT_MEDIUM_MINUTES = 25;
    public static final int DEFAULT_HARD_MINUTES = 40;

    private final long startMs;
    private final long limitMs;

    public ChallengeClock(long startMs, int limitMinutes) {
        if (limitMinutes <= 0) throw new IllegalArgumentException("limitMinutes must be positive");
        this.startMs = startMs;
        this.limitMs = limitMinutes * 60_000L;
    }

    /** Time limit used when the problem has none. */
    public static int defaultMinutes(String difficulty) {
        if (difficulty == null) return DEFAULT_EASY_MINUTES;
        String d = difficulty.trim().toLowerCase(Locale.ROOT);
        if (d.equals("hard")) return DEFAULT_HARD_MINUTES;
        if (d.equals("medium")) return DEFAULT_MEDIUM_MINUTES;
        return DEFAULT_EASY_MINUTES;
    }

    public long limitMs() {
        return limitMs;
    }

    /** Time used so far (never below 0 or above the limit). */
    public long elapsedMs(long nowMs) {
        long e = nowMs - startMs;
        if (e < 0) return 0;
        return Math.min(e, limitMs);
    }

    public long remainingMs(long nowMs) {
        return limitMs - elapsedMs(nowMs);
    }

    public boolean isExpired(long nowMs) {
        return remainingMs(nowMs) <= 0;
    }

    public Level level(long nowMs) {
        long left = remainingMs(nowMs);
        if (left <= limitMs * CRITICAL_FRACTION) return Level.CRITICAL;
        if (left <= limitMs * WARNING_FRACTION) return Level.WARNING;
        return Level.NORMAL;
    }

    /** Turns a time into "mm:ss" (or "h:mm:ss"). Part of a second counts as a full second. */
    public static String format(long durationMs) {
        long totalSeconds = Math.max(0, (durationMs + 999) / 1000);
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return h > 0
                ? String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s)
                : String.format(Locale.ROOT, "%02d:%02d", m, s);
    }
}
