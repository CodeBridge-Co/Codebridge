package za.co.eduvos.codebridge.core.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

/**
 * Wraps Android's SpeechRecognizer for long answers. Android stops listening after every pause, so
 * this starts it again while the student is still recording. It reports words and mic levels.
 * It tries offline recognition first.
 *
 * Use it on the main thread only.
 */
public class SpeechRecognizerManager {

    public interface Listener {
        void onPartial(String text);
        void onFinal(String text);
        void onLevel(float rmsDb);
        /** @param fatal true if listening stopped and the user has to act */
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

    /** Start or resume listening. */
    public void start() {
        wantListening = true;
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(appContext);
            recognizer.setRecognitionListener(new Callbacks());
        }
        beginListening();
    }

    /** Stop listening. Words already heard still arrive in onFinal. */
    public void pause() {
        wantListening = false;
        main.removeCallbacksAndMessages(null);
        if (recognizer != null) recognizer.stopListening();
    }

    /** Free the recognizer. Call this in onDestroyView. */
    public void destroy() {
        wantListening = false;
        main.removeCallbacksAndMessages(null);
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.destroy();
            recognizer = null;
        }
    }

    /**
     * Settings to try in order when there is a language or network error:
     * offline SA English, online SA English, online US English. The one that works is kept.
     */
    private static final String[] LANGUAGES = {"en-ZA", "en-ZA", "en-US"};
    private static final boolean[] PREFER_OFFLINE = {true, false, false};
    private int attempt = 0;
    private static final String TAG = "SpeechRecognizer";
    /** Sound level (dB) above which we count it as speech, not silence. */
    private static final float HEARD_SOUND_DB = 2f;
    private static final int NO_MATCH_LIMIT = 3;
    private int noMatchStreak = 0;
    private boolean heardSound = false;

    /** Switch to the next setting. Returns false if there are none left. */
    private boolean tryNextConfiguration() {
        if (attempt >= LANGUAGES.length - 1) return false;
        attempt++;
        if (recognizer != null) recognizer.cancel();
        restartSoon(RESTART_DELAY_MS);
        return true;
    }

    private void beginListening() {
        if (recognizer == null || !wantListening) return;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGES[attempt])
                .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, PREFER_OFFLINE[attempt])
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.getPackageName());
        Log.i(TAG, "startListening language=" + LANGUAGES[attempt] + " offline=" + PREFER_OFFLINE[attempt]);
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
            if (rmsdB > HEARD_SOUND_DB) heardSound = true;
            listener.onLevel(rmsdB);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            String text = first(partialResults, SpeechRecognizer.RESULTS_RECOGNITION);
            if (text != null) {
                noMatchStreak = 0;
                Log.d(TAG, "partial: " + text);
                listener.onPartial(text);
            }
        }

        @Override
        public void onResults(Bundle results) {
            String text = first(results, SpeechRecognizer.RESULTS_RECOGNITION);
            if (text != null) {
                noMatchStreak = 0;
                Log.d(TAG, "final: " + text);
                listener.onFinal(text);
            }
            heardSound = false;
            restartSoon(RESTART_DELAY_MS);
        }

        @Override
        public void onError(int error) {
            Log.w(TAG, "onError " + error);
            switch (error) {
                case SpeechRecognizer.ERROR_NO_MATCH:
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    // Silence is normal, so keep going. But if we keep hearing sound and get no
                    // words, this setting isn't working. Try the next one, or tell the user.
                    noMatchStreak++;
                    Log.d(TAG, "no match/timeout (error " + error + "), streak=" + noMatchStreak + ", heardSound=" + heardSound);
                    if (noMatchStreak >= NO_MATCH_LIMIT && heardSound) {
                        noMatchStreak = 0;
                        heardSound = false;
                        if (tryNextConfiguration()) {
                            listener.onError("Hearing you but no words came back. Trying another recognition setting...", false);
                        } else {
                            listener.onError("Hearing you but no words were recognised (error " + error + "). "
                                    + "Check your internet connection and the phone's speech language, "
                                    + "or tap Submit to type your answer instead.", false);
                            restartSoon(RESTART_DELAY_MS);
                        }
                        return;
                    }
                    restartSoon(RESTART_DELAY_MS);
                    return;
                case SpeechRecognizer.ERROR_CLIENT:
                    // We caused this by cancelling, so ignore it.
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
                    if (tryNextConfiguration()) return;
                    stopAfterError("Speech recognition isn't available right now (error " + error + "). "
                            + "Connect to the internet or install an offline speech pack.");
                    return;
                case 12: // ERROR_LANGUAGE_NOT_SUPPORTED (API 31+)
                case 13: // ERROR_LANGUAGE_UNAVAILABLE (API 31+)
                    if (tryNextConfiguration()) return;
                    stopAfterError("This device has no speech model for English yet (error " + error + ").");
                    return;
                default:
                    stopAfterError("Speech recognition stopped (error " + error + ").");
            }
        }

        private void stopAfterError(String message) {
            wantListening = false;
            attempt = 0; // the next tap starts from the first configuration again
            listener.onError(message, true);
        }
    }
}
