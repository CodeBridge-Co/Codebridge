package za.co.eduvos.codebridge.core.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "git_command_logs")
public class GitCommandLogEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;
    public String command;
    public boolean isError;
    public long timestamp;
}