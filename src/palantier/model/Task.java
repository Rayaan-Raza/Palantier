package palantier.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Task — Represents a single work item in the Palantier system.
 *
 * Each task has a title, description, status, priority, optional assignee,
 * the creator's identity, a creation date, and a list of comments.
 * Implements Serializable for file-based persistence.
 */
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Status constants ─────────────────────────────────────────────────
    public static final String STATUS_TODO = "To Do";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_DONE = "Done";

    public static final String[] ALL_STATUSES = {STATUS_TODO, STATUS_IN_PROGRESS, STATUS_DONE};

    // ── Priority constants ───────────────────────────────────────────────
    public static final String PRIORITY_LOW = "Low";
    public static final String PRIORITY_MEDIUM = "Medium";
    public static final String PRIORITY_HIGH = "High";

    public static final String[] ALL_PRIORITIES = {PRIORITY_LOW, PRIORITY_MEDIUM, PRIORITY_HIGH};

    // ── Fields ───────────────────────────────────────────────────────────
    private String id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String assigneeEmail;   // email of the assigned user (null = unassigned)
    private String assigneeName;    // display name of the assigned user
    private String createdByEmail;  // email of the user who created this task
    private String createdByName;   // display name of the creator
    private Date createdDate;
    private Date dueDate;           // optional due date
    private ArrayList<Comment> comments;

    /**
     * Creates a new Task with an auto-generated UUID and current date.
     *
     * @param title          short title for the task
     * @param description    detailed description
     * @param priority       one of PRIORITY_LOW, PRIORITY_MEDIUM, PRIORITY_HIGH
     * @param createdByEmail email of the creating user
     * @param createdByName  display name of the creating user
     */
    public Task(String title, String description, String priority,
                String createdByEmail, String createdByName) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.status = STATUS_TODO;          // new tasks always start as "To Do"
        this.priority = priority;
        this.createdByEmail = createdByEmail;
        this.createdByName = createdByName;
        this.createdDate = new Date();
        this.comments = new ArrayList<>();
        // assignee is null until explicitly set
        this.assigneeEmail = null;
        this.assigneeName = null;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public String getAssigneeEmail() {
        return assigneeEmail;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(createdDate);
    }

    public String getFormattedDueDate() {
        if (dueDate == null) {
            return "No due date";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(dueDate);
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public int getCommentCount() {
        return comments.size();
    }

    // ── Setters ──────────────────────────────────────────────────────────

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Assigns this task to a team member.
     *
     * @param email the assignee's email (null to unassign)
     * @param name  the assignee's display name
     */
    public void setAssignee(String email, String name) {
        this.assigneeEmail = email;
        this.assigneeName = name;
    }

    // ── Comments ─────────────────────────────────────────────────────────

    /**
     * Adds a comment to this task.
     *
     * @param comment the Comment object to add
     */
    public void addComment(Comment comment) {
        comments.add(comment);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Returns whether this task is currently assigned to someone.
     */
    public boolean isAssigned() {
        return assigneeEmail != null && !assigneeEmail.trim().isEmpty();
    }

    public boolean isOverdue() {
        if (dueDate == null || STATUS_DONE.equals(status)) {
            return false;
        }
        Date today = new Date();
        return dueDate.before(today);
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", assignee='" + (assigneeName != null ? assigneeName : "Unassigned") + '\'' +
                ", comments=" + comments.size() +
                '}';
    }
}
