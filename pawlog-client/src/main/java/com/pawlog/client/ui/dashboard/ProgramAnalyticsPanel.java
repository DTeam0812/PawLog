package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import com.pawlog.client.network.PawLogClient;
import com.pawlog.common.dto.analytics.ProgramAnalyticsDto;
import com.pawlog.common.dto.board.ProgramSearchDto;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public  class ProgramAnalyticsPanel extends JPanel {
    private static final String SELECT = "SELECT";
    private static final String DETAIL = "DETAIL";
    private final CardLayout cardLayout = new CardLayout();
    private final PawLogClient pawLogClient = PawLogClient.getInstance();

    // 1. SELECT 페이지 컴포넌트
    private final JTextField programSearchField = textField("프로그램 이름을 검색하세요");
    private final JPanel searchResultsPanel = transparentPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
    private final JLabel selectedNameLabel = label("프로그램을 검색하여 선택하세요.", Font.BOLD, 14, TEXT);
    private final JLabel selectedDescLabel = label("선택된 프로그램의 설명이 여기에 표시됩니다.", Font.PLAIN, 12, SUB_TEXT);
    private final JLabel statusWarnLabel = label("", Font.PLAIN, 11, RED);

    // 2. DETAIL 페이지 요약 카드 컴포넌트
    private final JLabel detailTitle = label("프로그램 분석", Font.BOLD, 24, TEXT);
    private final JLabel avgUsageValueLabel = label("0초", Font.BOLD, 19, PRIMARY);
    private final JLabel totalRankValueLabel = label("순위 없음", Font.BOLD, 19, PRIMARY);
    private final JLabel dayOverDayDiffLabel = label("0초", Font.BOLD, 19, PRIMARY);
    private final JLabel dayOverDaySubTextLabel = label("최근 2일간 사용 기록 없음", Font.PLAIN, 10, SUB_TEXT);
    private final JLabel dayOverDayCardTitleLabel = label("전날 대비", Font.PLAIN, 12, SUB_TEXT);

    // 3. 차트 및 통계 컴포넌트
    private final BarChartPanel barChartPanel = new BarChartPanel();
    private final JPanel demographicListPanel = transparentPanel();

    private long selectedProgramId = -1;
    private String selectedProgramName = "";

    public ProgramAnalyticsPanel() {
        configureProgramSearch();
        setLayout(cardLayout);
        add(createSelectPage(), SELECT);
        add(createDetailPage(), DETAIL);
        cardLayout.show(this, SELECT);
    }
    public void openByProcessName(String processName) {
    new SwingWorker<ProgramSearchDto.Response, Void>() {
        @Override
        protected ProgramSearchDto.Response doInBackground() throws Exception {
            return pawLogClient.searchPrograms(processName, 10);
        }

        @Override
        protected void done() {
            try {
                ProgramSearchDto.Response response = get();

                if(!response.isSuccess()
                    || response.getPrograms() == null
                    || response.getPrograms().isEmpty()) {

                    statusWarnLabel.setText("현재 프로그램을 찾을 수 없습니다.");
                    cardLayout.show(ProgramAnalyticsPanel.this, SELECT);
                    return;
                }

                ProgramSearchDto.ProgramItem program =
                    response.getPrograms().stream()
                        .filter(item ->
                            processName.equalsIgnoreCase(item.getProcessName())
                        )
                        .findFirst()
                        .orElse(response.getPrograms().get(0));

                selectProgram(
                    program.getProgramId(),
                    program.getDisplayName(),
                    program.getProcessName()
                );

                loadAnalyticsData(program.getProgramId());
            } catch(Exception exception) {
                statusWarnLabel.setText("프로그램 분석을 불러오지 못했습니다.");
                exception.printStackTrace();
            }
        }
    }.execute();
}
    private JPanel createSelectPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);

        JPanel center = transparentPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(62, 140, 50, 140));

        JPanel title = titlePanel("프로그램 분석", "사용 패턴과 연령별 통계를 자세히 분석할 프로그램을 검색하세요.");
        title.setAlignmentX(CENTER_ALIGNMENT);
        center.add(title);
        center.add(Box.createVerticalStrut(24));
        center.add(createSelectionCard());
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel createSelectionCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        card.setAlignmentX(CENTER_ALIGNMENT);

        // 88px 추천 영역에 맞춘 전체 카드 크기 고정 (560 x 445)
        card.setMaximumSize(new Dimension(560, 445));
        card.setPreferredSize(new Dimension(560, 445));
        card.setMinimumSize(new Dimension(560, 445));

        // 1. 타이틀 영역
        JLabel searchTitle = label("프로그램 찾기", Font.BOLD, 15, TEXT);
        searchTitle.setAlignmentX(CENTER_ALIGNMENT);
        searchTitle.setMaximumSize(new Dimension(500, 20));
        searchTitle.setPreferredSize(new Dimension(500, 20));

        JLabel searchSubTitle = label("분석할 프로그램 이름을 검색하고 추천 목록에서 선택하세요.", Font.PLAIN, 11, SUB_TEXT);
        searchSubTitle.setAlignmentX(CENTER_ALIGNMENT);
        searchSubTitle.setMaximumSize(new Dimension(500, 16));
        searchSubTitle.setPreferredSize(new Dimension(500, 16));

        card.add(searchTitle);
        card.add(Box.createVerticalStrut(3));
        card.add(searchSubTitle);
        card.add(Box.createVerticalStrut(12));

        // 2. 검색창
        programSearchField.setAlignmentX(CENTER_ALIGNMENT);
        programSearchField.setMaximumSize(new Dimension(500, 38));
        programSearchField.setPreferredSize(new Dimension(500, 38));
        card.add(programSearchField);
        card.add(Box.createVerticalStrut(8));

        // 3. 추천 검색 결과 패널 (요청하신 높이 88px 고정)
        searchResultsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 3));
        searchResultsPanel.setAlignmentX(CENTER_ALIGNMENT);
        searchResultsPanel.setMaximumSize(new Dimension(500, 88));
        searchResultsPanel.setPreferredSize(new Dimension(500, 88));
        searchResultsPanel.setMinimumSize(new Dimension(500, 88));
        card.add(searchResultsPanel);
        card.add(Box.createVerticalStrut(10));

        // 4. 선택 프로그램 정보 박스
        JPanel info = transparentPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        info.setAlignmentX(CENTER_ALIGNMENT);
        info.setMaximumSize(new Dimension(500, 68));
        info.setPreferredSize(new Dimension(500, 68));
        info.setMinimumSize(new Dimension(500, 68));

        selectedNameLabel.setAlignmentX(LEFT_ALIGNMENT);
        selectedDescLabel.setAlignmentX(LEFT_ALIGNMENT);
        info.add(selectedNameLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(selectedDescLabel);
        card.add(info);

        // 5. 경고 문구 및 분석 버튼
        card.add(Box.createVerticalStrut(6));
        statusWarnLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(statusWarnLabel);
        card.add(Box.createVerticalStrut(6));

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        action.setAlignmentX(CENTER_ALIGNMENT);
        action.setMaximumSize(new Dimension(500, 38));

        JButton viewButton = primaryButton("분석 보기");
        viewButton.setPreferredSize(new Dimension(140, 38));
        viewButton.addActionListener(event -> {
            if (selectedProgramId <= 0 || selectedProgramName.isBlank()) {
                statusWarnLabel.setText("분석을 진행할 프로그램을 먼저 선택해주세요.");
                return;
            }
            statusWarnLabel.setText("");
            loadAnalyticsData(selectedProgramId);
        });
        action.add(viewButton);
        card.add(action);

        updateProgramSearchResults();
        return card;
    }

    private void configureProgramSearch() {
        programSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateProgramSearchResults();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateProgramSearchResults();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateProgramSearchResults();
            }
        });

        programSearchField.addActionListener(event -> {
            String keyword = programSearchField.getText().trim();
            if (!keyword.isBlank()) {
                findAndSelectProgram(keyword);
            }
        });
    }

    private void updateProgramSearchResults() {
        searchResultsPanel.removeAll();
        String keyword = programSearchField.getText().trim();

        if (keyword.isBlank()) {
            searchResultsPanel.add(label("검색 결과가 여기에 표시됩니다.", Font.PLAIN, 11, SUB_TEXT));
        } else {
            loadProgramSearchResultsFromServer(keyword);
        }

        searchResultsPanel.revalidate();
        searchResultsPanel.repaint();
    }

    private void loadProgramSearchResultsFromServer(String keyword) {
        searchResultsPanel.removeAll();
        searchResultsPanel.add(label("프로그램 검색 중...", Font.PLAIN, 11, SUB_TEXT));
        searchResultsPanel.revalidate();
        searchResultsPanel.repaint();

        new SwingWorker<ProgramSearchDto.Response, Void>() {
            @Override
            protected ProgramSearchDto.Response doInBackground() throws Exception {
                return pawLogClient.searchPrograms(keyword, 6);
            }

            @Override
            protected void done() {
                if (!programSearchField.getText().trim().equalsIgnoreCase(keyword)) {
                    return;
                }

                searchResultsPanel.removeAll();

                try {
                    ProgramSearchDto.Response response = get();

                    if (!response.isSuccess()) {
                        searchResultsPanel.add(label(response.getMessage(), Font.PLAIN, 11, RED));
                    } else if (response.getPrograms() == null || response.getPrograms().isEmpty()) {
                        searchResultsPanel.add(label("일치하는 프로그램이 없습니다.", Font.PLAIN, 11, SUB_TEXT));
                    } else {
                        for (ProgramSearchDto.ProgramItem program : response.getPrograms()) {
                            searchResultsPanel.add(createProgramSearchResultButton(program));
                        }
                    }
                } catch (Exception exception) {
                    searchResultsPanel.add(label("서버 연결에 실패했습니다.", Font.PLAIN, 11, RED));
                    exception.printStackTrace();
                }

                searchResultsPanel.revalidate();
                searchResultsPanel.repaint();
            }
        }.execute();
    }

    private JButton createProgramSearchResultButton(ProgramSearchDto.ProgramItem program) {
        JButton button = outlineButton(program.getDisplayName());
        button.setPreferredSize(new Dimension(Math.max(76, program.getDisplayName().length() * 9 + 24), 28));
        button.addActionListener(event -> selectProgram(program.getProgramId(), program.getDisplayName(), program.getProcessName()));
        return button;
    }

    private void findAndSelectProgram(String programName) {
        searchResultsPanel.removeAll();
        searchResultsPanel.add(label("프로그램 확인 중...", Font.PLAIN, 11, SUB_TEXT));
        searchResultsPanel.revalidate();
        searchResultsPanel.repaint();

        new SwingWorker<ProgramSearchDto.Response, Void>() {
            @Override
            protected ProgramSearchDto.Response doInBackground() throws Exception {
                return pawLogClient.searchPrograms(programName, 10);
            }

            @Override
            protected void done() {
                try {
                    ProgramSearchDto.Response response = get();

                    if (!response.isSuccess() || response.getPrograms() == null || response.getPrograms().isEmpty()) {
                        searchResultsPanel.removeAll();
                        searchResultsPanel.add(label("해당 프로그램을 찾을 수 없습니다.", Font.PLAIN, 11, RED));
                        searchResultsPanel.revalidate();
                        searchResultsPanel.repaint();
                        return;
                    }

                    ProgramSearchDto.ProgramItem program = response.getPrograms().stream()
                            .filter(item -> programName.equalsIgnoreCase(item.getDisplayName())
                                    || programName.equalsIgnoreCase(item.getProcessName()))
                            .findFirst()
                            .orElse(response.getPrograms().get(0));

                    selectProgram(program.getProgramId(), program.getDisplayName(), program.getProcessName());
                } catch (Exception exception) {
                    searchResultsPanel.removeAll();
                    searchResultsPanel.add(label("서버 연결에 실패했습니다.", Font.PLAIN, 11, RED));
                    searchResultsPanel.revalidate();
                    searchResultsPanel.repaint();
                    exception.printStackTrace();
                }
            }
        }.execute();
    }

    private void selectProgram(long programId, String displayName, String processName) {
        this.selectedProgramId = programId;
        this.selectedProgramName = displayName;

        selectedNameLabel.setText(displayName);
        selectedDescLabel.setText("프로세스: " + (processName != null ? processName : displayName) + " · 분석 데이터가 준비되었습니다.");
        statusWarnLabel.setText("");
    }

    // 서버에 실제 분석 데이터를 요청하고 UI를 갱신
    public void loadAnalyticsData(long programId) {
        statusWarnLabel.setText("분석 데이터를 불러오는 중...");

        new SwingWorker<ProgramAnalyticsDto.Response, Void>() {
            @Override
            protected ProgramAnalyticsDto.Response doInBackground() throws Exception {
                return pawLogClient.getProgramAnalytics(programId);
            }

            @Override
            protected void done() {
                try {
                    ProgramAnalyticsDto.Response response = get();

                    if (!response.isSuccess() || response.getData() == null) {
                        statusWarnLabel.setText(response.getMessage() != null ? response.getMessage() : "데이터 조회 실패");
                        return;
                    }
                    statusWarnLabel.setText("");
                    renderAnalyticsDetail(response.getData());
                    cardLayout.show(ProgramAnalyticsPanel.this, DETAIL);
                } catch (Exception e) {
                    statusWarnLabel.setText("서버 연결 중 오류가 발생했습니다.");
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void renderAnalyticsDetail(ProgramAnalyticsDto.AnalyticsData data) {
        detailTitle.setText(data.getProgramName() + " 분석");
        avgUsageValueLabel.setText(data.getAvgDailyUsage());
        totalRankValueLabel.setText(data.getTotalRank());
        dayOverDayDiffLabel.setText(data.getDayOverDayDiff());
        dayOverDaySubTextLabel.setText(data.getDayOverDaySubText());
        dayOverDayCardTitleLabel.setText(data.getDayOverDayLabel());

        // 4시간 단위 6구간 차트 데이터 세팅
        barChartPanel.setValues(data.getHourlyUsage());

        // 8개 연령대 프로그레스 바 목록 세팅
        demographicListPanel.removeAll();
        List<ProgramAnalyticsDto.AgeGroupItem> stats = data.getAgeGroupStats();
        if (stats != null) {
            for (ProgramAnalyticsDto.AgeGroupItem item : stats) {
                addProgress(demographicListPanel, item.getAgeGroupName(), item.getTimeFormatted(), item.getPercentage());
            }
        }
        demographicListPanel.revalidate();
        demographicListPanel.repaint();
    }

    // --- DETAIL 페이지 UI 구성 ---
    private JPanel createDetailPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        page.add(createDetailHeader(), BorderLayout.NORTH);
        page.add(createDetailBody(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createDetailHeader() {
        JPanel header = transparentPanel(new BorderLayout());
        JPanel title = transparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        title.add(detailTitle);
        JLabel badge = label("최근 7일 분석", Font.BOLD, 11, PRIMARY);
        badge.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PRIMARY), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        title.add(badge);

        JButton backButton = outlineButton("다른 프로그램 선택");
        backButton.setPreferredSize(new Dimension(150, 36));
        backButton.addActionListener(event -> cardLayout.show(this, SELECT));
        header.add(title, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createDetailBody() {
        JPanel body = transparentPanel(new BorderLayout(0, 16));
        JPanel summary = transparentPanel(new GridLayout(1, 3, 14, 0));
        summary.setPreferredSize(new Dimension(0, 100));

        summary.add(createSummaryCard(label("일평균 사용 시간", Font.PLAIN, 12, SUB_TEXT), avgUsageValueLabel, label("전체 사용자 기준 일평균", Font.PLAIN, 10, SUB_TEXT)));
        summary.add(createSummaryCard(label("전체 순위", Font.PLAIN, 12, SUB_TEXT), totalRankValueLabel, label("평균 사용 시간 기준", Font.PLAIN, 10, SUB_TEXT)));
        summary.add(createSummaryCard(dayOverDayCardTitleLabel, dayOverDayDiffLabel, dayOverDaySubTextLabel));

        JPanel analytics = transparentPanel(new GridLayout(1, 2, 16, 0));
        analytics.add(createHourlyCard());
        analytics.add(createDemographicCard());
        body.add(summary, BorderLayout.NORTH);
        body.add(analytics, BorderLayout.CENTER);
        return body;
    }

    private JPanel createSummaryCard(JLabel titleLabel, JLabel valueLabel, JLabel descLabel) {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(descLabel);
        return card;
    }

    private JPanel createHourlyCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.add(label("시간대별 평균 사용량", Font.BOLD, 14, TEXT), BorderLayout.NORTH);
        card.add(barChartPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDemographicCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.add(label("연령대별 평균 사용량", Font.BOLD, 14, TEXT), BorderLayout.NORTH);

        demographicListPanel.setLayout(new BoxLayout(demographicListPanel, BoxLayout.Y_AXIS));
        card.add(demographicListPanel, BorderLayout.CENTER);
        return card;
    }

    private void addProgress(JPanel list, String age, String time, int percent) {
        JPanel labels = transparentPanel(new BorderLayout());
        labels.add(label(age, Font.BOLD, 11, TEXT), BorderLayout.WEST);
        labels.add(label(time + "  (" + percent + "%)", Font.PLAIN, 10, SUB_TEXT), BorderLayout.EAST);
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(percent);
        progress.setForeground(PRIMARY);
        progress.setBackground(BORDER);
        progress.setBorderPainted(false);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
        list.add(labels);
        list.add(Box.createVerticalStrut(2));
        list.add(progress);
        list.add(Box.createVerticalStrut(6));
    }

    // 4시간 단위 6구간 X축 렌더링 막대그래프
    private static class BarChartPanel extends JPanel {
        private int[] values = {0, 0, 0, 0, 0, 0};
        private final String[] labels = {"03시", "07시", "11시", "15시", "19시", "23시"};

        BarChartPanel() {
            setOpaque(false);
        }

        public void setValues(int[] newValues) {
            if (newValues != null && newValues.length == 6) {
                this.values = newValues;
            } else {
                this.values = new int[]{0, 0, 0, 0, 0, 0};
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int baseline = getHeight() - 28;
            int availableHeight = Math.max(10, baseline - 20);
            int gap = Math.max(38, getWidth() / values.length);
            int barWidth = 24;

            // 최대값 기준 비율 스케일링
            int maxVal = 1;
            for (int v : values) {
                if (v > maxVal) maxVal = v;
            }

            g2.setFont(font(Font.PLAIN, 10));

            for (int i = 0; i < values.length; i++) {
                int x = i * gap + Math.max(4, (gap - barWidth) / 2);
                int barH = (int) (((double) values[i] / maxVal) * availableHeight);
                barH = Math.max(values[i] > 0 ? 4 : 0, barH);

                g2.setColor(PRIMARY);
                g2.fillRoundRect(x, baseline - barH, barWidth, barH, 8, 8);
                g2.setColor(SUB_TEXT);
                g2.drawString(labels[i], x - 1, baseline + 18);
            }
            g2.dispose();
        }
    }
}