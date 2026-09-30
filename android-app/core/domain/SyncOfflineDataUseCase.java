package za.co.eduvos.codebridge.core.domain;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class SyncOfflineDataUseCase {

    private final Context context;

    public SyncOfflineDataUseCase(Context context) {
        this.context = context;
    }

    /**
     * Triggers the background sync process.
     */
    public void execute() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(syncWorkRequest);
    }
}
