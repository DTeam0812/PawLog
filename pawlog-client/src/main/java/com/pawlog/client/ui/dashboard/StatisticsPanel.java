package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import com.github.lgooddatepicker.components.DatePicker;
import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.network.PawLogClient;
import com.pawlog.common.dto.statistics.MyStatisticsDto;

import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
public class StatisticsPanel extends JPanel {
    private static final String PERSONAL = "PERSONAL";
    private final PopularRankingPanel popularPanel = new PopularRankingPanel(true);
    private String selectedType = PERSONAL;
    private final JLabel warnlabel = label("",Font.PLAIN,12 ,SUB_TEXT);
    private static final String POPULAR = "POPULAR";
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final CardLayout statisticsLayout = new CardLayout();
    private final JPanel statisticsContent = new JPanel(statisticsLayout);
    private final List<JButton> typeButtons = new ArrayList<>();
    private final JButton joinbutton = new JButton("조회");
    private final PawLogClient pawLogClient = PawLogClient.getInstance();
    private final AuthStore authStore = AuthStore.getInstance();
    private final JLabel totalTimeValueLabel = label("0초", Font.BOLD, 19, PRIMARY);
    private final JLabel mostUsedValueLabel = label("기록 없음", Font.BOLD, 19, PRIMARY);
    private final JLabel programCountValueLabel = label("0개", Font.BOLD, 19, PRIMARY);
    private final JPanel usageRowsPanel = transparentPanel();
    
    public StatisticsPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(createHeader(), BorderLayout.NORTH);
        add(createStatisticsContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = transparentPanel(new BorderLayout(15, 0));
        header.add(titlePanel("통계", ""), BorderLayout.WEST);

        JPanel controls = transparentPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(createTypeButtons());
        controls.add(Box.createVerticalStrut(8));
        controls.add(createPeriodButtons());
        controls.add(Box.createVerticalStrut(5));
        warnlabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        controls.add(warnlabel);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel createTypeButtons() {
        JPanel panel = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton personal = createSwitchButton("내 사용 통계", true);
        JButton popular = createSwitchButton("인기 순위", false);
        typeButtons.add(personal);
        typeButtons.add(popular);

        personal.addActionListener(event -> {
            selectedType = PERSONAL;
            statisticsLayout.show(statisticsContent, PERSONAL);
            selectButton(typeButtons, personal);
        });
        popular.addActionListener(event -> {
            selectedType = POPULAR;
            statisticsLayout.show(statisticsContent, POPULAR);
            selectButton(typeButtons, popular);
        });

        panel.add(personal);
        panel.add(popular);
        return panel;
    }

    private JPanel createPeriodButtons() {
        JPanel panel = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
       
        panel.add(startDatePicker);
        panel.add(endDatePicker);
        panel.add(joinbutton);
        joinbutton.addActionListener(event -> {
            LocalDate startDate = startDatePicker.getDate();
            LocalDate endDate = endDatePicker.getDate();

            if (startDate == null || endDate == null) {
                warnlabel.setText("시작일과 종료일을 선택해주세요.");
                return;
            }

            if (startDate.isAfter(endDate)) {
                warnlabel.setText("시작일은 종료일보다 늦을 수 없습니다.");
                return;
            }

            if (POPULAR.equals(selectedType)) {
                warnlabel.setText("");
                popularPanel.loadRankings(startDate, endDate);
                return;
            }
            if(PERSONAL.equals(selectedType)){
                loadPersonalStatistics(startDate, endDate);
                return;
            }
        });
        return panel;
    }

    private JButton createSwitchButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(font(Font.BOLD, 12));
        button.setPreferredSize(new Dimension(105, 31));
        button.setFocusPainted(false);
        applySelectedStyle(button, selected);
        return button;
    }

    private void selectButton(List<JButton> buttons, JButton selectedButton) {
        for(JButton button : buttons) applySelectedStyle(button, button == selectedButton);
    }

    private void applySelectedStyle(JButton button, boolean selected) {
        button.setForeground(selected ? CARD : TEXT);
        button.setBackground(selected ? PRIMARY : CARD);
        button.setBorder(BorderFactory.createLineBorder(selected ? PRIMARY : BORDER));
    }

    private JPanel createStatisticsContent() {
        statisticsContent.setOpaque(false);
        statisticsContent.add(createPersonalStatistics(), PERSONAL);
        statisticsContent.add(popularPanel, POPULAR);
        statisticsLayout.show(statisticsContent, PERSONAL);
        return statisticsContent;
    }

    private JPanel createPersonalStatistics() {
        JPanel page = transparentPanel(new BorderLayout(0, 13));
        JPanel summaryArea = transparentPanel(new BorderLayout(0, 10));
        JPanel summaryCards = transparentPanel(new GridLayout(1, 3, 12, 0));
        summaryCards.add(createSummaryCard("총 사용 시간", totalTimeValueLabel));
        summaryCards.add(createSummaryCard("가장 오래 사용", mostUsedValueLabel));
        summaryCards.add(createSummaryCard("사용한 프로그램", programCountValueLabel));
        summaryArea.add(summaryCards, BorderLayout.CENTER);
        page.add(summaryArea, BorderLayout.NORTH);
        page.add(createUsageCard(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 17, 14, 17));
        card.add(label(title, Font.PLAIN, 11, SUB_TEXT));
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        return card;
    }

    private JPanel createUsageCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(17, 20, 17, 20));
        usageRowsPanel.setLayout(new BoxLayout(usageRowsPanel, BoxLayout.Y_AXIS));
        usageRowsPanel.add(label("조회할 기간을 선택해주세요.", Font.PLAIN, 11, SUB_TEXT));

        JScrollPane scrollPane = new JScrollPane(usageRowsPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        card.add(label("프로그램별 사용 비율", Font.BOLD, 15, TEXT), BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void loadPersonalStatistics(LocalDate startDate, LocalDate endDate) {
        if (authStore.getUserId() <= 0) {
            warnlabel.setText("로그인 사용자 정보를 확인할 수 없습니다.");
            return;
        }

        joinbutton.setEnabled(false);
        warnlabel.setText("내 사용 통계 조회 중...");

        new SwingWorker<MyStatisticsDto.Response, Void>() {
            @Override
            protected MyStatisticsDto.Response doInBackground() throws Exception {
                return pawLogClient.getMyStatistics(
                    String.valueOf(authStore.getUserId()),
                    startDate,
                    endDate
                );
            }

            @Override
            protected void done() {
                try {
                    MyStatisticsDto.Response response = get();

                    if (!response.isSuccess()) {
                        warnlabel.setText(response.getMessage());
                        return;
                    }

                    if (response.getData() == null) {
                        warnlabel.setText("통계 데이터가 없습니다.");
                        return;
                    }

                    updatePersonalStatistics(response.getData());
                    warnlabel.setText("");
                } catch (Exception exception) {
                    warnlabel.setText("서버 연결에 실패했습니다.");
                    exception.printStackTrace();
                } finally {
                    joinbutton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void updatePersonalStatistics(MyStatisticsDto.DataPayload data) {
        totalTimeValueLabel.setText(formatSeconds(data.getTotalTime()));
        mostUsedValueLabel.setText(
            data.getMostUsedItem() == null || data.getMostUsedItem().isBlank()
                ? "기록 없음"
                : data.getMostUsedItem()
        );
        programCountValueLabel.setText(data.getAmountUsedItem() + "개");

        usageRowsPanel.removeAll();

        if (data.getRatio() == null || data.getRatio().isEmpty()) {
            usageRowsPanel.add(label("해당 기간의 사용 기록이 없습니다.", Font.PLAIN, 11, SUB_TEXT));
        } else {
            Color[] colors = {PRIMARY, CHART_BLUE, SUCCESS, CHART_PURPLE, CHART_PINK};

            for(int i = 0; i < data.getRatio().size(); i++) {
                MyStatisticsDto.ProgramRatioItem item = data.getRatio().get(i);
                usageRowsPanel.add(createUsageRow(
                    item.getProgramName(),
                    formatSeconds(item.getProgramTime()),
                    item.getProgramRatio(),
                    colors[i % colors.length]
                ));
            }
        }

        usageRowsPanel.revalidate();
        usageRowsPanel.repaint();
    }

    private String formatSeconds(String secondsText) {
        try {
            long totalSeconds = Long.parseLong(secondsText);
            long hours = totalSeconds / 3600;
            long minutes = totalSeconds % 3600 / 60;
            long seconds = totalSeconds % 60;

            if (hours > 0) return hours + "시간 " + minutes + "분";
            if (minutes > 0) return minutes + "분 " + seconds + "초";
            return seconds + "초";
        } catch (Exception exception) {
            return secondsText == null || secondsText.isBlank() ? "0초" : secondsText;
        }
    }

    public void resetUserData() {
        startDatePicker.setDate(null);
        endDatePicker.setDate(null);
        totalTimeValueLabel.setText("0초");
        mostUsedValueLabel.setText("기록 없음");
        programCountValueLabel.setText("0개");
        warnlabel.setText("");
        joinbutton.setEnabled(true);

        usageRowsPanel.removeAll();
        usageRowsPanel.add(label("조회할 기간을 선택해주세요.", Font.PLAIN, 11, SUB_TEXT));
        usageRowsPanel.revalidate();
        usageRowsPanel.repaint();
    }

    private JPanel createUsageRow(String program, String time, int percent, Color color) {
        JPanel row = transparentPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 51));

        JPanel labels = transparentPanel(new BorderLayout());
        labels.add(label(program, Font.BOLD, 12, TEXT), BorderLayout.WEST);
        labels.add(label(time + "   " + percent + "%", Font.PLAIN, 11, SUB_TEXT), BorderLayout.EAST);

        JPanel track = new JPanel(new BorderLayout());
        track.setBackground(SOFT_BORDER);
        track.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        track.setPreferredSize(new Dimension(600, 6));
        JPanel progress = new JPanel();
        progress.setBackground(color);
        progress.setPreferredSize(new Dimension(percent * 5, 6));
        track.add(progress, BorderLayout.WEST);

        row.add(labels);
        row.add(Box.createVerticalStrut(6));
        row.add(track);
        row.add(Box.createVerticalStrut(9));
        return row;
    }
}
