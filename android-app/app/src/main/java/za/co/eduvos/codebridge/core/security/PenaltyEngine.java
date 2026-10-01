package za.co.eduvos.codebridge.core.security;

public class PenaltyEngine {

    private static final int MAX_APP_SWITCHES = 3;
    private static final int MAX_FOCUS_LOSSES = 5;
    
    private int appSwitchCount = 0;
    private int focusLossCount = 0;
    
    private final SessionLockManager lockManager;

    public PenaltyEngine(SessionLockManager lockManager) {
        this.lockManager = lockManager;
    }

    public void onAppSwitch() {
        appSwitchCount++;
        if (appSwitchCount >= MAX_APP_SWITCHES) {
            applyPenalty("Excessive app switching");
        }
    }

    public void onFocusLoss() {
        focusLossCount++;
        if (focusLossCount >= MAX_FOCUS_LOSSES) {
            applyPenalty("Excessive focus loss");
        }
    }

    private void applyPenalty(String reason) {
        // TODO: Deduct XP, mark session as failed, etc.
        lockManager.lockSession();
    }
    
    public void reset() {
        appSwitchCount = 0;
        focusLossCount = 0;
    }
}
