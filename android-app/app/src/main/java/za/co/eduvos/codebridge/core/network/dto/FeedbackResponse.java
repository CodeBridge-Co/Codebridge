package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class FeedbackResponse {
    @SerializedName("strengths") private String strengths;
    @SerializedName("improvements") private String improvements;
    @SerializedName("ai_feedback") private String aiFeedback;

    public String getStrengths() { return strengths; }
    public String getImprovements() { return improvements; }
    public String getAiFeedback() { return aiFeedback; }
}