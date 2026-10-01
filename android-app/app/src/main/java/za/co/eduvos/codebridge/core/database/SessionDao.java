package za.co.eduvos.codebridge.core.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface SessionDao {
    @Insert
    long insert(AssessmentSessionEntity session);

    @Query("SELECT * FROM assessment_sessions WHERE sessionId = :id")
    AssessmentSessionEntity getById(int id);

    @Query("SELECT * FROM assessment_sessions WHERE studentHashId = :hashId")
    List<AssessmentSessionEntity> getByStudent(String hashId);
}