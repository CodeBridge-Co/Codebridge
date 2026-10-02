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
