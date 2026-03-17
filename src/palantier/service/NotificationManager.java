package palantier.service;

import palantier.model.Notification;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stores and manages user notifications.
 */
public class NotificationManager {

    private ArrayList<Notification> notifications;

    public NotificationManager() {
        DatabaseManager.initializeDatabase();
        notifications = loadNotifications();
    }

    public void addNotification(String message, String eventKey) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        if (eventKey != null && hasEventKey(eventKey)) {
            return;
        }
        notifications.add(new Notification(message.trim(), eventKey));
        saveNotifications();
    }

    public ArrayList<Notification> getAllNotificationsNewestFirst() {
        ArrayList<Notification> copy = new ArrayList<>(notifications);
        copy.sort(Comparator.comparing(Notification::getCreatedAt).reversed());
        return copy;
    }

    public int getUnreadCount() {
        int count = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    public void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.markRead();
        }
        saveNotifications();
    }

    public void markAsRead(String notificationId) {
        if (notificationId == null) {
            return;
        }
        for (Notification notification : notifications) {
            if (notificationId.equals(notification.getId())) {
                notification.markRead();
                saveNotifications();
                return;
            }
        }
    }

    private boolean hasEventKey(String eventKey) {
        for (Notification notification : notifications) {
            if (eventKey.equals(notification.getEventKey())) {
                return true;
            }
        }
        return false;
    }

    private void saveNotifications() {
        try {
            MongoCollection<Document> collection = DatabaseManager.getDatabase().getCollection("notifications");
            collection.deleteMany(new Document());
            
            if (notifications.isEmpty()) {
                return;
            }
            
            List<Document> docs = new ArrayList<>();
            for (Notification n : notifications) {
                Document doc = new Document("id", n.getId())
                        .append("message", n.getMessage())
                        .append("created_at_epoch_ms", n.getCreatedAt().getTime())
                        .append("is_read", n.isRead())
                        .append("event_key", n.getEventKey());
                docs.add(doc);
            }
            collection.insertMany(docs);
        } catch (Exception e) {
            System.err.println("Warning: Could not save notifications: " + e.getMessage());
        }
    }

    private ArrayList<Notification> loadNotifications() {
        ArrayList<Notification> loaded = new ArrayList<>();
        try {
            MongoCollection<Document> collection = DatabaseManager.getDatabase().getCollection("notifications");
            for (Document doc : collection.find()) {
                loaded.add(new Notification(
                        doc.getString("id"),
                        doc.getString("message"),
                        new java.util.Date(doc.getLong("created_at_epoch_ms")),
                        doc.getBoolean("is_read"),
                        doc.getString("event_key")));
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load notifications: " + e.getMessage());
        }
        return loaded;
    }
}
