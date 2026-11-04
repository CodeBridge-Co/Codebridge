package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterStudentRequest {
    @SerializedName("student_hash_id")
    private String studentHashId;

    @SerializedName("institution_id")
    private String institutionId;

    @SerializedName("password")
    private String password;

    public RegisterStudentRequest() {
    }

    public RegisterStudentRequest(String studentHashId, String institutionId, String password) {
        this.studentHashId = studentHashId;
        this.institutionId = institutionId;
        this.password = password;
    }

    public String getStudentHashId() {
        return studentHashId;
    }

    public void setStudentHashId(String studentHashId) {
        this.studentHashId = studentHashId;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
