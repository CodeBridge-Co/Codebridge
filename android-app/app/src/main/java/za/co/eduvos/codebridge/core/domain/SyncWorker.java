package za.co.eduvos.codebridge.core.domain;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Response;
import za.co.eduvos.codebridge.core.database.AppDatabase;
import za.co.eduvos.codebridge.core.database.SyncQueueDao;
import za.co.eduvos.codebridge.core.database.SyncQueueEntity;
import za.co.eduvos.codebridge.core.network.ApiService;
import za.co.eduvos.codebridge.core.network.RetrofitClient;
import za.co.eduvos.codebridge.core.network.dto.SyncItem;
import za.co.eduvos.codebridge.core.network.dto.SyncPayload;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        SyncQueueDao dao = AppDatabase.getInstance(getApplicationContext()).syncQueueDao();
        ApiService api = RetrofitClient.getInstance().getApiService();

        List<SyncQueueEntity> pending = dao.fetchPending();
        if (pending.isEmpty()) return Result.success();

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        List<SyncItem> items = new ArrayList<>();

        for (SyncQueueEntity entity : pending) {
            Map<String, Object> payload;
            try {
                payload = gson.fromJson(entity.getPayloadJson(), mapType);
            } catch (Exception e) {
                // Corrupt entry — mark as failed so it stops blocking the queue
                entity.setStatus("FAILED");
                dao.update(entity);
                continue;
            }
            items.add(new SyncItem(
                    String.valueOf(entity.getId()),
                    entity.getEntityType(),
                    payload
            ));
        }

        if (items.isEmpty()) return Result.success();

        try {
            Response<Map<String, Object>> response =
                    api.syncData(new SyncPayload(items)).execute();

            if (response.isSuccessful()) {
                for (SyncQueueEntity entity : pending) {
                    dao.markSynced(entity.getId());
                }
                return Result.success();
            } else {
                for (SyncQueueEntity entity : pending) {
                    dao.incrementRetry(entity.getId());
                }
                return Result.retry();
            }
        } catch (Exception e) {
            for (SyncQueueEntity entity : pending) {
                dao.incrementRetry(entity.getId());
            }
            return Result.retry();
        }
    }
}
