package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

import com.pawlog.client.network.PawLogClient;
import com.pawlog.common.dto.ranking.PopularRankingDto;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class PopularRankingPanel extends JPanel {
    private final JComboBox<String> ageBox = combo("전체 연령", "10대", "20대", "30대", "40대", "50대", "60대 이상");
    private final JComboBox<String> genderBox = combo("전체 성별", "남성", "여성");
    private final JButton searchButton = primaryButton("필터 적용");
    private final JLabel statusLabel = label("기간을 선택하고 조회해주세요.", Font.PLAIN, 12, SUB_TEXT);
    private final JPanel rankingList = transparentPanel();
    private final PawLogClient pawLogClient = PawLogClient.getInstance();
    private LocalDate lastStartDate;
    private LocalDate lastEndDate;

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
    public void loadRankings(LocalDate startDate, LocalDate endDate) {
        lastStartDate = startDate;
        lastEndDate = endDate;
        String gender = getSelectedGender();
        String ageGroup = getSelectedAgeGroup();

        statusLabel.setText("조회 중...");
        searchButton.setEnabled(false);

        new Thread(() -> {
            try {
                PopularRankingDto.Response response = pawLogClient.getPopularRankings(startDate, endDate, gender, ageGroup);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(response.getMessage());
                    if (response.isSuccess()) updateRankingList(response.getRankings());
                    searchButton.setEnabled(true);
                });
            } catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("서버 연결에 실패했습니다.");
                    searchButton.setEnabled(true);
                });
                exception.printStackTrace();
            }
        }, "popular-ranking-request").start();
    }

    private JPanel createHeader(boolean embedded) {
        JPanel header = transparentPanel(new BorderLayout());
        if (embedded) {
            header.add(label("전체 사용자 기준 TOP 10", Font.BOLD, 15, TEXT), BorderLayout.WEST);
        } else {
            header.add(titlePanel("인기 프로그램 순위", "전체 사용자 데이터를 기준으로 가장 많이 사용한 프로그램을 확인하세요."), BorderLayout.WEST);
        }

        JPanel filters = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 3));
        ageBox.setPreferredSize(new Dimension(115, 36));
        genderBox.setPreferredSize(new Dimension(105, 36));
        searchButton.setPreferredSize(new Dimension(90, 36));
        searchButton.addActionListener(event -> {
            if (lastStartDate == null || lastEndDate == null) {
                statusLabel.setText("먼저 기간을 선택하고 조회해주세요.");
                return;
            }
            loadRankings(lastStartDate, lastEndDate);
        });
        filters.add(ageBox);
        filters.add(genderBox);
        filters.add(searchButton);
        header.add(filters, BorderLayout.EAST);
        header.add(statusLabel, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createRankingCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.add(createTableHeader(), BorderLayout.NORTH);

        rankingList.setLayout(new BoxLayout(rankingList, BoxLayout.Y_AXIS));

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

    private void updateRankingList(List<PopularRankingDto.RankingItem> rankings) {
        rankingList.removeAll();
        if (rankings != null) {
            for(PopularRankingDto.RankingItem item : rankings) rankingList.add(createRankingRow(item));
        }
        rankingList.revalidate();
        rankingList.repaint();
    }

    private JPanel createRankingRow(PopularRankingDto.RankingItem item) {
        JPanel row = transparentPanel(new GridLayout(1, 5));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setPreferredSize(new Dimension(700, 48));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER));

        int rank = item.getRank();
        JLabel rankLabel = label(String.valueOf(rank), Font.BOLD, 14, rank <= 3 ? PRIMARY : SUB_TEXT);
        JLabel nameLabel = label(item.getProgramName(), Font.BOLD, 13, TEXT);
        row.add(cell(rankLabel));
        row.add(cell(nameLabel));
        row.add(cell(label(item.getCategoryName(), Font.PLAIN, 12, SUB_TEXT)));
        row.add(cell(label(item.getAvgUsageFormatted(), Font.PLAIN, 12, TEXT)));
        row.add(cell(label(item.getUsagePercentage() + "%", Font.BOLD, 12, PRIMARY)));
        return row;
    }

    private String getSelectedGender() {
        String selected = String.valueOf(genderBox.getSelectedItem());
        if ("남성".equals(selected)) return "M";
        if ("여성".equals(selected)) return "F";
        return "ALL";
    }

    private String getSelectedAgeGroup() {
        String selected = String.valueOf(ageBox.getSelectedItem());
        if ("10대".equals(selected)) return "10";
        if ("20대".equals(selected)) return "20";
        if ("30대".equals(selected)) return "30";
        if ("40대".equals(selected)) return "40";
        if ("50대".equals(selected)) return "50";
        if ("60대 이상".equals(selected)) return "60";
        return "ALL";
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
