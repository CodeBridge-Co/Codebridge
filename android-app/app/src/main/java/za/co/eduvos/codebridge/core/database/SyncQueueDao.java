package za.co.eduvos.codebridge.core.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface SyncQueueDao {

    @Insert
    long insert(SyncQueueEntity entity);

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    List<SyncQueueEntity> fetchPending();

    @Query("UPDATE sync_queue SET status = 'SYNCED' WHERE id = :id")
    void markSynced(int id);

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1 WHERE id = :id")
    void incrementRetry(int id);
    
    @Update
    void update(SyncQueueEntity entity);
}
