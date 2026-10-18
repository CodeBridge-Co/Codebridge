package za.co.eduvos.codebridge.core.git;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import za.co.eduvos.codebridge.core.database.GitCommandDao;
import za.co.eduvos.codebridge.core.database.GitCommandLogEntity;

/** Persists terminal commands to Room off the main thread (Room refuses main-thread writes). */
public class RoomGitLogSink implements CommandLogger.Sink {

    private final GitCommandDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public RoomGitLogSink(GitCommandDao dao) {
        this.dao = dao;
    }

    @Override
    public void record(int sessionId, String command, boolean isError, long timestamp) {
        final GitCommandLogEntity e = new GitCommandLogEntity();
        e.sessionId = sessionId;
        e.command = command;
        e.isError = isError;
        e.timestamp = timestamp;
        io.execute(new Runnable() {
            @Override
            public void run() {
                dao.insert(e);
            }
        });
    }
}
