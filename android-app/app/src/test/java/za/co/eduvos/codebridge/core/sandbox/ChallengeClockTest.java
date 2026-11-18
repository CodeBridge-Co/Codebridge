package za.co.eduvos.codebridge.core.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ChallengeClockTest {

    private static final long START = 1_000_000L;
    private static final long MIN = 60_000L;

    @Test
    public void countsDownFromTheLimit() {
        ChallengeClock c = new ChallengeClock(START, 10);
        assertEquals(10 * MIN, c.limitMs());
        assertEquals(10 * MIN, c.remainingMs(START));
        assertEquals(4 * MIN, c.remainingMs(START + 6 * MIN));
        assertEquals(6 * MIN, c.elapsedMs(START + 6 * MIN));
    }

    @Test
    public void expiresExactlyAtTheLimitAndStaysClamped() {
        ChallengeClock c = new ChallengeClock(START, 10);
        assertFalse(c.isExpired(START + 10 * MIN - 1));
        assertTrue(c.isExpired(START + 10 * MIN));
        assertTrue(c.isExpired(START + 99 * MIN));
        assertEquals(0, c.remainingMs(START + 99 * MIN));
        assertEquals(10 * MIN, c.elapsedMs(START + 99 * MIN));
    }

    @Test
    public void clockSetBackwardsNeverGoesNegativeOrAboveTheLimit() {
        ChallengeClock c = new ChallengeClock(START, 10);
        assertEquals(0, c.elapsedMs(START - 5 * MIN));
        assertEquals(10 * MIN, c.remainingMs(START - 5 * MIN));
    }

    @Test
    public void levelTurnsWarningThenCritical() {
        ChallengeClock c = new ChallengeClock(START, 20); // 20% = 4 min left, 5% = 1 min left
        assertEquals(ChallengeClock.Level.NORMAL, c.level(START));
        assertEquals(ChallengeClock.Level.NORMAL, c.level(START + 15 * MIN));       // 5 min left
        assertEquals(ChallengeClock.Level.WARNING, c.level(START + 16 * MIN));      // 4 min left
        assertEquals(ChallengeClock.Level.WARNING, c.level(START + 18 * MIN));      // 2 min left
        assertEquals(ChallengeClock.Level.CRITICAL, c.level(START + 19 * MIN));     // 1 min left
        assertEquals(ChallengeClock.Level.CRITICAL, c.level(START + 20 * MIN));
    }

    @Test
    public void formatsMinutesSecondsAndRoundsPartialSecondsUp() {
        assertEquals("00:00", ChallengeClock.format(0));
        assertEquals("00:01", ChallengeClock.format(1));
        assertEquals("00:59", ChallengeClock.format(59_000));
        assertEquals("01:00", ChallengeClock.format(60_000));
        assertEquals("15:00", ChallengeClock.format(15 * MIN));
        assertEquals("1:05:00", ChallengeClock.format(65 * MIN));
        assertEquals("00:00", ChallengeClock.format(-5));
    }

    @Test
    public void defaultLimitsFollowDifficulty() {
        assertEquals(10, ChallengeClock.defaultMinutes("Easy"));
        assertEquals(25, ChallengeClock.defaultMinutes("medium"));
        assertEquals(40, ChallengeClock.defaultMinutes(" HARD "));
        assertEquals(10, ChallengeClock.defaultMinutes("anything else"));
        assertEquals(10, ChallengeClock.defaultMinutes(null));
    }

    @Test
    public void rejectsNonPositiveLimit() {
        try {
            new ChallengeClock(START, 0);
            fail("limit 0 must be rejected");
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }

    @Test
    public void parserReadsTimeLimitAndDefaultsToZero() throws Exception {
        TestSuite with = TestSuiteParser.parse(
                "{\"timeLimitMinutes\":25,\"cases\":[{\"input\":\"1\",\"expected\":\"1\"}]}");
        assertEquals(25, with.timeLimitMinutes);
        TestSuite without = TestSuiteParser.parse("[{\"input\":\"1\",\"expected\":\"1\"}]");
        assertEquals(0, without.timeLimitMinutes);
    }

    @Test
    public void parserRejectsNonPositiveTimeLimit() {
        try {
            TestSuiteParser.parse("{\"timeLimitMinutes\":0,\"cases\":[{\"input\":\"1\",\"expected\":\"1\"}]}");
            fail("timeLimitMinutes 0 must be rejected");
        } catch (TestSuiteParser.ParseException expected) {
            // ok
        }
    }
}
