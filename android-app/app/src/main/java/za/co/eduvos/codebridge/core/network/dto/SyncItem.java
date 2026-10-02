package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class SyncItem {
    @SerializedName("client_id") private String clientId;
    @SerializedName("entity_type") private String entityType;
    @SerializedName("payload") private Map<String, Object> payload;

    public SyncItem(String clientId, String entityType, Map<String, Object> payload) {
        this.clientId = clientId;
        this.entityType = entityType;
        this.payload = payload;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
