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
import javax.swing.JTextField;

public class RecommendationPanel extends JPanel {
    private final String[][] rankingData = {
        {"4", "Notion", "생산성", "723"},
        {"5", "Figma", "디자인", "698"},
        {"6", "Slack", "커뮤니케이션", "612"},
        {"7", "Steam", "게임", "589"},
        {"8", "Spotify", "음악", "534"},
        {"9", "Postman", "개발 도구", "487"},
        {"10", "Firefox", "브라우저", "451"}//임시
    };

    public RecommendationPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        add(titlePanel("추천 프로그램", "사용자들이 많이 추천한 프로그램을 확인하고 직접 추천해보세요."), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createContent() {
        JPanel content = transparentPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(createFilterBar());
        content.add(Box.createVerticalStrut(15));
        content.add(label("추천 TOP 3", Font.BOLD, 15, TEXT));
        content.add(Box.createVerticalStrut(9));
        content.add(createTopCards());
        content.add(Box.createVerticalStrut(15));
        content.add(createRankingCard());
        return content;
    }

    private JPanel createFilterBar() {
        UiComponents.RoundedPanel bar = card();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 9, 10));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        JTextField search = textField("프로그램 검색");
        search.setPreferredSize(new Dimension(220, 36));
        JComboBox<String> category = combo("전체", "개발 도구", "브라우저", "디자인", "생산성", "게임");
        JComboBox<String> period = combo("전체 기간", "오늘", "이번 주", "이번 달");
        JComboBox<String> sort = combo("추천 많은 순", "평균 사용 시간 순", "이름 순");
        category.setPreferredSize(new Dimension(120, 36));
        period.setPreferredSize(new Dimension(120, 36));
        sort.setPreferredSize(new Dimension(145, 36));
        bar.add(search);
        bar.add(category);
        bar.add(period);
        bar.add(sort);
        return bar;
    }

    private JPanel createTopCards() {
        JPanel cards = transparentPanel(new GridLayout(1, 3, 14, 0));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        cards.setPreferredSize(new Dimension(800, 150));
        cards.add(createTopCard(1, "VS Code", "개발 도구", "추천 1,284개", "평균 2시간 30분"));
        cards.add(createTopCard(2, "Chrome", "브라우저", "추천 1,052개", "평균 1시간 40분"));
        cards.add(createTopCard(3, "Discord", "커뮤니케이션", "추천 847개", "평균 50분"));
        return cards;
    }

    private JPanel createTopCard(int rank, String name, String category, String count, String average) {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 13, 16));

        JPanel nameRow = transparentPanel(new BorderLayout());
        JLabel rankLabel = label(String.valueOf(rank), Font.BOLD, 14, CARD);
        rankLabel.setHorizontalAlignment(JLabel.CENTER);
        rankLabel.setOpaque(true);
        rankLabel.setBackground(PRIMARY);
        rankLabel.setPreferredSize(new Dimension(27, 27));

        JPanel names = transparentPanel();
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(label(name, Font.BOLD, 14, TEXT));
        names.add(label(category, Font.PLAIN, 10, SUB_TEXT));
        nameRow.add(rankLabel, BorderLayout.WEST);
        nameRow.add(names, BorderLayout.CENTER);

        JPanel info = transparentPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(label(count, Font.BOLD, 12, TEXT));
        info.add(Box.createVerticalStrut(3));
        info.add(label(average, Font.PLAIN, 10, SUB_TEXT));

        JPanel buttons = transparentPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        JButton recommend = outlineButton("추천하기");
        JButton detail = outlineButton("자세히 보기");
        recommend.setPreferredSize(new Dimension(82, 28));
        detail.setPreferredSize(new Dimension(90, 28));
        buttons.add(recommend);
        buttons.add(detail);

        card.add(nameRow, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createRankingCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(13, 16, 13, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 285));
        card.setPreferredSize(new Dimension(800, 285));
        card.add(label("전체 추천 순위", Font.BOLD, 14, TEXT), BorderLayout.NORTH);

        JPanel list = transparentPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(createHeaderRow());
        for(String[] data : rankingData) list.add(createRankingRow(data));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createHeaderRow() {
        JPanel row = transparentPanel(new GridLayout(1, 5));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.add(label("순위", Font.BOLD, 11, SUB_TEXT));
        row.add(label("프로그램", Font.BOLD, 11, SUB_TEXT));
        row.add(label("카테고리", Font.BOLD, 11, SUB_TEXT));
        row.add(label("추천 수", Font.BOLD, 11, SUB_TEXT));
        row.add(label("", Font.BOLD, 11, SUB_TEXT));
        return row;
    }

    private JPanel createRankingRow(String[] data) {
        JPanel row = transparentPanel(new GridLayout(1, 5));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        row.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TABLE_DIVIDER));
        row.add(label(data[0], Font.BOLD, 12, TEXT));
        row.add(label(data[1], Font.BOLD, 12, TEXT));
        row.add(label(data[2], Font.PLAIN, 11, SUB_TEXT));
        row.add(label(data[3], Font.PLAIN, 11, TEXT));
        JButton recommend = outlineButton("추천하기");
        recommend.setPreferredSize(new Dimension(74, 25));
        JPanel buttonCell = transparentPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        buttonCell.add(recommend);
        row.add(buttonCell);
        return row;
    }
}
