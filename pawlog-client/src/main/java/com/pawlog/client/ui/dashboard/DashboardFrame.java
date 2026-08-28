package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.pawlog.client.ui.widget.WidgetSettings;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JToggleButton;

public class DashboardFrame extends JFrame {
    private final Runnable showLogin;
    private final Consumer<Color> themeChangeHandler;
    private static final String HOME = "HOME";
    private static final String STATISTICS = "STATISTICS";
    private static final String PROGRAMS = "PROGRAMS";
    private static final String BOARD = "BOARD";
    private static final String SETTINGS = "SETTINGS";
    private static final String FOCUS = "FOCUS";
    private static final String MY_PAGE = "MY_PAGE";
    private static final String LOGOUT = "LOGOUT";
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final HomePanel homePanel = new HomePanel();
    private final StatisticsPanel statisticsPanel = new StatisticsPanel();
    private final Map<String, JButton> menuButtons = new LinkedHashMap<>();
    private String selectedCardName = HOME;
    private JPanel headerPanel;
    private JButton closeButton;

    private Point headerDragOffset;

    public DashboardFrame(Runnable showLogin, Consumer<Color> themeChangeHandler) {
        this.showLogin = showLogin;
        this.themeChangeHandler = themeChangeHandler == null ? color -> {} : themeChangeHandler;
        setUndecorated(true);
        setTitle("Pawlog");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1100, 800));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());
        updateWindowShape();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateWindowShape();
            }
        });

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 58));
        headerPanel.setBackground(PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(7, 22, 7, 10));

        JLabel titleLabel = new JLabel("Pawlog ");
        titleLabel.setFont(font(Font.BOLD, 20));
        titleLabel.setForeground(CARD);
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);

        closeButton = new HeaderCloseButton();
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
        sidebarPanel.setPreferredSize(new Dimension(220, 0));
        sidebarPanel.setBackground(SIDEBAR);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(22, 12, 18, 12));

        JButton homeButton = createMenuButton("홈", HOME);
        JButton statisticsButton = createMenuButton("통계", STATISTICS);
        JButton programsButton = createMenuButton("프로그램 분석", PROGRAMS);
        JButton boardButton = createMenuButton("게시판", BOARD);
        JButton focusButton = createMenuButton("집중 모드", FOCUS);
        JButton myPageButton = createMenuButton("마이페이지", MY_PAGE);
        JButton settingsButton = createMenuButton("설정", SETTINGS);
        JButton logout = createMenuButton("로그아웃", LOGOUT);
        logout.setForeground(new Color(255, 135, 126));
        logout.addActionListener(event -> {
            setVisible(false);
            showLogin.run();
        });
        sidebarPanel.add(homeButton);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(statisticsButton);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(programsButton);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(boardButton);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(focusButton);
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(myPageButton);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(settingsButton);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(logout);

        selectMenu(HOME);

        return sidebarPanel;
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton button = new SidebarButton(text, cardName);
        button.setFont(font(Font.BOLD, 18));
        button.setForeground(new Color(225, 230, 232));
        button.setBackground(SIDEBAR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(196, 48));
        button.setMinimumSize(new Dimension(196, 48));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 12));

        menuButtons.put(cardName, button);

        button.addActionListener(event -> {
            if (!LOGOUT.equals(cardName)) {
                cardLayout.show(contentPanel, cardName);
                selectMenu(cardName);
            }
        });

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (!cardName.equals(selectedCardName)) {
                    button.setBackground(SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                updateMenuButtonStyle(cardName, button);
            }
        });

        return button;
    }

    private void selectMenu(String cardName) {
        selectedCardName = cardName;
        menuButtons.forEach(this::updateMenuButtonStyle);
    }

    private void updateMenuButtonStyle(String cardName, JButton button) {
        boolean selected = cardName.equals(selectedCardName);
        button.setBackground(selected ? SIDEBAR_ACTIVE : SIDEBAR);
        button.setForeground(selected ? CARD : new Color(225, 230, 232));
        button.repaint();

        if (LOGOUT.equals(cardName) && !selected) {
            button.setForeground(new Color(255, 135, 126));
        }
    }

    private class SidebarButton extends JButton {
        private final String cardName;

        SidebarButton(String text, String cardName) {
            super(text);
            this.cardName = cardName;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean selected = cardName.equals(selectedCardName);
            Color fill = selected ? PRIMARY : getModel().isRollover() ? SIDEBAR_HOVER : SIDEBAR;

            if (selected) {
                g2.setColor(new Color(45, 29, 23, 28));
                g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 7, 28, 28);
            }

            g2.setColor(fill);
            g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 7, 28, 28);

            if (selected) {
                g2.setColor(CARD);
                int pawY = getHeight() / 2;
                g2.fillOval(14, pawY, 7, 6);
                g2.fillOval(13, pawY - 6, 3, 4);
                g2.fillOval(17, pawY - 8, 3, 4);
                g2.fillOval(21, pawY - 6, 3, 4);
            }
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private JPanel createContentPanel() {
        contentPanel.setBackground(BACKGROUND);
        contentPanel.add(homePanel, HOME);
        contentPanel.add(statisticsPanel, STATISTICS);
        contentPanel.add(new ProgramAnalyticsPanel(), PROGRAMS);
        contentPanel.add(new ProgramBoardPanel(), BOARD);
        contentPanel.add(new FocusModePanel(), FOCUS);
        contentPanel.add(new MyPagePanel(), MY_PAGE);
        contentPanel.add(createSettingsPage(), SETTINGS);

        cardLayout.show(contentPanel, HOME);

        return contentPanel;
    }

    private JPanel createPage(String title) {
        JPanel pagePanel = new JPanel(new BorderLayout());
        pagePanel.setBackground(BACKGROUND);
        pagePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        titleLabel.setForeground(TEXT);

        pagePanel.add(titleLabel, BorderLayout.CENTER);

        return pagePanel;
    }

    private JPanel createSettingsPage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(34, 38, 34, 38));

        JPanel heading = titlePanel("설정", "테마와 위젯 움직임을 내 취향에 맞게 바꿔보세요.");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel themeCard = card();
        themeCard.setLayout(new BorderLayout(0, 18));
        themeCard.setBorder(BorderFactory.createEmptyBorder(24, 26, 26, 26));
        themeCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        themeCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel themeTitle = transparentPanel();
        themeTitle.setLayout(new BoxLayout(themeTitle, BoxLayout.Y_AXIS));
        JLabel title = label("테마 색상", Font.BOLD, 18, TEXT);
        JLabel description = label("선택한 색상은 버튼과 강조 요소에 바로 적용돼요.", Font.PLAIN, 13, SUB_TEXT);
        themeTitle.add(title);
        themeTitle.add(Box.createVerticalStrut(5));
        themeTitle.add(description);

        JPanel choices = transparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        choices.add(new ThemeButton("복숭아", new Color(255, 157, 108)));
        choices.add(new ThemeButton("코랄", new Color(255, 122, 137)));
        choices.add(new ThemeButton("민트", new Color(83, 196, 163)));
        choices.add(new ThemeButton("라벤더", new Color(153, 137, 226)));
        choices.add(new ThemeButton("블루", new Color(101, 154, 235)));

        themeCard.add(themeTitle, BorderLayout.NORTH);
        themeCard.add(choices, BorderLayout.CENTER);

        RoundedPanel motionCard = card();
        motionCard.setLayout(new BorderLayout(0, 18));
        motionCard.setBorder(BorderFactory.createEmptyBorder(24, 26, 26, 26));
        motionCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        motionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel motionTitle = transparentPanel();
        motionTitle.setLayout(new BoxLayout(motionTitle, BoxLayout.Y_AXIS));
        motionTitle.add(label("위젯 움직임", Font.BOLD, 18, TEXT));
        motionTitle.add(Box.createVerticalStrut(5));
        motionTitle.add(label("강아지가 언제 움직일지 선택할 수 있어요.", Font.PLAIN, 13, SUB_TEXT));

        JPanel motionChoices = transparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        MovementModeButton hoverOnlyButton = new MovementModeButton("마우스를 올릴 때만", false);
        MovementModeButton alwaysButton = new MovementModeButton("항상 움직이기", true);
        ButtonGroup movementGroup = new ButtonGroup();
        movementGroup.add(hoverOnlyButton);
        movementGroup.add(alwaysButton);

        boolean alwaysAnimated = WidgetSettings.isAlwaysAnimated();
        hoverOnlyButton.setSelected(!alwaysAnimated);
        alwaysButton.setSelected(alwaysAnimated);
        motionChoices.add(hoverOnlyButton);
        motionChoices.add(alwaysButton);

        motionCard.add(motionTitle, BorderLayout.NORTH);
        motionCard.add(motionChoices, BorderLayout.CENTER);

        page.add(heading);
        page.add(Box.createVerticalStrut(24));
        page.add(themeCard);
        page.add(Box.createVerticalStrut(16));
        page.add(motionCard);
        page.add(Box.createVerticalGlue());
        return page;
    }

    private void applyTheme(Color accent) {
        Color previousPrimary = PRIMARY;
        Color previousHover = PRIMARY_HOVER;
        Color previousSoft = PRIMARY_SOFT;

        UiComponents.applyAccent(accent);
        replaceThemeColors(this, previousPrimary, previousHover, previousSoft);
        headerPanel.setBackground(PRIMARY);
        themeChangeHandler.accept(previousPrimary);
        selectMenu(selectedCardName);
        revalidate();
        repaint();
    }

    private void replaceThemeColors(
        Component component,
        Color previousPrimary,
        Color previousHover,
        Color previousSoft
    ) {
        if (previousPrimary.equals(component.getForeground())) {
            component.setForeground(PRIMARY);
        } else if (previousSoft.equals(component.getForeground())) {
            component.setForeground(PRIMARY_SOFT);
        }

        if (previousPrimary.equals(component.getBackground())) {
            component.setBackground(PRIMARY);
        } else if (previousHover.equals(component.getBackground())) {
            component.setBackground(PRIMARY_HOVER);
        } else if (previousSoft.equals(component.getBackground())) {
            component.setBackground(PRIMARY_SOFT);
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                replaceThemeColors(child, previousPrimary, previousHover, previousSoft);
            }
        }
    }

    private class ThemeButton extends JButton {
        private final Color accent;

        ThemeButton(String text, Color accent) {
            super(text);
            this.accent = accent;
            setFont(font(Font.BOLD, 13));
            setForeground(CARD);
            setPreferredSize(new Dimension(104, 42));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            addActionListener(event -> applyTheme(accent));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getModel().isPressed() ? accent.darker() : accent;
            g2.setColor(fill);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 3, 22, 22);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private class HeaderCloseButton extends JButton {
        HeaderCloseButton() {
            super("×");
            setFont(font(Font.BOLD, 22));
            setForeground(RED);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setMargin(new Insets(0, 0, 2, 0));
            setBorder(BorderFactory.createEmptyBorder());
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hovered = getModel().isRollover();
            boolean pressed = getModel().isPressed();
            Color fill = pressed ? RED.darker() : hovered ? RED : new Color(255, 235, 232);

            g2.setColor(new Color(94, 48, 42, 24));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 5, 24, 24);
            g2.setColor(fill);
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 4, 24, 24);
            g2.dispose();

            setForeground(hovered || pressed ? CARD : RED);
            super.paintComponent(graphics);
        }
    }

    private class MovementModeButton extends JToggleButton {
        private final boolean alwaysAnimated;

        MovementModeButton(String text, boolean alwaysAnimated) {
            super(text);
            this.alwaysAnimated = alwaysAnimated;
            setFont(font(Font.BOLD, 13));
            setForeground(TEXT);
            setPreferredSize(new Dimension(170, 42));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            addActionListener(event -> {
                WidgetSettings.setAlwaysAnimated(this.alwaysAnimated);
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isSelected() ? PRIMARY_SOFT : CARD);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 3, 22, 22);
            g2.setColor(isSelected() ? PRIMARY : BORDER);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 4, 22, 22);
            g2.dispose();
            setForeground(isSelected() ? PRIMARY : TEXT);
            super.paintComponent(graphics);
        }
    }

    public void open() {
        homePanel.refreshUserInfo();
        homePanel.loadTodayInfo();
        cardLayout.show(contentPanel, HOME);
        selectMenu(HOME);
        setState(JFrame.NORMAL);
        setVisible(true);
        toFront();
        requestFocus();
    }

    public void resetUserData() {
        homePanel.resetTodayInfo();
        statisticsPanel.resetUserData();
        cardLayout.show(contentPanel, HOME);
        selectMenu(HOME);
    }
}
