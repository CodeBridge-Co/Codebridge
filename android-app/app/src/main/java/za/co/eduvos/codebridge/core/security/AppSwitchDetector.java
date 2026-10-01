package za.co.eduvos.codebridge.core.security;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppSwitchDetector implements Application.ActivityLifecycleCallbacks {

    private final TelemetryLogger logger;

    public AppSwitchDetector(TelemetryLogger logger) {
        this.logger = logger;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // App came to foreground
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // App went to background or another activity started
        logger.logAppSwitch();
    }

    // ... Other lifecycle methods can remain empty
    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
    @Override public void onActivityStarted(@NonNull Activity activity) {}
    @Override public void onActivityStopped(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) {}
}
