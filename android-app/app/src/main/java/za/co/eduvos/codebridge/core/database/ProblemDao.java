package za.co.eduvos.codebridge.core.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ProblemDao {
    @Insert
    void insert(ProblemEntity problem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProblemEntity> problems);

    @Query("SELECT * FROM problems")
    List<ProblemEntity> getAll();
}