package za.co.eduvos.codebridge.core.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface GitCommandDao {
    @Insert
    void insert(GitCommandLogEntity log);

    @Query("SELECT * FROM git_command_logs WHERE sessionId = :sessionId")
    List<GitCommandLogEntity> getBySession(int sessionId);
}