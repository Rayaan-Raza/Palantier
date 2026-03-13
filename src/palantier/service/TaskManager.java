package palantier.service;

import palantier.model.Comment;
import palantier.model.Task;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * TaskManager — Handles all task-related business logic:
 * - Create / update / delete tasks
 * - Task assignment
 * - Comment management
 * - Filtering by status, priority, and assignee
 * - Persistence via SQLite database
 */
public class TaskManager {

    // In-memory list of all tasks
    private ArrayList<Task> tasks;
    private UserManager userManager;
    private NotificationManager notificationManager;

    /**
     * Creates a new TaskManager and loads existing tasks from the data file.
     */
    public TaskManager(UserManager userManager, NotificationManager notificationManager) {
        DatabaseManager.initializeDatabase();
        this.userManager = userManager;
        this.notificationManager = notificationManager;
        tasks = loadTasks();
    }

    // ══════════════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Creates a new task with the given details.
     *
     * @param title          task title (required)
     * @param description    task description
     * @param priority       one of Task.PRIORITY_LOW / MEDIUM / HIGH
     * @param assigneeEmail  email of the assigned user (can be null)
     * @param assigneeName   display name of the assigned user (can be null)
     * @param createdByEmail email of the current user
     * @param createdByName  display name of the current user
     * @return null on success, or an error message string on failure
     */
    public String createTask(String title, String description, String priority, Date dueDate,
                             String assigneeEmail, String assigneeName,
                             String createdByEmail, String createdByName) {

        // Validate title
        if (title == null || title.trim().isEmpty()) {
            return "Task title is required.";
        }

        // Validate priority
        if (!isValidPriority(priority)) {
            return "Invalid priority. Choose Low, Medium, or High.";
        }

        // Validate due date (if supplied)
        if (dueDate != null && dueDate.before(new Date(System.currentTimeMillis() - 24L * 60L * 60L * 1000L))) {
            return "Due date cannot be in the past.";
        }

        // Validate assignee against registered users
        if (assigneeEmail != null && !assigneeEmail.trim().isEmpty()) {
            if (userManager == null || !userManager.isValidUserEmail(assigneeEmail)) {
                return "Invalid assignee. Please select a valid team member.";
            }
        }

        // Create the task
        Task task = new Task(title.trim(), description != null ? description.trim() : "",
                priority, createdByEmail, createdByName);
        task.setDueDate(dueDate);

        // Set assignee if provided
        if (assigneeEmail != null && !assigneeEmail.trim().isEmpty()) {
            task.setAssignee(assigneeEmail, assigneeName);
            notifyTaskAssigned(task, assigneeName);
        }

        tasks.add(task);
        saveTasks();

        return null; // success
    }

    // ══════════════════════════════════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Updates a task in the list and persists the change.
     * The caller modifies the Task object directly and then calls this
     * to trigger a save.
     */
    public void updateTask() {
        saveTasks();
    }

    // ══════════════════════════════════════════════════════════════════════
    // DELETE
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Deletes the task with the given ID.
     *
     * @param taskId the UUID of the task to delete
     * @return true if the task was found and deleted, false otherwise
     */
    public boolean deleteTask(String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(taskId)) {
                tasks.remove(i);
                saveTasks();
                return true;
            }
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ASSIGNMENT
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Assigns a task to a team member.
     *
     * @param taskId        ID of the task
     * @param assigneeEmail email of the new assignee (null to unassign)
     * @param assigneeName  display name of the new assignee
     * @return null on success, or an error message on failure
     */
    public String assignTask(String taskId, String assigneeEmail, String assigneeName) {
        Task task = findTaskById(taskId);
        if (task == null) {
            return "Task not found.";
        }

        if (assigneeEmail != null && !assigneeEmail.trim().isEmpty()) {
            if (userManager == null || !userManager.isValidUserEmail(assigneeEmail)) {
                return "Invalid assignee. Please select a valid team member.";
            }
        }

        String oldAssignee = task.getAssigneeEmail();
        task.setAssignee(assigneeEmail, assigneeName);
        saveTasks();
        if (assigneeEmail != null && !assigneeEmail.trim().isEmpty()
                && (oldAssignee == null || !oldAssignee.equalsIgnoreCase(assigneeEmail))) {
            notifyTaskAssigned(task, assigneeName);
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    // COMMENTS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Adds a comment to a task.
     *
     * @param taskId      ID of the task to comment on
     * @param authorName  display name of the comment author
     * @param authorEmail email of the comment author
     * @param text        the comment text
     * @return null on success, or an error message on failure
     */
    public String addComment(String taskId, String authorName,
                             String authorEmail, String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Comment cannot be empty.";
        }

        Task task = findTaskById(taskId);
        if (task == null) {
            return "Task not found.";
        }

        Comment comment = new Comment(authorName, authorEmail, text.trim());
        task.addComment(comment);
        saveTasks();
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    // RETRIEVAL & FILTERING
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Returns all tasks.
     */
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Returns tasks filtered by the given criteria.
     * Pass null or "All" for any parameter to skip that filter.
     *
     * @param status   filter by status (e.g. "To Do", "In Progress", "Done")
     * @param priority filter by priority (e.g. "Low", "Medium", "High")
     * @param assignee filter by assignee email
     * @return filtered list of tasks
     */
    public ArrayList<Task> getFilteredTasks(String status, String priority, String assignee) {
        ArrayList<Task> filtered = new ArrayList<>();

        for (Task task : tasks) {
            // Status filter
            if (status != null && !status.equals("All")) {
                if (!task.getStatus().equals(status)) {
                    continue;
                }
            }

            // Priority filter
            if (priority != null && !priority.equals("All")) {
                if (!task.getPriority().equals(priority)) {
                    continue;
                }
            }

            // Assignee filter
            if (assignee != null && !assignee.equals("All")) {
                if (assignee.equals("Unassigned")) {
                    if (task.isAssigned()) {
                        continue;
                    }
                } else {
                    if (task.getAssigneeEmail() == null ||
                            !task.getAssigneeEmail().equals(assignee)) {
                        continue;
                    }
                }
            }

            filtered.add(task);
        }

        return filtered;
    }

    /**
     * Returns comments sorted oldest-to-newest for deterministic chronology.
     */
    public ArrayList<Comment> getCommentsChronological(String taskId) {
        Task task = findTaskById(taskId);
        ArrayList<Comment> sorted = new ArrayList<>();
        if (task == null) {
            return sorted;
        }
        sorted.addAll(task.getComments());
        sorted.sort(Comparator.comparing(Comment::getTimestamp));
        return sorted;
    }

    // ══════════════════════════════════════════════════════════════════════
    // STATISTICS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Returns the total number of tasks.
     */
    public int getTotalCount() {
        return tasks.size();
    }

    /**
     * Returns the number of tasks with a given status.
     */
    public int getCountByStatus(String status) {
        int count = 0;
        for (Task task : tasks) {
            if (task.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    public int getAssignedToCount(String userEmail) {
        int count = 0;
        for (Task task : tasks) {
            if (task.getAssigneeEmail() != null && task.getAssigneeEmail().equalsIgnoreCase(userEmail)) {
                count++;
            }
        }
        return count;
    }

    public int getOverdueCount() {
        int count = 0;
        for (Task task : tasks) {
            if (task.isOverdue()) {
                count++;
            }
        }
        return count;
    }

    public int getPendingCount() {
        int count = 0;
        for (Task task : tasks) {
            if (!Task.STATUS_DONE.equals(task.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getUnassignedCount() {
        int count = 0;
        for (Task task : tasks) {
            if (!task.isAssigned()) {
                count++;
            }
        }
        return count;
    }

    public Date parseDueDate(String dueDateText) {
        if (dueDateText == null || dueDateText.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            return sdf.parse(dueDateText.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    public void generateDueSoonNotifications() {
        if (notificationManager == null) {
            return;
        }
        Date now = new Date();
        long oneDayMs = 24L * 60L * 60L * 1000L;
        for (Task task : tasks) {
            Date dueDate = task.getDueDate();
            if (dueDate == null || Task.STATUS_DONE.equals(task.getStatus())) {
                continue;
            }
            long delta = dueDate.getTime() - now.getTime();
            long days = delta / oneDayMs;
            if (days >= 0 && days <= 2) {
                String eventKey = "due-soon-" + task.getId() + "-" + new SimpleDateFormat("yyyyMMdd").format(dueDate);
                String message = "Due soon: " + truncate(task.getTitle(), 28)
                        + " (" + task.getFormattedDueDate() + ")";
                notificationManager.addNotification(message, eventKey);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Finds a task by its UUID.
     */
    public Task findTaskById(String id) {
        for (Task task : tasks) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Checks if the given priority string is valid.
     */
    private boolean isValidPriority(String priority) {
        for (String p : Task.ALL_PRIORITIES) {
            if (p.equals(priority)) {
                return true;
            }
        }
        return false;
    }

    private void notifyTaskAssigned(Task task, String assigneeName) {
        if (notificationManager == null) {
            return;
        }
        String displayAssignee = (assigneeName == null || assigneeName.trim().isEmpty()) ? "a team member" : assigneeName;
        String message = "Assigned: " + truncate(task.getTitle(), 24) + " -> " + truncate(displayAssignee, 18);
        String eventKey = "assigned-" + task.getId() + "-" + (task.getAssigneeEmail() == null ? "none" : task.getAssigneeEmail());
        notificationManager.addNotification(message, eventKey);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "...";
    }

    // ══════════════════════════════════════════════════════════════════════
    // DATABASE PERSISTENCE (SQLite)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Saves the current list of tasks to the data file.
     */
    private void saveTasks() {
        try {
            MongoCollection<Document> collection = DatabaseManager.getDatabase().getCollection("tasks");
            collection.deleteMany(new Document());

            if (tasks.isEmpty()) {
                return;
            }

            List<Document> docs = new ArrayList<>();
            for (Task task : tasks) {
                Document doc = new Document("id", task.getId())
                        .append("title", task.getTitle())
                        .append("description", task.getDescription())
                        .append("status", task.getStatus())
                        .append("priority", task.getPriority())
                        .append("assignee_email", task.getAssigneeEmail())
                        .append("assignee_name", task.getAssigneeName())
                        .append("created_by_email", task.getCreatedByEmail())
                        .append("created_by_name", task.getCreatedByName())
                        .append("created_date_epoch_ms", task.getCreatedDate().getTime());

                if (task.getDueDate() != null) {
                    doc.append("due_date_epoch_ms", task.getDueDate().getTime());
                } else {
                    doc.append("due_date_epoch_ms", null);
                }

                List<Document> commentDocs = new ArrayList<>();
                for (Comment comment : task.getComments()) {
                    Document cDoc = new Document("id", comment.getId())
                            .append("author_name", comment.getAuthorName())
                            .append("author_email", comment.getAuthorEmail())
                            .append("text", comment.getText())
                            .append("timestamp_epoch_ms", comment.getTimestamp().getTime());
                    commentDocs.add(cDoc);
                }
                doc.append("comments", commentDocs);

                docs.add(doc);
            }
            collection.insertMany(docs);
        } catch (Exception e) {
            System.err.println("Warning: Could not save tasks to database: " + e.getMessage());
        }
    }

    /**
     * Loads tasks from the data file.
     * Returns an empty list if the file doesn't exist or is corrupted.
     */
    private ArrayList<Task> loadTasks() {
        ArrayList<Task> loaded = new ArrayList<>();
        try {
            MongoCollection<Document> collection = DatabaseManager.getDatabase().getCollection("tasks");
            for (Document doc : collection.find()) {
                Task task = new Task(
                        doc.getString("title"),
                        doc.getString("description"),
                        doc.getString("priority"),
                        doc.getString("created_by_email"),
                        doc.getString("created_by_name"));
                task.setId(doc.getString("id"));
                task.setStatus(doc.getString("status"));
                task.setAssignee(
                        doc.getString("assignee_email"),
                        doc.getString("assignee_name"));
                task.setCreatedDate(new Date(doc.getLong("created_date_epoch_ms")));
                Long dueEpoch = doc.getLong("due_date_epoch_ms");
                if (dueEpoch != null) {
                    task.setDueDate(new Date(dueEpoch));
                }
                
                List<Document> commentDocs = doc.getList("comments", Document.class);
                if (commentDocs != null) {
                    for (Document cDoc : commentDocs) {
                        Comment comment = new Comment(
                                cDoc.getString("author_name"),
                                cDoc.getString("author_email"),
                                cDoc.getString("text"));
                        comment.setId(cDoc.getString("id"));
                        comment.setTimestamp(new Date(cDoc.getLong("timestamp_epoch_ms")));
                        task.addComment(comment);
                    }
                }

                loaded.add(task);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load tasks from database: " + e.getMessage());
        }

        return loaded;
    }

    private Task findTaskByIdInList(ArrayList<Task> source, String id) {
        for (Task task : source) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }
}
