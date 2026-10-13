package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

/** Flat response of POST /auth/login and /auth/register (matches backend LoginResponse). */
public class LoginResponse {

    @SerializedName("student_hash_id")
    private String studentHashId;

    @SerializedName("token")
    private String token;

    public String getStudentHashId() { return studentHashId; }
    public void setStudentHashId(String studentHashId) { this.studentHashId = studentHashId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
