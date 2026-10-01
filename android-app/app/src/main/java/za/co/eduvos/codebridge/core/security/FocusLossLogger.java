package za.co.eduvos.codebridge.core.security;

import android.view.View;
import android.view.ViewTreeObserver;

public class FocusLossLogger implements ViewTreeObserver.OnWindowFocusChangeListener {

    private final TelemetryLogger logger;

    public FocusLossLogger(TelemetryLogger logger) {
        this.logger = logger;
    }

    public void attachToView(View view) {
        view.getViewTreeObserver().addOnWindowFocusChangeListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            logger.logFocusLoss();
        }
    }
}
