package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class EventDto {
    @SerializedName("event_id") private int eventId;
    @SerializedName("company_id") private int companyId;
    @SerializedName("title") private String title;
    @SerializedName("event_date") private String eventDate;
    // Getters and Setters omitted for brevity
}
