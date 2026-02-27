package palantier.ui;

import palantier.service.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * SignupForm — A Swing JFrame that displays the user registration form.
 *
 * Contains text fields for full name, email, password, and confirm password.
 * Validates input through UserManager and shows success/error dialogs.
 */
public class SignupForm extends JFrame {

    // Form input fields
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    // Reference to the shared UserManager (handles business logic)
    private UserManager userManager;

    /**
     * Creates and displays the Signup form window.
     *
     * @param userManager the shared UserManager instance
     */
    public SignupForm(UserManager userManager) {
        this.userManager = userManager;
        initializeUI();
    }

    /**
     * Sets up the entire user interface for the signup form.
     */
    private void initializeUI() {

        // ── Window settings ─────────────────────────────────────────────
        setTitle("Palantier — Sign Up");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);

        // ── Main panel with padding ─────────────────────────────────────
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // ── Title label ─────────────────────────────────────────────────
        JLabel titleLabel = new JLabel("Create Your Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ── Form panel (labels + fields) ────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); // Spacing around each component
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Full Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);

        nameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(nameField, gbc);

        // Row 1: Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Email:"), gbc);

        emailField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(emailField, gbc);

        // Row 2: Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(passwordField, gbc);

        // Row 3: Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Confirm Password:"), gbc);

        confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(confirmPasswordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ── Bottom panel (buttons) ──────────────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton signupButton = new JButton("Sign Up");
        signupButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        signupButton.setPreferredSize(new Dimension(120, 35));
        bottomPanel.add(signupButton);

        JButton switchToLoginButton = new JButton("Already have an account? Login");
        switchToLoginButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        switchToLoginButton.setBorderPainted(false);
        switchToLoginButton.setContentAreaFilled(false);
        switchToLoginButton.setForeground(new Color(0, 102, 204));
        switchToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(switchToLoginButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ── Add main panel to frame ─────────────────────────────────────
        add(mainPanel);

        // ── Button actions ──────────────────────────────────────────────

        // Sign Up button click handler
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSignup();
            }
        });

        // Switch to Login form
        switchToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close this window
                new LoginForm(userManager).setVisible(true);
            }
        });
    }

    /**
     * Reads form fields, calls UserManager.signup(), and shows the result.
     */
    private void handleSignup() {
        String fullName = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Call UserManager — returns null on success, or an error message
        String error = userManager.signup(fullName, email, password, confirmPassword);

        if (error != null) {
            // Show error dialog
            JOptionPane.showMessageDialog(this, error,
                    "Signup Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            // Show success dialog, then switch to Login form
            JOptionPane.showMessageDialog(this,
                    "Account created successfully! You can now log in.",
                    "Signup Successful", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            dispose();
            new LoginForm(userManager).setVisible(true);
        }
    }

    /**
     * Clears all input fields.
     */
    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }
}
