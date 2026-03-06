package palantier.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Notification for task-related events shown in dashboard UI.
 */
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String message;
    private Date createdAt;
    private boolean read;
    private String eventKey;

    public Notification(String message, String eventKey) {
        this.id = UUID.randomUUID().toString();
        this.message = message;
        this.createdAt = new Date();
        this.read = false;
        this.eventKey = eventKey;
    }

    public Notification(String id, String message, Date createdAt, boolean read, String eventKey) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
        this.read = read;
        this.eventKey = eventKey;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void markRead() {
        this.read = true;
    }

    public String getFormattedCreatedAt() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
        return sdf.format(createdAt);
    }
}
