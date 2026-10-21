package za.co.eduvos.codebridge.core.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import za.co.eduvos.codebridge.core.vision.AttentionTracker.State;

public class AttentionTrackerTest {

    private AttentionTracker monitor;

    private static FaceSample buildAttentiveFrame(long timestamp) {
        return FaceSample.createTracked(timestamp, 0.0f, 0.0f, 0.0f, 0.9f, 0.9f);
    }

    private static FaceSample buildDistractedFrame(long timestamp) {
        return FaceSample.createTracked(timestamp, 40.0f, 0.0f, 0.0f, 0.9f, 0.9f);
    }

    @Before
    public void setup() {
        monitor = new AttentionTracker();
    }

    @Test
    public void verifyStateClassification() {
        assertEquals(State.ATTENTIVE, monitor.classify(buildAttentiveFrame(0)));
        assertEquals(State.NO_FACE, monitor.classify(FaceSample.createEmpty(0)));
        assertEquals(State.MULTIPLE_FACES, monitor.classify(FaceSample.createCrowded(0, 2)));
        assertEquals(State.LOOKING_AWAY, monitor.classify(buildDistractedFrame(0)));
        assertEquals(State.LOOKING_AWAY, monitor.classify(FaceSample.createTracked(0, -40.0f, 0.0f, 0.0f, 0.9f, 0.9f)));
        assertEquals(State.LOOKING_AWAY, monitor.classify(FaceSample.createTracked(0, 0.0f, 30.0f, 0.0f, 0.9f, 0.9f)));
        assertEquals(State.EYES_CLOSED, monitor.classify(FaceSample.createTracked(0, 0.0f, 0.0f, 0.0f, 0.1f, 0.2f)));
    }

    @Test
    public void missingEyeMetricsDoNotTriggerClosedState() {
        assertEquals(State.ATTENTIVE,
                monitor.classify(FaceSample.createTracked(0, 0.0f, 0.0f, 0.0f, FaceSample.UNTRACKED_METRIC, FaceSample.UNTRACKED_METRIC)));
        // A single open eye is sufficient to remain attentive
        assertEquals(State.ATTENTIVE,
                monitor.classify(FaceSample.createTracked(0, 0.0f, 0.0f, 0.0f, 0.1f, 0.9f)));
    }

    @Test
    public void boundariesAreEvaluatedExclusively() {
        assertEquals(State.ATTENTIVE, monitor.classify(FaceSample.createTracked(0, 25.0f, 25.0f, 0.0f, 0.9f, 0.9f)));
        assertEquals(State.LOOKING_AWAY, monitor.classify(FaceSample.createTracked(0, 25.1f, 0.0f, 0.0f, 0.9f, 0.9f)));
    }

    @Test
    public void lookingAwayOverridesClosedEyes() {
        assertEquals(State.LOOKING_AWAY, monitor.classify(FaceSample.createTracked(0, 50.0f, 0.0f, 0.0f, 0.0f, 0.0f)));
    }

    @Test
    public void timeDeltasAreAttributedToPreviousState() {
        monitor.onSample(buildAttentiveFrame(0));
        monitor.onSample(buildDistractedFrame(1000));
        monitor.onSample(buildAttentiveFrame(2000));

        AttentionTracker.Summary result = monitor.summary();
        assertEquals(1000, result.focusedMs);
        assertEquals(1000, result.distractedMs);
        assertEquals(2000, result.totalDurationMs);
        assertEquals(0.5f, result.focusRatio, 1e-6f);
    }

    @Test
    public void gapsExceedingToleranceAreCapped() {
        monitor.onSample(buildAttentiveFrame(0));
        monitor.onSample(buildAttentiveFrame(10000));
        assertEquals(1000, monitor.summary().focusedMs);
    }

    @Test
    public void ignoresInvalidOrDuplicateTimestamps() {
        assertTrue(monitor.onSample(buildAttentiveFrame(1000)));
        assertFalse(monitor.onSample(buildAttentiveFrame(1000)));
        assertFalse(monitor.onSample(buildAttentiveFrame(500)));
        assertEquals(0, monitor.summary().totalDurationMs);
    }

    @Test
    public void evaluatesEpisodeDurationThresholds() {
        // Brief distraction (500ms) should not increment the count
        monitor.onSample(buildAttentiveFrame(0));
        monitor.onSample(buildDistractedFrame(500));
        monitor.onSample(buildAttentiveFrame(1000));
        assertEquals(0, monitor.summary().distractedCount);

        // Extended distraction (2000ms) should trigger an episode
        monitor.onSample(buildDistractedFrame(1500));
        monitor.onSample(buildDistractedFrame(2500));
        monitor.onSample(buildDistractedFrame(3500));
        monitor.onSample(buildAttentiveFrame(3600));

        AttentionTracker.Summary result = monitor.summary();
        assertEquals(1, result.distractedCount);
        assertTrue(result.maxDistractionTimeMs >= 2000);
    }

    @Test
    public void includesActiveEpisodesInLiveSummary() {
        monitor.onSample(buildAttentiveFrame(0));
        for (long t = 500; t <= 3000; t += 500) {
            monitor.onSample(buildDistractedFrame(t));
        }

        AttentionTracker.Summary result = monitor.summary();
        assertEquals(1, result.distractedCount);
        // Ensure subsequent calls don't increment the episode counter erroneously
        assertEquals(1, monitor.summary().distractedCount);
    }

    @Test
    public void isolatesMissingFaceFromMultipleFaces() {
        monitor.onSample(buildAttentiveFrame(0));
        for (long t = 1000; t <= 3000; t += 1000) {
            monitor.onSample(FaceSample.createEmpty(t));
        }
        for (long t = 4000; t <= 6000; t += 1000) {
            monitor.onSample(FaceSample.createCrowded(t, 2));
        }
        monitor.onSample(buildAttentiveFrame(7000));

        AttentionTracker.Summary result = monitor.summary();
        assertEquals(1, result.missingFaceCount);
        assertEquals(1, result.multipleFacesCount);
        assertEquals(0, result.distractedCount);
        assertTrue(result.missingFaceMs > 0);
        assertTrue(result.multipleFacesMs > 0);
    }

    @Test
    public void briefBlinksAreIgnored() {
        monitor.onSample(buildAttentiveFrame(0));
        monitor.onSample(FaceSample.createTracked(200, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        monitor.onSample(buildAttentiveFrame(400));
        monitor.onSample(buildAttentiveFrame(600));

        AttentionTracker.Summary result = monitor.summary();
        assertEquals(0, result.distractedCount);
        assertTrue(result.focusRatio > 0.6f);
    }

    @Test
    public void freshInstanceReportsDefaults() {
        AttentionTracker.Summary result = monitor.summary();
        assertEquals(0, result.totalDurationMs);
        assertEquals(0.0f, result.focusRatio, 0.0f);
        assertEquals(0, result.distractedCount);
    }

    @Test
    public void resetClearsAllState() {
        monitor.onSample(buildAttentiveFrame(0));
        monitor.onSample(buildAttentiveFrame(900));
        monitor.reset();

        assertEquals(0, monitor.summary().totalDurationMs);
        // Validates that earlier timestamps are accepted after a hard reset
        assertTrue(monitor.onSample(buildAttentiveFrame(100)));
    }

    @Test
    public void respectsCustomConfigurations() {
        AttentionTracker.Config strictConfig = new AttentionTracker.Config(10.0f, 10.0f, 0.3f, 1000L, 1500L);
        AttentionTracker strictMonitor = new AttentionTracker(strictConfig);

        assertEquals(State.LOOKING_AWAY, strictMonitor.classify(FaceSample.createTracked(0, 15.0f, 0.0f, 0.0f, 0.9f, 0.9f)));
        assertEquals(State.ATTENTIVE, monitor.classify(FaceSample.createTracked(0, 15.0f, 0.0f, 0.0f, 0.9f, 0.9f)));
    }
}