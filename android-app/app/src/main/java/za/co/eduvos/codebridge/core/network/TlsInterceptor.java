package za.co.eduvos.codebridge.core.network;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Best-effort TLS interceptor. Ensures HTTPS connections only.
 * For pinned certificates, add a CertificatePinner to OkHttpClientProvider.
 */
public class TlsInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!request.isHttps() && !request.url().host().equals("10.0.2.2") && !request.url().host().equals("localhost")) {
            throw new IOException("Cleartext HTTP not permitted: " + request.url());
        }
        return chain.proceed(request);
    }
}
