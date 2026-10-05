package za.co.eduvos.codebridge.core.sandbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OutputCaptureTest {

    @Test
    public void drainReturnsLinesAndClears() {
        OutputCapture c = new OutputCapture();
        c.println("a");
        c.println("b");
        assertEquals("a\nb\n", c.drain());
        assertEquals("", c.drain());
    }

    @Test
    public void capsOutputAndFlagsTruncation() {
        OutputCapture c = new OutputCapture();
        for (int i = 0; i < 5000; i++) c.println("0123456789");
        String out = c.drain();
        assertTrue(out.endsWith("[output truncated]\n"));
        assertTrue(out.length() <= OutputCapture.MAX_CHARS + 30);
        c.println("fresh");
        assertEquals("fresh\n", c.drain());
    }
}