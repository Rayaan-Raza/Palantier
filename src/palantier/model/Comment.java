package palantier.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Comment — Represents a single comment attached to a Task.
 *
 * Each comment records who wrote it, what they said, and when.
 * Comments are stored inside their parent Task object and persisted
 * together with it via Java Serialization.
 */
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String authorName;
    private String authorEmail;
    private String text;
    private Date timestamp;

    /**
     * Creates a new Comment with an auto-generated UUID and current timestamp.
     *
     * @param authorName  display name of the comment author
     * @param authorEmail email of the comment author
     * @param text        the comment body text
     */
    public Comment(String authorName, String authorEmail, String text) {
        this.id = UUID.randomUUID().toString();
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.text = text;
        this.timestamp = new Date();
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public String getText() {
        return text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns a human-readable formatted timestamp string.
     */
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
        return sdf.format(timestamp);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "author='" + authorName + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + getFormattedTimestamp() +
                '}';
    }
}
