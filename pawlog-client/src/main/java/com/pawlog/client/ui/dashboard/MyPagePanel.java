package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.Year;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.pawlog.Device.DeviceIdManager;
import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.network.PawLogClient;
import com.pawlog.common.dto.auth.AuthUpdateDto;

public class MyPagePanel extends JPanel {
    private final PawLogClient pawLogClient = PawLogClient.getInstance();
    private final AuthStore authStore = AuthStore.getInstance();

    private final JTextField emailField = textField("이메일");
    private final JTextField nicknameField = textField("닉네임");
    private final JLabel profileStatusLabel = label("", Font.PLAIN, 12, SUB_TEXT);

    // 생년월일 콤보박스 필드 분리
    private final JComboBox<String> yearCombo = new JComboBox<>();
    private final JComboBox<String> monthCombo = new JComboBox<>();
    private final JComboBox<String> dayCombo = new JComboBox<>();

    private final JPasswordField currentPasswordField = passwordField();
    private final JPasswordField newPasswordField = passwordField();
    private final JPasswordField confirmPasswordField = passwordField();
    private final JLabel securityStatusLabel = label("", Font.PLAIN, 12, SUB_TEXT);

    private final JLabel UUID = label(DeviceIdManager.getDeviceId(), Font.BOLD, 12, SUB_TEXT);

    public MyPagePanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(titlePanel("마이페이지", "내 정보와 비밀번호를 확인하고 변경할 수 있습니다."), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        // 패널이 화면에 실제로 보여질 때마다 자동으로 정보를 새로고침
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing()) {
                    refreshUserInfo();
                }
            }
        });

        refreshUserInfo();
    }

    // 로그인된 사용자 정보 동기화
    public void refreshUserInfo() {
        emailField.setText(authStore.getEmail() != null ? authStore.getEmail() : "user@example.com");
        nicknameField.setText(authStore.getNickname() != null ? authStore.getNickname() : "PawUser");

        // 생년월일 파싱 및 콤보박스 세팅
        String birthDate = authStore.getBirthDate();
        if (birthDate != null && birthDate.contains("-")) {
            String[] parts = birthDate.split("-");
            if (parts.length == 3) {
                yearCombo.setSelectedItem(parts[0] + "년");
                monthCombo.setSelectedItem(parts[1] + "월");
                dayCombo.setSelectedItem(parts[2] + "일");
            }
        }

        clearPasswordFields();
        profileStatusLabel.setText("");
        securityStatusLabel.setText("");
    }

    private JPanel createContent() {
        JPanel content = transparentPanel(new GridLayout(1, 2, 16, 0));
        content.add(createProfileCard());
        content.add(createSecurityCard());
        return content;
    }

    // 기본 정보 (닉네임) 변경 카드
    private JPanel createProfileCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        card.add(label("기본 정보", Font.BOLD, 17, TEXT));
        card.add(Box.createVerticalStrut(18));

        emailField.setEditable(false);
        addField(card, "이메일", emailField);
        card.add(Box.createVerticalStrut(14));
        addField(card, "닉네임", nicknameField);
        card.add(Box.createVerticalStrut(14));

        JLabel birthLabel = label("생년월일", Font.BOLD, 12, TEXT);
        birthLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(birthLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(createBirthPanel());
        card.add(Box.createVerticalStrut(10));

        profileStatusLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(profileStatusLabel);
        card.add(Box.createVerticalStrut(10));

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        action.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton saveInfoButton = primaryButton("정보 저장");
        saveInfoButton.addActionListener(event -> handleNicknameUpdate(saveInfoButton));
        action.add(saveInfoButton);
        card.add(action);

        card.add(Box.createVerticalStrut(15));
        card.add(label("기기 식별 번호 (UUID)", Font.BOLD, 12, TEXT));
        card.add(Box.createVerticalStrut(4));
        card.add(UUID);

        return card;
    }

    // 비밀번호 변경 카드
    private JPanel createSecurityCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        card.add(label("비밀번호 변경", Font.BOLD, 17, TEXT));
        card.add(Box.createVerticalStrut(7));
        card.add(label("안전한 계정 사용을 위해 주기적으로 비밀번호를 변경하세요.", Font.PLAIN, 11, SUB_TEXT));
        card.add(Box.createVerticalStrut(20));

        addField(card, "현재 비밀번호", currentPasswordField);
        card.add(Box.createVerticalStrut(14));
        addField(card, "새 비밀번호", newPasswordField);
        card.add(Box.createVerticalStrut(14));
        addField(card, "새 비밀번호 확인", confirmPasswordField);
        card.add(Box.createVerticalStrut(10));

        securityStatusLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(securityStatusLabel);
        card.add(Box.createVerticalStrut(10));

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        action.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton changeButton = primaryButton("비밀번호 변경");
        changeButton.setPreferredSize(new Dimension(130, 38));
        changeButton.addActionListener(event -> handlePasswordUpdate(changeButton));
        action.add(changeButton);
        card.add(action);

        return card;
    }

    // 닉네임 수정 처리 로직
    private void handleNicknameUpdate(JButton button) {
        String newNickname = nicknameField.getText().trim();

        if (newNickname.isEmpty()) {
            profileStatusLabel.setText("닉네임을 입력해주세요.");
            return;
        }

        if (newNickname.equals(authStore.getNickname())) {
            profileStatusLabel.setText("현재 닉네임과 동일합니다.");
            return;
        }

        button.setEnabled(false);
        profileStatusLabel.setText("저장 중...");

        new Thread(() -> {
            try {
                AuthUpdateDto.Response response = pawLogClient.updateAuth(newNickname, null, null);
                SwingUtilities.invokeLater(() -> {
                    profileStatusLabel.setText(response.getMessage());
                    if (response.isSuccess()) {
                        authStore.setNickname(response.getUpdatedNickname());
                    }
                    button.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    profileStatusLabel.setText("서버 연결에 실패했습니다.");
                    button.setEnabled(true);
                });
                e.printStackTrace();
            }
        }, "mypage-nickname-update").start();
    }

    // 비밀번호 수정 처리 로직
    private void handlePasswordUpdate(JButton button) {
        String currentPw = new String(currentPasswordField.getPassword());
        String newPw = new String(newPasswordField.getPassword());
        String confirmPw = new String(confirmPasswordField.getPassword());

        if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            securityStatusLabel.setText("모든 비밀번호 항목을 입력해주세요.");
            return;
        }

        if (!newPw.equals(confirmPw)) {
            securityStatusLabel.setText("새 비밀번호가 일치하지 않습니다.");
            return;
        }

        if (currentPw.equals(newPw)) {
            securityStatusLabel.setText("새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다.");
            return;
        }

        button.setEnabled(false);
        securityStatusLabel.setText("변경 중...");

        new Thread(() -> {
            try {
                AuthUpdateDto.Response response = pawLogClient.updateAuth(null, currentPw, newPw);
                SwingUtilities.invokeLater(() -> {
                    securityStatusLabel.setText(response.getMessage());
                    if (response.isSuccess()) {
                        clearPasswordFields();
                    }
                    button.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    securityStatusLabel.setText("서버 연결에 실패했습니다.");
                    button.setEnabled(true);
                });
                e.printStackTrace();
            }
        }, "mypage-password-update").start();
    }

    private void clearPasswordFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void addField(JPanel card, String title, JTextField field) {
        JLabel label = label(title, Font.BOLD, 12, TEXT);
        label.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(label);
        card.add(Box.createVerticalStrut(6));
        card.add(field);
    }

    private JPanel createBirthPanel() {
        JPanel panel = transparentPanel(new GridLayout(1, 3, 9, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        int currentYear = Year.now().getValue();
        for (int i = 0; i < 100; i++) yearCombo.addItem((currentYear - i) + "년");
        for (int i = 1; i <= 12; i++) monthCombo.addItem(String.format("%02d월", i));
        for (int i = 1; i <= 31; i++) dayCombo.addItem(String.format("%02d일", i));

        // 수정 불가능하도록 비활성화 처리
        yearCombo.setEnabled(false);
        monthCombo.setEnabled(false);
        dayCombo.setEnabled(false);

        panel.add(yearCombo);
        panel.add(monthCombo);
        panel.add(dayCombo);
        return panel;
    }

    private JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(font(Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(0, 11, 0, 11)));
        field.setPreferredSize(new Dimension(180, 40));
        return field;
    }
}