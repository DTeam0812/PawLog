package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.Storage.UsageLocalSaveService;
import com.pawlog.client.Storage.UsageServerSyncService;
import com.pawlog.client.network.PawLogClient;
import com.pawlog.client.tracker.ProgramTracker;
import com.pawlog.common.dto.board.PostRecommendationDto;
import com.pawlog.common.dto.usage.MyInfoDto;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

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
    private final Consumer<String> openProgramAnalytics;
    private final AuthStore authStore = AuthStore.getInstance();
    private final UsageLocalSaveService localSaveService = new UsageLocalSaveService();
    private final UsageServerSyncService serverSyncService = new UsageServerSyncService();
    private  JPanel recommendationRankingPanel ;
    private JLabel mostProgramLabel;
    private JLabel activeProgramNameLabel;
    private JLabel activeWindowTitleLabel;
    private JLabel activeSessionTimeLabel;
    private JLabel totalSecondLabel;
    private JLabel TotalAllowPr;
    private JLabel greetingLabel;
    private JPanel recentProgramsListPanel;
    private Timer recommendationRefreshTimer;
    private boolean recommendationLoading;
    private long serverTotalUsageSeconds;
    private String serverMostUsedProgram = "기록 없음";
    private int serverProgramCount;
    private final Map<String, Long> serverUsageMap = new HashMap<>();
    private boolean todayInfoLoading;
    private boolean todayInfoLoaded;

    public HomePanel(Consumer<String> openProgramAnalytics) {
        this.openProgramAnalytics = openProgramAnalytics;
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
        styleScrollPane(scrollPane);

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

        recommendationRefreshTimer = new Timer(60_000, event -> loadRecommendTop5());
        recommendationRefreshTimer.setInitialDelay(60_000);
        recommendationRefreshTimer.start();
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
        dateLabel.setForeground(SOFT_TEXT);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(6, 20, 0, 0));

        leftPanel.add(greetingLabel);
        leftPanel.add(dateLabel);

        JPanel trackingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        trackingPanel.setOpaque(false);

        JLabel trackingDot = new JLabel("●");
        trackingDot.setForeground(SUCCESS);

        JLabel trackingLabel = new JLabel("추적 중");
        JLabel lastSavedLabel = new JLabel("저장 전");
        lastSavedLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        lastSavedLabel.setForeground(SOFT_TEXT);

        JButton saveButton = new JButton("기록 저장");
        saveButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        saveButton.setForeground(PRIMARY);
        saveButton.setBackground(CARD);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createLineBorder(PRIMARY));
        saveButton.setPreferredSize(new Dimension(88, 30));
        saveButton.setMargin(new Insets(0, 8, 0, 8));
        saveButton.addActionListener(event -> {
            saveButton.setEnabled(false);
            saveButton.setText("저장 중...");
            lastSavedLabel.setText("기록 저장 중");

            new SwingWorker<SaveResult, Void>() {
                @Override
                protected SaveResult doInBackground() throws Exception {
                    boolean locallySaved = localSaveService.savePending();

                    try {
                        int syncedCount = serverSyncService.syncPending();
                        return new SaveResult(locallySaved, syncedCount, false);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        return new SaveResult(locallySaved, 0, true);
                    }
                }

                @Override
                protected void done() {
                    try {
                        SaveResult result = get();

                        if (result.serverFailed()) {
                            lastSavedLabel.setText("서버 전송 대기");
                        } else if (result.syncedCount() > 0) {
                            String savedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                            lastSavedLabel.setText("마지막 저장 " + savedTime);
                        } else if (result.locallySaved()) {
                            lastSavedLabel.setText("서버 전송 대기");
                        } else {
                            lastSavedLabel.setText("저장할 기록 없음");
                        }

                        if(!result.serverFailed()) {
                            loadRecommendTop5();
                        }
                    } catch (Exception exception) {
                        lastSavedLabel.setText("로컬 저장 실패");
                        exception.printStackTrace();
                    } finally {
                        saveButton.setEnabled(true);
                        saveButton.setText("기록 저장");
                    }
                }
            }.execute();
        });
        trackingLabel.setForeground(SUCCESS);
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
        String id = authStore.getId();

        if (id == null || id.isBlank()) {
            greetingLabel.setText("안녕하세요, 사용자님");
            return;
        }

        greetingLabel.setText("안녕하세요, " + id + "님");
    }

    public void loadTodayInfo() {
        String id = authStore.getId();

        if(id == null || id.isBlank()) {
            resetTodayInfo();
            return;
        }

        if(todayInfoLoading || todayInfoLoaded) {
            return;
        }

        todayInfoLoading = true;

        new SwingWorker<MyInfoDto.Response, Void>() {
            @Override
            protected MyInfoDto.Response doInBackground() throws Exception {
                return PawLogClient.getInstance().getTodayInfo(id, LocalDate.now());
            }

            @Override
            protected void done() {
                try {
                    MyInfoDto.Response response = get();

                    if(response.isSuccess()) {
                        setTodayInfo(response.getData());
                        todayInfoLoaded = true;
                    } else {
                        setTodayInfo(null);
                        System.out.println("오늘 기록 조회 실패: " + response.getMessage());
                    }
                } catch(Exception exception) {
                    setTodayInfo(null);
                    System.out.println("오늘 기록을 불러오지 못했습니다.");
                    exception.printStackTrace();
                } finally {
                    todayInfoLoading = false;
                }
            }
        }.execute();
    }

    public void resetTodayInfo() {
        todayInfoLoaded = false;
        todayInfoLoading = false;
        setTodayInfo(null);
    }

    public void setTodayInfo(MyInfoDto.TodayInfo todayInfo) {
        serverTotalUsageSeconds = 0;
        serverMostUsedProgram = "기록 없음";
        serverProgramCount = 0;
        serverUsageMap.clear();

        if(todayInfo != null) {
            serverTotalUsageSeconds = todayInfo.getTotalUsageSeconds();
            serverMostUsedProgram = todayInfo.getMostUsedProgram();
            serverProgramCount = todayInfo.getProgramCount();

            if(todayInfo.getPrograms() != null) {
                for(MyInfoDto.ProgramStatistics program : todayInfo.getPrograms()) {
                    if(program.getProgramName() == null || program.getProgramName().isBlank()) {
                        continue;
                    }

                    serverUsageMap.merge(
                        program.getProgramName(),
                        program.getUsageSeconds(),
                        Long::sum
                    );
                }
            }
        }

        updateTotalProgram();
        updateMostProgram();
        updateTotlaAllowPr();
        updateRecentPrograms();
    }

    private Map<String, Long> getCombinedUsageMap() {
        Map<String, Long> combinedUsageMap = new HashMap<>(serverUsageMap);

        for(Map.Entry<String, Long> entry : ProgramTracker.getUsageMap().entrySet()) {
            combinedUsageMap.merge(entry.getKey(), entry.getValue(), Long::sum);
        }

        return combinedUsageMap;
    }
    private JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(font(Font.BOLD, 12));
        button.setForeground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(110, 24));
        return button;
    }
    private JPanel createActiveProgramPanel() {
        JPanel card = createCardPanel();

        card.setLayout(new BorderLayout(15, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.setPreferredSize(new Dimension(900, 160));
        card.setBorder(BorderFactory.createEmptyBorder(19, 21, 19, 21));

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
        activeWindowTitleLabel.setForeground(SOFT_TEXT);

        programTextPanel.add(activeProgramNameLabel);
        programTextPanel.add(Box.createVerticalStrut(3));
        programTextPanel.add(activeWindowTitleLabel);

      
        programPanel.add(programTextPanel, BorderLayout.CENTER);

        JPanel sessionPanel = new JPanel();
        sessionPanel.setOpaque(false);
        sessionPanel.setLayout(new BoxLayout(sessionPanel, BoxLayout.Y_AXIS));

        JLabel sessionTitle = new JLabel("현재 세션");

        sessionTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        sessionTitle.setForeground(SOFT_TEXT);
        sessionTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);

        activeSessionTimeLabel = new JLabel("0초");

        activeSessionTimeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        activeSessionTimeLabel.setForeground(PRIMARY);
        activeSessionTimeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JButton analysisLinkbutton = linkButton("프로그램 분석보기");
        analysisLinkbutton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        analysisLinkbutton.setForeground(PRIMARY);
        analysisLinkbutton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        analysisLinkbutton.setHorizontalAlignment(SwingConstants.RIGHT);
        analysisLinkbutton.setPreferredSize(new Dimension(145, 28));
        analysisLinkbutton.setMinimumSize(new Dimension(145, 28));
        analysisLinkbutton.setMaximumSize(new Dimension(145, 28));
        analysisLinkbutton.addActionListener(event -> {
           //TEMP1
           String processName = ProgramTracker.getCurrentProcessName();
           if(processName == null || processName.isBlank()) {
        return;
            }

            openProgramAnalytics.accept(processName);
            
        });
        sessionPanel.add(sessionTitle);
        sessionPanel.add(Box.createVerticalStrut(4));
        sessionPanel.add(activeSessionTimeLabel);
        sessionPanel.add(Box.createVerticalStrut(12));
        sessionPanel.add(analysisLinkbutton);

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

    List<Map.Entry<String, Long>> topPrograms = getCombinedUsageMap().entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .toList();
    int count = Math.min(5, topPrograms.size());
    long totalSeconds = serverTotalUsageSeconds + ProgramTracker.getTotalSeconds();

    for(int i = 0; i < count; i++) {
        Map.Entry<String, Long> entry = topPrograms.get(i);

        String processName = entry.getKey();
        String displayName = ProgramTracker.formatProcessNameForDisplay(processName);
        long seconds = entry.getValue();
        String usageTime = formatSessionTime(seconds);

        int percent = 0;

        if(totalSeconds > 0) {
            percent = (int)(seconds * 100 / totalSeconds);
        }

        recentProgramsListPanel.add(createProgramUsageRow(displayName, "", usageTime, percent));
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
        long totalSeconds = serverTotalUsageSeconds + ProgramTracker.getTotalSeconds();
        totalSecondLabel.setText(formatSessionTime(totalSeconds));
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
    List<Map.Entry<String, Long>> topPrograms = getCombinedUsageMap().entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .toList();

    if(topPrograms.isEmpty()) {
        mostProgramLabel.setText(serverMostUsedProgram == null ? "기록 없음" : serverMostUsedProgram);
        return;
    }

    mostProgramLabel.setText(ProgramTracker.formatProcessNameForDisplay(topPrograms.get(0).getKey()));
    }
    private void updateTotlaAllowPr(){
    int programCount = getCombinedUsageMap().size();

    if(programCount == 0) {
        programCount = serverProgramCount;
    }

    TotalAllowPr.setText(programCount + "개");
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
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        titleLabel.setForeground(SOFT_TEXT);
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
    card.setBorder(BorderFactory.createEmptyBorder(19, 21, 19, 21));

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
        lastUsedLabel.setForeground(MUTED_TEXT);

        namePanel.add(nameLabel);
        namePanel.add(lastUsedLabel);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        timePanel.setOpaque(false);

        JLabel timeLabel = new JLabel(usageTime);

        timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        timeLabel.setForeground(SOFT_TEXT);

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
        progressBar.setBackground(BORDER);
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
    private void loadRecommendTop5() {
    if(recommendationLoading) {
        return;
    }

    recommendationLoading = true;

    new SwingWorker<PostRecommendationDto.Response, Void>() {
        @Override
        protected PostRecommendationDto.Response doInBackground() throws Exception {
            return PawLogClient.getInstance().getRecommendTop(  5 );
        }

        @Override
        protected void done() {
            try {
                PostRecommendationDto.Response response = get();

                recommendationRankingPanel.removeAll();

                if(!response.isSuccess()) {
                    recommendationRankingPanel.add(
                        new JLabel(response.getMessage())
                    );
                    return;
                }
                
                List<PostRecommendationDto.RecommendProgram> programs =
                    response.getData();

                if(programs == null || programs.isEmpty()) {
                    recommendationRankingPanel.add(
                        new JLabel("추천 프로그램이 없습니다.")
                    );
                    return;
                }

               
                int count = Math.min(5, programs.size());
                for(int i = 0; i < count ; i++) {
                    PostRecommendationDto.RecommendProgram program =
                        programs.get(i);

                    recommendationRankingPanel.add(createRankingRow(
                        i + 1,
                        program.getProgram(),
                        "최근 이용자수 " + program.getUsercount()
                    ));

                    if(i < count - 1) {
                        recommendationRankingPanel.add(
                            Box.createVerticalStrut(16)
                        );
                    }
                }
            } catch(Exception exception) {
                recommendationRankingPanel.removeAll();
                recommendationRankingPanel.add(
                    new JLabel("추천 프로그램 조회에 실패했습니다.")
                );

                exception.printStackTrace();
            } finally {
                recommendationLoading = false;
                recommendationRankingPanel.revalidate();
                recommendationRankingPanel.repaint();
            }
        }
    }.execute();
}
    private JPanel createRecommendationPanel() {
        JPanel card = createCardPanel();

        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(19, 19, 19, 19));

        JLabel title = new JLabel("실시간 인기 프로그램");
        title.add(Box.createVerticalStrut(10));
        title.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        title.setForeground(TEXT);

        recommendationRankingPanel = new JPanel();
        recommendationRankingPanel.setOpaque(false);
        recommendationRankingPanel.setLayout(
            new BoxLayout(recommendationRankingPanel, BoxLayout.Y_AXIS)
        );

        recommendationRankingPanel.add(new JLabel("추천 프로그램을 불러오는 중..."));

        loadRecommendTop5();
        
        
        
        
       

        card.add(title, BorderLayout.NORTH);
        card.add(recommendationRankingPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRankingRow(
            int rank,
            String program,
            String recommendations) {

        JPanel row = new JPanel(new BorderLayout());

        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 28));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

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
        countLabel.setForeground(SOFT_TEXT);

        leftPanel.add(rankLabel);
        leftPanel.add(programLabel);

        row.add(leftPanel, BorderLayout.WEST);
        row.add(countLabel, BorderLayout.EAST);

        return row;
    }

    private JPanel createCardPanel() {
        return card();
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

    private record SaveResult(boolean locallySaved, int syncedCount, boolean serverFailed) {
    }
}
