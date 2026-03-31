package palantier.ui;

import palantier.model.Notification;
import palantier.model.Task;
import palantier.model.User;
import palantier.service.NotificationManager;
import palantier.service.TaskManager;
import palantier.service.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Date;

/**
 * DashboardFrame — Full task management dashboard for Palantier.
 *
 * Provides:
 * - Task list with styled cards
 * - Filter bar (status, priority, assignee)
 * - Create Task button + dialog
 * - Live statistics cards
 * - Logout functionality
 */
public class DashboardFrame extends JFrame {

    private User currentUser;
    private UserManager userManager;
    private TaskManager taskManager;
    private NotificationManager notificationManager;

    // UI references for dynamic updates
    private JPanel taskListPanel;
    private JLabel totalStatLabel;
    private JLabel pendingStatLabel;
    private JLabel completedStatLabel;
    private JLabel overdueStatLabel;
    private JLabel assignedToMeStatLabel;
    private JLabel notificationBadgeLabel;
    private JComboBox<String> statusFilter;
    private JComboBox<String> priorityFilter;
    private JComboBox<String> assigneeFilter;

    public DashboardFrame(User user, UserManager userManager) {
        this.currentUser = user;
        this.userManager = userManager;
        this.notificationManager = new NotificationManager();
        this.taskManager = new TaskManager(userManager, notificationManager);
        initializeUI();
    }

    private void initializeUI() {

        setTitle("Palantier — Dashboard");
        setSize(960, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(800, 600));

        // ── Gradient background ─────────────────────────────────────────
        JPanel background = UITheme.createGradientBackground();
        background.setLayout(new BorderLayout());
        setContentPane(background);

        // ══════════════════════════════════════════════════════════════════
        // TOP BAR
        // ══════════════════════════════════════════════════════════════════

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));

        // Left: Brand
        JLabel brandLabel = UITheme.createLabel("◆ Palantier",
                UITheme.FONT_LABEL, UITheme.ACCENT_PRIMARY);
        topBar.add(brandLabel, BorderLayout.WEST);

        // Right: Notifications + User + Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JButton notificationsButton = UITheme.createSecondaryButton("Notifications");
        notificationsButton.setPreferredSize(new Dimension(130, 34));
        notificationsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNotificationsDialog();
            }
        });
        rightPanel.add(notificationsButton);

        JButton chatbotButton = UITheme.createSecondaryButton("AI Assistant");
        chatbotButton.setPreferredSize(new Dimension(120, 34));
        chatbotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ChatbotDialog(DashboardFrame.this, currentUser, taskManager).setVisible(true);
            }
        });
        rightPanel.add(chatbotButton);

        notificationBadgeLabel = UITheme.createLabel("0", UITheme.FONT_LABEL, UITheme.WARNING);
        rightPanel.add(notificationBadgeLabel);

        JLabel userLabel = UITheme.createLabel(currentUser.getFullName(),
                UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY);
        rightPanel.add(userLabel);

        JButton logoutButton = UITheme.createLinkButton("Logout");
        logoutButton.setForeground(UITheme.ERROR);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginForm(userManager).setVisible(true);
            }
        });
        rightPanel.add(logoutButton);

        topBar.add(rightPanel, BorderLayout.EAST);
        background.add(topBar, BorderLayout.NORTH);

        // ══════════════════════════════════════════════════════════════════
        // CENTER — Main content area
        // ══════════════════════════════════════════════════════════════════

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(4, 24, 0, 24));

        // ── Header row: title + Create Task button ──────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel dashTitle = UITheme.createLabel("Task Board",
                UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY);
        headerRow.add(dashTitle, BorderLayout.WEST);

        JButton createTaskBtn = UITheme.createPrimaryButton("+ New Task");
        createTaskBtn.setPreferredSize(new Dimension(140, 40));
        createTaskBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateTaskDialog();
            }
        });
        headerRow.add(createTaskBtn, BorderLayout.EAST);

        centerPanel.add(headerRow, BorderLayout.NORTH);

        // ── Filter bar ──────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        filterBar.add(UITheme.createLabel("Filters:", UITheme.FONT_LABEL, UITheme.TEXT_MUTED));

        // Status filter
        String[] statusOptions = {"All", Task.STATUS_TODO, Task.STATUS_IN_PROGRESS, Task.STATUS_DONE};
        statusFilter = UITheme.createStyledComboBox(statusOptions);
        statusFilter.setPreferredSize(new Dimension(130, 34));
        statusFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTaskList();
            }
        });
        filterBar.add(statusFilter);

        // Priority filter
        String[] priorityOptions = {"All", Task.PRIORITY_LOW, Task.PRIORITY_MEDIUM, Task.PRIORITY_HIGH};
        priorityFilter = UITheme.createStyledComboBox(priorityOptions);
        priorityFilter.setPreferredSize(new Dimension(120, 34));
        priorityFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTaskList();
            }
        });
        filterBar.add(priorityFilter);

        // Assignee filter
        buildAssigneeFilter();
        assigneeFilter.setPreferredSize(new Dimension(150, 34));
        assigneeFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTaskList();
            }
        });
        filterBar.add(assigneeFilter);

        // Clear filters button
        JButton clearFiltersBtn = UITheme.createSecondaryButton("Clear");
        clearFiltersBtn.setPreferredSize(new Dimension(80, 34));
        clearFiltersBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusFilter.setSelectedIndex(0);
                priorityFilter.setSelectedIndex(0);
                assigneeFilter.setSelectedIndex(0);
                refreshTaskList();
            }
        });
        filterBar.add(clearFiltersBtn);

        JButton myTasksBtn = UITheme.createSecondaryButton("My Tasks");
        myTasksBtn.setPreferredSize(new Dimension(100, 34));
        myTasksBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectCurrentUserInAssigneeFilter();
                refreshTaskList();
            }
        });
        filterBar.add(myTasksBtn);

        // Wrap filter bar above the task list
        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setOpaque(false);
        midPanel.add(filterBar, BorderLayout.NORTH);

        // ── Task list (scrollable) ──────────────────────────────────────
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(UITheme.BG_DARK);
        taskListPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UITheme.BG_DARK);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        midPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(midPanel, BorderLayout.CENTER);
        background.add(centerPanel, BorderLayout.CENTER);

        // ══════════════════════════════════════════════════════════════════
        // BOTTOM — Stats bar
        // ══════════════════════════════════════════════════════════════════

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));

        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        statsPanel.setOpaque(false);

        totalStatLabel = new JLabel("0");
        pendingStatLabel = new JLabel("0");
        completedStatLabel = new JLabel("0");
        overdueStatLabel = new JLabel("0");
        assignedToMeStatLabel = new JLabel("0");

        statsPanel.add(createStatCard("Total Tasks", totalStatLabel, UITheme.ACCENT_PRIMARY));
        statsPanel.add(createStatCard("Pending", pendingStatLabel, UITheme.WARNING));
        statsPanel.add(createStatCard("Completed", completedStatLabel, UITheme.SUCCESS));
        statsPanel.add(createStatCard("Overdue", overdueStatLabel, UITheme.ERROR));
        statsPanel.add(createStatCard("Assigned To Me", assignedToMeStatLabel, UITheme.ACCENT_HOVER));

        bottomBar.add(statsPanel, BorderLayout.CENTER);



        background.add(bottomBar, BorderLayout.SOUTH);

        // ── Initial data load ────────────────────────────────────────────
        taskManager.generateDueSoonNotifications();
        refreshTaskList();
        refreshStats();
        refreshNotificationBadge();
    }

    // ══════════════════════════════════════════════════════════════════════
    // TASK LIST RENDERING
    // ══════════════════════════════════════════════════════════════════════

    private void refreshTaskList() {
        taskManager.generateDueSoonNotifications();
        taskListPanel.removeAll();

        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedPriority = (String) priorityFilter.getSelectedItem();

        // Resolve assignee filter
        String assigneeValue = null;
        int assigneeIdx = assigneeFilter.getSelectedIndex();
        if (assigneeIdx == 0) {
            assigneeValue = "All";
        } else if (assigneeIdx == 1) {
            assigneeValue = "Unassigned";
        } else {
            // Map the selected name back to email
            ArrayList<User> users = userManager.getAllUsers();
            int userIdx = assigneeIdx - 2; // offset for "All" and "Unassigned"
            if (userIdx >= 0 && userIdx < users.size()) {
                assigneeValue = users.get(userIdx).getEmail();
            }
        }

        ArrayList<Task> tasks = taskManager.getFilteredTasks(selectedStatus, selectedPriority, assigneeValue);

        if (tasks.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            emptyPanel.setPreferredSize(new Dimension(100, 200));

            JLabel emptyLabel = UITheme.createLabel(
                    "No tasks found. Click '+ New Task' to get started!",
                    UITheme.FONT_BODY, UITheme.TEXT_MUTED);
            emptyPanel.add(emptyLabel);
            taskListPanel.add(emptyPanel);
        } else {
            for (Task task : tasks) {
                taskListPanel.add(createTaskCard(task));
                taskListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
        refreshStats();
        refreshNotificationBadge();
    }

    /**
     * Creates a styled task card for the task list.
     */
    private JPanel createTaskCard(Task task) {
        JPanel card = new JPanel() {
            private boolean hovering = false;

            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovering = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovering = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        openTaskDetail(task);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color baseColor = task.isOverdue() ? new Color(52, 24, 34) : UITheme.BG_CARD;
                g2.setColor(hovering ? UITheme.BG_INPUT_FOCUS : baseColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                // Subtle left accent bar
                Color accent = getStatusColor(task.getStatus());
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));

                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(12, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ── Left: Title + meta ──────────────────────────────────────────
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(UITheme.FONT_LABEL);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(titleLabel);

        leftPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        String assigneeText = task.isAssigned() ? task.getAssigneeName() : "Unassigned";
        JLabel metaLabel = new JLabel(assigneeText + "  •  " + task.getFormattedCreatedDate()
                + "  •  " + task.getCommentCount() + " comment" + (task.getCommentCount() != 1 ? "s" : ""));
        metaLabel.setFont(UITheme.FONT_LINK);
        metaLabel.setForeground(UITheme.TEXT_MUTED);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(metaLabel);

        card.add(leftPanel, BorderLayout.CENTER);

        // ── Right: Status + Priority badges ─────────────────────────────
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        rightPanel.add(createBadge(task.getStatus(), getStatusColor(task.getStatus())));
        rightPanel.add(createBadge(task.getPriority(), getPriorityColor(task.getPriority())));
        if (task.isOverdue()) {
            rightPanel.add(createBadge("Overdue", UITheme.ERROR));
        } else if (isDueSoon(task)) {
            rightPanel.add(createBadge("Due Soon", UITheme.WARNING));
        }

        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    /**
     * Creates a small colored badge label.
     */
    private JLabel createBadge(String text, Color color) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(color);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return badge;
    }

    // ══════════════════════════════════════════════════════════════════════
    // CREATE TASK DIALOG
    // ══════════════════════════════════════════════════════════════════════

    private void showCreateTaskDialog() {
        JDialog dialog = new JDialog(this, "Create New Task", true);
        dialog.setSize(460, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(UITheme.BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Title
        JLabel heading = UITheme.createLabel("Create New Task", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(heading);
        main.add(Box.createRigidArea(new Dimension(0, 20)));

        // Task title
        JLabel titleLbl = UITheme.createLabel("Title *", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(titleLbl);
        main.add(Box.createRigidArea(new Dimension(0, 4)));
        JTextField titleField = UITheme.createStyledTextField(20);
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        main.add(titleField);
        main.add(Box.createRigidArea(new Dimension(0, 14)));

        // Description
        JLabel descLbl = UITheme.createLabel("Description", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(descLbl);
        main.add(Box.createRigidArea(new Dimension(0, 4)));
        JTextArea descArea = UITheme.createStyledTextArea(3, 20);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        UITheme.styleScrollPane(descScroll);
        main.add(descScroll);
        main.add(Box.createRigidArea(new Dimension(0, 14)));

        // Priority
        JLabel priLbl = UITheme.createLabel("Priority", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY);
        priLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(priLbl);
        main.add(Box.createRigidArea(new Dimension(0, 4)));
        JComboBox<String> priorityCombo = UITheme.createStyledComboBox(Task.ALL_PRIORITIES);
        priorityCombo.setSelectedItem(Task.PRIORITY_MEDIUM);
        priorityCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        priorityCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        main.add(priorityCombo);
        main.add(Box.createRigidArea(new Dimension(0, 14)));

        // Due date
        JLabel dueLbl = UITheme.createLabel("Due Date (YYYY-MM-DD)", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY);
        dueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(dueLbl);
        main.add(Box.createRigidArea(new Dimension(0, 4)));
        JTextField dueDateField = UITheme.createStyledTextField(20);
        dueDateField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dueDateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        main.add(dueDateField);
        main.add(Box.createRigidArea(new Dimension(0, 14)));

        // Assignee
        JLabel assignLbl = UITheme.createLabel("Assign To", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY);
        assignLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(assignLbl);
        main.add(Box.createRigidArea(new Dimension(0, 4)));

        ArrayList<User> allUsers = userManager.getAllUsers();
        ArrayList<String> assigneeNames = new ArrayList<>();
        ArrayList<String> assigneeEmailsList = new ArrayList<>();
        assigneeNames.add("Unassigned");
        assigneeEmailsList.add("");
        for (User u : allUsers) {
            assigneeNames.add(u.getFullName());
            assigneeEmailsList.add(u.getEmail());
        }
        JComboBox<String> assigneeCombo = UITheme.createStyledComboBox(
                assigneeNames.toArray(new String[0]));
        assigneeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        assigneeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        main.add(assigneeCombo);
        main.add(Box.createRigidArea(new Dimension(0, 24)));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        btnPanel.add(cancelBtn);

        JButton createBtn = UITheme.createPrimaryButton("Create Task");
        createBtn.setPreferredSize(new Dimension(140, 40));
        createBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                String desc = descArea.getText();
                String priority = (String) priorityCombo.getSelectedItem();
                String dueDateText = dueDateField.getText();
                int aIdx = assigneeCombo.getSelectedIndex();
                String aEmail = assigneeEmailsList.get(aIdx);
                String aName = aEmail.isEmpty() ? null : assigneeNames.get(aIdx);
                Date dueDate = taskManager.parseDueDate(dueDateText);
                if (dueDate == null && dueDateText != null && !dueDateText.trim().isEmpty()) {
                    UITheme.showError(dialog, "Invalid due date format. Use YYYY-MM-DD.");
                    return;
                }

                String error = taskManager.createTask(title, desc, priority, dueDate,
                        aEmail.isEmpty() ? null : aEmail, aName,
                        currentUser.getEmail(), currentUser.getFullName());

                if (error != null) {
                    UITheme.showError(dialog, error);
                } else {
                    dialog.dispose();
                    refreshTaskList();
                }
            }
        });
        btnPanel.add(createBtn);

        main.add(btnPanel);

        dialog.setContentPane(main);
        dialog.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TASK DETAIL
    // ══════════════════════════════════════════════════════════════════════

    private void openTaskDetail(Task task) {
        TaskDetailDialog dialog = new TaskDetailDialog(this, task, currentUser, taskManager, userManager);
        dialog.setVisible(true);

        if (dialog.wasModified()) {
            refreshTaskList();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // STATS
    // ══════════════════════════════════════════════════════════════════════

    private void refreshStats() {
        totalStatLabel.setText(String.valueOf(taskManager.getTotalCount()));
        pendingStatLabel.setText(String.valueOf(taskManager.getPendingCount()));
        completedStatLabel.setText(String.valueOf(
                taskManager.getCountByStatus(Task.STATUS_DONE)));
        overdueStatLabel.setText(String.valueOf(taskManager.getOverdueCount()));
        assignedToMeStatLabel.setText(String.valueOf(
                taskManager.getAssignedToCount(currentUser.getEmail())));
    }

    private void refreshNotificationBadge() {
        notificationBadgeLabel.setText(String.valueOf(notificationManager.getUnreadCount()));
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════

    private JPanel createStatCard(String label, JLabel valueLabel, Color accentColor) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(UITheme.FONT_LINK);
        nameLabel.setForeground(UITheme.TEXT_MUTED);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(valueLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(nameLabel);

        return panel;
    }

    private void buildAssigneeFilter() {
        ArrayList<User> users = userManager.getAllUsers();
        ArrayList<String> items = new ArrayList<>();
        items.add("All");
        items.add("Unassigned");
        for (User u : users) {
            items.add(u.getFullName());
        }
        assigneeFilter = UITheme.createStyledComboBox(items.toArray(new String[0]));
    }

    private void selectCurrentUserInAssigneeFilter() {
        for (int i = 0; i < assigneeFilter.getItemCount(); i++) {
            if (currentUser.getFullName().equals(assigneeFilter.getItemAt(i))) {
                assigneeFilter.setSelectedIndex(i);
                return;
            }
        }
    }

    private void showNotificationsDialog() {
        final ArrayList<Notification> notifications = notificationManager.getAllNotificationsNewestFirst();
        JDialog dialog = new JDialog(this, "Notifications", true);
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        DefaultListModel<Notification> model = new DefaultListModel<>();
        if (notifications.isEmpty()) {
            model.addElement(new Notification("No notifications yet.", "empty"));
        } else {
            for (Notification notification : notifications) {
                model.addElement(notification);
            }
        }

        JList<Notification> list = new JList<>(model);
        list.setBackground(UITheme.BG_INPUT);
        list.setForeground(UITheme.TEXT_PRIMARY);
        list.setSelectionBackground(UITheme.ACCENT_PRIMARY);
        list.setFont(UITheme.FONT_LINK);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> listRef, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        listRef, value, index, isSelected, cellHasFocus);
                Notification n = (Notification) value;
                String prefix = n.isRead() ? "   " : "• ";
                label.setText(prefix + n.getMessage() + "  •  " + n.getFormattedCreatedAt());
                label.setFont(new Font("Segoe UI", n.isRead() ? Font.PLAIN : Font.BOLD, 12));
                return label;
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                int idx = list.locationToIndex(e.getPoint());
                if (idx < 0 || idx >= notifications.size()) {
                    return;
                }
                Notification selected = model.getElementAt(idx);
                if ("empty".equals(selected.getEventKey())) {
                    return;
                }
                notificationManager.markAsRead(selected.getId());
                selected.markRead();
                refreshNotificationBadge();
                model.setElementAt(selected, idx);
                list.repaint();
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DEFAULT, 1, true));
        UITheme.styleScrollPane(scrollPane);
        main.add(scrollPane, BorderLayout.CENTER);

        JButton close = UITheme.createPrimaryButton("Close");
        close.setPreferredSize(new Dimension(110, 38));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(close);
        main.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(main);
        dialog.setVisible(true);
    }

    private boolean isDueSoon(Task task) {
        if (task.getDueDate() == null || Task.STATUS_DONE.equals(task.getStatus())) {
            return false;
        }
        long deltaMs = task.getDueDate().getTime() - System.currentTimeMillis();
        long days = deltaMs / (24L * 60L * 60L * 1000L);
        return days >= 0 && days <= 2;
    }

    private Color getStatusColor(String status) {
        if (Task.STATUS_DONE.equals(status)) {
            return UITheme.SUCCESS;
        } else if (Task.STATUS_IN_PROGRESS.equals(status)) {
            return UITheme.WARNING;
        } else {
            return UITheme.ACCENT_PRIMARY;
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
}
