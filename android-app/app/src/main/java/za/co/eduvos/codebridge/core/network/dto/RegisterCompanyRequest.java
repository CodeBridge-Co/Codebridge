package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class RegisterCompanyRequest {
    @SerializedName("student_hash_id") private String email;
    @SerializedName("institution_id") private String companyName;
    @SerializedName("password_hash") private String password;

    public RegisterCompanyRequest(String email, String companyName, String password) {
        this.email = email; this.companyName = companyName; this.password = password;
    }
}
