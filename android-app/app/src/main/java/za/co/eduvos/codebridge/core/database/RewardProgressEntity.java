package za.co.eduvos.codebridge.core.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reward_progress")
public class RewardProgressEntity {
    @PrimaryKey(autoGenerate = true)
    public int rewardId;

    public String studentHashId;
    public int streakCount;
    public int xpPoints;
    public String badgesJson;
}