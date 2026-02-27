package palantier.ui;

import palantier.model.User;
import palantier.service.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * LoginForm — A Swing JFrame that displays the user login form.
 *
 * Contains text fields for email and password.
 * Validates credentials through UserManager and shows appropriate dialogs.
 * On successful login, opens the DashboardFrame.
 */
public class LoginForm extends JFrame {

    // Form input fields
    private JTextField emailField;
    private JPasswordField passwordField;

    // Reference to the shared UserManager
    private UserManager userManager;

    /**
     * Creates and displays the Login form window.
     *
     * @param userManager the shared UserManager instance
     */
    public LoginForm(UserManager userManager) {
        this.userManager = userManager;
        initializeUI();
    }

    /**
     * Sets up the entire user interface for the login form.
     */
    private void initializeUI() {

        // ── Window settings ─────────────────────────────────────────────
        setTitle("Palantier — Login");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);

        // ── Main panel with padding ─────────────────────────────────────
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // ── Title label ─────────────────────────────────────────────────
        JLabel titleLabel = new JLabel("Login to Palantier", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ── Form panel (labels + fields) ────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Email:"), gbc);

        emailField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(emailField, gbc);

        // Row 1: Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(passwordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ── Bottom panel (buttons) ──────────────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        bottomPanel.add(loginButton);

        JButton switchToSignupButton = new JButton("Don't have an account? Sign Up");
        switchToSignupButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        switchToSignupButton.setBorderPainted(false);
        switchToSignupButton.setContentAreaFilled(false);
        switchToSignupButton.setForeground(new Color(0, 102, 204));
        switchToSignupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(switchToSignupButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ── Add main panel to frame ─────────────────────────────────────
        add(mainPanel);

        // ── Button actions ──────────────────────────────────────────────

        // Login button click handler
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Switch to Signup form
        switchToSignupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close this window
                new SignupForm(userManager).setVisible(true);
            }
        });
    }

    /**
     * Reads form fields, calls UserManager.login(), and shows the result.
     */
    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        // Call UserManager — returns User on success, or null on failure
        User loggedInUser = userManager.login(email, password);

        if (loggedInUser == null) {
            // Show the specific error message from UserManager
            JOptionPane.showMessageDialog(this,
                    userManager.getLoginError(),
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            // Login successful — open Dashboard
            JOptionPane.showMessageDialog(this,
                    "Login Successful! Welcome, " + loggedInUser.getFullName() + "!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new DashboardFrame(loggedInUser, userManager).setVisible(true);
        }
    }
}
