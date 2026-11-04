package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class AiAssistRequest {
    @SerializedName("session_id") private String sessionId;
    @SerializedName("problem_title") private String problemTitle;
    @SerializedName("current_code") private String currentCode;

    public AiAssistRequest(String sessionId, String problemTitle, String currentCode) {
        this.sessionId = sessionId;
        this.problemTitle = problemTitle;
        this.currentCode = currentCode;
    }

    public String getSessionId() { return sessionId; }
    public String getProblemTitle() { return problemTitle; }
    public String getCurrentCode() { return currentCode; }
}
