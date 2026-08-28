package com.pawlog.client.ui.widget;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

class WidgetView extends JPanel {
    private final ImageIcon animatedDog;
    private final BufferedImage idleDog;
    private boolean paused;
    private boolean hovered;
    private boolean alwaysAnimated;

    WidgetView() {
        URL imageUrl = getClass().getResource("/images/dogIcon_move.gif");
        if (imageUrl == null) {
            imageUrl = getClass().getResource("/images/pawIcon.png");
        }

        animatedDog = imageUrl == null ? null : new ImageIcon(imageUrl);
        idleDog = readFirstFrame(imageUrl);
        alwaysAnimated = WidgetSettings.isAlwaysAnimated();
        setOpaque(false);
        setPreferredSize(new Dimension(120, 120));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    void updateStatus(long seconds, boolean paused) {
        this.paused = paused;
        repaint();
    }

    void setHovered(boolean hovered) {
        this.hovered = hovered;
        repaint();
    }

    void setAlwaysAnimated(boolean alwaysAnimated) {
        this.alwaysAnimated = alwaysAnimated;
        repaint();
    }

    private BufferedImage readFirstFrame(URL imageUrl) {
        if (imageUrl == null) return null;
        try {
            return ImageIO.read(imageUrl);
        } catch (IOException exception) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D)graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int imageX = (getWidth() - 120) / 2;
        int imageY = (getHeight() - 120) / 2;

        if ((alwaysAnimated || hovered) && animatedDog != null) {
            animatedDog.paintIcon(this, g2, imageX, imageY);
        } else if (idleDog != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(idleDog, imageX, imageY, 120, 120, this);
        }

        int badgeSize = 17;
        int badgeX = getWidth() - 22;
        int badgeY = getHeight() - 22;
        g2.setColor(CARD);
        g2.fillOval(badgeX - 2, badgeY - 2, badgeSize + 4, badgeSize + 4);
        g2.setColor(paused ? BUTTER : SUCCESS);
        g2.fillOval(badgeX, badgeY, badgeSize, badgeSize);
        g2.setColor(Color.WHITE);
        g2.fillOval(badgeX + 6, badgeY + 6, 5, 5);
        g2.dispose();
    }
}
