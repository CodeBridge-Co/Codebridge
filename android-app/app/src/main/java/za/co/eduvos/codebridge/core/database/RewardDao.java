package za.co.eduvos.codebridge.core.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;

@Dao
public interface RewardDao {
    @Insert
    void insert(RewardProgressEntity reward);

    @Update
    void update(RewardProgressEntity reward);

    @Query("SELECT * FROM reward_progress WHERE studentHashId = :hashId LIMIT 1")
    RewardProgressEntity getByStudent(String hashId);
}