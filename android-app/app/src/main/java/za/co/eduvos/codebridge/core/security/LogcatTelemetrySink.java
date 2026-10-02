package za.co.eduvos.codebridge.core.security;

import android.util.Log;

public class LogcatTelemetrySink implements TelemetrySink {
    private static final String TAG = "CodeBridgeTelemetry";

    @Override
    public void onAppSwitch(long timestampMs) {
        Log.w(TAG, "APP_SWITCH at " + timestampMs);
    }

    @Override
    public void onFocusLoss(long timestampMs) {
        Log.w(TAG, "FOCUS_LOSS at " + timestampMs);
    }

    @Override
    public void onClipboardPaste(String pastedText, long timestampMs) {
        Log.w(TAG, "CLIPBOARD_PASTE len=" + (pastedText != null ? pastedText.length() : 0) + " at " + timestampMs);
    }

    @Override
    public void onAiAssistAttempt(long timestampMs) {
        Log.w(TAG, "AI_ASSIST_ATTEMPT at " + timestampMs);
    }
}
