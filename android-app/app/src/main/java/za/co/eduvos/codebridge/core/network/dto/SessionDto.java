package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class SessionDto {
    @SerializedName("session_id")
    private Integer sessionId; // Not mandatory for new offline sessions
    @SerializedName("student_hash_id")
    private String studentHashId;
    @SerializedName("problem_id")
    private int problemId;
    @SerializedName("start_time")
    private String startTime; // ISO 8601 String
    @SerializedName("end_time")
    private String endTime;
    @SerializedName("is_offline")
    private boolean isOffline;

    // Getters and Setters
    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }
    public String getStudentHashId() { return studentHashId; }
    public void setStudentHashId(String studentHashId) { this.studentHashId = studentHashId; }
    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public boolean isOffline() { return isOffline; }
    public void setOffline(boolean offline) { isOffline = offline; }
}
