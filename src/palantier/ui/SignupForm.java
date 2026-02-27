package palantier.ui;

import palantier.service.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * SignupForm — Premium dark-themed signup window for Palantier.
 */
public class SignupForm extends JFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private UserManager userManager;

    public SignupForm(UserManager userManager) {
        this.userManager = userManager;
        initializeUI();
    }

    private void initializeUI() {

        setTitle("Palantier — Sign Up");
        setSize(520, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Gradient background ─────────────────────────────────────────
        JPanel background = UITheme.createGradientBackground();
        background.setLayout(new GridBagLayout());
        setContentPane(background);

        // ── Card panel ──────────────────────────────────────────────────
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(420, 520));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 32, 0, 32);

        // ── Brand logo text ─────────────────────────────────────────────
        gbc.gridy = 0;
        gbc.insets = new Insets(28, 32, 2, 32);
        JLabel brandLabel = UITheme.createLabel("◆ Palantier", UITheme.FONT_LABEL, UITheme.ACCENT_PRIMARY);
        card.add(brandLabel, gbc);

        // ── Title ───────────────────────────────────────────────────────
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 32, 2, 32);
        JLabel titleLabel = UITheme.createLabel("Create account", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        card.add(titleLabel, gbc);

        // ── Subtitle ────────────────────────────────────────────────────
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 32, 18, 32);
        JLabel subtitleLabel = UITheme.createLabel("Start managing your team's tasks today",
                UITheme.FONT_SUBTITLE, UITheme.TEXT_SECONDARY);
        card.add(subtitleLabel, gbc);

        // ── Full Name ───────────────────────────────────────────────────
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 32, 4, 32);
        card.add(UITheme.createLabel("Full Name", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY), gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 32, 12, 32);
        nameField = UITheme.createStyledTextField(20);
        card.add(nameField, gbc);

        // ── Email ───────────────────────────────────────────────────────
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 32, 4, 32);
        card.add(UITheme.createLabel("Email Address", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY), gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 32, 12, 32);
        emailField = UITheme.createStyledTextField(20);
        card.add(emailField, gbc);

        // ── Password ────────────────────────────────────────────────────
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 32, 4, 32);
        card.add(UITheme.createLabel("Password", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY), gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 32, 12, 32);
        passwordField = UITheme.createStyledPasswordField(20);
        card.add(passwordField, gbc);

        // ── Confirm Password ────────────────────────────────────────────
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 32, 4, 32);
        card.add(UITheme.createLabel("Confirm Password", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY), gbc);

        gbc.gridy = 10;
        gbc.insets = new Insets(0, 32, 18, 32);
        confirmPasswordField = UITheme.createStyledPasswordField(20);
        card.add(confirmPasswordField, gbc);

        // ── Sign Up button ──────────────────────────────────────────────
        gbc.gridy = 11;
        gbc.insets = new Insets(0, 32, 12, 32);
        JButton signupButton = UITheme.createPrimaryButton("Create Account");
        card.add(signupButton, gbc);

        // ── Switch to Login link ────────────────────────────────────────
        gbc.gridy = 12;
        gbc.insets = new Insets(0, 32, 24, 32);
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkPanel.setOpaque(false);
        JLabel alreadyLabel = UITheme.createLabel("Already have an account?  ",
                UITheme.FONT_LINK, UITheme.TEXT_MUTED);
        JButton loginLink = UITheme.createLinkButton("Sign in");
        linkPanel.add(alreadyLabel);
        linkPanel.add(loginLink);
        card.add(linkPanel, gbc);

        // ── Add card to background ──────────────────────────────────────
        background.add(card);

        // ── Actions ─────────────────────────────────────────────────────
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSignup();
            }
        });

        loginLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginForm(userManager).setVisible(true);
            }
        });
    }

    private void handleSignup() {
        String fullName = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        String error = userManager.signup(fullName, email, password, confirmPassword);

        if (error != null) {
            UITheme.showError(this, error);
        } else {
            UITheme.showSuccess(this, "Account created successfully! You can now log in.");
            dispose();
            new LoginForm(userManager).setVisible(true);
        }
    }
}
