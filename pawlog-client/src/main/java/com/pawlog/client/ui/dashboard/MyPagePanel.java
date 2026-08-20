package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
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
import com.pawlog.Device.DeviceIdManager;


public class MyPagePanel extends JPanel {
    JLabel UUID = label(DeviceIdManager.getDeviceId(),Font.BOLD, 12, TEXT);
    public MyPagePanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(titlePanel("마이페이지", "내 정보와 비밀번호를 확인하고 변경할 수 있습니다."), BorderLayout.NORTH);
    
        add(createContent(), BorderLayout.CENTER);
        
    }
    
    private JPanel createContent() {
        JPanel content = transparentPanel(new GridLayout(1, 2, 16, 0));
        content.add(createProfileCard());
        content.add(createSecurityCard());
        return content;
    }

    private JPanel createProfileCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        card.add(label("기본 정보", Font.BOLD, 17, TEXT));
        card.add(Box.createVerticalStrut(18));

        JTextField email = textField("이메일");
        email.setText("user@example.com");
        email.setEditable(false);
        JTextField nickname = textField("닉네임");
        nickname.setText("PawUser");
        addField(card, "이메일", email);
        card.add(Box.createVerticalStrut(14));
        addField(card, "닉네임", nickname);
        card.add(Box.createVerticalStrut(14));

        JLabel birthLabel = label("생년월일", Font.BOLD, 12, TEXT);
        birthLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(birthLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(createBirthPanel());
        card.add(Box.createVerticalStrut(18));

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        action.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        action.add(primaryButton("정보 저장"));
        card.add(action);
        card.add(UUID);
        return card;
    }

    private JPanel createSecurityCard() {
        UiComponents.RoundedPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        card.add(label("비밀번호 변경", Font.BOLD, 17, TEXT));
        card.add(Box.createVerticalStrut(7));
        card.add(label("안전한 계정 사용을 위해 주기적으로 비밀번호를 변경하세요.", Font.PLAIN, 11, SUB_TEXT));
        card.add(Box.createVerticalStrut(20));

        JPasswordField current = passwordField();
        JPasswordField next = passwordField();
        JPasswordField confirm = passwordField();
        addField(card, "현재 비밀번호", current);
        card.add(Box.createVerticalStrut(14));
        addField(card, "새 비밀번호", next);
        card.add(Box.createVerticalStrut(14));
        addField(card, "새 비밀번호 확인", confirm);
        card.add(Box.createVerticalStrut(18));

        JPanel action = transparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        action.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton changeButton = primaryButton("비밀번호 변경");
        changeButton.setPreferredSize(new Dimension(130, 38));
        action.add(changeButton);
        card.add(action);
        return card;
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
        String[] years = new String[100];
        String[] months = new String[12];
        String[] days = new String[31];
        int currentYear = Year.now().getValue();
        for(int i = 0; i < years.length; i++) years[i] = currentYear - i + "년";
        for(int i = 0; i < months.length; i++) months[i] = String.format("%02d월", i + 1);
        for(int i = 0; i < days.length; i++) days[i] = String.format("%02d일", i + 1);
        panel.add(new JComboBox<>(years));
        panel.add(new JComboBox<>(months));
        panel.add(new JComboBox<>(days));
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
