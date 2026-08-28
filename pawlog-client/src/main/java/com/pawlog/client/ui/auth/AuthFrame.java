package com.pawlog.client.ui.auth;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.*;

import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.Storage.ServerSyncScheduler;
import com.pawlog.client.Storage.UsageAutoSaveScheduler;
import com.pawlog.client.Storage.UsageLocalSaveService;
import com.pawlog.client.network.PawLogClient;
import com.pawlog.client.tracker.ProgramTracker;
import com.pawlog.client.tracker.UsageKey;
import com.pawlog.client.ui.common.PawLogDialog;
import com.pawlog.client.ui.dashboard.DashboardFrame;

public class AuthFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private Point headerDragOffset;
    private final DashboardFrame dashboardFrame;
    private boolean loggedIn;
    private final UsageAutoSaveScheduler autoSaveScheduler = new UsageAutoSaveScheduler();
    private final ServerSyncScheduler serverSyncScheduler = new ServerSyncScheduler();
    private final UsageLocalSaveService localSaveService = new UsageLocalSaveService();
    private static AuthStore authStore = AuthStore.getInstance();
    private Consumer<Boolean> widgetVisibilityHandler = visible -> {};
    String id = authStore.getId();
    
    public AuthFrame() {
        this(true);
    }

    private void openDashboard() {
        serverSyncScheduler.start();
        autoSaveScheduler.start();
        ProgramTracker.startTracking();

        loggedIn = true;
        widgetVisibilityHandler.accept(true);
        setVisible(false);
        dashboardFrame.open();
    }

    public AuthFrame(boolean showImmediately) {
        dashboardFrame = new DashboardFrame(this::showLoginAfterLogout);
        PawLogClient.getInstance().setForceLogoutListener(message -> {
            SwingUtilities.invokeLater(() -> handleForcedLogout(message));
        });
        setSize(680, 760);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        updateWindowShape();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateWindowShape();
            }
        });

        Login loginPanel = new Login(() -> showPage("REGISTER"), this::openDashboard);
        Register registerPanel = new Register(() -> showPage("LOGIN"));

        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(registerPanel, "REGISTER");

        add(topPanel(), BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        showPage("LOGIN");
        if (showImmediately) {
            setVisible(true);
        }
    }

    private void showPage(String pageName) {
        cardLayout.show(cardPanel, pageName);
    }

    private void handleForcedLogout(String message) {
        if (!loggedIn) {
            return;
        }

        widgetVisibilityHandler.accept(false);
        showLoginAfterLogout();

        PawLogDialog.warning(this, "로그아웃 안내", message);
    }

    private void showLoginAfterLogout() {
        dashboardFrame.setVisible(false);
        ProgramTracker.stopTracking();
        autoSaveScheduler.stop();
        serverSyncScheduler.stop();

        try {
            localSaveService.savePending();
        } catch (Exception exception) {
            System.out.println("로그아웃 전 사용 기록 저장 실패");
            exception.printStackTrace();
        }

        ProgramTracker.resetTrackingData();
        dashboardFrame.resetUserData();
        PawLogClient.getInstance().disconnect();
        authStore.logout();
        loggedIn = false;
        showPage("LOGIN");
        setState(JFrame.NORMAL);
        setVisible(true);
        toFront();
        requestFocus();
    }

    public void setWidgetVisibilityHandler(
        Consumer<Boolean> widgetVisibilityHandler
    ) {
        this.widgetVisibilityHandler = widgetVisibilityHandler == null
            ? visible -> {}
            : widgetVisibilityHandler;
    }

    private JPanel topPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 55));
        headerPanel.setBackground(PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));

//----------------원본-----------------------
//      JLabel titleLabel = new JLabel("🐾 Pawlog");
//---------------------------------------

//----------------수정본-----------------------
        JLabel titleLabel;
        // 이미지 로드 및 리사이징 (24x24)
        ImageIcon icon = null;
        URL imgUrl = getClass().getResource("/images/pawIcon_card.png");
        if (imgUrl == null) {
            titleLabel = new JLabel("🐾 Pawlog");
        }else {
            Image img = new ImageIcon(imgUrl).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
            titleLabel = new JLabel("Pawlog", icon, SwingConstants.LEFT);
            titleLabel.setIconTextGap(8); // 이미지와 텍스트 사이 간격
        }
//---------------------------------------


        titleLabel.setForeground(CARD);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));

        JButton closeButton = new AuthCloseButton();
        closeButton.setPreferredSize(new Dimension(38, 38));
        closeButton.addActionListener(event -> setVisible(false));

        JPanel closeButtonPanel = new JPanel(new GridBagLayout());
        closeButtonPanel.setOpaque(false);
        closeButtonPanel.add(closeButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeButtonPanel, BorderLayout.EAST);
        
        addHeaderDrag(headerPanel);
        addHeaderDrag(titleLabel);

        return headerPanel;
    }

    private void updateWindowShape() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
    }

    private class AuthCloseButton extends JButton {
        AuthCloseButton() {
            super("×");
            setFont(font(Font.BOLD, 22));
            setForeground(RED);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setMargin(new Insets(0, 0, 2, 0));
            setBorder(BorderFactory.createEmptyBorder());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean active = getModel().isRollover() || getModel().isPressed();
            g2.setColor(active ? RED : new Color(255, 235, 232));
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 4, 24, 24);
            g2.dispose();
            setForeground(active ? CARD : RED);
            super.paintComponent(graphics);
        }
    }

    private void addHeaderDrag(Component component) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                headerDragOffset = event.getLocationOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (headerDragOffset == null) {
                    return;
                }

                Point currentPoint = event.getLocationOnScreen();
                int movedX = currentPoint.x - headerDragOffset.x;
                int movedY = currentPoint.y - headerDragOffset.y;

                setLocation(getX() + movedX, getY() + movedY);
                headerDragOffset = currentPoint;
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                headerDragOffset = null;
            }
        };

        component.addMouseListener(mouseAdapter);
        component.addMouseMotionListener(mouseAdapter);
    }
    public void open() {
        SwingUtilities.invokeLater(() -> {
            if (loggedIn) {
                dashboardFrame.open();
                return;
            }

            setState(JFrame.NORMAL);
            setVisible(true);
            toFront();
            requestFocus();
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(AuthFrame::new);
    }
}
