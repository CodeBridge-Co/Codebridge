package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class AiAssistResponse {
    @SerializedName("allowed") private boolean allowed;
    @SerializedName("remaining_uses") private int remainingUses;
    @SerializedName("hint_text") private String hintText;

    public boolean isAllowed() { return allowed; }
    public int getRemainingUses() { return remainingUses; }
    public String getHintText() { return hintText; }
}
