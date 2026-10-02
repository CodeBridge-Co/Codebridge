package za.co.eduvos.codebridge.core.security;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppSwitchDetector implements Application.ActivityLifecycleCallbacks {

    private final TelemetrySink sink;

    public AppSwitchDetector(TelemetrySink sink) {
        this.sink = sink;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        sink.onAppSwitch(System.currentTimeMillis());
    }

    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle s) {}
    @Override public void onActivityStarted(@NonNull Activity activity) {}
    @Override public void onActivityResumed(@NonNull Activity activity) {}
    @Override public void onActivityStopped(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle s) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) {}
}
