package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class SpeechResultDto {
    @SerializedName("transcript") private String transcript;
    @SerializedName("keyword_density") private float keywordDensity;

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }
    public float getKeywordDensity() { return keywordDensity; }
    public void setKeywordDensity(float keywordDensity) { this.keywordDensity = keywordDensity; }
}
