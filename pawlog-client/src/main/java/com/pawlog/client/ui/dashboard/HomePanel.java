package com.pawlog.client.ui.dashboard;

import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.network.PawLogClient;
import com.pawlog.client.network.PawLogClient.Response;
import com.pawlog.client.tracker.ProgramTracker;
import com.pawlog.client.tracker.UsageKey;

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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

public class HomePanel extends JPanel {
    private static final Color BACKGROUND = new Color(247, 244, 239);
    private JLabel mostProgramLabel;
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private final PawLogClient pawLogClient = new PawLogClient();
    private final AuthStore authStore = AuthStore.getInstance();
    private static final Color PRIMARY = new Color(255, 159, 67);
    private static final Color TEXT = new Color(45, 52, 54);
    private static final Color SUB_TEXT = new Color(120, 130, 132);
    
    private static final Color PROGRESS_BACKGROUND = new Color(225, 225, 225);
    private JLabel activeProgramNameLabel;
    private JLabel activeWindowTitleLabel;
    private JLabel activeSessionTimeLabel;
    private JLabel totalSecondLabel;
    private JLabel TotalAllowPr;
    private JLabel greetingLabel;
    private JPanel recentProgramsListPanel;
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

        Timer uiTimer = new Timer(1000, event -> {
            updateActiveProgram();
            updateTotalProgram();
            updateMostProgram();
            updateTotlaAllowPr();
            updateRecentPrograms();
        });
        uiTimer.setInitialDelay(0);
        uiTimer.start();
    }

    private JPanel createGreetingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        
        greetingLabel = new JLabel("안녕하세요, 사용자님");
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
        JLabel lastSavedLabel = new JLabel("저장 전");
        lastSavedLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        lastSavedLabel.setForeground(SUB_TEXT);

        JButton saveButton = new JButton("기록 저장");
        saveButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        saveButton.setForeground(PRIMARY);
        saveButton.setBackground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createLineBorder(PRIMARY));
        saveButton.setPreferredSize(new Dimension(88, 30));
        saveButton.setMargin(new Insets(0, 8, 0, 8));
        saveButton.addActionListener(event -> {
            Map<UsageKey, Long> data =
                ProgramTracker.getHourlyUsageMap();

            if (data.isEmpty()) {
                lastSavedLabel.setText("저장할 기록 없음");
                return;
            }

            saveButton.setEnabled(false);
            saveButton.setText("저장 중...");
            lastSavedLabel.setText("서버 연결 중");

            new SwingWorker<Response, Void>() {
                @Override
                protected Response doInBackground() throws Exception {
                    return pawLogClient.saveUsage(data);
                }

                @Override
                protected void done() {
                    try {
                        Response response = get();

                        if (response.success()) {
                            String savedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                            lastSavedLabel.setText("마지막 저장 " + savedTime);
                        } else {
                            lastSavedLabel.setText("저장 실패");
                        }
                    } catch (Exception exception) {
                        lastSavedLabel.setText("서버 응답 없음");
                        exception.printStackTrace();
                    } finally {
                        saveButton.setEnabled(true);
                        saveButton.setText("기록 저장");
                    }
                }
            }.execute();
        });
        trackingLabel.setForeground(new Color(0, 184, 148));
        trackingLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        trackingPanel.add(trackingDot);
        trackingPanel.add(trackingLabel);
        trackingPanel.add(Box.createHorizontalStrut(5));
        trackingPanel.add(lastSavedLabel);
        trackingPanel.add(saveButton);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(trackingPanel, BorderLayout.EAST);
        return panel;
    }

    public void refreshUserInfo() {
        String email = authStore.getEmail();

        if (email == null || email.isBlank()) {
            greetingLabel.setText("안녕하세요, 사용자님");
            return;
        }

        greetingLabel.setText("안녕하세요, " + email + "님");
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

        


        JPanel programTextPanel = new JPanel();
        programTextPanel.setOpaque(false);
        programTextPanel.setLayout(new BoxLayout(programTextPanel, BoxLayout.Y_AXIS));

        activeProgramNameLabel = new JLabel("확인 중...");

        activeProgramNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 19));
        activeProgramNameLabel.setForeground(TEXT);

        activeWindowTitleLabel = new JLabel("활성 창을 확인하고 있습니다.");

        activeWindowTitleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        activeWindowTitleLabel.setForeground(SUB_TEXT);

        programTextPanel.add(activeProgramNameLabel);
        programTextPanel.add(Box.createVerticalStrut(3));
        programTextPanel.add(activeWindowTitleLabel);

      
        programPanel.add(programTextPanel, BorderLayout.CENTER);

        JPanel sessionPanel = new JPanel();
        sessionPanel.setOpaque(false);
        sessionPanel.setLayout(new BoxLayout(sessionPanel, BoxLayout.Y_AXIS));

        JLabel sessionTitle = new JLabel("현재 세션");

        sessionTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        sessionTitle.setForeground(SUB_TEXT);
        sessionTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);

        activeSessionTimeLabel = new JLabel("0초");

        activeSessionTimeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        activeSessionTimeLabel.setForeground(PRIMARY);
        activeSessionTimeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel analysisLink = new JLabel("프로그램 분석 보기 →");

        analysisLink.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        analysisLink.setForeground(PRIMARY);
        analysisLink.setAlignmentX(Component.RIGHT_ALIGNMENT);

        sessionPanel.add(sessionTitle);
        sessionPanel.add(Box.createVerticalStrut(4));
        sessionPanel.add(activeSessionTimeLabel);
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
    private void updateRecentPrograms() {
    recentProgramsListPanel.removeAll();

    List<Map.Entry<String, Long>> topPrograms = ProgramTracker.getTopPrograms();
    int count = Math.min(5, topPrograms.size());
    long totalSeconds = ProgramTracker.getTotalSeconds();

    for(int i = 0; i < count; i++) {
        Map.Entry<String, Long> entry = topPrograms.get(i);

        String programName = entry.getKey();
        long seconds = entry.getValue();
        String usageTime = formatSessionTime(seconds);

        int percent = 0;

        if(totalSeconds > 0) {
            percent = (int)(seconds * 100 / totalSeconds);
        }

        recentProgramsListPanel.add(createProgramUsageRow(programName, "", usageTime, percent));
    }

    if(topPrograms.isEmpty()) {
        recentProgramsListPanel.add(new JLabel("아직 사용 기록이 없습니다."));
    }

    recentProgramsListPanel.revalidate();
    recentProgramsListPanel.repaint();
}
    private void updateActiveProgram() {
        activeProgramNameLabel.setText(ProgramTracker.getCurrentProgram());
        activeWindowTitleLabel.setText(ProgramTracker.getCurrentWindowTitle());
        activeSessionTimeLabel.setText(formatSessionTime(ProgramTracker.getUsageSeconds()));
    }
    private void updateTotalProgram() {
        totalSecondLabel.setText(formatSessionTime(ProgramTracker.getTotalSeconds()));
    }
    private String formatSessionTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return hours + "시간 " + minutes + "분 " + seconds + "초";
        }
        if (minutes > 0) {
            return minutes + "분 " + seconds + "초";
        }
        return seconds + "초";
    }
    private void updateMostProgram() {
    mostProgramLabel.setText(ProgramTracker.getMostProgram());
    }
    private void updateTotlaAllowPr(){
    TotalAllowPr.setText(String.valueOf(ProgramTracker.amount()));
    }
    private JPanel createSummaryRowPanel() {
        JPanel rowPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        long amount = ProgramTracker.amount();

        String amount2 = String.valueOf(amount);
        totalSecondLabel = new JLabel("0초");
        TotalAllowPr = new JLabel("0개");
        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        rowPanel.setPreferredSize(new Dimension(900, 85));
        
        rowPanel.add(createSummaryCard("오늘 활성 사용 시간", totalSecondLabel, true));

        
        mostProgramLabel = new JLabel("기록 없음");
        rowPanel.add(createSummaryCard("가장 오래 활성화한 프로그램", mostProgramLabel, false));
        rowPanel.add(createSummaryCard("오늘 활성화한 프로그램 수", TotalAllowPr, false));

        return rowPanel;
    }

    

    private JPanel createSummaryCard(
            String title,
            JLabel valueLabel,
            boolean primaryValue) {

        JPanel card = createCardPanel();

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)), BorderFactory.createEmptyBorder(15, 17, 15, 17)));

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        titleLabel.setForeground(SUB_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

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

    card.setLayout(new BorderLayout());
    card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(235, 235, 235)), BorderFactory.createEmptyBorder(18, 20, 18, 20)));

    JLabel title = new JLabel("최근 활성 프로그램");
    title.setFont(new Font("맑은 고딕", Font.BOLD, 15));
    title.setForeground(TEXT);

    recentProgramsListPanel = new JPanel();
    recentProgramsListPanel.setOpaque(false);
    recentProgramsListPanel.setLayout(new BoxLayout(recentProgramsListPanel, BoxLayout.Y_AXIS));

    card.add(title, BorderLayout.NORTH);
    card.add(recentProgramsListPanel, BorderLayout.CENTER);

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
