package za.co.eduvos.codebridge.core.data;

public class DataRepository {

    private static volatile DataRepository instance;
    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    private DataRepository(LocalDataSource local, RemoteDataSource remote) {
        this.localDataSource = local;
        this.remoteDataSource = remote;
    }
    /**
     * Must be called exactly once from Application.onCreate().
     * Throws if called again with the intent to reinitialize.
     */
    public static synchronized void init(LocalDataSource local, RemoteDataSource remote) {
        if (instance != null) {
            throw new IllegalStateException("DataRepository already initialized");
        }
        instance = new DataRepository(local, remote);
    }

    public static DataRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Call DataRepository.init() from Application.onCreate() first");
        }
        return instance;
    }

    public LocalDataSource getLocalDataSource() { return localDataSource; }
    public RemoteDataSource getRemoteDataSource() { return remoteDataSource; }
}
