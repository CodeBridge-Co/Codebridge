package za.co.eduvos.codebridge.core.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

/**
 * Wraps android.speech.SpeechRecognizer for long, continuous answers. The platform recognizer stops
 * after each pause in speech, so this restarts it while the student is still "recording" and reports
 * partial text, finished utterances and microphone levels. Prefers on-device recognition, so it can
 * work offline where the device has a speech model installed.
 *
 * Must be created and used on the main thread.
 */
public class SpeechRecognizerManager {

    public interface Listener {
        void onPartial(String text);
        void onFinal(String text);
        void onLevel(float rmsDb);
        /** @param fatal true when listening has stopped and cannot continue without the user acting */
        void onError(String message, boolean fatal);
    }

    private static final long RESTART_DELAY_MS = 150;
    private static final long BUSY_RETRY_DELAY_MS = 400;

    private final Context appContext;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private boolean wantListening;

    public SpeechRecognizerManager(Context context, Listener listener) {
        this.appContext = context.getApplicationContext();
        this.listener = listener;
    }

    public static boolean isAvailable(Context context) {
        return SpeechRecognizer.isRecognitionAvailable(context.getApplicationContext());
    }

    /** Begin (or resume) listening. */
    public void start() {
        wantListening = true;
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(appContext);
            recognizer.setRecognitionListener(new Callbacks());
        }
        beginListening();
    }

    /** Stop listening; the utterance in progress is still delivered through onFinal. */
    public void pause() {
        wantListening = false;
        main.removeCallbacksAndMessages(null);
        if (recognizer != null) recognizer.stopListening();
    }

    /** Release the recognizer. Call from onDestroyView. */
    public void destroy() {
        wantListening = false;
        main.removeCallbacksAndMessages(null);
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.destroy();
            recognizer = null;
        }
    }

    private void beginListening() {
        if (recognizer == null || !wantListening) return;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.getPackageName());
        recognizer.startListening(intent);
    }

    private void restartSoon(long delayMs) {
        if (!wantListening) return;
        main.postDelayed(new Runnable() {
            @Override
            public void run() {
                beginListening();
            }
        }, delayMs);
    }

    private static String first(Bundle results, String key) {
        ArrayList<String> list = results == null ? null : results.getStringArrayList(key);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    private class Callbacks implements RecognitionListener {
        @Override public void onReadyForSpeech(Bundle params) { }
        @Override public void onBeginningOfSpeech() { }
        @Override public void onBufferReceived(byte[] buffer) { }
        @Override public void onEndOfSpeech() { }
        @Override public void onEvent(int eventType, Bundle params) { }

        @Override
        public void onRmsChanged(float rmsdB) {
            listener.onLevel(rmsdB);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            String text = first(partialResults, SpeechRecognizer.RESULTS_RECOGNITION);
            if (text != null) listener.onPartial(text);
        }

        @Override
        public void onResults(Bundle results) {
            String text = first(results, SpeechRecognizer.RESULTS_RECOGNITION);
            if (text != null) listener.onFinal(text);
            restartSoon(RESTART_DELAY_MS);
        }

        @Override
        public void onError(int error) {
            switch (error) {
                case SpeechRecognizer.ERROR_NO_MATCH:
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    // Silence or unintelligible audio: normal during a long answer, so keep going.
                    restartSoon(RESTART_DELAY_MS);
                    return;
                case SpeechRecognizer.ERROR_CLIENT:
                    // Raised when we cancel or stop ourselves.
                    return;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    if (recognizer != null) recognizer.cancel();
                    restartSoon(BUSY_RETRY_DELAY_MS);
                    return;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    stopAfterError("Microphone permission is needed to record your answer.");
                    return;
                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                case SpeechRecognizer.ERROR_SERVER:
                    stopAfterError("Offline speech recognition isn't available on this device. "
                            + "Connect to the internet or install an offline speech pack.");
                    return;
                case 12: // ERROR_LANGUAGE_NOT_SUPPORTED (API 31+)
                case 13: // ERROR_LANGUAGE_UNAVAILABLE (API 31+)
                    stopAfterError("This device has no speech model for your language yet.");
                    return;
                default:
                    stopAfterError("Speech recognition stopped (error " + error + ").");
            }
        }

        private void stopAfterError(String message) {
            wantListening = false;
            listener.onError(message, true);
        }
    }
}
