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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class ProgramBoardPanel extends JPanel {
    private static final String SELECT = "SELECT";
    private static final String BOARD = "BOARD";
    private final CardLayout cardLayout = new CardLayout();
    private final JComboBox<String> programBox = combo("VS Code", "Chrome", "Discord", "Figma", "Notion");
    private final JLabel boardTitle = label("VS Code 게시판", Font.BOLD, 24, TEXT);

    public ProgramBoardPanel() {
        setLayout(cardLayout);
        add(createSelectPage(), SELECT);
        add(createBoardPage(), BOARD);
        cardLayout.show(this, SELECT);
    }

    private JPanel createSelectPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        page.add(titlePanel("게시판", "추천이 많은 프로그램을 확인하거나 프로그램별 게시판에 참여하세요."), BorderLayout.NORTH);

        JPanel content = transparentPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        JLabel hotTitle = label("핫 프로그램 TOP 3", Font.BOLD, 15, TEXT);
        hotTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(hotTitle);
        content.add(Box.createVerticalStrut(9));
        content.add(createHotProgramCards());
        content.add(Box.createVerticalStrut(15));
        content.add(createSelectionCard());
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createHotProgramCards() {
        JPanel cards = transparentPanel(new GridLayout(1, 3, 13, 0));
        cards.setAlignmentX(LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        cards.setPreferredSize(new Dimension(700, 145));
        cards.add(createHotProgramCard(1, "VS Code", "개발 도구", "추천 1,284"));
        cards.add(createHotProgramCard(2, "Chrome", "브라우저", "추천 1,052"));
        cards.add(createHotProgramCard(3, "Discord", "커뮤니케이션", "추천 847"));
        return cards;
    }

    private JPanel createHotProgramCard(int rank, String name, String category, String count) {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(13, 15, 12, 15));

        JPanel titleRow = transparentPanel(new BorderLayout(8, 0));
        JLabel rankLabel = label(String.valueOf(rank), Font.BOLD, 13, CARD);
        rankLabel.setHorizontalAlignment(JLabel.CENTER);
        rankLabel.setOpaque(true);
        rankLabel.setBackground(PRIMARY);
        rankLabel.setPreferredSize(new Dimension(28, 28));

        JPanel names = transparentPanel();
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(label(name, Font.BOLD, 14, TEXT));
        names.add(label(category, Font.PLAIN, 10, SUB_TEXT));
        titleRow.add(rankLabel, BorderLayout.WEST);
        titleRow.add(names, BorderLayout.CENTER);

        JPanel footer = transparentPanel(new BorderLayout());
        footer.add(label(count, Font.BOLD, 11, PRIMARY), BorderLayout.WEST);
        JPanel buttons = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton recommendButton = outlineButton("추천하기");
        recommendButton.setPreferredSize(new Dimension(72, 28));
        recommendButton.addActionListener(event -> {
            recommendButton.setText("추천 완료");
            recommendButton.setEnabled(false);
        });
        JButton boardButton = primaryButton("게시판 보기");
        boardButton.setPreferredSize(new Dimension(82, 28));
        boardButton.addActionListener(event -> openBoard(name));
        buttons.add(recommendButton);
        buttons.add(boardButton);
        footer.add(buttons, BorderLayout.EAST);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(label("사용자들이 많이 추천한 프로그램", Font.PLAIN, 10, SUB_TEXT), BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createSelectionCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(15, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        card.setPreferredSize(new Dimension(700, 145));
        card.add(label("프로그램별 게시판 찾기", Font.BOLD, 14, TEXT), BorderLayout.NORTH);

        JComboBox<String> category = combo("개발 도구", "브라우저", "커뮤니케이션", "디자인", "생산성");
        JPanel fields = transparentPanel(new GridLayout(1, 2, 12, 0));
        fields.add(createField("카테고리", category));
        fields.add(createField("프로그램", programBox));
        card.add(fields, BorderLayout.CENTER);

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.CENTER));
        JButton enterButton = primaryButton("게시판 입장");
        enterButton.addActionListener(event -> openBoard(String.valueOf(programBox.getSelectedItem())));
        action.add(enterButton);
        card.add(action, BorderLayout.EAST);
        return card;
    }

    private JPanel createField(String name, JComboBox<String> comboBox) {
        JPanel field = transparentPanel();
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));
        JLabel fieldLabel = label(name, Font.BOLD, 11, TEXT);
        fieldLabel.setAlignmentX(LEFT_ALIGNMENT);
        comboBox.setAlignmentX(LEFT_ALIGNMENT);
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.add(fieldLabel);
        field.add(Box.createVerticalStrut(5));
        field.add(comboBox);
        return field;
    }

    private void openBoard(String programName) {
        programBox.setSelectedItem(programName);
        boardTitle.setText(programName + " 게시판");
        cardLayout.show(this, BOARD);
    }

    private JPanel createBoardPage() {
        JPanel page = new JPanel(new BorderLayout(0, 15));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(22, 27, 22, 27));
        page.add(createBoardHeader(), BorderLayout.NORTH);
        page.add(createPostList(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createBoardHeader() {
        JPanel header = transparentPanel(new BorderLayout());
        JPanel title = transparentPanel();
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(boardTitle);
        title.add(Box.createVerticalStrut(4));
        title.add(label("프로그램 사용 팁과 의견을 자유롭게 나눠보세요.", Font.PLAIN, 12, SUB_TEXT));

        JPanel actions = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        JTextField search = textField("게시글 검색");
        search.setPreferredSize(new Dimension(180, 36));
        JButton write = primaryButton("글쓰기");
        JButton change = outlineButton("프로그램 변경");
        change.setPreferredSize(new Dimension(105, 36));
        change.addActionListener(event -> cardLayout.show(this, SELECT));
        actions.add(search);
        actions.add(write);
        actions.add(change);
        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createPostList() {
        JPanel list = transparentPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(createPost("VS Code 확장 프로그램 추천합니다", "개발하는 분들은 Error Lens와 GitLens를 같이 사용해보세요. 오류 확인과 Git 기록을 보는 데 편리합니다.", "코딩하는강아지", "12", "5"));
        list.add(Box.createVerticalStrut(11));
        list.add(createPost("테마는 어떤 걸 사용하시나요?", "오래 작업해도 눈이 덜 피로한 다크 테마를 찾고 있습니다. 추천 부탁드려요.", "주황발바닥", "8", "11"));
        list.add(Box.createVerticalStrut(11));
        list.add(createPost("단축키 모음 공유", "자주 사용하는 단축키를 정리했습니다. Ctrl+P, Ctrl+Shift+P부터 익히면 작업 속도가 빨라져요.", "개발새싹", "21", "7"));
        list.add(Box.createVerticalStrut(11));
        list.add(createPost("설정 동기화 사용 후기", "집과 학교 PC에서 같은 설정을 사용할 수 있어서 편했습니다.", "PawUser", "6", "3"));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private JPanel createPost(String title, String content, String author, String likes, String comments) {
        UiComponents.RoundedPanel post = card();
        post.setLayout(new BorderLayout(0, 10));
        post.setBorder(BorderFactory.createEmptyBorder(15, 18, 14, 18));
        post.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        post.setPreferredSize(new Dimension(800, 125));

        JPanel titleRow = transparentPanel(new BorderLayout());
        titleRow.add(label(title, Font.BOLD, 14, TEXT), BorderLayout.WEST);
        titleRow.add(label("방금 전", Font.PLAIN, 10, SUB_TEXT), BorderLayout.EAST);
        JLabel contentLabel = label("<html>" + content + "</html>", Font.PLAIN, 12, SUB_TEXT);

        JPanel footer = transparentPanel(new BorderLayout());
        footer.add(label(author, Font.BOLD, 11, TEXT), BorderLayout.WEST);
        footer.add(label("♡ " + likes + "    댓글 " + comments, Font.PLAIN, 11, PRIMARY), BorderLayout.EAST);
        post.add(titleRow, BorderLayout.NORTH);
        post.add(contentLabel, BorderLayout.CENTER);
        post.add(footer, BorderLayout.SOUTH);
        return post;
    }
}
