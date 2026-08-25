package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import com.pawlog.client.network.PawLogClient;
import com.pawlog.common.dto.board.PostDetailDto;
import com.pawlog.common.dto.board.PostListDto;
import com.pawlog.common.dto.board.ProgramSearchDto;
import com.pawlog.common.dto.board.PostLikeDto;
import com.pawlog.common.dto.board.PostDetailDto.commentitem;

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
import javax.swing.SwingWorker;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ProgramBoardPanel extends JPanel {
    private static final String SELECT = "SELECT";
    private static final String BOARD = "BOARD";
    private static final String WRITE = "WRITE";
    private static final String DETAIL = "DETAIL";
    private static final int PAGE_SIZE = 9;
    private static final int PAGE_BUTTON_COUNT = 5;
    private static final int MAX_CONTENT_LENGTH = 500;
    private final CardLayout cardLayout = new CardLayout();
    private final PawLogClient pawLogClient = PawLogClient.getInstance();
    private final List<PostListDto.PostItem> posts = new ArrayList<>();
    private final List<ProgramRef> recentPrograms = new ArrayList<>();
    private final JTextField programSearchField = textField("프로그램 이름을 검색하세요");
    private final JPanel programSearchResults = transparentPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
    private final JPanel recentProgramButtons = transparentPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
    private final JLabel boardTitle = label("VS Code 게시판", Font.BOLD, 24, TEXT);
    private final JPanel postRows = transparentPanel();
    private final JPanel paginationPanel = transparentPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
    private final JTextField postTitleField = textField("제목을 입력해주세요");
    private final JTextArea postContentArea = new JTextArea();
    private final JLabel contentCountLabel = label("0 / 500", Font.PLAIN, 11, SUB_TEXT);
    private final JLabel detailTitleLabel = label("게시글 제목", Font.BOLD, 22, TEXT);
    private final JLabel detailMetaLabel = label("작성자 · 작성 시간", Font.PLAIN, 11, SUB_TEXT);
    private final JTextArea detailContentArea = new JTextArea();
    private final JPanel commentRows = new ViewportWidthPanel();
    private final JTextField commentField = textField("댓글을 입력해주세요");
    private final JButton detailLikeButton = outlineButton("♡ 0");
    private int currentPage;
    private int detailLikeCount;
    private long selectedProgramId = -1;
    private long currentDetailPostId = -1; // 상세 조회 중인 게시글 ID
    private boolean isCurrentPostLiked = false; // 좋아요 상태
    public ProgramBoardPanel() {
        configureContentLimit();
        configureProgramSearch();
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
        boardButton.addActionListener(event -> findProgramAndOpenBoard(name));
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
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(17, 20, 17, 20));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));
        card.setPreferredSize(new Dimension(700, 270));

        JPanel heading = transparentPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = label("프로그램별 게시판 찾기", Font.BOLD, 15, TEXT);
        JLabel description = label("프로그램 이름을 검색하거나 최근 본 게시판을 선택하세요.", Font.PLAIN, 11, SUB_TEXT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        description.setAlignmentX(LEFT_ALIGNMENT);
        heading.add(title);
        heading.add(Box.createVerticalStrut(3));
        heading.add(description);
        card.add(heading, BorderLayout.NORTH);

        JPanel body = transparentPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        programSearchField.setAlignmentX(LEFT_ALIGNMENT);
        programSearchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        programSearchField.setPreferredSize(new Dimension(620, 38));
        programSearchResults.setAlignmentX(LEFT_ALIGNMENT);
        programSearchResults.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        body.add(programSearchField);
        body.add(Box.createVerticalStrut(8));
        body.add(programSearchResults);
        body.add(Box.createVerticalStrut(10));
        body.add(createProgramShortcutSection("최근 본 프로그램", recentProgramButtons));

        updateRecentProgramButtons();
        updateProgramSearchResults();
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProgramShortcutSection(String title, JPanel buttons) {
        JPanel section = transparentPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        section.setPreferredSize(new Dimension(620, 82));
        JLabel titleLabel = label(title, Font.BOLD, 11, TEXT);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        buttons.setAlignmentX(LEFT_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        section.add(titleLabel);
        section.add(Box.createVerticalStrut(6));
        section.add(buttons);
        return section;
    }

    private JButton createProgramShortcutButton(ProgramRef program) {
        JButton button = outlineButton(program.displayName());
        button.setPreferredSize(new Dimension(Math.max(76, program.displayName().length() * 9 + 24), 28));

        // 최근 프로그램은 ID를 함께 저장하므로 이름으로 다시 검색하지 않는다.
        button.addActionListener(event -> openBoard(program.programId(), program.displayName()));
        return button;
    }

    private JButton createProgramSearchResultButton(ProgramSearchDto.ProgramItem program) {
        JButton button = outlineButton(program.getDisplayName());
        button.setPreferredSize(new Dimension(Math.max(76, program.getDisplayName().length() * 9 + 24), 28));

        // 검색 응답에는 이미 programId가 있으므로 다시 검색할 필요가 없다.
        button.addActionListener(event -> openBoard(program.getProgramId(), program.getDisplayName()));
        return button;
    }

    private void configureProgramSearch() {
        programSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateProgramSearchResults();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateProgramSearchResults();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateProgramSearchResults();
            }
        });

        programSearchField.addActionListener(event -> {
            String keyword = programSearchField.getText().trim();
            if (!keyword.isBlank()) findProgramAndOpenBoard(keyword);
        });
    }

    private void updateProgramSearchResults() {
        programSearchResults.removeAll();
        String keyword = programSearchField.getText().trim().toLowerCase();

        if (keyword.isBlank()) {
            programSearchResults.add(label("검색 결과가 여기에 표시됩니다.", Font.PLAIN, 11, SUB_TEXT));
        } else {
            //서버 만들어지기 전까지 주석처리
            loadProgramSearchResultsFromServer(keyword); 
        }

        programSearchResults.revalidate();
        programSearchResults.repaint();
    }

    private void loadProgramSearchResultsFromServer(String keyword) {
        programSearchResults.removeAll();
        programSearchResults.add(label("프로그램 검색 중...", Font.PLAIN, 11, SUB_TEXT));
        programSearchResults.revalidate();
        programSearchResults.repaint();

        new SwingWorker<ProgramSearchDto.Response, Void>() {
            @Override
            protected ProgramSearchDto.Response doInBackground() throws Exception {
                return pawLogClient.searchPrograms(keyword, 6);
            }

            @Override
            protected void done() {
                if (!programSearchField.getText().trim().equalsIgnoreCase(keyword)) {
                    return;
                }

                programSearchResults.removeAll();

                try {
                    ProgramSearchDto.Response response = get();

                    if (!response.isSuccess()) {
                        programSearchResults.add(label(response.getMessage(), Font.PLAIN, 11, RED));
                    } else if (response.getPrograms() == null || response.getPrograms().isEmpty()) {
                        programSearchResults.add(label("일치하는 프로그램이 없습니다.", Font.PLAIN, 11, SUB_TEXT));
                    } else {
                        for(ProgramSearchDto.ProgramItem program : response.getPrograms()) {
                            programSearchResults.add(createProgramSearchResultButton(program));
                        }
                    }
                } catch (Exception exception) {
                    programSearchResults.add(label("서버 연결에 실패했습니다.", Font.PLAIN, 11, RED));
                    exception.printStackTrace();
                }

                programSearchResults.revalidate();
                programSearchResults.repaint();
            }
        }.execute();
    }

    private void updateRecentProgramButtons() {
        recentProgramButtons.removeAll();

        if (recentPrograms.isEmpty()) {
            recentProgramButtons.add(label("최근 본 프로그램이 없습니다.", Font.PLAIN, 11, SUB_TEXT));
        }

        for(ProgramRef program : recentPrograms) {
            recentProgramButtons.add(createProgramShortcutButton(program));
        }

        recentProgramButtons.revalidate();
        recentProgramButtons.repaint();
    }

    // 이름만 알고 있는 프로그램은 먼저 검색해서 고유한 programId를 확인한다.
    private void findProgramAndOpenBoard(String programName) {
        programSearchResults.removeAll();
        programSearchResults.add(label("프로그램 확인 중...", Font.PLAIN, 11, SUB_TEXT));
        programSearchResults.revalidate();
        programSearchResults.repaint();

        new SwingWorker<ProgramSearchDto.Response, Void>() {
            @Override
            protected ProgramSearchDto.Response doInBackground() throws Exception {
                return pawLogClient.searchPrograms(programName, 10);
            }

            @Override
            protected void done() {
                try {
                    ProgramSearchDto.Response response = get();

                    if (!response.isSuccess() || response.getPrograms() == null || response.getPrograms().isEmpty()) {
                        showProgramSearchMessage("해당 프로그램을 찾을 수 없습니다.", RED);
                        return;
                    }

                    // 대소문자를 무시하고 이름이 정확히 같은 프로그램을 우선 선택한다.
                    ProgramSearchDto.ProgramItem program = response.getPrograms().stream()
                        .filter(item -> programName.equalsIgnoreCase(item.getDisplayName())
                            || programName.equalsIgnoreCase(item.getProcessName()))
                        .findFirst()
                        .orElse(response.getPrograms().get(0));

                    openBoard(program.getProgramId(), program.getDisplayName());
                } catch (Exception exception) {
                    showProgramSearchMessage("서버 연결에 실패했습니다.", RED);
                    exception.printStackTrace();
                }
            }
        }.execute();
    }

    private void showProgramSearchMessage(String message, Color color) {
        programSearchResults.removeAll();
        programSearchResults.add(label(message, Font.PLAIN, 11, color));
        programSearchResults.revalidate();
        programSearchResults.repaint();
    }

    // programName은 제목 표시용이고 programId는 서버 게시글 조회용이다.
    private void openBoard(long programId, String programName) {
        selectedProgramId = programId;
        recentPrograms.removeIf(program -> program.programId() == programId);
        recentPrograms.add(0, new ProgramRef(programId, programName));

        while(recentPrograms.size() > 8) {
            recentPrograms.remove(recentPrograms.size() - 1);
        }

        updateRecentProgramButtons();
        programSearchField.setText("");
        boardTitle.setText(programName + " 게시판");

        // 프로그램 버튼을 누르는 순간 첫 페이지 게시글을 서버에 요청한다.
        loadPostList(programId, 0);
    }

    // 서버 통신은 EDT 밖에서 실행해 조회 중에도 Swing 화면이 멈추지 않게 한다.
    private void loadPostList(long programId, int page) {
        postRows.removeAll();
        postRows.add(label("게시글을 불러오는 중...", Font.PLAIN, 13, SUB_TEXT));
        postRows.revalidate();
        postRows.repaint();
        cardLayout.show(this, BOARD);

        new SwingWorker<PostListDto.Response, Void>() {
            @Override
            protected PostListDto.Response doInBackground() throws Exception {
                return pawLogClient.getPostList(programId, page, PAGE_SIZE);
            }

            @Override
            protected void done() {
                try {
                    PostListDto.Response response = get();

                    if (!response.isSuccess()) {
                        showPostListMessage(response.getMessage());
                        return;
                    }

                    renderPostList(response.getData());
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    String causeMessage = cause.getMessage();
                    String message = causeMessage == null || causeMessage.isBlank()
                        ? "게시글 목록을 불러오지 못했습니다."
                        : "게시글 조회 실패: " + causeMessage;

                    showPostListMessage(message);
                    exception.printStackTrace();
                }
            }
        }.execute();
    }

    private void showPostListMessage(String message) {
        postRows.removeAll();
        postRows.add(label(message, Font.PLAIN, 13, RED));
        paginationPanel.removeAll();
        postRows.revalidate();
        postRows.repaint();
        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    // 목록 응답 DTO를 별도의 목업 객체로 변환하지 않고 그대로 화면에 표시한다.
    private void renderPostList(PostListDto.PageData data) {
        posts.clear();

        if(data != null && data.getPosts() != null) {
            posts.addAll(data.getPosts());
        }

        currentPage = data == null ? 0 : data.getCurrentPage();
        postRows.removeAll();

        for(PostListDto.PostItem post : posts) {
            postRows.add(createPostRow(post));
        }

        if(posts.isEmpty()) {
            postRows.add(label("등록된 게시글이 없습니다.", Font.PLAIN, 13, SUB_TEXT));
        }

        renderPagination(data == null ? 1 : Math.max(1, data.getTotalPages()));
        postRows.revalidate();
        postRows.repaint();
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
        postRows.add(label("프로그램을 선택하면 게시글을 불러옵니다.", Font.PLAIN, 13, SUB_TEXT));
        return table;   
    }
    
    private JPanel createPostRow(PostListDto.PostItem post) {
        JPanel row = transparentPanel(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_DIVIDER));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setPreferredSize(new Dimension(0, 50));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel titleArea = transparentPanel(new BorderLayout(8, 0));
        titleArea.setBorder(BorderFactory.createEmptyBorder(11, 8, 11, 0));
        /*Color tagColor = "질문".equals(post.category()) ? new Color(255, 239, 222) : new Color(238, 242, 245);*/
        /*titleArea.add(createTagLabel(post.category(), tagColor, "질문".equals(post.category()) ? PRIMARY : SUB_TEXT), BorderLayout.WEST);*/
        titleArea.add(label(post.getTitle(), Font.PLAIN, 13, TEXT), BorderLayout.CENTER);

        JPanel infoArea = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 12, 13));
        JLabel authorLabel = label(post.getAuthorNickname(), Font.PLAIN, 11, SUB_TEXT);
        authorLabel.setPreferredSize(new Dimension(75, 20));
        JLabel timeLabel = label(post.getCreatedAt(), Font.PLAIN, 11, MUTED_TEXT);
        timeLabel.setPreferredSize(new Dimension(60, 20));
        infoArea.add(authorLabel);
        infoArea.add(timeLabel);
        infoArea.add(label("♡ " + post.getLikeCount(), Font.PLAIN, 11, PRIMARY));
        infoArea.add(label("○ " + post.getCommentCount(), Font.PLAIN, 11, SUB_TEXT));

        row.add(titleArea, BorderLayout.CENTER);
        row.add(infoArea, BorderLayout.EAST);
        makeClickable(row, () -> openPostDetail(post.getPostId()));
        /////////////////////////////////////////////////////////////////////////////////////
        return row;
    }

    private void renderPagination(int pageCount) {
        paginationPanel.removeAll();

        if (pageCount <= 1) {
            paginationPanel.revalidate();
            paginationPanel.repaint();
            return;
        }

        addPaginationButton("≪ 처음", false, currentPage == 0, () -> {
            openPostPage(0);
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
                openPostPage(pageIndex);
            });
        }

        if (endPage < pageCount - 1) addPaginationText("…");

        addPaginationButton("끝 ≫", false, currentPage == pageCount - 1, () -> {
            openPostPage(pageCount - 1);
        });

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    // 페이지 번호를 누를 때도 현재 선택한 programId와 새 페이지 번호를 서버에 보낸다.
    private void openPostPage(int page) {
        currentPage = page;

        if (selectedProgramId > 0) {
            loadPostList(selectedProgramId, page);
        }
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

    private JPanel createWritePage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel header = transparentPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JButton backButton = linkButton("← 돌아가기");
        backButton.addActionListener(event -> {
            clearPostForm();
            cardLayout.show(this, BOARD);
        });
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
        form.add(createContentField());
        form.add(Box.createVerticalStrut(14));

        JPanel actions = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancelButton = outlineButton("취소");
        cancelButton.setPreferredSize(new Dimension(75, 38));
        cancelButton.addActionListener(event -> {
            clearPostForm();
            cardLayout.show(this, BOARD);
        });
        JButton publishButton = primaryButton("게시하기");
        publishButton.setPreferredSize(new Dimension(90, 38));
        publishButton.addActionListener(event -> {
    String title1 = postTitleField.getText().trim();
    String content = postContentArea.getText().trim();

    if (selectedProgramId <= 0) {
        return;
    }

    if (title1.isEmpty() || content.isEmpty()) {
        return;
    }

    publishButton.setEnabled(false);
    publishButton.setText("게시 중...");

    new SwingWorker<PawLogClient.Response, Void>() {
        @Override
        protected PawLogClient.Response doInBackground() throws Exception {
            return pawLogClient.createPost(
                selectedProgramId,
                title1,
                content
            );
        }

        @Override
        protected void done() {
            try {
                PawLogClient.Response response = get();

                if (!response.success()) {
                    System.out.println(response.message());
                    return;
                }

                clearPostForm();

                loadPostList(selectedProgramId, 0);
                cardLayout.show(ProgramBoardPanel.this, BOARD);
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                publishButton.setEnabled(true);
                publishButton.setText("게시하기");
            }
        }
    }.execute();
});
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.add(cancelButton);
        actions.add(publishButton);
        form.add(actions);

        page.add(header, BorderLayout.NORTH);
        page.add(form, BorderLayout.CENTER);
        return page;
    }

    // 작성 화면을 벗어날 때 이전에 입력하던 내용을 남기지 않는다.
    private void clearPostForm() {
        postTitleField.setText("");
        postContentArea.setText("");
        contentCountLabel.setText("0 / " + MAX_CONTENT_LENGTH);
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

    private JPanel createDetailPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JButton backButton = linkButton("← 목록으로");
        backButton.addActionListener(event -> {
            if (selectedProgramId > 0) {
                loadPostList(selectedProgramId, currentPage); //
            } else {
                cardLayout.show(this, BOARD); //
            }
        });
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
            if (currentDetailPostId <= 0) return;

            detailLikeButton.setEnabled(false);

            new SwingWorker<PostLikeDto.Response, Void>() {
                @Override
                protected PostLikeDto.Response doInBackground() throws Exception {
                    return pawLogClient.togglePostLike(currentDetailPostId);
                }

                @Override
                protected void done() {
                    try {
                        PostLikeDto.Response res = get();
                        if (res.isSuccess() && res.getData() != null) {
                            isCurrentPostLiked = res.getData().isLiked();
                            detailLikeCount = res.getData().getLikeCount();
                            detailLikeButton.setText((isCurrentPostLiked ? "♥ " : "♡ ") + detailLikeCount);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        detailLikeButton.setEnabled(true);
                    }
                }
            }.execute();
        });
        action.add(detailLikeButton);

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

    // 목록에서 받은 postId로 서버에 상세 내용을 요청한다.
    private void openPostDetail(long postId) {
        this.currentDetailPostId = postId;//현재 게시글 ID 저장

        detailTitleLabel.setText("게시글을 불러오는 중...");
        detailMetaLabel.setText("");
        detailContentArea.setText("");
        commentRows.removeAll();
        commentRows.add(label("댓글을 불러오는 중...", Font.PLAIN, 12, SUB_TEXT));
        commentRows.revalidate();
        commentRows.repaint();
        cardLayout.show(this, DETAIL);

        new SwingWorker<PostDetailDto.Response, Void>() {
            @Override
            protected PostDetailDto.Response doInBackground() throws Exception {
                return pawLogClient.joinPost(postId);
            }
            
            @Override
            protected void done() {
                try {
                    PostDetailDto.Response response = get();

                    if(!response.isSuccess() || response.getData() == null) {
                        showPostDetailError(response.getMessage());
                        return;
                    }

                    renderPostDetail(response.getData());
                } catch(Exception exception) {
                    showPostDetailError("게시글 상세 정보를 불러오지 못했습니다.");
                    exception.printStackTrace();
                }
            }
        }.execute();
    }

    // 상세 응답 DTO의 게시글과 댓글을 상세 화면에 표시한다.
    private void renderPostDetail(PostDetailDto.PostDetailData post) {
        detailTitleLabel.setText(post.getTitle());
        detailMetaLabel.setText(
            post.getAuthorNickname()
                + "  ·  " + post.getCreatedAt()
                + "  ·  댓글 " + post.getCommentCount()
        );
        detailContentArea.setText(post.getContent());
        detailContentArea.setCaretPosition(0);

        // 좋아요 상태 및 카운트 동기화
        this.isCurrentPostLiked = post.isLiked();
        this.detailLikeCount = post.getLikeCount();
        detailLikeButton.setText((isCurrentPostLiked ? "♥ " : "♡ ") + detailLikeCount);

        commentRows.removeAll();
        List<commentitem> comments = post.getComments();
        System.out.println(post.getComments());
        if(comments != null && !comments.isEmpty()) {
            for(commentitem comment : comments) {
                commentRows.add(createCommentRow(
                    comment.getCommentAuthor(),
                    comment.getCommentcontent(),
                    comment.getCommmenttime()
                ));
            }
        } else {
            commentRows.add(label("아직 댓글이 없습니다. 첫 댓글을 남겨보세요.", Font.PLAIN, 12, SUB_TEXT));
        }

        commentRows.revalidate();
        commentRows.repaint();
    }

    private void showPostDetailError(String message) {
        detailTitleLabel.setText("게시글을 불러오지 못했습니다.");
        detailMetaLabel.setText("");
        detailContentArea.setText(message == null || message.isBlank() ? "서버 응답을 확인해주세요." : message);
        commentRows.removeAll();
        commentRows.add(label("댓글을 표시할 수 없습니다.", Font.PLAIN, 12, SUB_TEXT));
        commentRows.revalidate();
        commentRows.repaint();
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

    // 최근 프로그램에서 표시 이름과 서버 요청용 ID를 한 쌍으로 보관한다.
    private record ProgramRef(
        long programId,
        String displayName
    ) {
    }
}
