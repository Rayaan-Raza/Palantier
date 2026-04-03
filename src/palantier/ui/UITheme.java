package palantier.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UITheme — Centralized design system for the Palantier application.
 *
 * Provides custom-styled components (rounded text fields, gradient buttons,
 * styled panels) and a consistent dark colour palette inspired by modern
 * SaaS applications.
 */
public class UITheme {

    // ══════════════════════════════════════════════════════════════════════
    // COLOUR PALETTE
    // ══════════════════════════════════════════════════════════════════════

    // Backgrounds
    public static final Color BG_DARK = new Color(15, 15, 26); // Main background
    public static final Color BG_CARD = new Color(24, 24, 42); // Card / form background
    public static final Color BG_INPUT = new Color(34, 34, 58); // Text field background
    public static final Color BG_INPUT_FOCUS = new Color(44, 44, 72); // Text field focused

    // Accent colours
    public static final Color ACCENT_PRIMARY = new Color(99, 102, 241); // Indigo-500
    public static final Color ACCENT_HOVER = new Color(129, 140, 248); // Indigo-400
    public static final Color ACCENT_GRADIENT = new Color(168, 85, 247); // Purple-500

    // Text
    public static final Color TEXT_PRIMARY = new Color(241, 245, 249); // Almost white
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184); // Slate-400
    public static final Color TEXT_MUTED = new Color(100, 116, 139); // Slate-500

    // Semantic
    public static final Color SUCCESS = new Color(34, 197, 94); // Green-500
    public static final Color ERROR = new Color(239, 68, 68); // Red-500
    public static final Color WARNING = new Color(245, 158, 11); // Amber-500

    // Borders
    public static final Color BORDER_DEFAULT = new Color(51, 51, 77);
    public static final Color BORDER_FOCUS = ACCENT_PRIMARY;

    // ══════════════════════════════════════════════════════════════════════
    // FONTS
    // ══════════════════════════════════════════════════════════════════════

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_LINK = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 16);

    // ══════════════════════════════════════════════════════════════════════
    // CUSTOM COMPONENTS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * A JTextField with rounded corners, dark styling, and focus highlighting.
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? BORDER_FOCUS : BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                g2.dispose();
            }
        };
        styleTextField(field);
        return field;
    }

    /**
     * A JPasswordField with rounded corners, dark styling, and focus highlighting.
     */
    public static JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? BORDER_FOCUS : BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                g2.dispose();
            }
        };
        styleTextField(field);
        return field;
    }

    /**
     * Applies common styling to text input fields.
     */
    private static void styleTextField(JTextField field) {
        field.setFont(FONT_INPUT);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 42));
        field.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        // Focus highlight effect
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBackground(BG_INPUT_FOCUS);
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBackground(BG_INPUT);
                field.repaint();
            }
        });
    }

    /**
     * Creates a gradient button with rounded corners and hover effects.
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            private boolean hovering = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovering = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                Color start = hovering ? ACCENT_HOVER : ACCENT_PRIMARY;
                Color end = ACCENT_GRADIENT;
                GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), getHeight(), end);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

                // Button text
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 44));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Creates a text-only link-style button.
     */
    public static JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_LINK);
        button.setForeground(ACCENT_HOVER);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(ACCENT_HOVER);
            }
        });

        return button;
    }

    /**
     * Creates a styled label.
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Creates the card-style form panel with rounded corners and a subtle border.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));

                // Subtle border
                g2.setColor(BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 24, 24));

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Creates the gradient background panel for the main window.
     */
    public static JPanel createGradientBackground() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Subtle radial-like gradient from dark center to darker edges
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(20, 20, 40),
                        getWidth(), getHeight(), BG_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Add a subtle accent glow in the top-right area
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                g2.setColor(ACCENT_PRIMARY);
                g2.fillOval(getWidth() / 2, -100, 500, 500);

                // Add a second glow bottom-left
                g2.setColor(ACCENT_GRADIENT);
                g2.fillOval(-200, getHeight() - 200, 400, 400);

                g2.dispose();
            }
        };
        panel.setOpaque(true);
        return panel;
    }

    /**
     * Creates a styled JComboBox matching the dark theme.
     * Forces dark colours even on Windows system look-and-feel.
     */
    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(BG_INPUT);
                        g2.fillRect(0, 0, getWidth(), getHeight());

                        int midX = getWidth() / 2;
                        int midY = getHeight() / 2;
                        Polygon triangle = new Polygon(
                                new int[]{midX - 4, midX + 4, midX},
                                new int[]{midY - 2, midY - 2, midY + 3},
                                3);
                        g2.setColor(TEXT_SECONDARY);
                        g2.fillPolygon(triangle);
                        g2.dispose();
                    }
                };
                arrow.setPreferredSize(new Dimension(32, 0));
                arrow.setBorder(BorderFactory.createEmptyBorder());
                arrow.setFocusPainted(false);
                arrow.setContentAreaFilled(false);
                arrow.setBorderPainted(false);
                arrow.setOpaque(false);
                return arrow;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(hasFocus ? BG_INPUT_FOCUS : BG_INPUT);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        combo.setFont(FONT_INPUT);
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setOpaque(true);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 38));
        combo.setBorder(BorderFactory.createLineBorder(BORDER_DEFAULT, 1, true));

        // Force dark theme on the combo popup list
        Object popup = combo.getUI().getAccessibleChild(combo, 0);
        if (popup instanceof JPopupMenu) {
            JPopupMenu pm = (JPopupMenu) popup;
            pm.setBorder(BorderFactory.createLineBorder(BORDER_DEFAULT));
            for (Component c : pm.getComponents()) {
                if (c instanceof JScrollPane) {
                    JScrollPane sp = (JScrollPane) c;
                    sp.getViewport().setBackground(BG_INPUT);
                }
            }
        }

        // Style the dropdown renderer — handles both the closed display and the list
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(FONT_INPUT);
                list.setBackground(BG_INPUT);
                list.setForeground(TEXT_PRIMARY);
                list.setSelectionBackground(ACCENT_PRIMARY);
                list.setSelectionForeground(Color.WHITE);
                if (isSelected) {
                    setBackground(ACCENT_PRIMARY);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(BG_INPUT);
                    setForeground(TEXT_PRIMARY);
                }
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return this;
            }
        });

        // Force the editor/button sub-components to use dark colours
        for (Component child : combo.getComponents()) {
            if (child instanceof JButton) {
                JButton arrow = (JButton) child;
                arrow.setBackground(BG_INPUT);
                arrow.setBorder(BorderFactory.createEmptyBorder());
            }
        }

        return combo;
    }

    /**
     * Creates a styled JTextArea matching the dark theme.
     */
    public static JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setFont(FONT_INPUT);
        area.setBackground(BG_INPUT);
        area.setForeground(TEXT_PRIMARY);
        area.setCaretColor(TEXT_PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                area.setBackground(BG_INPUT_FOCUS);
            }

            @Override
            public void focusLost(FocusEvent e) {
                area.setBackground(BG_INPUT);
            }
        });

        return area;
    }

    /**
     * Applies dark styling to scroll panes, including scrollbar buttons/arrows.
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(BG_INPUT);
        scrollPane.getViewport().setBackground(BG_INPUT);

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setBackground(BG_INPUT);
        vertical.setUI(createDarkScrollBarUI());

        JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
        horizontal.setBackground(BG_INPUT);
        horizontal.setUI(createDarkScrollBarUI());
    }

    private static BasicScrollBarUI createDarkScrollBarUI() {
        return new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(66, 66, 94);
                trackColor = BG_INPUT;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createScrollButton(orientation);
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createScrollButton(orientation);
            }

            private JButton createScrollButton(int orientation) {
                JButton button = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(BG_INPUT);
                        g2.fillRect(0, 0, getWidth(), getHeight());

                        int midX = getWidth() / 2;
                        int midY = getHeight() / 2;
                        Polygon triangle;
                        if (orientation == SwingConstants.NORTH) {
                            triangle = new Polygon(new int[]{midX - 4, midX + 4, midX},
                                    new int[]{midY + 2, midY + 2, midY - 3}, 3);
                        } else if (orientation == SwingConstants.SOUTH) {
                            triangle = new Polygon(new int[]{midX - 4, midX + 4, midX},
                                    new int[]{midY - 2, midY - 2, midY + 3}, 3);
                        } else if (orientation == SwingConstants.WEST) {
                            triangle = new Polygon(new int[]{midX + 2, midX + 2, midX - 3},
                                    new int[]{midY - 4, midY + 4, midY}, 3);
                        } else {
                            triangle = new Polygon(new int[]{midX - 2, midX - 2, midX + 3},
                                    new int[]{midY - 4, midY + 4, midY}, 3);
                        }
                        g2.setColor(TEXT_SECONDARY);
                        g2.fillPolygon(triangle);
                        g2.dispose();
                    }
                };
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setFocusPainted(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setOpaque(false);
                return button;
            }
        };
    }

    /**
     * Creates a secondary outline-style button for filter/cancel actions.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            private boolean hovering = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovering = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (hovering) {
                    g2.setColor(new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(),
                            ACCENT_PRIMARY.getBlue(), 30));
                } else {
                    g2.setColor(BG_INPUT);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // Border
                g2.setColor(hovering ? ACCENT_PRIMARY : BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));

                // Text
                g2.setColor(hovering ? ACCENT_HOVER : TEXT_SECONDARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_SECONDARY);
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 38));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Shows a modern styled error dialog.
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a modern styled success dialog.
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
