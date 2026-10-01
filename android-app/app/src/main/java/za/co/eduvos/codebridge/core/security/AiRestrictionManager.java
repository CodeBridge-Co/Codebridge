package za.co.eduvos.codebridge.core.security;
import android.content.Context;

public class AiRestrictionManager {

    private static final int MAX_AI_USES = 3;
    private static final String PREF_KEY_AI_USES = "ai_uses_current_session";

    private final SecurePreferences securePreferences;

    public AiRestrictionManager(Context context) {
        this.securePreferences = new SecurePreferences(context);
    }

    /**
     * Checks if the student can use AI assistance in the current session.
     */
    public boolean canUseAi() {
        int uses = securePreferences.getInt(PREF_KEY_AI_USES, 0);
        return uses < MAX_AI_USES;
    }

    /**
     * Records a successful AI assistance use.
     */
    public void recordAiUse() {
        int uses = securePreferences.getInt(PREF_KEY_AI_USES, 0);
        securePreferences.putInt(PREF_KEY_AI_USES, uses + 1);
    }

    /**
     * Resets the counter for a new session.
     */
    public void resetForSession() {
        securePreferences.putInt(PREF_KEY_AI_USES, 0);
    }
}

