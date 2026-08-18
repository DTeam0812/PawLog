package com.pawlog.client.ui.dashboard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class UiComponents {
    static final Color BACKGROUND = new Color(247, 244, 239);
    static final Color CARD = Color.WHITE;
    static final Color PRIMARY = new Color(255, 159, 67);
    static final Color TEXT = new Color(45, 52, 54);
    static final Color SUB_TEXT = new Color(99, 110, 114);
    static final Color BORDER = new Color(225, 225, 225);
    static final Color GREEN = new Color(0, 184, 148);
    static final Color RED = new Color(231, 76, 60);

    private UiComponents() {}

    static Font font(int style, int size) {
        return new Font("맑은 고딕", style, size);
    }

    static JLabel label(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(style, size));
        label.setForeground(color);
        return label;
    }

    static JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    static JPanel transparentPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    static JPanel titlePanel(String title, String description) {
        JPanel panel = transparentPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel titleLabel = label(title, Font.BOLD, 24, TEXT);
        JLabel descriptionLabel = label(description, Font.PLAIN, 13, SUB_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descriptionLabel);
        return panel;
    }

    static RoundedPanel card() {
        return new RoundedPanel(16, CARD);
    }

    static JButton primaryButton(String text) {
        JButton button = new RoundedButton(text, PRIMARY, Color.WHITE);
        button.setFont(font(Font.BOLD, 13));
        button.setPreferredSize(new Dimension(110, 38));
        return button;
    }

    static JButton outlineButton(String text) {
        JButton button = new JButton(text);
        button.setFont(font(Font.BOLD, 12));
        button.setForeground(PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY));
        button.setPreferredSize(new Dimension(110, 36));
        return button;
    }

    static JTextField textField(String tooltip) {
        JTextField field = new JTextField();
        field.setToolTipText(tooltip);
        field.setFont(font(Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(0, 11, 0, 11)));
        field.setPreferredSize(new Dimension(180, 38));
        return field;
    }

    static JComboBox<String> combo(String... values) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setFont(font(Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
        combo.setPreferredSize(new Dimension(160, 38));
        return combo;
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;

        RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundedButton extends JButton {
        private final Color fill;

        RoundedButton(String text, Color fill, Color foreground) {
            super(text);
            this.fill = fill;
            setForeground(foreground);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
