package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class LeaderboardDto {
    @SerializedName("hash_id") private String hashId;
    @SerializedName("xp") private int xp;
    @SerializedName("streak") private int streak;
    // Getters and Setters omitted for brevity
}
