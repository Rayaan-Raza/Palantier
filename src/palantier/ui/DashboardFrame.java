package palantier.ui;

import palantier.model.User;
import palantier.service.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * DashboardFrame — A placeholder dashboard shown after successful login.
 *
 * Displays a welcome message with the user's name.
 * Includes a Logout button that returns to the LoginForm.
 *
 * In future sprints, this will be extended with task management features.
 */
public class DashboardFrame extends JFrame {

    /**
     * Creates and displays the dashboard window.
     *
     * @param user        the currently logged-in user
     * @param userManager the shared UserManager instance (passed to LoginForm on
     *                    logout)
     */
    public DashboardFrame(User user, UserManager userManager) {

        // ── Window settings ─────────────────────────────────────────────
        setTitle("Palantier — Dashboard");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Main panel ──────────────────────────────────────────────────
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // ── Welcome message ─────────────────────────────────────────────
        JLabel welcomeLabel = new JLabel("Welcome to Palantier", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // ── User greeting ───────────────────────────────────────────────
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel greetingLabel = new JLabel("Hello, " + user.getFullName() + "!");
        greetingLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        greetingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel("Login Successful");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        statusLabel.setForeground(new Color(0, 153, 76));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel infoLabel = new JLabel("Task management features coming in Sprint 2...");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(greetingLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(infoLabel);
        centerPanel.add(Box.createVerticalGlue());

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ── Logout button ───────────────────────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutButton.setPreferredSize(new Dimension(120, 35));
        bottomPanel.add(logoutButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ── Add main panel to frame ─────────────────────────────────────
        add(mainPanel);

        // ── Logout action ───────────────────────────────────────────────
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close dashboard
                new LoginForm(userManager).setVisible(true);
            }
        });
    }
}
