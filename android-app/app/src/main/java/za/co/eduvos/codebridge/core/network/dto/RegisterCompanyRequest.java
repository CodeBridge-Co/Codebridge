package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterCompanyRequest {
    @SerializedName("student_hash_id")
    private String email;

    @SerializedName("institution_id")
    private String companyName;

    @SerializedName("password")
    private String password;

    public RegisterCompanyRequest() {
    }

    public RegisterCompanyRequest(String email, String companyName, String password) {
        this.email = email;
        this.companyName = companyName;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
