package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class SpeechResultDto {
    @SerializedName("transcript") private String transcript;
    @SerializedName("keyword_density") private float keywordDensity;
    // Getters and Setters omitted for brevity
}
