package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;

public class PredictionPanel extends JPanel {
    private static final String VOTE = "VOTE";
    private static final String RESULT = "RESULT";
    private final CardLayout cardLayout = new CardLayout();

    public PredictionPanel() {
        setLayout(cardLayout);
        add(createVotePage(), VOTE);
        add(createResultPage(), RESULT);
        cardLayout.show(this, VOTE);
    }

    private JPanel createVotePage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);

        JPanel content = transparentPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(45, 125, 40, 125));
        JPanel title = titlePanel("예측 이벤트", "다음 주 가장 많이 사용될 프로그램을 예측해보세요.");
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(22));
        content.add(createVoteCard());
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createVoteCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 24, 30));
        card.setAlignmentX(CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(650, 430));
        card.setPreferredSize(new Dimension(650, 430));

        JLabel badge = label("진행 중 · D-3", Font.BOLD, 11, PRIMARY);
        badge.setAlignmentX(LEFT_ALIGNMENT);
        JLabel question = label("다음 주 개발 도구 사용률 1위는?", Font.BOLD, 20, TEXT);
        question.setAlignmentX(LEFT_ALIGNMENT);
        JLabel description = label("한 가지 프로그램을 선택하세요. 투표 후에는 현재 결과를 확인할 수 있습니다.", Font.PLAIN, 12, SUB_TEXT);
        description.setAlignmentX(LEFT_ALIGNMENT);
        card.add(badge);
        card.add(Box.createVerticalStrut(10));
        card.add(question);
        card.add(Box.createVerticalStrut(5));
        card.add(description);
        card.add(Box.createVerticalStrut(22));

        ButtonGroup group = new ButtonGroup();
        JPanel options = transparentPanel(new GridLayout(2, 2, 12, 12));
        String[] names = {"VS Code", "IntelliJ IDEA", "Eclipse", "Visual Studio"};
        for(int i = 0; i < names.length; i++) {
            JRadioButton option = new JRadioButton(names[i]);
            option.setFont(font(Font.BOLD, 13));
            option.setForeground(TEXT);
            option.setBackground(Color.WHITE);
            option.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            option.setBorderPainted(true);
            if(i == 0) option.setSelected(true);
            group.add(option);
            options.add(option);
        }
        options.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.add(options);
        card.add(Box.createVerticalStrut(24));

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.CENTER));
        JButton voteButton = primaryButton("투표하기");
        voteButton.setPreferredSize(new Dimension(160, 42));
        voteButton.addActionListener(event -> cardLayout.show(this, RESULT));
        action.add(voteButton);
        card.add(action);
        return card;
    }

    private JPanel createResultPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(23, 28, 23, 28));
        page.add(createResultHeader(), BorderLayout.NORTH);
        page.add(createResults(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createResultHeader() {
        JPanel header = transparentPanel(new BorderLayout());
        header.add(titlePanel("예측 이벤트 결과", "현재 진행 중인 투표와 지난 이벤트 결과를 확인하세요."), BorderLayout.WEST);
        JButton changeVote = outlineButton("선택 화면 보기");
        changeVote.setPreferredSize(new Dimension(120, 36));
        changeVote.addActionListener(event -> cardLayout.show(this, VOTE));
        header.add(changeVote, BorderLayout.EAST);
        return header;
    }

    private JPanel createResults() {
        JPanel results = transparentPanel(new GridLayout(1, 2, 16, 0));
        results.add(createCurrentResult());
        results.add(createPreviousResults());
        return results;
    }

    private JPanel createCurrentResult() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JPanel title = transparentPanel();
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(label("현재 투표 현황", Font.BOLD, 16, TEXT));
        title.add(Box.createVerticalStrut(4));
        title.add(label("총 1,248명 참여 · D-3", Font.PLAIN, 11, SUB_TEXT));

        JPanel list = transparentPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        addResult(list, "VS Code", 42, "524표");
        addResult(list, "IntelliJ IDEA", 31, "387표");
        addResult(list, "Eclipse", 17, "212표");
        addResult(list, "Visual Studio", 10, "125표");
        card.add(title, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPreviousResults() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 13));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.add(label("지난 투표 결과", Font.BOLD, 16, TEXT), BorderLayout.NORTH);

        JPanel list = transparentPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(previousEvent("가장 오래 사용하는 브라우저", "Chrome", "56%", "2026.08.09 종료"));
        list.add(Box.createVerticalStrut(13));
        list.add(previousEvent("협업 도구 사용률 1위", "Discord", "41%", "2026.08.02 종료"));
        list.add(Box.createVerticalStrut(13));
        list.add(previousEvent("생산성 앱 인기 1위", "Notion", "48%", "2026.07.26 종료"));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private void addResult(JPanel list, String name, int percent, String votes) {
        JPanel labels = transparentPanel(new BorderLayout());
        labels.add(label(name, Font.BOLD, 12, TEXT), BorderLayout.WEST);
        labels.add(label(votes + "  " + percent + "%", Font.PLAIN, 11, SUB_TEXT), BorderLayout.EAST);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(percent);
        bar.setForeground(PRIMARY);
        bar.setBackground(BORDER);
        bar.setBorderPainted(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9));
        list.add(labels);
        list.add(Box.createVerticalStrut(5));
        list.add(bar);
        list.add(Box.createVerticalStrut(17));
    }

    private JPanel previousEvent(String question, String winner, String rate, String date) {
        JPanel event = transparentPanel(new BorderLayout());
        event.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JPanel text = transparentPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(label(question, Font.BOLD, 12, TEXT));
        text.add(Box.createVerticalStrut(4));
        text.add(label(date, Font.PLAIN, 10, SUB_TEXT));
        event.add(text, BorderLayout.CENTER);
        event.add(label(winner + "  " + rate, Font.BOLD, 13, PRIMARY), BorderLayout.EAST);
        return event;
    }
}
