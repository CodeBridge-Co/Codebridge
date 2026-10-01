package za.co.eduvos.codebridge.core.data;

public class DataRepository {

    private static DataRepository instance;
    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    private DataRepository(LocalDataSource localDataSource, RemoteDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
    }

    public static synchronized DataRepository getInstance(LocalDataSource local, RemoteDataSource remote) {
        if (instance == null) {
            instance = new DataRepository(local, remote);
        }
        return instance;
    }

    public LocalDataSource getLocalDataSource() {
        return localDataSource;
    }

    public RemoteDataSource getRemoteDataSource() {
        return remoteDataSource;
    }
}
