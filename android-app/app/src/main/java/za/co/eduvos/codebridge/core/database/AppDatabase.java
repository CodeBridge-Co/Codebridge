package za.co.eduvos.codebridge.core.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import za.co.eduvos.codebridge.core.security.SqlCipherManager;

@Database(
        entities = {
                ProblemEntity.class,
                AssessmentSessionEntity.class,
                TelemetryLogEntity.class,
                RewardProgressEntity.class,
                GitCommandLogEntity.class,
                SpeechTranscriptEntity.class,
                SyncQueueEntity.class,

        },
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract ProblemDao problemDao();
    public abstract SessionDao sessionDao();
    public abstract TelemetryDao telemetryDao();
    public abstract RewardDao rewardDao();
    public abstract GitCommandDao gitCommandDao();
    public abstract SpeechTranscriptDao speechTranscriptDao();
    public abstract SyncQueueDao syncQueueDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "codebridge.db"
                    )
                            .openHelperFactory(SqlCipherManager.getSupportFactory(context))
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
