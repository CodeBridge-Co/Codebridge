package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class RewardDto {
    @SerializedName("reward_id") private int rewardId;
    @SerializedName("student_hash_id") private String studentHashId;
    @SerializedName("streak_count") private int streakCount;
    @SerializedName("xp_points") private int xpPoints;
    @SerializedName("badges_json") private String badgesJson;
    // Getters and Setters omitted for brevity
}
