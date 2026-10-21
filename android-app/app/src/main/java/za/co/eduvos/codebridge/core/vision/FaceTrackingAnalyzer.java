package za.co.eduvos.codebridge.core.vision;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

/**
 * On-device frame processor that converts raw camera input into structured facial metric snapshots.
 */
public class FaceTrackingAnalyzer implements ImageAnalysis.Analyzer {

    public interface OutputListener {
        void onMetricExtracted(FaceSample snapshot);
    }

    // Limit evaluation to roughly 5 FPS to reduce heat throttling on lower-end devices
    private static final long FRAME_THROTTLE_MS = 200L;

    private final FaceDetector visionEngine;
    private final OutputListener outputSink;
    private long lastExecutionTimestamp;

    public FaceTrackingAnalyzer(OutputListener listener) {
        this.outputSink = listener;

        FaceDetectorOptions detectorConfig = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .build();

        this.visionEngine = FaceDetection.getClient(detectorConfig);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull final ImageProxy imageProxy) {
        final long currentSystemTime = SystemClock.elapsedRealtime();

        // Skip processing if incoming frames exceed target rate or if frame buffer is null
        if (currentSystemTime - lastExecutionTimestamp < FRAME_THROTTLE_MS || imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }
        lastExecutionTimestamp = currentSystemTime;

        InputImage frameImage = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        visionEngine.process(frameImage)
                .addOnSuccessListener(detectedFaces -> {
                    FaceSample metrics = mapToFaceSample(currentSystemTime, detectedFaces);
                    outputSink.onMetricExtracted(metrics);
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    public void close() {
        visionEngine.close();
    }

    static FaceSample mapToFaceSample(long timestamp, List<Face> faceList) {
        if (faceList.isEmpty()) {
            return FaceSample.createEmpty(timestamp);
        }
        if (faceList.size() > 1) {
            return FaceSample.createCrowded(timestamp, faceList.size());
        }

        Face targetFace = faceList.get(0);
        return FaceSample.createTracked(
                timestamp,
                targetFace.getHeadEulerAngleY(),
                targetFace.getHeadEulerAngleX(),
                targetFace.getHeadEulerAngleZ(),
                extractEyeScore(targetFace.getLeftEyeOpenProbability()),
                extractEyeScore(targetFace.getRightEyeOpenProbability())
        );
    }

    private static float extractEyeScore(Float eyeOpenProbability) {
        if (eyeOpenProbability == null) {
            return FaceSample.UNTRACKED_METRIC;
        }
        return eyeOpenProbability;
    }
}