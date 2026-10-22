package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("student_hash_id")
    private String studentHashId;

    @SerializedName("institution_id")
    private String institutionId;

    @SerializedName("role")
    private String role;

    public LoginRequest(String studentHashId, String institutionId, String role) {
        this.studentHashId = studentHashId;
        this.institutionId = institutionId;
        this.role = role;
    }

    // Getters and setters
    public String getStudentHashId() { return studentHashId; }
    public void setStudentHashId(String studentHashId) { this.studentHashId = studentHashId; }
    
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
