package za.co.eduvos.codebridge.core.sandbox;

/** Collects print()/console.log output from user code, capped so a print loop can't exhaust memory. */
public class OutputCapture {

    public static final int MAX_CHARS = 10_000;

    private final StringBuilder buffer = new StringBuilder();
    private boolean truncated;

    public synchronized void println(String line) {
        if (buffer.length() >= MAX_CHARS) {
            truncated = true;
            return;
        }
        int room = MAX_CHARS - buffer.length();
        String text = line + "\n";
        if (text.length() > room) {
            buffer.append(text, 0, room);
            truncated = true;
        } else {
            buffer.append(text);
        }
    }

    /** Returns everything captured since the last drain and clears the buffer. */
    public synchronized String drain() {
        String out = buffer.toString();
        if (truncated) {
            out += "[output truncated]\n";
        }
        buffer.setLength(0);
        truncated = false;
        return out;
    }
}
