package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class ProgramBoardPanel extends JPanel {
    private static final String SELECT = "SELECT";
    private static final String BOARD = "BOARD";
    private static final String WRITE = "WRITE";
    private static final String DETAIL = "DETAIL";
    private static final int PAGE_SIZE = 9;
    private static final int PAGE_BUTTON_COUNT = 5;
    private static final int MAX_CONTENT_LENGTH = 500;
    private final CardLayout cardLayout = new CardLayout();
    private final List<PostItem> posts = new ArrayList<>();
    private final JComboBox<String> programBox = combo("VS Code", "Chrome", "Discord", "Figma", "Notion");
    private final JLabel boardTitle = label("VS Code 게시판", Font.BOLD, 24, TEXT);
    private final JPanel postRows = transparentPanel();
    private final JPanel paginationPanel = transparentPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
    private final JTextField postTitleField = textField("제목을 입력해주세요");
    private final JComboBox<String> postCategoryBox = combo("질문", "정보", "팁", "잡담");
    private final JTextArea postContentArea = new JTextArea();
    private final JLabel contentCountLabel = label("0 / 500", Font.PLAIN, 11, SUB_TEXT);
    private final JLabel detailCategoryLabel = new JLabel();
    private final JLabel detailTitleLabel = label("게시글 제목", Font.BOLD, 22, TEXT);
    private final JLabel detailMetaLabel = label("작성자 · 작성 시간", Font.PLAIN, 11, SUB_TEXT);
    private final JTextArea detailContentArea = new JTextArea();
    private final JPanel commentRows = new ViewportWidthPanel();
    private final JTextField commentField = textField("댓글을 입력해주세요");
    private final JButton detailLikeButton = outlineButton("♡ 0");
    private int currentPage;
    private int detailLikeCount;

    public ProgramBoardPanel() {
        createSamplePosts();
        configureContentLimit();
        setLayout(cardLayout);
        add(createSelectPage(), SELECT);
        add(createBoardPage(), BOARD);
        add(createWritePage(), WRITE);
        add(createDetailPage(), DETAIL);
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
        recommendButton.setPreferredSize(new Dimension(80, 28));
        recommendButton.addActionListener(event -> {
            recommendButton.setText("추천 완료");
            recommendButton.setEnabled(false);
        });
        JButton boardButton = primaryButton("게시판 보기");
        boardButton.setPreferredSize(new Dimension(100, 28));
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
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        page.add(createBoardHeader(), BorderLayout.NORTH);
        page.add(createPostTable(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createBoardHeader() {
        JPanel header = transparentPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JButton backButton = linkButton("← 돌아가기");
        backButton.addActionListener(event -> cardLayout.show(this, SELECT));
        header.add(backButton);
        header.add(Box.createVerticalStrut(10));

        JPanel titleRow = transparentPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.add(boardTitle);
        titleRow.add(createTagLabel("개발 도구", new Color(238, 242, 245), SUB_TEXT));
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        header.add(titleRow);
        header.add(Box.createVerticalStrut(15));

        JPanel toolbar = transparentPanel(new BorderLayout(10, 0));
        JTextField searchField = textField("게시판 내에서 검색");
        searchField.setPreferredSize(new Dimension(230, 36));
        JPanel rightActions = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JComboBox<String> sortBox = combo("최신순", "추천순", "댓글순");
        sortBox.setPreferredSize(new Dimension(90, 36));
        JButton writeButton = primaryButton("✎ 글쓰기");
        writeButton.setPreferredSize(new Dimension(95, 36));
        writeButton.addActionListener(event -> cardLayout.show(this, WRITE));
        rightActions.add(sortBox);
        rightActions.add(writeButton);
        toolbar.add(searchField, BorderLayout.WEST);
        toolbar.add(rightActions, BorderLayout.EAST);
        toolbar.setAlignmentX(LEFT_ALIGNMENT);
        header.add(toolbar);
        return header;
    }

    private JPanel createPostTable() {
        UiComponents.RoundedPanel table = card();
        table.setLayout(new BorderLayout());
        table.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        postRows.setLayout(new BoxLayout(postRows, BoxLayout.Y_AXIS));
        table.add(postRows, BorderLayout.CENTER);
        table.add(paginationPanel, BorderLayout.SOUTH);
        renderPostPage();
        return table;
    }
    
    private JPanel createPostRow(PostItem post) {
        JPanel row = transparentPanel(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setPreferredSize(new Dimension(0, 50));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel titleArea = transparentPanel(new BorderLayout(8, 0));
        titleArea.setBorder(BorderFactory.createEmptyBorder(11, 8, 11, 0));
        Color tagColor = "질문".equals(post.category()) ? new Color(255, 239, 222) : new Color(238, 242, 245);
        titleArea.add(createTagLabel(post.category(), tagColor, "질문".equals(post.category()) ? PRIMARY : SUB_TEXT), BorderLayout.WEST);
        titleArea.add(label(post.title(), Font.PLAIN, 13, TEXT), BorderLayout.CENTER);

        JPanel infoArea = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 12, 13));
        JLabel authorLabel = label(post.author(), Font.PLAIN, 11, SUB_TEXT);
        authorLabel.setPreferredSize(new Dimension(75, 20));
        JLabel timeLabel = label(post.time(), Font.PLAIN, 11, MUTED_TEXT);
        timeLabel.setPreferredSize(new Dimension(60, 20));
        infoArea.add(authorLabel);
        infoArea.add(timeLabel);
        infoArea.add(label("♡ " + post.likes(), Font.PLAIN, 11, PRIMARY));
        infoArea.add(label("○ " + post.comments(), Font.PLAIN, 11, SUB_TEXT));

        row.add(titleArea, BorderLayout.CENTER);
        row.add(infoArea, BorderLayout.EAST);
        makeClickable(row, () -> openPostDetail(post));
        return row;
    }

    private void renderPostPage() {
        postRows.removeAll();

        int pageCount = Math.max(1, (posts.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (currentPage >= pageCount) currentPage = pageCount - 1;

        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, posts.size());

        for (int index = startIndex; index < endIndex; index++) {
            postRows.add(createPostRow(posts.get(index)));
        }

        if (posts.isEmpty()) {
            postRows.add(label("등록된 게시글이 없습니다.", Font.PLAIN, 13, SUB_TEXT));
        }

        renderPagination(pageCount);
        postRows.revalidate();
        postRows.repaint();
    }

    private void renderPagination(int pageCount) {
        paginationPanel.removeAll();

        if (pageCount <= 1) {
            paginationPanel.revalidate();
            paginationPanel.repaint();
            return;
        }

        addPaginationButton("≪ 처음", false, currentPage == 0, () -> {
            currentPage = 0;
            renderPostPage();
        });

        int half = PAGE_BUTTON_COUNT / 2;
        int startPage = Math.max(0, currentPage - half);
        int endPage = Math.min(pageCount - 1, startPage + PAGE_BUTTON_COUNT - 1);

        if (endPage - startPage + 1 < PAGE_BUTTON_COUNT) {
            startPage = Math.max(0, endPage - PAGE_BUTTON_COUNT + 1);
        }

        if (startPage > 0) addPaginationText("…");

        for (int page = startPage; page <= endPage; page++) {
            int pageIndex = page;
            boolean selected = page == currentPage;
            addPaginationButton(String.valueOf(page + 1), selected, false, () -> {
                currentPage = pageIndex;
                renderPostPage();
            });
        }

        if (endPage < pageCount - 1) addPaginationText("…");

        addPaginationButton("끝 ≫", false, currentPage == pageCount - 1, () -> {
            currentPage = pageCount - 1;
            renderPostPage();
        });

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private void addPaginationButton(String text, boolean selected, boolean disabled, Runnable action) {
        addPaginationSeparator();

        JButton button = new JButton(text);
        button.setFont(font(Font.BOLD, 12));
        button.setForeground(selected ? CARD : SUB_TEXT);
        button.setBackground(selected ? PRIMARY : CARD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(selected ? PRIMARY : BORDER));
        button.setPreferredSize(new Dimension(text.length() > 2 ? 68 : 32, 28));
        button.setEnabled(!disabled);
        button.addActionListener(event -> action.run());
        paginationPanel.add(button);
    }

    private void addPaginationText(String text) {
        addPaginationSeparator();
        paginationPanel.add(label(text, Font.BOLD, 12, MUTED_TEXT));
    }

    private void addPaginationSeparator() {
        if (paginationPanel.getComponentCount() > 0) {
            paginationPanel.add(label("|", Font.PLAIN, 12, MUTED_TEXT));
        }
    }

    private void createSamplePosts() {
        posts.add(new PostItem("질문", "VS Code에서 테마 추천 및 폰트 세팅 어떻게 하시나요?", "오래 작업해도 눈이 편한 테마와 개발용 폰트를 찾고 있습니다. 현재 사용 중인 조합을 추천해주세요.", "초보코더", "10분 전", 12, 4));
        posts.add(new PostItem("정보", "2026년 필수 생산성 확장 프로그램 5선 정리", "Error Lens, GitLens, Prettier 등 개발할 때 유용한 확장 프로그램을 정리했습니다.", "코딩마스터", "2시간 전", 45, 12));
        posts.add(new PostItem("잡담", "이번 업데이트로 단축키 살짝 바뀐 것 같네요", "업데이트 이후 일부 단축키 동작이 달라진 것 같습니다. 같은 현상을 겪는 분이 있나요?", "김철수", "어제", 8, 3));
        posts.add(new PostItem("정보", "C++ 개발 환경 세팅 디테일한 튜토리얼 공유", "컴파일러 설치부터 tasks.json과 launch.json 설정까지 순서대로 정리했습니다.", "테크리드", "2일 전", 32, 8));
        posts.add(new PostItem("질문", "Git 연동할 때 자꾸 충돌 에러가 나는데 조언 부탁드립니다.", "원격 저장소에서 pull할 때 충돌이 반복됩니다. 안전하게 해결하는 방법을 알고 싶습니다.", "리액트린이", "3일 전", 15, 19));
        posts.add(new PostItem("팁", "자주 사용하는 단축키 모음", "Ctrl+P와 Ctrl+Shift+P부터 익히면 작업 속도를 높일 수 있습니다.", "단축키장인", "4일 전", 21, 7));
        posts.add(new PostItem("정보", "설정 동기화 사용 후기", "집과 학교 PC에서 같은 설정을 사용할 수 있어서 편리했습니다.", "PawUser", "5일 전", 6, 3));
        posts.add(new PostItem("질문", "Java 확장팩은 어떤 것을 사용하시나요?", "Java 프로젝트에 필요한 확장팩 추천을 받고 싶습니다.", "자바초보", "6일 전", 9, 5));
        posts.add(new PostItem("팁", "터미널 기본 프로필 변경 방법", "VS Code 설정에서 기본 터미널을 PowerShell로 변경하는 방법입니다.", "윈도우개발자", "7일 전", 17, 6));
        posts.add(new PostItem("정보", "새 버전 업데이트 내용 정리", "새 버전에서 변경된 편집기 기능과 단축키를 정리했습니다.", "업데이트봇", "8일 전", 24, 9));
        posts.add(new PostItem("잡담", "여러분은 어떤 색상 테마를 쓰시나요?", "현재 다크 테마를 사용 중인데 다른 테마도 궁금합니다.", "테마수집가", "9일 전", 11, 8));
        posts.add(new PostItem("질문", "자동 저장 주기는 얼마가 적당할까요?", "파일 자동 저장 주기를 어떻게 설정하는지 궁금합니다.", "개발새싹", "10일 전", 7, 2));
         posts.add(new PostItem("질문", "VS Code에서 테마 추천 및 폰트 세팅 어떻게 하시나요?", "오래 작업해도 눈이 편한 테마와 개발용 폰트를 찾고 있습니다. 현재 사용 중인 조합을 추천해주세요.", "초보코더", "10분 전", 12, 4));
        posts.add(new PostItem("정보", "2026년 필수 생산성 확장 프로그램 5선 정리", "Error Lens, GitLens, Prettier 등 개발할 때 유용한 확장 프로그램을 정리했습니다.", "코딩마스터", "2시간 전", 45, 12));
        posts.add(new PostItem("잡담", "이번 업데이트로 단축키 살짝 바뀐 것 같네요", "업데이트 이후 일부 단축키 동작이 달라진 것 같습니다. 같은 현상을 겪는 분이 있나요?", "김철수", "어제", 8, 3));
        posts.add(new PostItem("정보", "C++ 개발 환경 세팅 디테일한 튜토리얼 공유", "컴파일러 설치부터 tasks.json과 launch.json 설정까지 순서대로 정리했습니다.", "테크리드", "2일 전", 32, 8));
        posts.add(new PostItem("질문", "Git 연동할 때 자꾸 충돌 에러가 나는데 조언 부탁드립니다.", "원격 저장소에서 pull할 때 충돌이 반복됩니다. 안전하게 해결하는 방법을 알고 싶습니다.", "리액트린이", "3일 전", 15, 19));
        posts.add(new PostItem("팁", "자주 사용하는 단축키 모음", "Ctrl+P와 Ctrl+Shift+P부터 익히면 작업 속도를 높일 수 있습니다.", "단축키장인", "4일 전", 21, 7));
        posts.add(new PostItem("정보", "설정 동기화 사용 후기", "집과 학교 PC에서 같은 설정을 사용할 수 있어서 편리했습니다.", "PawUser", "5일 전", 6, 3));
        posts.add(new PostItem("질문", "Java 확장팩은 어떤 것을 사용하시나요?", "Java 프로젝트에 필요한 확장팩 추천을 받고 싶습니다.", "자바초보", "6일 전", 9, 5));
        posts.add(new PostItem("팁", "터미널 기본 프로필 변경 방법", "VS Code 설정에서 기본 터미널을 PowerShell로 변경하는 방법입니다.", "윈도우개발자", "7일 전", 17, 6));
        posts.add(new PostItem("정보", "새 버전 업데이트 내용 정리", "새 버전에서 변경된 편집기 기능과 단축키를 정리했습니다.", "업데이트봇", "8일 전", 24, 9));
        posts.add(new PostItem("잡담", "여러분은 어떤 색상 테마를 쓰시나요?", "현재 다크 테마를 사용 중인데 다른 테마도 궁금합니다.", "테마수집가", "9일 전", 11, 8));
         posts.add(new PostItem("질문", "VS Code에서 테마 추천 및 폰트 세팅 어떻게 하시나요?", "오래 작업해도 눈이 편한 테마와 개발용 폰트를 찾고 있습니다. 현재 사용 중인 조합을 추천해주세요.", "초보코더", "10분 전", 12, 4));
        posts.add(new PostItem("정보", "2026년 필수 생산성 확장 프로그램 5선 정리", "Error Lens, GitLens, Prettier 등 개발할 때 유용한 확장 프로그램을 정리했습니다.", "코딩마스터", "2시간 전", 45, 12));
        posts.add(new PostItem("잡담", "이번 업데이트로 단축키 살짝 바뀐 것 같네요", "업데이트 이후 일부 단축키 동작이 달라진 것 같습니다. 같은 현상을 겪는 분이 있나요?", "김철수", "어제", 8, 3));
        posts.add(new PostItem("정보", "C++ 개발 환경 세팅 디테일한 튜토리얼 공유", "컴파일러 설치부터 tasks.json과 launch.json 설정까지 순서대로 정리했습니다.", "테크리드", "2일 전", 32, 8));
        posts.add(new PostItem("질문", "Git 연동할 때 자꾸 충돌 에러가 나는데 조언 부탁드립니다.", "원격 저장소에서 pull할 때 충돌이 반복됩니다. 안전하게 해결하는 방법을 알고 싶습니다.", "리액트린이", "3일 전", 15, 19));
        posts.add(new PostItem("팁", "자주 사용하는 단축키 모음", "Ctrl+P와 Ctrl+Shift+P부터 익히면 작업 속도를 높일 수 있습니다.", "단축키장인", "4일 전", 21, 7));
        posts.add(new PostItem("정보", "설정 동기화 사용 후기", "집과 학교 PC에서 같은 설정을 사용할 수 있어서 편리했습니다.", "PawUser", "5일 전", 6, 3));
        posts.add(new PostItem("질문", "Java 확장팩은 어떤 것을 사용하시나요?", "Java 프로젝트에 필요한 확장팩 추천을 받고 싶습니다.", "자바초보", "6일 전", 9, 5));
        posts.add(new PostItem("팁", "터미널 기본 프로필 변경 방법", "VS Code 설정에서 기본 터미널을 PowerShell로 변경하는 방법입니다.", "윈도우개발자", "7일 전", 17, 6));
        posts.add(new PostItem("정보", "새 버전 업데이트 내용 정리", "새 버전에서 변경된 편집기 기능과 단축키를 정리했습니다.", "업데이트봇", "8일 전", 24, 9));
        posts.add(new PostItem("잡담", "여러분은 어떤 색상 테마를 쓰시나요?", "현재 다크 테마를 사용 중인데 다른 테마도 궁금합니다.", "테마수집가", "9일 전", 11, 8));
         posts.add(new PostItem("질문", "VS Code에서 테마 추천 및 폰트 세팅 어떻게 하시나요?", "오래 작업해도 눈이 편한 테마와 개발용 폰트를 찾고 있습니다. 현재 사용 중인 조합을 추천해주세요.", "초보코더", "10분 전", 12, 4));
        posts.add(new PostItem("정보", "2026년 필수 생산성 확장 프로그램 5선 정리", "Error Lens, GitLens, Prettier 등 개발할 때 유용한 확장 프로그램을 정리했습니다.", "코딩마스터", "2시간 전", 45, 12));
        posts.add(new PostItem("잡담", "이번 업데이트로 단축키 살짝 바뀐 것 같네요", "업데이트 이후 일부 단축키 동작이 달라진 것 같습니다. 같은 현상을 겪는 분이 있나요?", "김철수", "어제", 8, 3));
        posts.add(new PostItem("정보", "C++ 개발 환경 세팅 디테일한 튜토리얼 공유", "컴파일러 설치부터 tasks.json과 launch.json 설정까지 순서대로 정리했습니다.", "테크리드", "2일 전", 32, 8));
        posts.add(new PostItem("질문", "Git 연동할 때 자꾸 충돌 에러가 나는데 조언 부탁드립니다.", "원격 저장소에서 pull할 때 충돌이 반복됩니다. 안전하게 해결하는 방법을 알고 싶습니다.", "리액트린이", "3일 전", 15, 19));
        posts.add(new PostItem("팁", "자주 사용하는 단축키 모음", "Ctrl+P와 Ctrl+Shift+P부터 익히면 작업 속도를 높일 수 있습니다.", "단축키장인", "4일 전", 21, 7));
        posts.add(new PostItem("정보", "설정 동기화 사용 후기", "집과 학교 PC에서 같은 설정을 사용할 수 있어서 편리했습니다.", "PawUser", "5일 전", 6, 3));
        posts.add(new PostItem("질문", "Java 확장팩은 어떤 것을 사용하시나요?", "Java 프로젝트에 필요한 확장팩 추천을 받고 싶습니다.", "자바초보", "6일 전", 9, 5));
        posts.add(new PostItem("팁", "터미널 기본 프로필 변경 방법", "VS Code 설정에서 기본 터미널을 PowerShell로 변경하는 방법입니다.", "윈도우개발자", "7일 전", 17, 6));
        posts.add(new PostItem("정보", "새 버전 업데이트 내용 정리", "새 버전에서 변경된 편집기 기능과 단축키를 정리했습니다.", "업데이트봇", "8일 전", 24, 9));
        posts.add(new PostItem("잡담", "여러분은 어떤 색상 테마를 쓰시나요?", "현재 다크 테마를 사용 중인데 다른 테마도 궁금합니다.", "테마수집가", "9일 전", 11, 8)); posts.add(new PostItem("질문", "VS Code에서 테마 추천 및 폰트 세팅 어떻게 하시나요?", "오래 작업해도 눈이 편한 테마와 개발용 폰트를 찾고 있습니다. 현재 사용 중인 조합을 추천해주세요.", "초보코더", "10분 전", 12, 4));
        posts.add(new PostItem("정보", "2026년 필수 생산성 확장 프로그램 5선 정리", "Error Lens, GitLens, Prettier 등 개발할 때 유용한 확장 프로그램을 정리했습니다.", "코딩마스터", "2시간 전", 45, 12));
        posts.add(new PostItem("잡담", "이번 업데이트로 단축키 살짝 바뀐 것 같네요", "업데이트 이후 일부 단축키 동작이 달라진 것 같습니다. 같은 현상을 겪는 분이 있나요?", "김철수", "어제", 8, 3));
        posts.add(new PostItem("정보", "C++ 개발 환경 세팅 디테일한 튜토리얼 공유", "컴파일러 설치부터 tasks.json과 launch.json 설정까지 순서대로 정리했습니다.", "테크리드", "2일 전", 32, 8));
  
    
    }   

    private JPanel createWritePage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel header = transparentPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JButton backButton = linkButton("← 돌아가기");
        backButton.addActionListener(event -> cardLayout.show(this, BOARD));
        header.add(backButton);
        header.add(Box.createVerticalStrut(10));
        JLabel title = label("새 게시글 작성", Font.BOLD, 24, TEXT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);

        UiComponents.RoundedPanel form = card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        form.add(createTextFieldGroup("제목", postTitleField));
        form.add(Box.createVerticalStrut(13));
        form.add(createComboFieldGroup("카테고리", postCategoryBox));
        form.add(Box.createVerticalStrut(13));
        form.add(createContentField());
        form.add(Box.createVerticalStrut(14));

        JPanel actions = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancelButton = outlineButton("취소");
        cancelButton.setPreferredSize(new Dimension(75, 38));
        cancelButton.addActionListener(event -> cardLayout.show(this, BOARD));
        JButton publishButton = primaryButton("게시하기");
        publishButton.setPreferredSize(new Dimension(90, 38));
        publishButton.addActionListener(event -> publishDraftPost());
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.add(cancelButton);
        actions.add(publishButton);
        form.add(actions);

        page.add(header, BorderLayout.NORTH);
        page.add(form, BorderLayout.CENTER);
        return page;
    }

    private JPanel createTextFieldGroup(String title, JTextField field) {
        JPanel group = transparentPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        JLabel fieldLabel = label(title, Font.BOLD, 12, TEXT);
        fieldLabel.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        group.setAlignmentX(LEFT_ALIGNMENT);
        group.add(fieldLabel);
        group.add(Box.createVerticalStrut(6));
        group.add(field);
        return group;
    }

    private JPanel createComboFieldGroup(String title, JComboBox<String> comboBox) {
        JPanel group = transparentPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        JLabel fieldLabel = label(title, Font.BOLD, 12, TEXT);
        fieldLabel.setAlignmentX(LEFT_ALIGNMENT);
        comboBox.setAlignmentX(LEFT_ALIGNMENT);
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        group.setAlignmentX(LEFT_ALIGNMENT);
        group.add(fieldLabel);
        group.add(Box.createVerticalStrut(6));
        group.add(comboBox);
        return group;
    }

    private JPanel createContentField() {
        JPanel group = transparentPanel(new BorderLayout(0, 6));
        group.setAlignmentX(LEFT_ALIGNMENT);

        JPanel header = transparentPanel(new BorderLayout());
        header.add(label("본문 내용", Font.BOLD, 12, TEXT), BorderLayout.WEST);
        header.add(contentCountLabel, BorderLayout.EAST);
        group.add(header, BorderLayout.NORTH);

        postContentArea.setFont(font(Font.PLAIN, 13));
        postContentArea.setLineWrap(true);
        postContentArea.setWrapStyleWord(true);
        postContentArea.setBorder(BorderFactory.createEmptyBorder(10, 11, 10, 11));
        JScrollPane scroll = new JScrollPane(postContentArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setPreferredSize(new Dimension(600, 230));
        group.add(scroll, BorderLayout.CENTER);
        return group;
    }

    private void configureContentLimit() {
        AbstractDocument document = (AbstractDocument)postContentArea.getDocument();
        document.setDocumentFilter(new ContentLengthFilter());
    }

    private void updateContentCount() {
        int length = postContentArea.getDocument().getLength();
        contentCountLabel.setText(length + " / " + MAX_CONTENT_LENGTH);
        contentCountLabel.setForeground(length >= MAX_CONTENT_LENGTH ? PRIMARY : SUB_TEXT);
    }

    private void publishDraftPost() {
        String title = postTitleField.getText().trim();
        String content = postContentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            postTitleField.setToolTipText("제목과 본문을 모두 입력해주세요.");
            return;
        }

        String category = String.valueOf(postCategoryBox.getSelectedItem());
        posts.add(0, new PostItem(category, title, content, "PawUser", "방금 전", 0, 0));
        currentPage = 0;
        renderPostPage();
        postTitleField.setText("");
        postContentArea.setText("");
        cardLayout.show(this, BOARD);
    }

    private JPanel createDetailPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JButton backButton = linkButton("← 목록으로");
        backButton.addActionListener(event -> cardLayout.show(this, BOARD));
        page.add(backButton, BorderLayout.NORTH);

        JPanel content = transparentPanel(new BorderLayout(0, 12));
        content.add(createDetailPostCard(), BorderLayout.NORTH);
        content.add(createCommentCard(), BorderLayout.CENTER);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createDetailPostCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 16, 22));
        card.setPreferredSize(new Dimension(700, 320));

        detailCategoryLabel.setFont(font(Font.BOLD, 10));
        detailCategoryLabel.setForeground(PRIMARY);
        detailCategoryLabel.setOpaque(true);
        detailCategoryLabel.setBackground(new Color(255, 239, 222));
        detailCategoryLabel.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
        detailCategoryLabel.setAlignmentX(LEFT_ALIGNMENT);
        detailTitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        detailMetaLabel.setAlignmentX(LEFT_ALIGNMENT);

        detailContentArea.setFont(font(Font.PLAIN, 13));
        detailContentArea.setForeground(TEXT);
        detailContentArea.setLineWrap(true);
        detailContentArea.setWrapStyleWord(true);
        detailContentArea.setEditable(false);
        detailContentArea.setOpaque(false);
        detailContentArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        detailContentArea.setMinimumSize(new Dimension(0, 150));
        detailContentArea.setPreferredSize(new Dimension(700, 150));
        detailContentArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        detailContentArea.setAlignmentX(LEFT_ALIGNMENT);

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        action.setAlignmentX(LEFT_ALIGNMENT);
        detailLikeButton.setPreferredSize(new Dimension(85, 34));
        detailLikeButton.addActionListener(event -> {
            detailLikeCount++;
            detailLikeButton.setText("♡ " + detailLikeCount);
        });
        action.add(detailLikeButton);

        card.add(detailCategoryLabel);
        card.add(Box.createVerticalStrut(7));
        card.add(detailTitleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(detailMetaLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(detailContentArea);
        card.add(action);
        return card;
    }

    private JPanel createCommentCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.add(label("댓글", Font.BOLD, 15, TEXT), BorderLayout.NORTH);

        commentRows.setLayout(new BoxLayout(commentRows, BoxLayout.Y_AXIS));
        JScrollPane commentScroll = new JScrollPane(commentRows);
        commentScroll.setBorder(null);
        commentScroll.setOpaque(false);
        commentScroll.getViewport().setOpaque(false);
        commentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        commentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        commentScroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(commentScroll, BorderLayout.CENTER);

        JPanel inputRow = transparentPanel(new BorderLayout(8, 0));
        JButton commentButton = primaryButton("댓글 등록");
        commentButton.setPreferredSize(new Dimension(90, 38));
        commentButton.addActionListener(event -> addComment());
        inputRow.add(commentField, BorderLayout.CENTER);
        inputRow.add(commentButton, BorderLayout.EAST);
        card.add(inputRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCommentRow(String author, String content, String time) {
        JPanel row = transparentPanel(new BorderLayout(10, 3));
        row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER), BorderFactory.createEmptyBorder(7, 2, 7, 2)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel meta = transparentPanel(new BorderLayout());
        meta.add(label(author, Font.BOLD, 11, TEXT), BorderLayout.WEST);
        meta.add(label(time, Font.PLAIN, 10, MUTED_TEXT), BorderLayout.EAST);
        row.add(meta, BorderLayout.NORTH);
        row.add(label(content, Font.PLAIN, 12, SUB_TEXT), BorderLayout.CENTER);
        return row;
    }

    private void openPostDetail(PostItem post) {
        detailCategoryLabel.setText(post.category());
        detailTitleLabel.setText(post.title());
        detailMetaLabel.setText(post.author() + "  ·  " + post.time() + "  ·  댓글 " + post.comments());
        detailContentArea.setText(post.content());
        detailContentArea.setCaretPosition(0);
        detailLikeCount = post.likes();
        detailLikeButton.setText("♡ " + detailLikeCount);

        commentRows.removeAll();
        if (post.comments() > 0) {
            commentRows.add(createCommentRow("코딩하는강아지", "좋은 정보 감사합니다. 저도 적용해볼게요!", "5분 전"));
            commentRows.add(createCommentRow("주황발바닥", "설명이 이해하기 쉽게 정리되어 있네요.", "2분 전"));
        } else {
            commentRows.add(label("아직 댓글이 없습니다. 첫 댓글을 남겨보세요.", Font.PLAIN, 12, SUB_TEXT));
        }
        commentRows.revalidate();
        commentRows.repaint();
        cardLayout.show(this, DETAIL);
    }

    private void addComment() {
        String content = commentField.getText().trim();
        if (content.isEmpty()) return;

        if (commentRows.getComponentCount() == 1 && commentRows.getComponent(0) instanceof JLabel) {
            commentRows.removeAll();
        }
        commentRows.add(createCommentRow("PawUser", content, "방금 전"), 0);
        commentField.setText("");
        commentRows.revalidate();
        commentRows.repaint();
    }

    private void makeClickable(Component component, Runnable action) {
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                action.run();
            }
        };

        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(listener);
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) makeClickable(child, action);
        }
    }

    private JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(font(Font.BOLD, 12));
        button.setForeground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(110, 24));
        return button;
    }

    private JLabel createTagLabel(String text, Color background, Color foreground) {
        JLabel tag = label(text, Font.BOLD, 10, foreground);
        tag.setOpaque(true);
        tag.setBackground(background);
        tag.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
        tag.setHorizontalAlignment(SwingConstants.CENTER);
        return tag;
    }

    private class ContentLengthFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass filterBypass, int offset, String text, AttributeSet attributes) throws BadLocationException {
            if (text == null) return;

            int available = MAX_CONTENT_LENGTH - filterBypass.getDocument().getLength();
            if (available > 0) {
                String acceptedText = text.substring(0, Math.min(available, text.length()));
                super.insertString(filterBypass, offset, acceptedText, attributes);
                updateContentCount();
            }
        }

        @Override
        public void replace(FilterBypass filterBypass, int offset, int length, String text, AttributeSet attributes) throws BadLocationException {
            String replacement = text == null ? "" : text;
            int available = MAX_CONTENT_LENGTH - (filterBypass.getDocument().getLength() - length);
            String acceptedText = replacement.substring(0, Math.min(Math.max(0, available), replacement.length()));
            super.replace(filterBypass, offset, length, acceptedText, attributes);
            updateContentCount();
        }

        @Override
        public void remove(FilterBypass filterBypass, int offset, int length) throws BadLocationException {
            super.remove(filterBypass, offset, length);
            updateContentCount();
        }
    }

    private static class ViewportWidthPanel extends JPanel implements Scrollable {
        ViewportWidthPanel() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 14;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(14, visibleRect.height - 14);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private record PostItem(
        String category,
        String title,
        String content,
        String author,
        String time,
        int likes,
        int comments
    ) {
    }
}
