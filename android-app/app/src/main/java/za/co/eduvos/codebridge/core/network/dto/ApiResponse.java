package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("data")
    private T data;
    @SerializedName("error")
    private ErrorResponse error;
    @SerializedName("status")
    private String status;

    public T getData() { return data; }
    public ErrorResponse getError() { return error; }
    public String getStatus() { return status; }
}
