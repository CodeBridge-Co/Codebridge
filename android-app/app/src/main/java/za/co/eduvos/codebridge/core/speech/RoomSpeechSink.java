package za.co.eduvos.codebridge.core.speech;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import za.co.eduvos.codebridge.core.database.SpeechTranscriptDao;
import za.co.eduvos.codebridge.core.database.SpeechTranscriptEntity;

/** Saves one analysed answer to Room off the main thread (Room refuses main-thread writes). */
public class RoomSpeechSink {

    private final SpeechTranscriptDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public RoomSpeechSink(SpeechTranscriptDao dao) {
        this.dao = dao;
    }

    public void save(int sessionId, String transcript, float keywordDensity, long durationMs, long timestamp) {
        final SpeechTranscriptEntity e = new SpeechTranscriptEntity();
        e.sessionId = sessionId;
        e.transcriptText = transcript;
        e.keywordDensity = keywordDensity;
        e.durationMs = durationMs;
        e.timestamp = timestamp;
        io.execute(new Runnable() {
            @Override
            public void run() {
                dao.insert(e);
            }
        });
    }
}
