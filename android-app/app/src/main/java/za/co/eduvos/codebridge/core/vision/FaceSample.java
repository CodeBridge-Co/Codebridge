package za.co.eduvos.codebridge.core.vision;

/**
 * Immutable data object representing a single frame's pose and facial landmark metrics.
 * Decoupled from the camera implementation to allow for headless unit testing.
 */
public final class FaceSample {

    public static final float UNTRACKED_METRIC = -1.0f;

    private final long captureTimeMs;
    private final int detectedFaces;
    private final float yawDegrees;
    private final float pitchDegrees;
    private final float rollDegrees;
    private final float leftEyeProbability;
    private final float rightEyeProbability;

    private FaceSample(long timeMs, int faceCount, float yaw, float pitch, float roll,
                       float leftEye, float rightEye) {
        this.captureTimeMs = timeMs;
        this.detectedFaces = faceCount;
        this.yawDegrees = yaw;
        this.pitchDegrees = pitch;
        this.rollDegrees = roll;
        this.leftEyeProbability = leftEye;
        this.rightEyeProbability = rightEye;
    }

    public static FaceSample createEmpty(long timeMs) {
        return new FaceSample(timeMs, 0, 0.0f, 0.0f, 0.0f, UNTRACKED_METRIC, UNTRACKED_METRIC);
    }

    public static FaceSample createCrowded(long timeMs, int faceCount) {
        return new FaceSample(timeMs, faceCount, 0.0f, 0.0f, 0.0f, UNTRACKED_METRIC, UNTRACKED_METRIC);
    }

    public static FaceSample createTracked(long timeMs, float yaw, float pitch, float roll,
                                           float leftEye, float rightEye) {
        return new FaceSample(timeMs, 1, yaw, pitch, roll, leftEye, rightEye);
    }

    /**
     * @return true if both eye openness probabilities were successfully evaluated by the vision model.
     */
    public boolean hasEyeMetrics() {
        return leftEyeProbability >= 0.0f && rightEyeProbability >= 0.0f;
    }

    public long getCaptureTimeMs() { return captureTimeMs; }
    public int getDetectedFaces() { return detectedFaces; }
    public float getYawDegrees() { return yawDegrees; }
    public float getPitchDegrees() { return pitchDegrees; }
    public float getRollDegrees() { return rollDegrees; }
    public float getLeftEyeProbability() { return leftEyeProbability; }
    public float getRightEyeProbability() { return rightEyeProbability; }
}