package za.co.eduvos.codebridge;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import za.co.eduvos.codebridge.core.database.AppDatabase;
import za.co.eduvos.codebridge.core.database.SyncQueueDao;
import za.co.eduvos.codebridge.core.database.SyncQueueEntity;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SyncQueueDaoTest {

    private AppDatabase db;
    private SyncQueueDao dao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        // Use in-memory database for testing
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.syncQueueDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testInsertAndFetchPending() {
        SyncQueueEntity entity = new SyncQueueEntity("SESSION", "{\"data\":\"test\"}", System.currentTimeMillis());
        dao.insert(entity);

        List<SyncQueueEntity> pending = dao.fetchPending();
        assertEquals(1, pending.size());
        assertEquals("PENDING", pending.get(0).getStatus());
    }

    @Test
    public void testMarkSynced() {
        SyncQueueEntity entity = new SyncQueueEntity("SESSION", "{\"data\":\"test\"}", System.currentTimeMillis());
        long id = dao.insert(entity);

        dao.markSynced((int) id);
        List<SyncQueueEntity> pending = dao.fetchPending();
        
        assertTrue(pending.isEmpty());
    }
}
