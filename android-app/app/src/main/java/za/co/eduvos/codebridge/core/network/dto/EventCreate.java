package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class EventCreate {
    @SerializedName("title") private String title;
    @SerializedName("event_date") private String eventDate;
    @SerializedName("category") private String category;

    public EventCreate(String title, String eventDate, String category) {
        this.title = title;
        this.eventDate = eventDate;
        this.category = category;
    }
}