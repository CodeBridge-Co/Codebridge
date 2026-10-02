package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class RewardDto {
    @SerializedName("reward_id") private int rewardId;
    @SerializedName("student_hash_id") private String studentHashId;
    @SerializedName("streak_count") private int streakCount;
    @SerializedName("xp_points") private int xpPoints;
    @SerializedName("badges_json") private String badgesJson;

    public int getRewardId() { return rewardId; }
    public void setRewardId(int rewardId) { this.rewardId = rewardId; }
    public String getStudentHashId() { return studentHashId; }
    public void setStudentHashId(String studentHashId) { this.studentHashId = studentHashId; }
    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }
    public int getXpPoints() { return xpPoints; }
    public void setXpPoints(int xpPoints) { this.xpPoints = xpPoints; }
    public String getBadgesJson() { return badgesJson; }
    public void setBadgesJson(String badgesJson) { this.badgesJson = badgesJson; }
}
