package za.co.eduvos.codebridge.core.git;

import android.content.Context;

import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import za.co.eduvos.codebridge.core.data.mapper.GitTelemetryMapper;
import za.co.eduvos.codebridge.core.database.AppDatabase;
import za.co.eduvos.codebridge.core.database.SyncQueueEntity;
import za.co.eduvos.codebridge.core.domain.SyncOfflineDataUseCase;

/**
 * Hands the Git stage results to the offline sync queue: one "TELEMETRY" row in sync_queue, then a
 * request to run SyncWorker (which only runs when the phone is online).
 */
public final class GitTelemetryReporter {

    public static final String ENTITY_TYPE = "TELEMETRY";

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private GitTelemetryReporter() {}

    public static void report(Context context, int sessionId, GitTelemetryCollector.Summary summary) {
        final Context app = context.getApplicationContext();
        final String json = new Gson().toJson(GitTelemetryMapper.toDto(sessionId, summary));
        IO.execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getInstance(app).syncQueueDao()
                        .insert(new SyncQueueEntity(ENTITY_TYPE, json, System.currentTimeMillis()));
                new SyncOfflineDataUseCase(app).execute();
            }
        });
    }
}
