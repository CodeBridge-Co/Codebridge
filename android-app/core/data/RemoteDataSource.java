package za.co.eduvos.codebridge.core.data;

import za.co.eduvos.codebridge.core.network.ApiService;
import za.co.eduvos.codebridge.core.network.RetrofitClient;

public class RemoteDataSource {
    private final ApiService apiService;

    public RemoteDataSource() {
        this.apiService = RetrofitClient.getInstance().getApiService();
    }

    public ApiService getApiService() {
        return apiService;
    }
}
