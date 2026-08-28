package com.pawlog.client.ui.widget;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import com.pawlog.client.tracker.ProgramTracker;
import com.pawlog.client.ui.auth.AuthFrame;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences; // windows 레지스트리에 사용자설정을 저장
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.ImageIcon;
import java.net.URL;

public class Widget {

    
    private final AuthFrame AUTH;
  
    private static final String WINDOW_X_KEY = "widgetX";
    private static final String WINDOW_Y_KEY = "widgetY";
    private final Preferences preferences = Preferences.userNodeForPackage(Widget.class);

    private void saveWindowLocation() {
    preferences.putInt(WINDOW_X_KEY, window.getX());
    preferences.putInt(WINDOW_Y_KEY, window.getY());
    }

    private void loadWindowLocation() {
        int defaultValue = Integer.MIN_VALUE;
        int x = preferences.getInt(WINDOW_X_KEY, defaultValue);
        int y = preferences.getInt(WINDOW_Y_KEY, defaultValue);

        if (x == defaultValue || y == defaultValue || !isVisibleOnScreen(x, y)) {
            setDefaultWindowLocation();
            saveWindowLocation();
            return;
        }

        window.setLocation(x, y);
    }

    private boolean isVisibleOnScreen(int x, int y) {
        Rectangle widgetBounds = new Rectangle(x, y, window.getWidth(), window.getHeight());

        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            Rectangle screenBounds = device.getDefaultConfiguration().getBounds();
            Rectangle visibleArea = widgetBounds.intersection(screenBounds);

            if (visibleArea.width >= 32 && visibleArea.height >= 32) {
                return true;
            }
        }

        return false;
    }

    private void setDefaultWindowLocation() {
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle screen = config.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
        int margin = 24;

        int x = screen.x + screen.width - insets.right - window.getWidth() - margin;
        int y = screen.y + screen.height - insets.bottom - window.getHeight() - margin;
        window.setLocation(x, y);
    }
    private static final int DRAG_THRESHOLD = 5;

    private final JWindow window;
    private final JWindow speechBubble;
    private final WidgetView widgetView;
    private final JLabel programLabel;
    private final JLabel sessionLabel;
    private TrayIcon trayIcon;
    private JCheckBoxMenuItem widgetPauseItem;
    private CheckboxMenuItem trayPauseItem;
    private Point dragOffset;
    private Point pressScreenPoint;
    private boolean dragged;
    private boolean paused;

    public Widget() {
    ProgramTracker.startTracking();
    window = new JWindow();
    speechBubble = new JWindow(window);
    AUTH = new AuthFrame(false);
    AUTH.setWidgetVisibilityHandler(visible -> {
        window.setVisible(visible);
        if (!visible) speechBubble.setVisible(false);
    });

    widgetView = new WidgetView();
    WidgetAlertService.setBarkHandlers(widgetView::startBarking, widgetView::stopBarking);
    programLabel = label("활성 프로그램 확인 중", Font.BOLD, 13, TEXT);
    sessionLabel = label("현재 세션 0초", Font.PLAIN, 11, SUB_TEXT);

    addMouseEvents(widgetView);
    addPopupMenu(widgetView);
    addHoverBubble(widgetView);

    window.add(widgetView);
    window.setSize(120, 120);
    window.setBackground(TRANSPARENT);
    window.setAlwaysOnTop(true);

    createSpeechBubble();
    createTrayIcon();
    startWidgetRefresh();
    loadWindowLocation();
}

//----------------원본-----------------------
//    private JLabel createDogLabel() {
//        JLabel dogLabel = new JLabel("🐶", SwingConstants.CENTER);
//        dogLabel.setForeground(CARD);
//        dogLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
//        dogLabel.setOpaque(false);
//        dogLabel.setToolTipText("클릭: 상세 정보 / 드래그: 이동 / 우클릭: 메뉴");
//
//        return dogLabel;
//    }
//---------------------------------------



//----------------수정본-----------------------
    private JLabel createDogLabel() {
//        //강아지 gif 버전
//        URL imgUrl = getClass().getResource("/images/dogIcon_move.gif");
//        JLabel dogLabel;
//
//        if (imgUrl == null) {
//            dogLabel = new JLabel("🐶", SwingConstants.CENTER);
//            dogLabel.setForeground(CARD);
//            dogLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
//        }else{
//            ImageIcon icon = new ImageIcon(imgUrl);
//            dogLabel = new JLabel(icon, SwingConstants.CENTER);
//        }

        //발바닥 버전
        URL imgUrl = getClass().getResource("/images/pawIcon.png");
        JLabel dogLabel;

        if (imgUrl == null) {
            dogLabel = new JLabel("🐶", SwingConstants.CENTER);
            dogLabel.setForeground(CARD);
            dogLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        }else{
            // 이미지를 72x72 크기로 부드럽게 리사이징
            Image img = new ImageIcon(imgUrl).getImage().getScaledInstance(72, 72, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(img);
            dogLabel = new JLabel(icon, SwingConstants.CENTER);
        }

        dogLabel.setOpaque(false);
        dogLabel.setToolTipText("클릭: 상세 정보 / 드래그: 이동 / 우클릭: 메뉴");

        return dogLabel;
    }
//------------------------------------------

    private void addMouseEvents(JComponent dogLabel) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    dragOffset = event.getPoint();
                    pressScreenPoint = event.getLocationOnScreen();
                    dragged = false;
                }
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (dragOffset == null || pressScreenPoint == null) {
                    return;
                }

                Point currentScreenPoint = event.getLocationOnScreen();

                int movedX = currentScreenPoint.x - pressScreenPoint.x;
                int movedY = currentScreenPoint.y - pressScreenPoint.y;

                if (Math.abs(movedX) >= DRAG_THRESHOLD
                        || Math.abs(movedY) >= DRAG_THRESHOLD) {
                    dragged = true;
                }

                window.setLocation(
                        currentScreenPoint.x - dragOffset.x,
                        currentScreenPoint.y - dragOffset.y
                );
            }

          
            @Override
            public void mouseReleased(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event) && !dragged) {  bounceAndOpen();  }
                if (dragged) {  saveWindowLocation(); }  
                dragOffset = null;
                pressScreenPoint = null;
                dragged = false; }};

        dogLabel.addMouseListener(mouseAdapter);
        dogLabel.addMouseMotionListener(mouseAdapter);
    }

    private void addPopupMenu(JComponent dogLabel) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setLightWeightPopupEnabled(false);//창이 안잘리게해줌

        JMenuItem detailsItem = new JMenuItem("상세 정보");
        widgetPauseItem = new JCheckBoxMenuItem("추적 일시정지");
        JMenuItem exitItem = new JMenuItem("종료");

        detailsItem.addActionListener(event -> openDetails());

        widgetPauseItem.addActionListener(event -> setTrackingPaused(widgetPauseItem.isSelected()));
        exitItem.addActionListener(event -> exitApplication());

        popupMenu.add(detailsItem);
        popupMenu.add(widgetPauseItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        dogLabel.setComponentPopupMenu(popupMenu);
    }

    private void createTrayIcon() {
        if (!SystemTray.isSupported()) return;

        URL imageUrl = getClass().getResource("/images/trayIcon.png");
        if (imageUrl == null) return;

        PopupMenu trayMenu = new PopupMenu();
        MenuItem openItem = new MenuItem("PawLog 열기");
        trayPauseItem = new CheckboxMenuItem("추적 일시정지");
        MenuItem showWidgetItem = new MenuItem("위젯 보이기");
        MenuItem exitItem = new MenuItem("종료");

        openItem.addActionListener(event -> SwingUtilities.invokeLater(this::openDetails));
        trayPauseItem.addItemListener(event -> setTrackingPaused(trayPauseItem.getState()));
        showWidgetItem.addActionListener(event -> SwingUtilities.invokeLater(this::show));
        exitItem.addActionListener(event -> exitApplication());

        trayMenu.add(openItem);
        trayMenu.add(showWidgetItem);
        trayMenu.add(trayPauseItem);
        trayMenu.addSeparator();
        trayMenu.add(exitItem);

        Image image = Toolkit.getDefaultToolkit().getImage(imageUrl);
        trayIcon = new TrayIcon(image, "PawLog", trayMenu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(event -> SwingUtilities.invokeLater(this::openDetails));

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException exception) {
            trayIcon = null;
            exception.printStackTrace();
        }
    }

    private void setTrackingPaused(boolean paused) {
        this.paused = paused;

        if (paused) ProgramTracker.stopTracking();
        else ProgramTracker.startTracking();

        if (widgetPauseItem != null) widgetPauseItem.setSelected(paused);
        if (trayPauseItem != null) trayPauseItem.setState(paused);
        widgetView.updateStatus(ProgramTracker.getUsageSeconds(), paused);
    }

    private void exitApplication() {
        ProgramTracker.stopTracking();
        WidgetAlertService.setBarkHandlers(null, null);
        speechBubble.dispose();
        window.dispose();

        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        System.exit(0);
    }

    private void createSpeechBubble() {
        JPanel bubble = card();
        bubble.setLayout(new BorderLayout(0, 4));
        bubble.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16));
        bubble.add(programLabel, BorderLayout.NORTH);
        bubble.add(sessionLabel, BorderLayout.CENTER);

        speechBubble.add(bubble);
        speechBubble.setBackground(TRANSPARENT);
        speechBubble.setAlwaysOnTop(true);
        speechBubble.setFocusableWindowState(false);
        speechBubble.pack();
    }

    private void addHoverBubble(JComponent component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                widgetView.setHovered(true);
                updateSpeechBubble();
                showSpeechBubble();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                widgetView.setHovered(false);
                speechBubble.setVisible(false);
            }
        });
    }

    private void startWidgetRefresh() {
        Timer refreshTimer = new Timer(1000, event -> {
            widgetView.setAlwaysAnimated(WidgetSettings.isAlwaysAnimated());
            widgetView.updateStatus(ProgramTracker.getUsageSeconds(), paused);
            if (speechBubble.isVisible()) {
                updateSpeechBubble();
                showSpeechBubble();
            }
        });
        refreshTimer.start();
    }

    private void bounceAndOpen() {
        int startX = window.getX();
        int startY = window.getY();
        int[] offsets = {0, -3, -7, -11, -8, -4, 0};
        int[] index = {0};
        Timer bounceTimer = new Timer(28, null);

        bounceTimer.addActionListener(event -> {
            window.setLocation(startX, startY + offsets[index[0]++]);
            if (index[0] == offsets.length) {
                bounceTimer.stop();
                window.setLocation(startX, startY);
                openDetails();
            }
        });
        bounceTimer.start();
    }

    private void updateSpeechBubble() {
        String program = ProgramTracker.getCurrentProgram();
        programLabel.setText(program == null || program.isBlank() ? "활성 프로그램 확인 중" : program);
        sessionLabel.setText((paused ? "추적 일시정지 · " : "현재 세션 · ") + formatDuration(ProgramTracker.getUsageSeconds()));
        speechBubble.pack();
    }

    private void showSpeechBubble() {
        Rectangle screen = window.getGraphicsConfiguration().getBounds();
        int x = window.getX() + window.getWidth() - speechBubble.getWidth();
        int y = window.getY() - speechBubble.getHeight() - 8;

        if (y < screen.y + 8) {
            y = window.getY() + window.getHeight() + 8;
        }
        x = Math.max(screen.x + 8, Math.min(x, screen.x + screen.width - speechBubble.getWidth() - 8));
        speechBubble.setLocation(x, y);
        speechBubble.setVisible(true);
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = seconds % 3600 / 60;
        long remainSeconds = seconds % 60;

        if (hours > 0) return hours + "시간 " + minutes + "분";
        if (minutes > 0) return minutes + "분 " + remainSeconds + "초";
        return remainSeconds + "초";
    }

    private void openDetails() {
        AUTH.open();
    }

    public void show() {
        window.setVisible(true);
        window.toFront();
    }
        
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Widget widget = new Widget();
            widget.show();
        });
    }
}
