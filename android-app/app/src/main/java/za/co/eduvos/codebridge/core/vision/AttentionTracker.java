package za.co.eduvos.codebridge.core.vision;

import java.util.Arrays;

/**
 * Evaluates face tracking data to determine user attention state.
 */
public class AttentionTracker {

    public enum State { ATTENTIVE, LOOKING_AWAY, EYES_CLOSED, NO_FACE, MULTIPLE_FACES }

    public static final class Config {
        public final float yawThreshold;
        public final float pitchThreshold;
        public final float eyeClosureLimit;
        public final long maxGapTolerance;
        public final long minEpisodeDuration;

        public Config(float yawThreshold, float pitchThreshold, float eyeClosureLimit, long maxGapTolerance, long minEpisodeDuration) {
            this.yawThreshold = yawThreshold;
            this.pitchThreshold = pitchThreshold;
            this.eyeClosureLimit = eyeClosureLimit;
            this.maxGapTolerance = maxGapTolerance;
            this.minEpisodeDuration = minEpisodeDuration;
        }

        public static Config defaults() {
            // 25 degrees allows for natural slight head movements without triggering a distraction event
            return new Config(25.0f, 25.0f, 0.3f, 1000L, 1500L);
        }
    }

    public static final class Summary {
        public final long totalDurationMs;
        public final long focusedMs;
        public final long distractedMs;
        public final long closedEyesMs;
        public final long missingFaceMs;
        public final long multipleFacesMs;
        public final float focusRatio;
        public final int distractedCount;
        public final int missingFaceCount;
        public final int multipleFacesCount;
        public final long maxDistractionTimeMs;

        Summary(long[] durations, int[] counts, long maxDistracted) {
            this.focusedMs = durations[State.ATTENTIVE.ordinal()];
            this.distractedMs = durations[State.LOOKING_AWAY.ordinal()];
            this.closedEyesMs = durations[State.EYES_CLOSED.ordinal()];
            this.missingFaceMs = durations[State.NO_FACE.ordinal()];
            this.multipleFacesMs = durations[State.MULTIPLE_FACES.ordinal()];

            this.totalDurationMs = focusedMs + distractedMs + closedEyesMs + missingFaceMs + multipleFacesMs;
            // Prevent division by zero if the session was immediately aborted
            this.focusRatio = (totalDurationMs > 0) ? ((float) focusedMs / totalDurationMs) : 0.0f;

            this.distractedCount = counts[State.LOOKING_AWAY.ordinal()];
            this.missingFaceCount = counts[State.NO_FACE.ordinal()];
            this.multipleFacesCount = counts[State.MULTIPLE_FACES.ordinal()];
            this.maxDistractionTimeMs = maxDistracted;
        }
    }

    private final Config settings;
    private final long[] accumulatedTime = new long[State.values().length];
    private final int[] stateOccurrences = new int[State.values().length];
    private long peakDistractionMs;

    private FaceSample previousRecord;
    private State activeCondition;
    private long activeConditionTimer;

    public AttentionTracker() {
        this.settings = Config.defaults();
    }

    public AttentionTracker(Config config) {
        this.settings = config;
    }

    public synchronized State classify(FaceSample sample) {
        if (sample.getDetectedFaces() == 0) {
            return State.NO_FACE;
        }
        if (sample.getDetectedFaces() > 1) {
            return State.MULTIPLE_FACES;
        }

        boolean isLookingAway = Math.abs(sample.getYawDegrees()) > settings.yawThreshold
                || Math.abs(sample.getPitchDegrees()) > settings.pitchThreshold;

        if (isLookingAway) {
            return State.LOOKING_AWAY;
        }

        // Both eyes must drop below the threshold to filter out winking or uneven blinks
        boolean eyesAreClosed = sample.hasEyeMetrics()
                && sample.getLeftEyeProbability() < settings.eyeClosureLimit
                && sample.getRightEyeProbability() < settings.eyeClosureLimit;

        if (eyesAreClosed) {
            return State.EYES_CLOSED;
        }

        return State.ATTENTIVE;
    }

    public synchronized boolean onSample(FaceSample sample) {
        if (previousRecord != null) {
            long timeDelta = sample.getCaptureTimeMs() - previousRecord.getCaptureTimeMs();

            // Ignore backwards jumping timestamps or duplicate frames
            if (timeDelta <= 0) {
                return false;
            }

            // Cap the time delta so app pauses or camera stutters don't wildly skew the session totals
            long validDelta = Math.min(timeDelta, settings.maxGapTolerance);
            accumulatedTime[activeCondition.ordinal()] += validDelta;
            activeConditionTimer += validDelta;
        }

        State evaluatedState = classify(sample);
        if (activeCondition != null && evaluatedState != activeCondition) {
            finalizeCurrentState();
            activeConditionTimer = 0;
        }

        activeCondition = evaluatedState;
        previousRecord = sample;
        return true;
    }

    public synchronized Summary summary() {
        int[] currentCounts = stateOccurrences.clone();
        long maxDistractionRecord = peakDistractionMs;

        // If an episode is currently active, include it in the live summary counts provided it meets the minimum duration
        if (activeCondition != null && activeCondition != State.ATTENTIVE && activeConditionTimer >= settings.minEpisodeDuration) {
            currentCounts[activeCondition.ordinal()]++;
            if (activeCondition == State.LOOKING_AWAY) {
                maxDistractionRecord = Math.max(maxDistractionRecord, activeConditionTimer);
            }
        }
        return new Summary(accumulatedTime.clone(), currentCounts, maxDistractionRecord);
    }

    public synchronized void reset() {
        Arrays.fill(accumulatedTime, 0);
        Arrays.fill(stateOccurrences, 0);
        peakDistractionMs = 0;
        previousRecord = null;
        activeCondition = null;
        activeConditionTimer = 0;
    }

    private void finalizeCurrentState() {
        // Only count non-attentive stretches that last long enough to be considered a true episode (filters out brief blips)
        if (activeCondition == State.ATTENTIVE || activeConditionTimer < settings.minEpisodeDuration) {
            return;
        }
        stateOccurrences[activeCondition.ordinal()]++;
        if (activeCondition == State.LOOKING_AWAY) {
            peakDistractionMs = Math.max(peakDistractionMs, activeConditionTimer);
        }
    }
}