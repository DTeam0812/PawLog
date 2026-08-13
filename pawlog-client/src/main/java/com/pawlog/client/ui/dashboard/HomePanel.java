package com.pawlog.client.ui.dashboard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class HomePanel extends JPanel {
    private static final Color BACKGROUND = new Color(247, 244, 239);

    private static final Color CARD_BACKGROUND = Color.WHITE;

    private static final Color PRIMARY = new Color(255, 159, 67);

    private static final Color TEXT = new Color(45, 52, 54);

    private static final Color SUB_TEXT = new Color(120, 130, 132);

    private static final Color PROGRESS_BACKGROUND = new Color(225, 225, 225);

    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        JPanel bodyPanel = new ViewportWidthPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(BACKGROUND);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        bodyPanel.add(createGreetingPanel());
        bodyPanel.add(Box.createVerticalStrut(15));

        bodyPanel.add(createActiveProgramPanel());
        bodyPanel.add(Box.createVerticalStrut(15));

        bodyPanel.add(createSummaryRowPanel());
        bodyPanel.add(Box.createVerticalStrut(15));

        bodyPanel.add(createBottomRowPanel());

        JScrollPane scrollPane = new JScrollPane(bodyPanel);

        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createGreetingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        JLabel greetingLabel = new JLabel("안녕하세요, 사용자님");

        greetingLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        greetingLabel.setForeground(TEXT);

        LocalDate today = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN);

        JLabel dateLabel = new JLabel(today.format(formatter));

        dateLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        dateLabel.setForeground(SUB_TEXT);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(6, 20, 0, 0));

        leftPanel.add(greetingLabel);
        leftPanel.add(dateLabel);

        JPanel trackingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        trackingPanel.setOpaque(false);

        JLabel trackingDot = new JLabel("●");
        trackingDot.setForeground(new Color(0, 184, 148));

        JLabel trackingLabel = new JLabel("추적 중");

        trackingLabel.setForeground(new Color(0, 184, 148));
        trackingLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        trackingPanel.add(trackingDot);
        trackingPanel.add(trackingLabel);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(trackingPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createActiveProgramPanel() {
        JPanel card = createCardPanel();

        card.setLayout(new BorderLayout(15, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.setPreferredSize(new Dimension(900, 160));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)), BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        JLabel cardTitle = new JLabel("현재 활성 프로그램");

        cardTitle.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        cardTitle.setForeground(TEXT);

        JPanel programPanel = new JPanel(new BorderLayout(15, 0));
        programPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("VS", SwingConstants.CENTER);

        iconLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(65, 105, 225));
        iconLabel.setPreferredSize(new Dimension(52, 52));

        JPanel programTextPanel = new JPanel();
        programTextPanel.setOpaque(false);
        programTextPanel.setLayout(new BoxLayout(programTextPanel, BoxLayout.Y_AXIS));

        JLabel programName = new JLabel("VS Code");

        programName.setFont(new Font("맑은 고딕", Font.BOLD, 19));
        programName.setForeground(TEXT);

        JLabel windowTitle = new JLabel("PawLog - Visual Studio Code");

        windowTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        windowTitle.setForeground(SUB_TEXT);

        programTextPanel.add(programName);
        programTextPanel.add(Box.createVerticalStrut(3));
        programTextPanel.add(windowTitle);

        programPanel.add(iconLabel, BorderLayout.WEST);
        programPanel.add(programTextPanel, BorderLayout.CENTER);

        JPanel sessionPanel = new JPanel();
        sessionPanel.setOpaque(false);
        sessionPanel.setLayout(new BoxLayout(sessionPanel, BoxLayout.Y_AXIS));

        JLabel sessionTitle = new JLabel("현재 세션");

        sessionTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        sessionTitle.setForeground(SUB_TEXT);
        sessionTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel sessionTime = new JLabel("32분 15초");

        sessionTime.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        sessionTime.setForeground(PRIMARY);
        sessionTime.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel analysisLink = new JLabel("프로그램 분석 보기 →");

        analysisLink.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        analysisLink.setForeground(PRIMARY);
        analysisLink.setAlignmentX(Component.RIGHT_ALIGNMENT);

        sessionPanel.add(sessionTitle);
        sessionPanel.add(Box.createVerticalStrut(4));
        sessionPanel.add(sessionTime);
        sessionPanel.add(Box.createVerticalStrut(12));
        sessionPanel.add(analysisLink);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        centerPanel.add(programPanel, BorderLayout.CENTER);
        centerPanel.add(sessionPanel, BorderLayout.EAST);

        card.add(cardTitle, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSummaryRowPanel() {
        JPanel rowPanel = new JPanel(new GridLayout(1, 3, 15, 0));

        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        rowPanel.setPreferredSize(new Dimension(900, 85));

        rowPanel.add(createSummaryCard("오늘 활성 사용 시간", "4시간 20분", true));

        rowPanel.add(createSummaryCard("가장 오래 활성화한 프로그램", "Chrome", false));

        rowPanel.add(createSummaryCard("오늘 활성화한 프로그램 수", "7개", false));

        return rowPanel;
    }

    private JPanel createSummaryCard(
            String title,
            String value,
            boolean primaryValue) {

        JPanel card = createCardPanel();

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)), BorderFactory.createEmptyBorder(15, 17, 15, 17)));

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        titleLabel.setForeground(SUB_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);

        valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        valueLabel.setForeground(primaryValue ? PRIMARY : TEXT);

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);

        return card;
    }

    private JPanel createBottomRowPanel() {
        JPanel rowPanel = new JPanel(new GridBagLayout());

        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        rowPanel.setPreferredSize(new Dimension(900, 360));

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1.0;

        constraints.gridx = 0;
        constraints.weightx = 0.65;
        constraints.insets = new Insets(0, 0, 0, 8);

        rowPanel.add(createRecentProgramsPanel(), constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.35;
        constraints.insets = new Insets(0, 8, 0, 0);

        rowPanel.add(createRecommendationPanel(), constraints);

        return rowPanel;
    }

    private JPanel createRecentProgramsPanel() {
        JPanel card = createCardPanel();

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)), BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        JLabel title = new JLabel("최근 활성 프로그램");

        title.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(12));

        card.add(createProgramUsageRow("Chrome", "2분 전", "1시간 45분", 40));

        card.add(createProgramUsageRow("VS Code", "현재", "1시간 10분", 27));

        card.add(createProgramUsageRow("Slack", "15분 전", "35분", 13));

        card.add(createProgramUsageRow("Notion", "30분 전", "25분", 10));

        card.add(createProgramUsageRow("Figma", "1시간 전", "20분", 8));

        return card;
    }

    private JPanel createProgramUsageRow(
            String programName,
            String lastUsed,
            String usageTime,
            int percent) {

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 57));

        JPanel textRow = new JPanel(new BorderLayout());
        textRow.setOpaque(false);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        namePanel.setOpaque(false);

        JLabel nameLabel = new JLabel(programName);

        nameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        nameLabel.setForeground(TEXT);

        JLabel lastUsedLabel = new JLabel("  " + lastUsed);

        lastUsedLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        lastUsedLabel.setForeground(new Color(180, 185, 187));

        namePanel.add(nameLabel);
        namePanel.add(lastUsedLabel);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        timePanel.setOpaque(false);

        JLabel timeLabel = new JLabel(usageTime);

        timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        timeLabel.setForeground(SUB_TEXT);

        JLabel percentLabel = new JLabel(percent + "%");

        percentLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        percentLabel.setForeground(PRIMARY);

        timePanel.add(timeLabel);
        timePanel.add(percentLabel);

        textRow.add(namePanel, BorderLayout.WEST);
        textRow.add(timePanel, BorderLayout.EAST);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(percent);
        progressBar.setForeground(PRIMARY);
        progressBar.setBackground(PROGRESS_BACKGROUND);
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setPreferredSize(new Dimension(100, 5));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));

        row.add(textRow);
        row.add(Box.createVerticalStrut(4));
        row.add(progressBar);
        row.add(Box.createVerticalStrut(8));

        return row;
    }

    private JPanel createRecommendationPanel() {
        JPanel card = createCardPanel();

        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)), BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel title = new JLabel("추천 프로그램 TOP 3");

        title.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        title.setForeground(TEXT);

        JPanel rankingPanel = new JPanel();
        rankingPanel.setOpaque(false);
        rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));

        rankingPanel.add(createRankingRow(1, "Notion", "추천 128"));
        rankingPanel.add(Box.createVerticalStrut(12));

        rankingPanel.add(createRankingRow(2, "Figma", "추천 95"));
        rankingPanel.add(Box.createVerticalStrut(12));

        rankingPanel.add(createRankingRow(3, "Obsidian", "추천 73"));

        JLabel link = new JLabel("전체 추천 보기 →", SwingConstants.RIGHT);

        link.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        link.setForeground(PRIMARY);

        card.add(title, BorderLayout.NORTH);
        card.add(rankingPanel, BorderLayout.CENTER);
        card.add(link, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createRankingRow(
            int rank,
            String program,
            String recommendations) {

        JPanel row = new JPanel(new BorderLayout());

        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        JLabel rankLabel = new JLabel(rank + "  ");

        rankLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        rankLabel.setForeground(PRIMARY);

        JLabel programLabel = new JLabel(program);

        programLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        programLabel.setForeground(TEXT);

        JLabel countLabel = new JLabel(recommendations);

        countLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        countLabel.setForeground(SUB_TEXT);

        leftPanel.add(rankLabel);
        leftPanel.add(programLabel);

        row.add(leftPanel, BorderLayout.WEST);
        row.add(countLabel, BorderLayout.EAST);

        return row;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BACKGROUND);
        return card;
    }

    private static class ViewportWidthPanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
