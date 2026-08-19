package com.pawlog.client.ui.auth;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.tracker.ProgramTracker;
import com.pawlog.client.tracker.UsageKey;
import com.pawlog.client.ui.dashboard.DashboardFrame;

public class AuthFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private Point headerDragOffset;
    private final DashboardFrame dashboardFrame;
    private boolean loggedIn;
    
    
    private static AuthStore authStore = AuthStore.getInstance();
    String email = authStore.getEmail();
    
    public AuthFrame() {
        this(true);
    }

    public AuthFrame(boolean showImmediately) {
        dashboardFrame = new DashboardFrame(this::showLoginAfterLogout);
        setSize(590, 750);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Login loginPanel = new Login(() -> showPage("REGISTER"), () -> {
            loggedIn = true;
            setVisible(false);
            dashboardFrame.open();
        });
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

    private void showLoginAfterLogout() {
        loggedIn = false;
        showPage("LOGIN");
        setState(JFrame.NORMAL);
        setVisible(true);
        toFront();
        requestFocus();
    }

    private JPanel topPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 55));
        headerPanel.setBackground(PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));

        JLabel titleLabel = new JLabel("🐾 Pawlog");
        titleLabel.setForeground(CARD);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        

   

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        closeButton.setForeground(CARD);
        closeButton.setBackground(RED);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setPreferredSize(new Dimension(38, 38));
        closeButton.setMargin(new Insets(0, 0, 2, 0));
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
