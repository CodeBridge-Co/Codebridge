package za.co.eduvos.codebridge.core.speech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import za.co.eduvos.codebridge.core.speech.SpeechRecorder.State;

public class SpeechRecorderTest {

    private AtomicLong now;
    private SpeechRecorder rec;

    @Before
    public void setUp() {
        now = new AtomicLong(1_000L);
        rec = new SpeechRecorder(now::get);
    }

    @Test
    public void startsIdleWithNothingRecorded() {
        assertEquals(State.IDLE, rec.getState());
        assertEquals(0, rec.elapsedMs());
        assertEquals("", rec.transcript());
        assertEquals(0, rec.wordCount());
    }

    @Test
    public void elapsedTimeCountsOnlyRecordingTime() {
        rec.start();
        now.addAndGet(2_000);
        assertEquals(2_000, rec.elapsedMs());

        rec.pause();
        now.addAndGet(10_000);                      // paused time must not count
        assertEquals(2_000, rec.elapsedMs());

        rec.resume();
        now.addAndGet(1_500);
        assertEquals(3_500, rec.elapsedMs());

        rec.stop();
        now.addAndGet(5_000);
        assertEquals(3_500, rec.elapsedMs());
        assertEquals(State.STOPPED, rec.getState());
    }

    @Test
    public void illegalTransitionsAreRejectedWithoutSideEffects() {
        assertFalse(rec.pause());
        assertFalse(rec.resume());
        assertFalse(rec.stop());
        assertTrue(rec.start());
        assertFalse(rec.start());
        assertFalse(rec.resume());
        assertTrue(rec.pause());
        assertFalse(rec.pause());
        assertTrue(rec.stop());
        assertFalse(rec.start());
        assertFalse(rec.resume());
        assertEquals(State.STOPPED, rec.getState());
    }

    @Test
    public void stopWhilePausedKeepsElapsed() {
        rec.start();
        now.addAndGet(1_000);
        rec.pause();
        now.addAndGet(4_000);
        assertTrue(rec.stop());
        assertEquals(1_000, rec.elapsedMs());
    }

    @Test
    public void resetReturnsToABlankIdleSession() {
        rec.start();
        now.addAndGet(500);
        rec.onFinalResult("something");
        rec.reset();
        assertEquals(State.IDLE, rec.getState());
        assertEquals(0, rec.elapsedMs());
        assertEquals("", rec.transcript());
        assertTrue(rec.start());
    }

    @Test
    public void rmsIsNormalisedAndClamped() {
        assertEquals(0f, SpeechRecorder.normalizeRms(-2f), 1e-6f);
        assertEquals(0f, SpeechRecorder.normalizeRms(-10f), 1e-6f);
        assertEquals(0.5f, SpeechRecorder.normalizeRms(4f), 1e-6f);
        assertEquals(1f, SpeechRecorder.normalizeRms(10f), 1e-6f);
        assertEquals(1f, SpeechRecorder.normalizeRms(40f), 1e-6f);
    }

    @Test
    public void levelOnlyTracksWhileRecordingAndDropsToZeroOnPause() {
        rec.onRms(10f);
        assertEquals(0f, rec.level(), 0f);          // not started yet
        rec.start();
        rec.onRms(4f);
        assertEquals(0.5f, rec.level(), 1e-6f);
        rec.pause();
        assertEquals(0f, rec.level(), 0f);
        rec.onRms(10f);
        assertEquals(0f, rec.level(), 0f);
    }

    @Test
    public void partialResultsReplaceEachOtherAndFinalCommits() {
        rec.start();
        rec.onPartialResult("I would");
        assertEquals("I would", rec.transcript());
        rec.onPartialResult("I would use a hash");
        assertEquals("I would use a hash", rec.transcript());
        rec.onFinalResult("I would use a hash map");
        assertEquals("I would use a hash map", rec.transcript());

        rec.onPartialResult("then I loop");
        assertEquals("I would use a hash map then I loop", rec.transcript());
        rec.onFinalResult("then I loop over it");
        assertEquals("I would use a hash map then I loop over it", rec.transcript());
        assertEquals(11, rec.wordCount());
    }

    @Test
    public void lateFinalResultAfterPauseOrStopIsStillKept() {
        rec.start();
        rec.onPartialResult("the complexity is");
        rec.pause();
        rec.onFinalResult("the complexity is linear");
        assertEquals("the complexity is linear", rec.transcript());

        rec.stop();
        rec.onFinalResult("and constant space");
        assertEquals("the complexity is linear and constant space", rec.transcript());
    }

    @Test
    public void partialsAreIgnoredWhenNotRecordingAndNothingIsAcceptedWhenIdle() {
        rec.onPartialResult("ignored");
        rec.onFinalResult("ignored");
        assertEquals("", rec.transcript());

        rec.start();
        rec.pause();
        rec.onPartialResult("late partial");
        assertEquals("", rec.transcript());
    }

    @Test
    public void blankAndNullResultsAddNothing() {
        rec.start();
        rec.onFinalResult("   ");
        rec.onFinalResult(null);
        rec.onPartialResult(null);
        assertEquals("", rec.transcript());
        rec.onFinalResult("  hello  ");
        rec.onFinalResult("world ");
        assertEquals("hello world", rec.transcript());
    }
}
