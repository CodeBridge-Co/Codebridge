package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("student_hash_id")
    private String studentHashId;
    @SerializedName("institution_id")
    private String institutionId;

    public RegisterRequest(String studentHashId, String institutionId) {
        this.studentHashId = studentHashId;
        this.institutionId = institutionId;
    }

    public String getStudentHashId() { return studentHashId; }
    public void setStudentHashId(String studentHashId) { this.studentHashId = studentHashId; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
}