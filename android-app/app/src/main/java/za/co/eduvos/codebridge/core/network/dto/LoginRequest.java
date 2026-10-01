package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

// For Login
public class LoginRequest {
    @SerializedName("student_hash_id")
    private String studentHashId;
    @SerializedName("institution_id")
    private String institutionId;

    public LoginRequest(String studentHashId, String institutionId) {
        this.studentHashId = studentHashId;
        this.institutionId = institutionId;
    }
    // Getters and Setters
    public String getStudentHashId() { return studentHashId; }
    public void setStudentHashId(String studentHashId) { this.studentHashId = studentHashId; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
}
