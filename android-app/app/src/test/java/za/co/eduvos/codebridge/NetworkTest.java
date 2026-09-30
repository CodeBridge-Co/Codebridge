package za.co.eduvos.codebridge;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import za.co.eduvos.codebridge.core.network.ApiService;
import za.co.eduvos.codebridge.core.network.dto.ApiResponse;
import java.io.IOException;

public class NetworkTest {

    private MockWebServer mockWebServer;
    private ApiService apiService;

    @Before
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        apiService = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testHealthCheckReturns200OK() throws IOException {
        // Arrange
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody("{\"data\":\"OK\",\"error\":null,\"status\":\"success\"}");
        mockWebServer.enqueue(mockResponse);

        // Act
        Response<ApiResponse<String>> response = apiService.healthCheck().execute();
        // Assert
        Assert.assertEquals(200, response.code());
        Assert.assertNotNull(response.body());
        Assert.assertEquals("OK", response.body().getData());
    }
}
