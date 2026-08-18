package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgramAnalyticsPanel extends JPanel {
    private static final String SELECT = "SELECT";
    private static final String DETAIL = "DETAIL";
    private final CardLayout cardLayout = new CardLayout();
    private final JComboBox<String> categoryBox = combo("개발 도구", "브라우저", "디자인", "생산성", "커뮤니케이션", "게임");
    private final JComboBox<String> programBox = combo("VS Code", "Chrome", "Figma", "Notion", "Discord", "Steam");
    private final JLabel detailTitle = label("VS Code 분석", Font.BOLD, 24, TEXT);

    public ProgramAnalyticsPanel() {
        setLayout(cardLayout);
        add(createSelectPage(), SELECT);
        add(createDetailPage(), DETAIL);
        cardLayout.show(this, SELECT);
    }

    private JPanel createSelectPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);

        JPanel center = transparentPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(62, 140, 50, 140));

        JPanel title = titlePanel("프로그램 분석", "사용 패턴과 연령별, 성별 통계를 자세히 분석할 프로그램을 선택하세요.");
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
        card.setBorder(BorderFactory.createEmptyBorder(25, 28, 25, 28));
        card.setAlignmentX(CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(520, 360));
        card.setPreferredSize(new Dimension(520, 360));

        categoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        programBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addField(card, "카테고리 선택", categoryBox);
        card.add(Box.createVerticalStrut(18));
        addField(card, "프로그램 선택", programBox);
        card.add(Box.createVerticalStrut(20));

        JPanel info = transparentPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        info.setAlignmentX(LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        info.add(label("VS Code", Font.BOLD, 13, TEXT));
        info.add(Box.createVerticalStrut(4));
        info.add(label("카테고리: 개발 도구 · 텍스트 에디터 및 개발 생산성 애플리케이션", Font.PLAIN, 11, SUB_TEXT));
        card.add(info);
        card.add(Box.createVerticalStrut(20));

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JButton viewButton = primaryButton("분석 보기");
        viewButton.addActionListener(event -> {
            detailTitle.setText(programBox.getSelectedItem() + " 분석");
            cardLayout.show(this, DETAIL);
        });
        action.add(viewButton);
        card.add(action);
        return card;
    }

    private void addField(JPanel card, String name, JComboBox<String> comboBox) {
        JLabel label = label(name, Font.BOLD, 12, TEXT);
        label.setAlignmentX(LEFT_ALIGNMENT);
        comboBox.setAlignmentX(LEFT_ALIGNMENT);
        card.add(label);
        card.add(Box.createVerticalStrut(6));
        card.add(comboBox);
    }

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
        JLabel badge = label("개발 도구", Font.BOLD, 11, PRIMARY);
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
        summary.add(summaryCard("평균 사용 시간", "3시간 12분", "개발 업무 집중도 높음"));
        summary.add(summaryCard("전체 순위", "2위", "인기 프로그램 상위권"));
        summary.add(summaryCard("전날 대비", "+28분", "어제보다 14% 더 오래 사용"));

        JPanel analytics = transparentPanel(new GridLayout(1, 2, 16, 0));
        analytics.add(createHourlyCard());
        analytics.add(createDemographicCard());
        body.add(summary, BorderLayout.NORTH);
        body.add(analytics, BorderLayout.CENTER);
        return body;
    }

    private JPanel summaryCard(String title, String value, String description) {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        card.add(label(title, Font.PLAIN, 12, SUB_TEXT));
        card.add(Box.createVerticalStrut(6));
        card.add(label(value, Font.BOLD, 19, PRIMARY));
        card.add(Box.createVerticalStrut(2));
        card.add(label(description, Font.PLAIN, 10, SUB_TEXT));
        return card;
    }

    private JPanel createHourlyCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.add(label("시간대별 평균 사용량", Font.BOLD, 14, TEXT), BorderLayout.NORTH);
        card.add(new BarChartPanel(), BorderLayout.CENTER);
        return card;
    }

    private JPanel createDemographicCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.add(label("연령대별 평균 사용량", Font.BOLD, 14, TEXT), BorderLayout.NORTH);
        JPanel list = transparentPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        addProgress(list, "10대", "45분", 24);
        addProgress(list, "20대", "2시간 15분", 63);
        addProgress(list, "30대", "3시간 02분", 78);
        addProgress(list, "40대", "2시간 38분", 68);
        addProgress(list, "50대", "1시간 21분", 38);
        addProgress(list, "60대 이상", "52분", 27);
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private void addProgress(JPanel list, String age, String time, int percent) {
        JPanel labels = transparentPanel(new BorderLayout());
        labels.add(label(age, Font.BOLD, 11, TEXT), BorderLayout.WEST);
        labels.add(label(time, Font.PLAIN, 10, SUB_TEXT), BorderLayout.EAST);
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(percent);
        progress.setForeground(PRIMARY);
        progress.setBackground(BORDER);
        progress.setBorderPainted(false);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
        list.add(labels);
        list.add(Box.createVerticalStrut(4));
        list.add(progress);
        list.add(Box.createVerticalStrut(11));
    }

    private static class BarChartPanel extends JPanel {
        private final int[] values = {38, 70, 112, 94, 51, 82, 26};
        private final String[] labels = {"09시", "11시", "13시", "15시", "17시", "19시", "21시"};

        BarChartPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int baseline = getHeight() - 28;
            int gap = Math.max(38, getWidth() / values.length);
            int barWidth = 24;
            g2.setFont(font(Font.PLAIN, 10));

            for(int i = 0; i < values.length; i++) {
                int x = i * gap + Math.max(4, (gap - barWidth) / 2);
                g2.setColor(PRIMARY);
                g2.fillRoundRect(x, baseline - values[i], barWidth, values[i], 8, 8);
                g2.setColor(SUB_TEXT);
                g2.drawString(labels[i], x - 1, baseline + 18);
            }
            g2.dispose();
        }
    }
}
