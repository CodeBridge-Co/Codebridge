package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class EventDto {
    @SerializedName("event_id") private int eventId;
    @SerializedName("company_id") private int companyId;
    @SerializedName("title") private String title;
    @SerializedName("event_date") private String eventDate;
    @SerializedName("category") private String category;

    public int getEventId() { return eventId; }
    public int getCompanyId() { return companyId; }
    public String getTitle() { return title; }
    public String getEventDate() { return eventDate; }
    public String getCategory() { return category; }
}