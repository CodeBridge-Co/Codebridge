package za.co.eduvos.codebridge.core.network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import za.co.eduvos.codebridge.BuildConfig;

public class RetrofitClient {

    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient() {
        OkHttpClient client = OkHttpClientProvider.provide();
        // Uncomment to enable mock responses for UI development without backend
        // OkHttpClient.Builder builder = client.newBuilder();
        // builder.addInterceptor(new MockApiInterceptor());
        // client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) instance = new RetrofitClient();
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }
}
