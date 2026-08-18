package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class PopularRankingPanel extends JPanel {
    private final JPanel rankingList = transparentPanel();
    private final String[][] programs = {
        {"VS Code", "개발 도구", "3시간 12분", "18.4%"},
        {"Chrome", "브라우저", "2시간 48분", "16.1%"},
        {"Discord", "커뮤니케이션", "2시간 05분", "12.0%"},
        {"Notion", "생산성", "1시간 42분", "9.8%"},
        {"Figma", "디자인", "1시간 31분", "8.7%"},
        {"Slack", "커뮤니케이션", "1시간 12분", "6.9%"},
        {"Steam", "게임", "58분", "5.6%"},
        {"Spotify", "음악", "47분", "4.5%"},
        {"Postman", "개발 도구", "41분", "3.9%"},
        {"Firefox", "브라우저", "35분", "3.4%"}
    };

    public PopularRankingPanel() {
        this(false);
    }

    public PopularRankingPanel(boolean embedded) {
        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(embedded ? BorderFactory.createEmptyBorder() : BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(createHeader(embedded), BorderLayout.NORTH);
        add(createRankingCard(), BorderLayout.CENTER);
    }

    private JPanel createHeader(boolean embedded) {
        JPanel header = transparentPanel(new BorderLayout());
        if (embedded) {
            header.add(label("전체 사용자 기준 TOP 10", Font.BOLD, 15, TEXT), BorderLayout.WEST);
        } else {
            header.add(titlePanel("인기 프로그램 순위", "전체 사용자 데이터를 기준으로 가장 많이 사용한 프로그램을 확인하세요."), BorderLayout.WEST);
        }

        JPanel filters = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 3));
        JComboBox<String> ageBox = combo("전체 연령", "10대", "20대", "30대", "40대", "50대", "60대 이상");
        JComboBox<String> genderBox = combo("전체 성별", "남성", "여성");
        ageBox.setPreferredSize(new Dimension(115, 36));
        genderBox.setPreferredSize(new Dimension(105, 36));
        JButton searchButton = primaryButton("필터 적용");
        searchButton.setPreferredSize(new Dimension(90, 36));
        filters.add(ageBox);
        filters.add(genderBox);
        filters.add(searchButton);
        header.add(filters, BorderLayout.EAST);
        return header;
    }

    private JPanel createRankingCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.add(createTableHeader(), BorderLayout.NORTH);

        rankingList.setLayout(new BoxLayout(rankingList, BoxLayout.Y_AXIS));
        for(int i = 0; i < programs.length; i++) rankingList.add(createRankingRow(i + 1, programs[i]));

        JScrollPane scroll = new JScrollPane(rankingList);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTableHeader() {
        JPanel row = transparentPanel(new GridLayout(1, 5));
        row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), BorderFactory.createEmptyBorder(0, 10, 10, 10)));
        row.add(headerLabel("순위"));
        row.add(headerLabel("프로그램"));
        row.add(headerLabel("카테고리"));
        row.add(headerLabel("평균 사용 시간"));
        row.add(headerLabel("사용 비율"));
        return row;
    }

    private JPanel createRankingRow(int rank, String[] data) {
        JPanel row = transparentPanel(new GridLayout(1, 5));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setPreferredSize(new Dimension(700, 48));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER));

        JLabel rankLabel = label(String.valueOf(rank), Font.BOLD, 14, rank <= 3 ? PRIMARY : SUB_TEXT);
        JLabel nameLabel = label(data[0], Font.BOLD, 13, TEXT);
        row.add(cell(rankLabel));
        row.add(cell(nameLabel));
        row.add(cell(label(data[1], Font.PLAIN, 12, SUB_TEXT)));
        row.add(cell(label(data[2], Font.PLAIN, 12, TEXT)));
        row.add(cell(label(data[3], Font.BOLD, 12, PRIMARY)));
        return row;
    }

    private JPanel cell(JLabel label) {
        JPanel panel = transparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        panel.add(label);
        return panel;
    }

    private JLabel headerLabel(String text) {
        return label(text, Font.BOLD, 12, SUB_TEXT);
    }
}
