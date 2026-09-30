package za.co.eduvos.codebridge.core.security;

public class SessionLockManager {
    private boolean isLocked = false;

    public boolean isLocked() { return isLocked; }
    public void lockSession() { isLocked = true; }
    public void unlockSession() { isLocked = false; }
}
