package com.pawlog.client.ui.common;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public final class PawLogDialog {
    private enum Type {
        INFO,
        WARNING,
        ERROR
    }

    private PawLogDialog() {}

    public static void info(Component parent, String title, String message) {
        show(parent, title, message, Type.INFO, false, "확인했어요");
    }

    public static void warning(Component parent, String title, String message) {
        show(parent, title, message, Type.WARNING, false, "확인했어요");
    }

    public static void error(Component parent, String title, String message) {
        show(parent, title, message, Type.ERROR, false, "확인했어요");
    }

    public static boolean confirm(
        Component parent,
        String title,
        String message,
        String confirmText
    ) {
        return show(parent, title, message, Type.WARNING, true, confirmText);
    }

    private static boolean show(
        Component parent,
        String title,
        String message,
        Type type,
        boolean confirm,
        String confirmText
    ) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        boolean[] accepted = {false};

        dialog.setUndecorated(true);
        dialog.setBackground(TRANSPARENT);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel outside = new JPanel(new BorderLayout());
        outside.setOpaque(false);
        outside.setBorder(BorderFactory.createEmptyBorder(10, 10, 14, 10));

        RoundedPanel card = new RoundedPanel(30, CARD);
        card.setLayout(new BorderLayout(18, 18));
        card.setBorder(BorderFactory.createEmptyBorder(24, 26, 22, 26));

        JPanel heading = transparentPanel(new BorderLayout(14, 0));
        heading.add(new PawBadge(type), BorderLayout.WEST);

        JPanel headingText = transparentPanel();
        headingText.setLayout(new BoxLayout(headingText, BoxLayout.Y_AXIS));
        headingText.add(label(title, Font.BOLD, 19, TEXT));
        headingText.add(Box.createVerticalStrut(4));
        headingText.add(label(typeDescription(type), Font.PLAIN, 12, SUB_TEXT));
        heading.add(headingText, BorderLayout.CENTER);

        JTextArea messageArea = new JTextArea(message == null ? "" : message);
        messageArea.setFont(font(Font.PLAIN, 14));
        messageArea.setForeground(TEXT);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setOpaque(false);
        messageArea.setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));

        JPanel buttons = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));

        if (confirm) {
            JButton cancelButton = outlineButton("취소할래요");
            cancelButton.setPreferredSize(new Dimension(105, 40));
            cancelButton.addActionListener(event -> dialog.dispose());
            buttons.add(cancelButton);
        }

        JButton confirmButton = primaryButton(confirmText);
        confirmButton.setPreferredSize(new Dimension(confirm ? 112 : 118, 40));
        confirmButton.addActionListener(event -> {
            accepted[0] = true;
            dialog.dispose();
        });
        buttons.add(confirmButton);

        card.add(heading, BorderLayout.NORTH);
        card.add(messageArea, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        outside.add(card, BorderLayout.CENTER);
        dialog.setContentPane(outside);

        JRootPane rootPane = dialog.getRootPane();
        rootPane.setDefaultButton(confirmButton);
        rootPane.registerKeyboardAction(
            event -> dialog.dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JRootPane.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.setSize(430, 250);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return accepted[0];
    }

    private static String typeDescription(Type type) {
        return switch (type) {
            case INFO -> "PawLog가 기쁜 소식을 가져왔어요";
            case WARNING -> "잠깐, 한 번만 확인해 주세요";
            case ERROR -> "문제가 생겼어요. 같이 확인해 볼까요?";
        };
    }

    private static class PawBadge extends JPanel {
        private final Type type;

        PawBadge(Type type) {
            this.type = type;
            setOpaque(false);
            setPreferredSize(new Dimension(52, 52));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(badgeColor(type));
            g2.fillOval(0, 0, 50, 50);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
            g2.setColor(TEXT);
            g2.drawString("🐾", 11, 35);
            g2.dispose();
        }
    }

    private static Color badgeColor(Type type) {
        return switch (type) {
            case INFO -> MINT;
            case WARNING -> BUTTER;
            case ERROR -> new Color(255, 216, 213);
        };
    }
}
