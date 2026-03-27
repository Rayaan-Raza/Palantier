package palantier.ui;

import palantier.model.Comment;
import palantier.model.Task;
import palantier.model.User;
import palantier.service.TaskManager;
import palantier.service.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Date;

/**
 * TaskDetailDialog — Modal dialog for viewing / editing a single task.
 *
 * Shows task info, allows status/assignee changes, displays comments,
 * and provides a comment input area.
 */
public class TaskDetailDialog extends JDialog {

    private Task task;
    private User currentUser;
    private TaskManager taskManager;
    private UserManager userManager;
    private boolean taskModified;

    // UI references we need to refresh
    private JPanel commentsListPanel;
    private JTextArea commentInput;
    private JComboBox<String> statusCombo;
    private JComboBox<String> assigneeCombo;
    private JTextField dueDateField;

    // Assignee email list (parallel to combo box entries)
    private ArrayList<String> assigneeEmails;

    public TaskDetailDialog(JFrame parent, Task task, User currentUser,
                            TaskManager taskManager, UserManager userManager) {
        super(parent, "Task Details", true);
        this.task = task;
        this.currentUser = currentUser;
        this.taskManager = taskManager;
        this.userManager = userManager;
        this.taskModified = false;

        initializeUI();
    }

    /**
     * Returns true if any changes were made (so the caller can refresh).
     */
    public boolean wasModified() {
        return taskModified;
    }

    private void initializeUI() {
        setSize(580, 680);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // Main panel with dark background
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_DARK);
        setContentPane(main);

        // ── Scrollable content ───────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.BG_DARK);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // ── Title ────────────────────────────────────────────────────────
        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(UITheme.FONT_HEADING);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLabel);
        content.add(Box.createRigidArea(new Dimension(0, 6)));

        // ── Meta info line ───────────────────────────────────────────────
        String meta = "Created by " + task.getCreatedByName() + " on " + task.getFormattedCreatedDate();
        JLabel metaLabel = new JLabel(meta);
        metaLabel.setFont(UITheme.FONT_LINK);
        metaLabel.setForeground(UITheme.TEXT_MUTED);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(metaLabel);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // ── Description ──────────────────────────────────────────────────
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            JTextArea descArea = new JTextArea(task.getDescription());
            descArea.setFont(UITheme.FONT_INPUT);
            descArea.setForeground(UITheme.TEXT_SECONDARY);
            descArea.setBackground(UITheme.BG_CARD);
            descArea.setEditable(false);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            descArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(descArea);
            content.add(Box.createRigidArea(new Dimension(0, 16)));
        }

        // ── Status + Priority + Assignee row ─────────────────────────────
        JPanel controlRow = new JPanel(new GridLayout(1, 4, 12, 0));
        controlRow.setOpaque(false);
        controlRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Status
        JPanel statusPanel = createFieldPanel("Status");
        statusCombo = UITheme.createStyledComboBox(Task.ALL_STATUSES);
        statusCombo.setSelectedItem(task.getStatus());
        statusCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newStatus = (String) statusCombo.getSelectedItem();
                if (newStatus != null && !newStatus.equals(task.getStatus())) {
                    task.setStatus(newStatus);
                    taskManager.updateTask();
                    taskModified = true;
                }
            }
        });
        statusPanel.add(statusCombo);
        controlRow.add(statusPanel);

        // Priority (display only)
        JPanel priorityPanel = createFieldPanel("Priority");
        JLabel priorityValue = new JLabel(task.getPriority());
        priorityValue.setFont(UITheme.FONT_INPUT);
        priorityValue.setForeground(getPriorityColor(task.getPriority()));
        priorityValue.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        priorityPanel.add(priorityValue);
        controlRow.add(priorityPanel);

        // Assignee
        JPanel assigneePanel = createFieldPanel("Assignee");
        buildAssigneeCombo();
        assigneeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = assigneeCombo.getSelectedIndex();
                if (idx >= 0 && idx < assigneeEmails.size()) {
                    String email = assigneeEmails.get(idx);
                    String name = (String) assigneeCombo.getSelectedItem();
                    String error = taskManager.assignTask(
                            task.getId(),
                            email.isEmpty() ? null : email,
                            email.isEmpty() ? null : name);
                    if (error != null) {
                        UITheme.showError(TaskDetailDialog.this, error);
                        buildAssigneeCombo();
                        return;
                    }
                    taskModified = true;
                }
            }
        });
        assigneePanel.add(assigneeCombo);
        controlRow.add(assigneePanel);

        JPanel dueDatePanel = createFieldPanel("Due Date");
        dueDateField = UITheme.createStyledTextField(10);
        dueDateField.setText(task.getDueDate() == null
                ? ""
                : new java.text.SimpleDateFormat("yyyy-MM-dd").format(task.getDueDate()));
        dueDateField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDueDateChange();
            }
        });
        dueDatePanel.add(dueDateField);
        controlRow.add(dueDatePanel);

        content.add(controlRow);
        content.add(Box.createRigidArea(new Dimension(0, 24)));

        // ── Divider ──────────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER_DEFAULT);
        sep.setBackground(UITheme.BG_DARK);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sep);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // ── Comments header ──────────────────────────────────────────────
        JLabel commentsHeader = new JLabel("Comments (" + task.getCommentCount() + ")");
        commentsHeader.setFont(UITheme.FONT_LABEL);
        commentsHeader.setForeground(UITheme.TEXT_PRIMARY);
        commentsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(commentsHeader);
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        // ── Comments list ────────────────────────────────────────────────
        commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        commentsListPanel.setBackground(UITheme.BG_DARK);
        commentsListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshComments();
        content.add(commentsListPanel);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // ── Comment input ────────────────────────────────────────────────
        JLabel addLabel = new JLabel("Add a comment");
        addLabel.setFont(UITheme.FONT_LABEL);
        addLabel.setForeground(UITheme.TEXT_SECONDARY);
        addLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(addLabel);
        content.add(Box.createRigidArea(new Dimension(0, 6)));

        commentInput = UITheme.createStyledTextArea(3, 30);
        commentInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JScrollPane inputScroll = new JScrollPane(commentInput);
        inputScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true));
        inputScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        UITheme.styleScrollPane(inputScroll);
        content.add(inputScroll);
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton addCommentBtn = UITheme.createPrimaryButton("Add Comment");
        addCommentBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addCommentBtn.setMaximumSize(new Dimension(160, 40));
        addCommentBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddComment();
            }
        });
        content.add(addCommentBtn);

        // ── Wrap in scroll pane ──────────────────────────────────────────
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(UITheme.BG_DARK);
        scroll.getViewport().setBackground(UITheme.BG_DARK);
        main.add(scroll, BorderLayout.CENTER);

        // ── Bottom bar with Delete + Close ───────────────────────────────
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_CARD);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_DEFAULT));

        JButton deleteBtn = UITheme.createSecondaryButton("Delete Task");
        deleteBtn.setForeground(UITheme.ERROR);
        deleteBtn.setPreferredSize(new Dimension(130, 36));
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        TaskDetailDialog.this,
                        "Are you sure you want to delete this task?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    taskManager.deleteTask(task.getId());
                    taskModified = true;
                    dispose();
                }
            }
        });
        bottomBar.add(deleteBtn);

        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.setPreferredSize(new Dimension(100, 36));
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomBar.add(closeBtn);

        main.add(bottomBar, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════════════
    // COMMENT HANDLING
    // ══════════════════════════════════════════════════════════════════════

    private void handleAddComment() {
        String text = commentInput.getText();
        String error = taskManager.addComment(
                task.getId(),
                currentUser.getFullName(),
                currentUser.getEmail(),
                text);

        if (error != null) {
            UITheme.showError(this, error);
        } else {
            commentInput.setText("");
            taskModified = true;
            refreshComments();
        }
    }

    private void refreshComments() {
        commentsListPanel.removeAll();

        ArrayList<Comment> comments = taskManager.getCommentsChronological(task.getId());

        if (comments.isEmpty()) {
            JLabel empty = new JLabel("No comments yet. Be the first to comment!");
            empty.setFont(UITheme.FONT_LINK);
            empty.setForeground(UITheme.TEXT_MUTED);
            empty.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            commentsListPanel.add(empty);
        } else {
            for (Comment comment : comments) {
                commentsListPanel.add(createCommentCard(comment));
                commentsListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        commentsListPanel.revalidate();
        commentsListPanel.repaint();
    }

    private JPanel createCommentCard(Comment comment) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Top: author + timestamp
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel authorLabel = new JLabel(comment.getAuthorName());
        authorLabel.setFont(UITheme.FONT_LABEL);
        authorLabel.setForeground(UITheme.ACCENT_HOVER);
        header.add(authorLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(comment.getFormattedTimestamp());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(UITheme.TEXT_MUTED);
        header.add(timeLabel, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // Body
        JLabel bodyLabel = new JLabel("<html><p style='width:460px'>" + comment.getText() + "</p></html>");
        bodyLabel.setFont(UITheme.FONT_INPUT);
        bodyLabel.setForeground(UITheme.TEXT_SECONDARY);
        bodyLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        card.add(bodyLabel, BorderLayout.CENTER);

        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel createFieldPanel(String label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_LABEL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));

        return panel;
    }

    private void buildAssigneeCombo() {
        ArrayList<User> users = userManager.getAllUsers();
        assigneeEmails = new ArrayList<>();

        ArrayList<String> names = new ArrayList<>();
        names.add("Unassigned");
        assigneeEmails.add("");

        for (User u : users) {
            names.add(u.getFullName());
            assigneeEmails.add(u.getEmail());
        }

        String[] items = names.toArray(new String[0]);
        assigneeCombo = UITheme.createStyledComboBox(items);

        // Select current assignee
        if (task.isAssigned()) {
            int idx = assigneeEmails.indexOf(task.getAssigneeEmail());
            if (idx >= 0) {
                assigneeCombo.setSelectedIndex(idx);
            }
        } else {
            assigneeCombo.setSelectedIndex(0);
        }
    }

    private Color getPriorityColor(String priority) {
        if (Task.PRIORITY_HIGH.equals(priority)) {
            return UITheme.ERROR;
        } else if (Task.PRIORITY_MEDIUM.equals(priority)) {
            return UITheme.WARNING;
        } else {
            return UITheme.SUCCESS;
        }
    }

    private void handleDueDateChange() {
        String dueDateText = dueDateField.getText();
        Date parsedDate = taskManager.parseDueDate(dueDateText);
        if (parsedDate == null && dueDateText != null && !dueDateText.trim().isEmpty()) {
            UITheme.showError(this, "Invalid due date format. Use YYYY-MM-DD.");
            dueDateField.setText(task.getDueDate() == null
                    ? ""
                    : new java.text.SimpleDateFormat("yyyy-MM-dd").format(task.getDueDate()));
            return;
        }

        task.setDueDate(parsedDate);
        taskManager.updateTask();
        taskModified = true;
    }
}
