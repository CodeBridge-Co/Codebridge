package za.co.eduvos.codebridge.core.security;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ClipboardMonitor {

    private final TelemetrySink sink;

    public ClipboardMonitor(TelemetrySink sink) {
        this.sink = sink;
    }

    public void attachToEditText(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 10 && before == 0) {
                    sink.onClipboardPaste(s.toString(), System.currentTimeMillis());
                }
            }
        });
    }
}
