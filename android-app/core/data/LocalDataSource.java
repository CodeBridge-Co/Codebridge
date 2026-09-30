package za.co.eduvos.codebridge.core.data;

import za.co.eduvos.codebridge.core.database.SyncQueueDao;

public class LocalDataSource {
    private final SyncQueueDao syncQueueDao;

    public LocalDataSource(SyncQueueDao syncQueueDao) {
        this.syncQueueDao = syncQueueDao;
    }

    public SyncQueueDao getSyncQueueDao() {
        return syncQueueDao;
    }
}
