package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class ChallengeCompleteRequest {
    @SerializedName("student_hash_id") private String studentHashId;
    @SerializedName("difficulty") private String difficulty;
    @SerializedName("session_id") private String sessionId;

    public ChallengeCompleteRequest(String studentHashId, String difficulty, String sessionId) {
        this.studentHashId = studentHashId;
        this.difficulty = difficulty;
        this.sessionId = sessionId;
    }
}