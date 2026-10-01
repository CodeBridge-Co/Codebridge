package za.co.eduvos.codebridge.core.security;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ClipboardMonitor {

    private final TelemetryLogger logger;

    public ClipboardMonitor(TelemetryLogger logger) {
        this.logger = logger;
    }

    /**
     * Attaches a custom paste listener to an EditText.
     * Note: Android 10+ restricts clipboard access. This works best on API < 29.
     */
    public void attachToEditText(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Heuristic: If a large chunk of text appears instantly, it's a paste
                if (count > 10 && before == 0) {
                    logger.logClipboardPaste(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
