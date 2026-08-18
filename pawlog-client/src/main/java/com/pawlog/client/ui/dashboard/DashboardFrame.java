package com.pawlog.client.ui.dashboard;

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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardFrame extends JFrame {

    private static final String HOME = "HOME";
    private static final String STATISTICS = "STATISTICS";
    private static final String PROGRAMS = "PROGRAMS";
    private static final String BOARD = "BOARD";
    private static final String SETTINGS = "SETTINGS";
    private static final String PREDICTION = "PREDICTION";
    private static final String MY_PAGE = "MY_PAGE";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private Point headerDragOffset;

    public DashboardFrame() {
        setUndecorated(true);
        setTitle("Pawlog");
        setSize(900, 600);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 50));
        headerPanel.setBackground(new Color(255, 159, 67));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));

        JLabel titleLabel = new JLabel("Pawlog");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(231, 76, 60));
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

    private JPanel createSidebar() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(160, 0));
        sidebarPanel.setBackground(new Color(45, 52, 54));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton homeButton = createMenuButton("홈", HOME);
        JButton statisticsButton = createMenuButton("통계", STATISTICS);
        JButton programsButton = createMenuButton("프로그램 분석", PROGRAMS);
        JButton boardButton = createMenuButton("게시판", BOARD);
        JButton predictionButton = createMenuButton("예측 이벤트", PREDICTION);
        JButton myPageButton = createMenuButton("마이페이지", MY_PAGE);
        JButton settingsButton = createMenuButton("설정", SETTINGS);

        sidebarPanel.add(homeButton);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(statisticsButton);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(programsButton);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(boardButton);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(predictionButton);
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(myPageButton);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(settingsButton);

        return sidebarPanel;
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(45, 52, 54));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        button.addActionListener(event -> {
            cardLayout.show(contentPanel, cardName);
        });

        return button;
    }

    private JPanel createContentPanel() {
        contentPanel.add(new HomePanel(), HOME);
        contentPanel.add(new StatisticsPanel(), STATISTICS);
        contentPanel.add(new ProgramAnalyticsPanel(), PROGRAMS);
        contentPanel.add(new ProgramBoardPanel(), BOARD);
        contentPanel.add(new PredictionPanel(), PREDICTION);
        contentPanel.add(new MyPagePanel(), MY_PAGE);
        contentPanel.add(createPage("설정"), SETTINGS);

        cardLayout.show(contentPanel, HOME);

        return contentPanel;
    }

    private JPanel createPage(String title) {
        JPanel pagePanel = new JPanel(new BorderLayout());
        pagePanel.setBackground(new Color(247, 244, 239));
        pagePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        titleLabel.setForeground(new Color(45, 52, 54));

        pagePanel.add(titleLabel, BorderLayout.CENTER);

        return pagePanel;
    }

    public void open() {
        setState(JFrame.NORMAL);
        setVisible(true);
        toFront();
        requestFocus();
    }
}
