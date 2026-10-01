package za.co.eduvos.codebridge.core.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "problems")
public class ProblemEntity {
    @PrimaryKey
    public int problemId;

    @NonNull
    public String title = "";

    public String difficultyLevel;   // Easy, Medium, Hard
    public String testCasesJson;
}