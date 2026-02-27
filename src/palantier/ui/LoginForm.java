package palantier.ui;

import palantier.model.User;
import palantier.service.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * LoginForm — Premium dark-themed login window for Palantier.
 */
public class LoginForm extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private UserManager userManager;

    public LoginForm(UserManager userManager) {
        this.userManager = userManager;
        initializeUI();
    }

    private void initializeUI() {

        setTitle("Palantier — Login");
        setSize(480, 480);
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
        card.setPreferredSize(new Dimension(400, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ── Brand logo text ─────────────────────────────────────────────
        gbc.gridy = 0;
        gbc.insets = new Insets(32, 36, 2, 36);
        JLabel brandLabel = UITheme.createLabel("◆ Palantier", UITheme.FONT_LABEL, UITheme.ACCENT_PRIMARY);
        card.add(brandLabel, gbc);

        // ── Title ───────────────────────────────────────────────────────
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 36, 2, 36);
        JLabel titleLabel = UITheme.createLabel("Welcome back", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        card.add(titleLabel, gbc);

        // ── Subtitle ────────────────────────────────────────────────────
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 36, 24, 36);
        JLabel subtitleLabel = UITheme.createLabel("Sign in to continue to your dashboard",
                UITheme.FONT_SUBTITLE, UITheme.TEXT_SECONDARY);
        card.add(subtitleLabel, gbc);

        // ── Email ───────────────────────────────────────────────────────
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 36, 4, 36);
        card.add(UITheme.createLabel("Email Address", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY), gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 36, 16, 36);
        emailField = UITheme.createStyledTextField(20);
        card.add(emailField, gbc);

        // ── Password ────────────────────────────────────────────────────
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 36, 4, 36);
        card.add(UITheme.createLabel("Password", UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY), gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 36, 24, 36);
        passwordField = UITheme.createStyledPasswordField(20);
        card.add(passwordField, gbc);

        // ── Login button ────────────────────────────────────────────────
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 36, 14, 36);
        JButton loginButton = UITheme.createPrimaryButton("Sign In");
        card.add(loginButton, gbc);

        // ── Switch to Signup link ───────────────────────────────────────
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 36, 28, 36);
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkPanel.setOpaque(false);
        JLabel noAccountLabel = UITheme.createLabel("Don't have an account?  ",
                UITheme.FONT_LINK, UITheme.TEXT_MUTED);
        JButton signupLink = UITheme.createLinkButton("Create one");
        linkPanel.add(noAccountLabel);
        linkPanel.add(signupLink);
        card.add(linkPanel, gbc);

        // ── Add card to background ──────────────────────────────────────
        background.add(card);

        // ── Actions ─────────────────────────────────────────────────────
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        signupLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new SignupForm(userManager).setVisible(true);
            }
        });
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        User loggedInUser = userManager.login(email, password);

        if (loggedInUser == null) {
            UITheme.showError(this, userManager.getLoginError());
        } else {
            dispose();
            new DashboardFrame(loggedInUser, userManager).setVisible(true);
        }
    }
}
