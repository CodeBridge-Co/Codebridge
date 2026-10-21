package za.co.eduvos.codebridge.core.vision;

import android.content.Context;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hooks up the device's front camera to the lifecycle and pipes frame data into the attention monitor.
 * Note: Assumes camera permissions are already granted by the calling activity.
 */
public class CameraFaceTracker {

    public interface LifecycleCallback {
        void onCameraError(String errorMessage);
    }

    private final Context applicationContext;
    private final AttentionTracker attentionMonitor;
    private final LifecycleCallback errorListener;

    private ProcessCameraProvider cameraProvider;
    private FaceTrackingAnalyzer faceProcessor;
    private ExecutorService backgroundExecutor;
    private boolean isTerminated;

    public CameraFaceTracker(Context context, AttentionTracker monitor, LifecycleCallback listener) {
        // Grab app context to avoid memory leaks if the caller passes an Activity context
        this.applicationContext = context.getApplicationContext();
        this.attentionMonitor = monitor;
        this.errorListener = listener;
    }

    public void start(LifecycleOwner lifecycleOwner, PreviewView viewfinder) {
        isTerminated = false;
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(applicationContext);

        providerFuture.addListener(() -> {
            if (isTerminated) {
                return;
            }
            try {
                cameraProvider = providerFuture.get();
                initializeCamera(lifecycleOwner, viewfinder);
            } catch (Exception ex) {
                errorListener.onCameraError("Failed to initialize camera provider: " + ex.getMessage());
            }
        }, ContextCompat.getMainExecutor(applicationContext));
    }

    private void initializeCamera(LifecycleOwner lifecycleOwner, PreviewView viewfinder) {
        Preview livePreview = new Preview.Builder().build();
        livePreview.setSurfaceProvider(viewfinder.getSurfaceProvider());

        // Drop older frames if processing falls behind to prevent memory bloat and latency
        ImageAnalysis frameAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        faceProcessor = new FaceTrackingAnalyzer(snapshot -> attentionMonitor.onSample(snapshot));

        backgroundExecutor = Executors.newSingleThreadExecutor();
        frameAnalysis.setAnalyzer(backgroundExecutor, faceProcessor);

        try {
            // Must unbind any previous use cases before attaching new ones to the lifecycle
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    livePreview,
                    frameAnalysis
            );
        } catch (IllegalArgumentException ex) {
            // Thrown if the specific hardware doesn't have a front-facing lens
            errorListener.onCameraError("Device lacks a compatible front-facing camera.");
        }
    }

    public void stop() {
        isTerminated = true;

        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (faceProcessor != null) {
            faceProcessor.close();
        }
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdown();
        }

        // Null out references to encourage GC
        cameraProvider = null;
        faceProcessor = null;
        backgroundExecutor = null;
    }
}