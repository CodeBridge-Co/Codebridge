package za.co.eduvos.codebridge.core.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExecutionTimerTest {

    @Test
    public void expiresOnlyAfterBudget() {
        long[] now = {0};
        ExecutionTimer t = new ExecutionTimer(100, () -> now[0]);
        assertFalse(t.isExpired());
        now[0] = 99_000_000L;
        assertFalse(t.isExpired());
        assertEquals(99, t.elapsedMs());
        now[0] = 100_000_000L;
        assertTrue(t.isExpired());
    }

    @Test
    public void restartResetsElapsed() {
        long[] now = {0};
        ExecutionTimer t = new ExecutionTimer(100, () -> now[0]);
        now[0] = 500_000_000L;
        assertTrue(t.isExpired());
        t.restart();
        assertFalse(t.isExpired());
        assertEquals(0, t.elapsedMs());
    }
}