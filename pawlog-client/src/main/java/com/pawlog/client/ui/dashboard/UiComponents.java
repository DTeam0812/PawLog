package com.pawlog.client.ui.dashboard;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class UiComponents {
    private static final Preferences THEME_PREFERENCES = Preferences.userNodeForPackage(UiComponents.class);
    private static final int DEFAULT_ACCENT_RGB = new Color(255, 157, 108).getRGB();
    public static final Color BACKGROUND = new Color(255, 249, 243);
    public static final Color CARD = new Color(255, 254, 252);
    public static Color PRIMARY = new Color(THEME_PREFERENCES.getInt("accentRgb", DEFAULT_ACCENT_RGB), true);
    public static Color PRIMARY_HOVER = mix(PRIMARY, Color.BLACK, 0.10f);
    public static Color PRIMARY_PRESSED = mix(PRIMARY, Color.BLACK, 0.18f);
    public static Color PRIMARY_SOFT = mix(PRIMARY, Color.WHITE, 0.78f);
    public static final Color TEXT = new Color(70, 63, 59);
    public static final Color SUB_TEXT = new Color(133, 118, 111);
    public static final Color SOFT_TEXT = new Color(154, 139, 132);
    public static final Color MUTED_TEXT = new Color(194, 181, 174);
    public static final Color BORDER = new Color(239, 218, 207);
    public static final Color SOFT_BORDER = new Color(245, 231, 223);
    public static final Color TABLE_DIVIDER = new Color(247, 235, 228);
    public static final Color SUCCESS = new Color(79, 195, 161);
    public static final Color RED = new Color(244, 112, 104);
    public static final Color SIDEBAR = new Color(86, 75, 71);
    public static final Color SIDEBAR_HOVER = new Color(105, 90, 84);
    public static Color SIDEBAR_ACTIVE = PRIMARY;
    public static final Color MINT = new Color(201, 239, 226);
    public static final Color LAVENDER = new Color(229, 218, 250);
    public static final Color BUTTER = new Color(255, 233, 170);
    public static final Color CHART_BLUE = new Color(72, 126, 255);
    public static final Color CHART_PURPLE = new Color(162, 155, 254);
    public static final Color CHART_PINK = new Color(253, 121, 168);
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private UiComponents() {}

    public static void applyAccent(Color accent) {
        PRIMARY = accent;
        PRIMARY_HOVER = mix(accent, Color.BLACK, 0.10f);
        PRIMARY_PRESSED = mix(accent, Color.BLACK, 0.18f);
        PRIMARY_SOFT = mix(accent, Color.WHITE, 0.78f);
        SIDEBAR_ACTIVE = accent;
        THEME_PREFERENCES.putInt("accentRgb", accent.getRGB());
    }

    private static Color mix(Color first, Color second, float secondRatio) {
        float firstRatio = 1f - secondRatio;
        int red = Math.round(first.getRed() * firstRatio + second.getRed() * secondRatio);
        int green = Math.round(first.getGreen() * firstRatio + second.getGreen() * secondRatio);
        int blue = Math.round(first.getBlue() * firstRatio + second.getBlue() * secondRatio);
        return new Color(red, green, blue);
    }

    public static Font font(int style, int size) {
        return new Font("맑은 고딕", style, size);
    }

    public static JLabel label(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(style, size));
        label.setForeground(color);
        return label;
    }

    public static JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel transparentPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel titlePanel(String title, String description) {
        JPanel panel = transparentPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel titleLabel = label(title, Font.BOLD, 26, TEXT);
        JLabel descriptionLabel = label(description, Font.PLAIN, 13, SUB_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(descriptionLabel);
        return panel;
    }

    public static RoundedPanel card() {
        return new RoundedPanel(28, CARD);
    }

    public static JButton primaryButton(String text) {
        RoundedButton button = new RoundedButton(text, false);
        button.setFont(font(Font.BOLD, 13));
        button.setPreferredSize(new Dimension(110, 40));
        return button;
    }

    public static JButton outlineButton(String text) {
        RoundedButton button = new RoundedButton(text, true);
        button.setFont(font(Font.BOLD, 12));
        button.setPreferredSize(new Dimension(110, 38));
        return button;
    }

    public static JTextField textField(String tooltip) {
        RoundedTextField field = new RoundedTextField();
        field.setToolTipText(tooltip);
        field.setFont(font(Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(180, 40));
        return field;
    }

    public static JPasswordField passwordField(String tooltip) {
        RoundedPasswordField field = new RoundedPasswordField();
        field.setToolTipText(tooltip);
        field.setFont(font(Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(180, 40));
        return field;
    }

    public static JComboBox<String> combo(String... values) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setFont(font(Font.PLAIN, 13));
        combo.setBackground(CARD);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        combo.setPreferredSize(new Dimension(160, 40));
        return combo;
    }

    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;

        public RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(105, 77, 61, 18));
            g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 7, radius, radius);
            g2.setColor(fill);
            g2.fillRoundRect(1, 1, getWidth() - 4, getHeight() - 6, radius, radius);
            g2.setColor(SOFT_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(1, 1, getWidth() - 5, getHeight() - 7, radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundedButton extends JButton {
        private final boolean outlined;

        RoundedButton(String text, boolean outlined) {
            super(text);
            this.outlined = outlined;
            setForeground(outlined ? PRIMARY : CARD);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill;

            if (!isEnabled()) {
                fill = outlined ? CARD : new Color(210, 213, 215);
            } else if (getModel().isPressed()) {
                fill = outlined ? new Color(255, 232, 212) : PRIMARY_PRESSED;
            } else if (getModel().isRollover()) {
                fill = outlined ? PRIMARY_SOFT : PRIMARY_HOVER;
            } else {
                fill = outlined ? CARD : PRIMARY;
            }

            if (!outlined && isEnabled()) {
                g2.setColor(new Color(185, 88, 45, 28));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 5, 20, 20);
            }

            g2.setColor(fill);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 4, 20, 20);

            if (outlined) {
                g2.setColor(isEnabled() ? PRIMARY : BORDER);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 5, 20, 20);
            }

            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundedTextField extends JTextField {
        RoundedTextField() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 13, 0, 13));
            setBackground(CARD);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            paintFieldBackground(graphics, this, hasFocus());
            super.paintComponent(graphics);
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        RoundedPasswordField() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 13, 0, 13));
            setBackground(CARD);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            paintFieldBackground(graphics, this, hasFocus());
            super.paintComponent(graphics);
        }
    }

    private static void paintFieldBackground(Graphics graphics, Component component, boolean focused) {
        Graphics2D g2 = (Graphics2D)graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(CARD);
        g2.fillRoundRect(1, 1, component.getWidth() - 2, component.getHeight() - 2, 18, 18);
        g2.setColor(focused ? PRIMARY : BORDER);
        g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
        g2.drawRoundRect(1, 1, component.getWidth() - 3, component.getHeight() - 3, 18, 18);
        g2.dispose();
    }
}
