package za.co.eduvos.codebridge.core.security;

public interface TelemetryLogger {
    void logAppSwitch();
    void logFocusLoss();
    void logClipboardPaste(String pastedText);
}
