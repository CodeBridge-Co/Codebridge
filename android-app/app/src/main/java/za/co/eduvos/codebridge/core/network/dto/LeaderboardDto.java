package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class LeaderboardDto {
    @SerializedName("hash_id") private String hashId;
    @SerializedName("xp") private int xp;
    @SerializedName("streak") private int streak;

    public String getHashId() { return hashId; }
    public void setHashId(String hashId) { this.hashId = hashId; }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
}
