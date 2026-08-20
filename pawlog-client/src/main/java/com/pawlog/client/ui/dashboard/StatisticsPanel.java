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
import com.pawlog.common.dto.auth.AuthLoginDto.Response;

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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
public class StatisticsPanel extends JPanel {
    private static final String PERSONAL = "PERSONAL";
    private final PawLogClient pawLogClient = PawLogClient.getInstance();
    private final JLabel warnlabel = label("",Font.PLAIN,12 ,SUB_TEXT);
    private static final String POPULAR = "POPULAR";
    private DatePicker datePicker = new DatePicker();
    private DatePicker startDatePicker = new DatePicker();
    private DatePicker endDatePicker = new DatePicker();
    private final CardLayout statisticsLayout = new CardLayout();
    private final JPanel statisticsContent = new JPanel(statisticsLayout);
    private final List<JButton> typeButtons = new ArrayList<>();
    private final List<JButton> periodButtons = new ArrayList<>();
    private final JLabel periodLabel = label("오늘 기준", Font.PLAIN, 12, SUB_TEXT);
    private final JButton joinbutton = new JButton("조회");
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
            statisticsLayout.show(statisticsContent, PERSONAL);
            selectButton(typeButtons, personal);
        });
        popular.addActionListener(event -> {
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
        LocalDate sdate = startDatePicker.getDate();
        LocalDate edate = endDatePicker.getDate();
        if (sdate == null || edate == null) {
            warnlabel.setText("시작일과 종료일을 선택해주세요.");
            return;
        }
        if (sdate.isAfter(edate)) {
            warnlabel.setText("시작일은 종료일보다 늦을 수 없습니다.");
            return;
        }
        warnlabel.setText("조회 중...");
        new Thread(() -> {
            try {
                PawLogClient.Response response = pawLogClient.DateJoin(
                    AuthStore.getInstance().getEmail(),
                    sdate.toString(),
                    edate.toString()
                );

                SwingUtilities.invokeLater(() -> {
                    warnlabel.setText(response.message());
                });
            } catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    warnlabel.setText("서버 연결에 실패했습니다.");
                });

                exception.printStackTrace();
            }
        }).start();
        
    });
    return panel;
}
    

    private void addPeriodButton(JPanel panel, String text, String description, boolean selected) {
        JButton button = createSwitchButton(text, selected);
        button.setPreferredSize(new Dimension(58, 30));
        button.addActionListener(event -> {
            periodLabel.setText(description);
            selectButton(periodButtons, button);
        });
        periodButtons.add(button);
        panel.add(button);
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
        statisticsContent.add(new PopularRankingPanel(true), POPULAR);
        statisticsLayout.show(statisticsContent, PERSONAL);
        return statisticsContent;
    }

    private JPanel createPersonalStatistics() {
        JPanel page = transparentPanel(new BorderLayout(0, 13));
        JPanel summaryArea = transparentPanel(new BorderLayout(0, 10));
        warnlabel.setHorizontalAlignment(SwingConstants.RIGHT);
        summaryArea.add(warnlabel, BorderLayout.NORTH);
        JPanel summaryCards = transparentPanel(new GridLayout(1, 3, 12, 0));
        summaryCards.add(createSummaryCard("총 사용 시간", "4시간 20분", "어제보다 18분 증가"));
        summaryCards.add(createSummaryCard("가장 오래 사용", "Chrome", "1시간 45분"));
        summaryCards.add(createSummaryCard("사용한 프로그램", "7개", "개발 도구 3개"));
        summaryArea.add(summaryCards, BorderLayout.CENTER);
        page.add(summaryArea, BorderLayout.NORTH);
        page.add(createUsageCard(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createSummaryCard(String title, String value, String description) {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 17, 14, 17));
        card.add(label(title, Font.PLAIN, 11, SUB_TEXT));
        card.add(Box.createVerticalStrut(5));
        card.add(label(value, Font.BOLD, 19, PRIMARY));
        card.add(Box.createVerticalStrut(4));
        card.add(label(description, Font.PLAIN, 10, SUB_TEXT));
        return card;
    }

    private JPanel createUsageCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(17, 20, 17, 20));
        card.add(label("프로그램별 사용 비율", Font.BOLD, 15, TEXT));
        card.add(Box.createVerticalStrut(12));
        card.add(createUsageRow("Chrome", "1시간 45분", 40, PRIMARY));
        card.add(createUsageRow("VS Code", "1시간 10분", 27, CHART_BLUE));
        card.add(createUsageRow("Slack", "35분", 13, SUCCESS));
        card.add(createUsageRow("Notion", "25분", 10, CHART_PURPLE));
        card.add(createUsageRow("Figma", "20분", 8, CHART_PINK));
        return card;
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
