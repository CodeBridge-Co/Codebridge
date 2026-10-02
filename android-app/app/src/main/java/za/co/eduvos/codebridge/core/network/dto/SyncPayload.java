package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SyncPayload {
    @SerializedName("items") private List<SyncItem> items;

    public SyncPayload(List<SyncItem> items) {
        this.items = items;
    }

    public List<SyncItem> getItems() { return items; }
    public void setItems(List<SyncItem> items) { this.items = items; }
}
