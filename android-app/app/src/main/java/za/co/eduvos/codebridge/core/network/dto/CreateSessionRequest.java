package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class CreateSessionRequest {
    @SerializedName("session_id") private String sessionId;
    @SerializedName("student_hash_id") private String studentHashId;
    @SerializedName("problem_id") private int problemId;
    @SerializedName("start_time") private String startTime;
    @SerializedName("end_time") private String endTime;
    @SerializedName("is_offline") private boolean isOffline;

    public CreateSessionRequest(String sessionId, String studentHashId, int problemId, String startTime, String endTime, boolean isOffline) {
        this.sessionId = sessionId;
        this.studentHashId = studentHashId;
        this.problemId = problemId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isOffline = isOffline;
    }

    public String getSessionId() { return sessionId; }
    public String getStudentHashId() { return studentHashId; }
    public int getProblemId() { return problemId; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public boolean isOffline() { return isOffline; }
}