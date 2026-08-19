package com.pawlog.client.ui.dashboard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class UiComponents {
    // 앱 전체 기본 아이보리 배경색
    public static final Color BACKGROUND = new Color(247, 244, 239);
    
    // 카드, 입력창, 밝은 버튼 
    public static final Color CARD = Color.WHITE;

    // PawLog의 주요 강조색으로 버튼, 링크
    public static final Color PRIMARY = new Color(255, 159, 67);

    // 제목과 본문에 사용하는 기본 진회색
    public static final Color TEXT = new Color(45, 52, 54);

    // 설명과 부가 정보에 사용하는 보조 글자색
    public static final Color SUB_TEXT = new Color(99, 110, 114);

    // 홈 화면의 덜 강조된 설명에 사용하는 부드러운 글자색
    public static final Color SOFT_TEXT = new Color(120, 130, 132);

    // 시간과 비활성 정보에 사용하는 가장 흐린 글자색
    public static final Color MUTED_TEXT = new Color(180, 185, 187);

    // 입력창과 진행률 배경에 사용하는 기본 테두리색
    public static final Color BORDER = new Color(225, 225, 225);

    // 카드 외곽선에 사용하는 연한 테두리색
    public static final Color SOFT_BORDER = new Color(235, 235, 235);

    // 표의 행을 구분하는 매우 연한 선 색상
    public static final Color TABLE_DIVIDER = new Color(239, 239, 239);

    // 추적 중, 저장 성공 등 정상 상태를 나타내는 초록색
    public static final Color SUCCESS = new Color(0, 184, 148);

    // 닫기, 삭제, 오류 등 위험 동작을 나타내는 빨간색
    public static final Color RED = new Color(231, 76, 60);

    // 통계 그래프에서 프로그램을 구분하는 보조 색상
    public static final Color CHART_BLUE = new Color(72, 126, 255);
    public static final Color CHART_PURPLE = new Color(162, 155, 254);
    public static final Color CHART_PINK = new Color(253, 121, 168);

    // 투명한 위젯 창 배경에 사용하는 완전 투명색
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // 공통 UI 기능만 제공하는 클래스이므로 객체 생성을 막음
    private UiComponents() {}

    // 모든 화면에서 공통으로 사용할 맑은 고딕 폰트를 생성
    static Font font(int style, int size) {
        return new Font("맑은 고딕", style, size);
    }

    // 글자, 굵기, 크기, 색상을 적용한 공통 라벨을 생성
    static JLabel label(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(style, size));
        label.setForeground(color);
        return label;
    }

    // 배경이 보이지 않는 기본 투명 패널을 생성
    static JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    // 원하는 LayoutManager가 적용된 투명 패널을 생성
    static JPanel transparentPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    // 페이지 제목과 설명을 세로로 배치한 제목 패널을 생성
    static JPanel titlePanel(String title, String description) {
        JPanel panel = transparentPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel titleLabel = label(title, Font.BOLD, 24, TEXT);
        JLabel descriptionLabel = label(description, Font.PLAIN, 13, SUB_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descriptionLabel);
        return panel;
    }

    // 흰색 배경과 둥근 모서리를 가진 기본 카드 패널을 생성
    static RoundedPanel card() {
        return new RoundedPanel(16, CARD);
    }

    // 로그인, 저장 등 주요 기능에 사용하는 주황색 버튼을 생성
    static JButton primaryButton(String text) {
        JButton button = new RoundedButton(text, PRIMARY, CARD);
        button.setFont(font(Font.BOLD, 13));
        button.setPreferredSize(new Dimension(110, 38));
        return button;
    }

    // 취소, 필터 등 보조 기능에 사용하는 테두리 버튼을 생성
    static JButton outlineButton(String text) {
        JButton button = new JButton(text);
        button.setFont(font(Font.BOLD, 12));
        button.setForeground(PRIMARY);
        button.setBackground(CARD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY));
        button.setPreferredSize(new Dimension(110, 36));
        return button;
    }

    // 공통 글꼴, 테두리, 내부 여백을 적용한 한 줄 입력창을 생성
    static JTextField textField(String tooltip) {
        JTextField field = new JTextField();
        field.setToolTipText(tooltip);
        field.setFont(font(Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(0, 11, 0, 11)));
        field.setPreferredSize(new Dimension(180, 38));
        return field;
    }

    // 전달받은 문자열 목록으로 공통 드롭다운을 생성
    static JComboBox<String> combo(String... values) {
        JComboBox<String> combo = new JComboBox<>(values);
        combo.setFont(font(Font.PLAIN, 13));
        combo.setBackground(CARD);
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
        combo.setPreferredSize(new Dimension(160, 38));
        return combo;
    }

    // 모서리가 둥근 배경을 직접 그리는 카드용 패널
    static class RoundedPanel extends JPanel {
        // radius는 둥근 정도, fill은 카드의 배경색
        private final int radius;
        private final Color fill;

        // 둥근 정도와 배경색을 받아 카드 패널을 초기화
        RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        // JPanel의 기본 사각 배경 대신 둥근 사각형 배경을 그림
        @Override
        protected void paintComponent(Graphics graphics) {
            // 원본 Graphics를 변경하지 않기 위해 복사본을 사용
            Graphics2D g2 = (Graphics2D)graphics.create();

            // 둥근 모서리가 계단처럼 보이지 않도록 부드럽게 처리
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();

            // 패널 내부에 추가된 자식 컴포넌트가 정상적으로 그려지게 함
            super.paintComponent(graphics);
        }
    }

    // 모서리가 둥근 배경을 직접 그리는 주요 기능용 버튼
    private static class RoundedButton extends JButton {
        // 버튼에 채울 배경색
        private final Color fill;

        // 버튼 문구, 배경색, 글자색을 받아 버튼을 초기화
        RoundedButton(String text, Color fill, Color foreground) {
            super(text);
            this.fill = fill;
            setForeground(foreground);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            setContentAreaFilled(false);
            setOpaque(false);
        }

        // JButton의 기본 사각 배경 대신 둥근 사각형 배경을 그림
        @Override
        protected void paintComponent(Graphics graphics) {
            // 원본 Graphics를 변경하지 않기 위해 복사본을 사용
            Graphics2D g2 = (Graphics2D)graphics.create();

            // 둥근 모서리가 매끄럽게 표시되도록 설정
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();

            // 둥근 배경 위에 버튼 글자를 그림
            super.paintComponent(graphics);
        }
    }
}
