package za.co.eduvos.codebridge.backend;

import za.co.eduvos.codebridge.R;
import za.co.eduvos.codebridge.feature.events.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Java Backend Repository for Hackathons and Tech Events (Requirement 4).
 * Manages event records and dynamic image URL references with fallback placeholders.
 */
public class EventRepository {

    private static final List<Event> eventsStore = new ArrayList<>();

    static {
        eventsStore.add(new Event("Capitec Bank", "Developer Hackathon 2026", "Build real-time payment solutions with Capitec engineers.", "OCT 28", "Johannesburg", "Hackathon", R.color.cb_tile_lime));
        eventsStore.add(new Event("Takealot", "Tech Talk: AI in E-Commerce", "Explore machine learning recommendation algorithms.", "NOV 06", "Cape Town", "Tech Talk", R.color.cb_tile_sky));
        eventsStore.add(new Event("Discovery Health", "Engineering Better Together", "Workshop on healthcare claim processing & microservices.", "NOV 12", "Johannesburg", "Workshop", R.color.cb_tile_peach));
        eventsStore.add(new Event("Standard Bank", "Cloud & Security Challenge", "Test your AWS cloud infrastructure skills.", "DEC 01", "Online", "Challenge", R.color.cb_tile_mint));
    }

    public static synchronized List<Event> getAllEvents() {
        return new ArrayList<>(eventsStore);
    }

    public static synchronized void addEvent(Event event) {
        eventsStore.add(event);
    }

    public static synchronized void deleteEvent(int index) {
        if (index >= 0 && index < eventsStore.size()) {
            eventsStore.remove(index);
        }
    }
}
