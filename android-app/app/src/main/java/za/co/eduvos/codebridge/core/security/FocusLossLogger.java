package za.co.eduvos.codebridge.core.security;

import android.view.View;
import android.view.ViewTreeObserver;

public class FocusLossLogger implements ViewTreeObserver.OnWindowFocusChangeListener {

    private final TelemetrySink sink;

    public FocusLossLogger(TelemetrySink sink) {
        this.sink = sink;
    }

    public void attachToView(View view) {
        view.getViewTreeObserver().addOnWindowFocusChangeListener(this);
    }

    public void detachFromView(View view) {
        view.getViewTreeObserver().removeOnWindowFocusChangeListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) sink.onFocusLoss(System.currentTimeMillis());
    }
}
