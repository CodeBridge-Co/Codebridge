package za.co.eduvos.codebridge.core.network;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Response;
import java.io.IOException;

/**
 * TEMPORARY PLACEHOLDER — Lebo, replace with the real mock interceptor.
 * Currently just passes requests through unchanged so the build isn't broken.
 */
public class MockApiInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}