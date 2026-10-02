package za.co.eduvos.codebridge;

import android.app.Application;
import za.co.eduvos.codebridge.core.database.AppDatabase;
import za.co.eduvos.codebridge.core.data.DataRepository;
import za.co.eduvos.codebridge.core.data.LocalDataSource;
import za.co.eduvos.codebridge.core.data.RemoteDataSource;
import za.co.eduvos.codebridge.core.network.TokenManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TokenManager.init(this);

        AppDatabase db = AppDatabase.getInstance(this);
        DataRepository.init(
                new LocalDataSource(db.syncQueueDao()),
                new RemoteDataSource()
        );
    }
}
