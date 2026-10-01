package za.co.eduvos.codebridge;

import android.app.Application;
import za.co.eduvos.codebridge.core.network.TokenManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TokenManager.init(this);
    }
}