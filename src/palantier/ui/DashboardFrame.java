package palantier.ui;

import palantier.model.User;
import palantier.service.UserManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * DashboardFrame — Premium dark-themed dashboard shown after successful login.
 */
public class DashboardFrame extends JFrame {

    public DashboardFrame(User user, UserManager userManager) {

        setTitle("Palantier — Dashboard");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Gradient background ─────────────────────────────────────────
        JPanel background = UITheme.createGradientBackground();
        background.setLayout(new BorderLayout());
        setContentPane(background);

        // ── Top bar ─────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel brandLabel = UITheme.createLabel("◆ Palantier", UITheme.FONT_LABEL, UITheme.ACCENT_PRIMARY);
        topBar.add(brandLabel, BorderLayout.WEST);

        // User info + logout on the right
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JLabel userLabel = UITheme.createLabel(user.getFullName(),
                UITheme.FONT_LABEL, UITheme.TEXT_SECONDARY);
        rightPanel.add(userLabel);

        JButton logoutButton = UITheme.createLinkButton("Logout");
        logoutButton.setForeground(UITheme.ERROR);
        rightPanel.add(logoutButton);

        topBar.add(rightPanel, BorderLayout.EAST);
        background.add(topBar, BorderLayout.NORTH);

        // ── Center content ──────────────────────────────────────────────
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(520, 280));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ── Welcome heading ─────────────────────────────────────────────
        gbc.gridy = 0;
        gbc.insets = new Insets(32, 40, 4, 40);
        JLabel welcomeLabel = UITheme.createLabel("Welcome to Palantier",
                UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(welcomeLabel, gbc);

        // ── Greeting ────────────────────────────────────────────────────
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 40, 8, 40);
        JLabel greetingLabel = UITheme.createLabel("Hello, " + user.getFullName() + "!",
                UITheme.FONT_BODY, UITheme.TEXT_SECONDARY);
        greetingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(greetingLabel, gbc);

        // ── Success badge ───────────────────────────────────────────────
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 40, 8, 40);
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgePanel.setOpaque(false);

        JLabel badge = new JLabel("  ✓  Login Successful  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(34, 197, 94, 25));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setFont(UITheme.FONT_LABEL);
        badge.setForeground(UITheme.SUCCESS);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(34, 197, 94, 60), 1, true),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        badgePanel.add(badge);
        card.add(badgePanel, gbc);

        // ── Stat cards row ──────────────────────────────────────────────
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 40, 32, 40);
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(createStatCard("Tasks", "0", UITheme.ACCENT_PRIMARY));
        statsPanel.add(createStatCard("In Progress", "0", UITheme.WARNING));
        statsPanel.add(createStatCard("Completed", "0", UITheme.SUCCESS));
        card.add(statsPanel, gbc);

        centerWrapper.add(card);
        background.add(centerWrapper, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel footerLabel = UITheme.createLabel("Sprint 2: Task management features coming soon",
                UITheme.FONT_LINK, UITheme.TEXT_MUTED);
        footer.add(footerLabel);
        background.add(footer, BorderLayout.SOUTH);

        // ── Logout action ───────────────────────────────────────────────
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginForm(userManager).setVisible(true);
            }
        });
    }

    /**
     * Creates a mini stat card with a number and label.
     */
    private JPanel createStatCard(String label, String value, Color accentColor) {
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

        JLabel valueLabel = new JLabel(value);
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
}
