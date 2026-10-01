package za.co.eduvos.codebridge.core.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_queue")
public class SyncQueueEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String entityType; // e.g., "SESSION", "TELEMETRY"
    private String payloadJson;
    private long createdAt;
    private int retryCount;
    private String status; // "PENDING", "SYNCED", "FAILED"

    public SyncQueueEntity(String entityType, String payloadJson, long createdAt) {
        this.entityType = entityType;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
        this.retryCount = 0;
        this.status = "PENDING";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
