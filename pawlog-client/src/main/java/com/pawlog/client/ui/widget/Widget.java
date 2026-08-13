package com.pawlog.client.ui.widget;

import com.pawlog.client.ui.dashboard.*;
import com.pawlog.client.ui.dashboard.DashboardFrame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences; // windows 레지스트리에 사용자설정을 저장
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

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

    if (x == defaultValue || y == defaultValue) {
        window.setLocationRelativeTo(null);
        return;
    }

    window.setLocation(x, y);
}
    private static final int DRAG_THRESHOLD = 5;

    private final JWindow window;
    private Point dragOffset;
    private Point pressScreenPoint;
    private boolean dragged;

    public Widget() {
    window = new JWindow();
    AUTH = new AuthFrame();
    
    JLabel dogLabel = createDogLabel();

    addMouseEvents(dogLabel);
    addPopupMenu(dogLabel);

    window.add(dogLabel);
    window.setSize(120, 120);
    window.setBackground(new Color(0, 0, 0, 0));
    window.setAlwaysOnTop(true);
    
    loadWindowLocation();
}

    private JLabel createDogLabel() {
        JLabel dogLabel = new JLabel("🐶", SwingConstants.CENTER);
        dogLabel.setForeground(new Color(255,255,255));
        dogLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        dogLabel.setOpaque(false);
        dogLabel.setToolTipText("클릭: 상세 정보 / 드래그: 이동 / 우클릭: 메뉴");

        return dogLabel;
    }

    private void addMouseEvents(JLabel dogLabel) {
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
                if (SwingUtilities.isLeftMouseButton(event) && !dragged) {  openDetails();  }
                if (dragged) {  saveWindowLocation(); }  
                dragOffset = null;
                pressScreenPoint = null;
                dragged = false; }};

        dogLabel.addMouseListener(mouseAdapter);
        dogLabel.addMouseMotionListener(mouseAdapter);
    }

    private void addPopupMenu(JLabel dogLabel) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setLightWeightPopupEnabled(false);//창이 안잘리게해줌

        JMenuItem detailsItem = new JMenuItem("상세 정보");
        JCheckBoxMenuItem pauseItem = new JCheckBoxMenuItem("추적 일시정지");
        JMenuItem exitItem = new JMenuItem("종료");

        detailsItem.addActionListener(event -> openDetails());

        pauseItem.addActionListener(event -> {
            boolean paused =
            pauseItem.isSelected();

            if (pauseItem.isSelected()) {
                System.out.println("추적을 일시정지합니다.");
            } else {
                System.out.println("추적을 다시 시작합니다.");
            }
        });

        exitItem.addActionListener(event -> {
            window.dispose();
            System.exit(0);
        });

        popupMenu.add(detailsItem);
        popupMenu.add(pauseItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        dogLabel.setComponentPopupMenu(popupMenu);
    }

    private void openDetails() {
        AUTH.open();
    }

    public void show() {
        window.setVisible(true);
    }
        
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Widget widget = new Widget();
            widget.show();
        });
    }
}