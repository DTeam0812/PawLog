package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

        JPanel content = transparentPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(64, 150, 60, 150));
        JPanel title = titlePanel("프로그램 게시판", "프로그램을 선택해 사용자들의 게시글과 댓글을 확인하세요.");
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(25));
        content.add(createSelectionCard());
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createSelectionCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(26, 30, 26, 30));
        card.setAlignmentX(CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(500, 285));
        card.setPreferredSize(new Dimension(500, 285));

        JComboBox<String> category = combo("개발 도구", "브라우저", "커뮤니케이션", "디자인", "생산성");
        category.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        programBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addField(card, "카테고리 선택", category);
        card.add(Box.createVerticalStrut(18));
        addField(card, "프로그램 선택", programBox);
        card.add(Box.createVerticalStrut(25));

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.CENTER));
        JButton enterButton = primaryButton("게시판 입장");
        enterButton.addActionListener(event -> {
            boardTitle.setText(programBox.getSelectedItem() + " 게시판");
            cardLayout.show(this, BOARD);
        });
        action.add(enterButton);
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
