package za.co.eduvos.codebridge.core.security;

public interface TelemetrySink {
    void onAppSwitch(long timestampMs);
    void onFocusLoss(long timestampMs);
    void onClipboardPaste(String pastedText, long timestampMs);
    void onAiAssistAttempt(long timestampMs);
}
