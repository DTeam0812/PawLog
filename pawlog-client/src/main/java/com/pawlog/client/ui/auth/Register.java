package com.pawlog.client.ui.auth;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;

import com.pawlog.client.network.PawLogClient;
import com.pawlog.client.ui.common.PawLogDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.time.Year;
import javax.swing.JComboBox;

public class Register extends JPanel{
    private JComboBox<String> yearBox;
    private JComboBox<String> monthBox;
    private JComboBox<String> dayBox;

    private JRadioButton maleButton;
    private JRadioButton femaleButton;
    private final PawLogClient pawLogClient = PawLogClient.getInstance();

    private JPanel BirthPanel (){
        
        JPanel birthPanel = new JPanel( new GridLayout(1, 3, 10, 0));
        birthPanel.setMaximumSize( new Dimension(Integer.MAX_VALUE, 40));

        birthPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] years = new String[100];

        int currentYear = Year.now().getValue();

        String[] months = new String[12];
        String[] days = new String[31];

        for (int i = 0; i < months.length; i++) {
            months[i] = String.format("%02d월", i + 1);
        }

        for (int i = 0; i < days.length; i++) {
            days[i] = String.format("%02d일", i + 1);
        }
        for (int i = 0; i < years.length; i++) {
            years[i] = (currentYear - i) + "년";
        }

        yearBox = combo(years);
        monthBox = combo(months);
        dayBox = combo(days);

        birthPanel.setOpaque(false);

        birthPanel.add(yearBox);
        birthPanel.add(monthBox);
        birthPanel.add(dayBox);
        return birthPanel;
    }

    private JPanel GenderPanel(){
        JPanel genderPanel = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
        genderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genderPanel.setMaximumSize( new Dimension(Integer.MAX_VALUE, 30));
        genderPanel.setOpaque(false);
        
        maleButton = new JRadioButton("남성");
        femaleButton = new JRadioButton("여성");
        ButtonGroup genderGroup = new ButtonGroup();
       
        genderPanel.add(maleButton);
        genderPanel.add(Box.createHorizontalStrut(15));
        genderPanel.add(femaleButton);
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);
        maleButton.setSelected(true);
        maleButton.setOpaque(false);
        femaleButton.setOpaque(false);

        return genderPanel;
    }


        public Register(Runnable showLogin) {
            setLayout(new BorderLayout());
            setBackground(BACKGROUND);
            add(registerPanel(showLogin), BorderLayout.CENTER);
        }

        private JPanel registerPanel(Runnable showLogin) {
        JPanel registerform = card();
        registerform.setLayout(new BoxLayout(registerform,BoxLayout.Y_AXIS));
        registerform.setBorder(BorderFactory.createEmptyBorder(26,42,22,42));
        registerform.setPreferredSize(new Dimension(560, 665));
        JLabel title = new JLabel("회원가입");
        JPanel birthSelectPanel = BirthPanel();
        JPanel genderSelectPanel = GenderPanel();
        JLabel idLabel = new JLabel("아이디");
        JTextField idField = textField("아이디를 입력하세요");
        JLabel nicknameLabel = new JLabel("닉네임");
        JTextField nicknameField = textField("닉네임을 입력하세요");
        JLabel passwordLabel = new JLabel("비밀번호");
        JPasswordField passwordField = passwordField("비밀번호를 입력하세요");
        nicknameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nicknameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nicknameField.setPreferredSize(new Dimension(500, 40));
        nicknameField.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 40)
        );
        JLabel ChpasswordLabel = new JLabel("비밀번호 확인");
        JPasswordField ChpasswordField = passwordField("비밀번호를 다시 입력하세요");
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        idField.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        ChpasswordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ChpasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        idField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        passwordField.setMaximumSize( new Dimension(Integer.MAX_VALUE, 40));

        ChpasswordField.setMaximumSize( new Dimension(Integer.MAX_VALUE, 40));
        idField.setPreferredSize(new Dimension(500, 40));
        passwordField.setPreferredSize(new Dimension(500, 40));
        ChpasswordField.setPreferredSize(new Dimension(500, 40));
        Font labelFont = new Font("맑은 고딕", Font.BOLD, 14);
        nicknameLabel.setFont(labelFont);
        nicknameLabel.setForeground(TEXT);
        idLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        ChpasswordLabel.setFont(labelFont);
        JLabel birthLabel = new JLabel("생년월일");
        JLabel genderLabel = new JLabel("성별");

        birthLabel.setFont(labelFont);
        genderLabel.setFont(labelFont);

        birthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton registerButton = primaryButton("회원가입");
        registerButton.setFont(new Font("맑은 고딕", Font.BOLD, 16) );
        registerButton.setForeground(CARD);
        registerButton.setFocusPainted(false);
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.setMaximumSize( new Dimension(Integer.MAX_VALUE, 49) );  
        registerButton.addActionListener(event -> {
            System.out.println("회원가입 버튼 클릭됨");

            String nickname = nicknameField.getText().trim();
            String id = idField.getText().trim();
            String password = new String(passwordField.getPassword());
            String checkPassword = new String(ChpasswordField.getPassword());

            if (nickname.isEmpty() || id.isEmpty() || password.isEmpty() || checkPassword.isEmpty()) {
                PawLogDialog.warning(this, "입력 확인", "모든 항목을 입력해 주세요.");
                return;
            }

            if (!id.matches("[A-Za-z0-9_]{4,20}")) {
                PawLogDialog.warning(this, "입력 확인", "아이디는 영문, 숫자, 밑줄을 사용하여 4~20자로 입력해 주세요.");
                return;
            }

            if (!password.equals(checkPassword)) {
                PawLogDialog.warning(this, "입력 확인", "비밀번호가 서로 일치하지 않습니다.");
                return;
            }

            try {
                int year = Year.now().getValue() - yearBox.getSelectedIndex();
                int month = monthBox.getSelectedIndex() + 1;
                int day = dayBox.getSelectedIndex() + 1;
                LocalDate birthDate = LocalDate.of(year, month, day);
                String gender = maleButton.isSelected() ? "M" : "F";

                PawLogClient.Response response = pawLogClient.register(
                    id,
                    password,
                    nickname,
                    gender,
                    birthDate
                );

                if (!response.success()) {
                    PawLogDialog.warning(this, "회원가입 실패", response.message());
                    return;
                }

                PawLogDialog.info(this, "회원가입 성공", "회원가입이 완료되었습니다.");
                showLogin.run();
            } catch (DateTimeException exception) {
                PawLogDialog.warning(this, "입력 확인", "올바른 생년월일을 선택해 주세요.");
            } catch (Exception exception) {
                exception.printStackTrace();
                PawLogDialog.error(this, "회원가입 실패", "서버 오류: " + exception.getMessage());
            }
        });
        JButton backButton = outlineButton("로그인으로 돌아가기");
        backButton.setPreferredSize(new Dimension(180, 38));
        backButton.addActionListener(event -> { showLogin.run(); });

        JPanel backPanel = new JPanel(new GridBagLayout());
        backPanel.setOpaque(false);
        backPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        backPanel.setMaximumSize( new Dimension(Integer.MAX_VALUE, 40) );
        backPanel.add(backButton);
            title.setFont(
    new Font("맑은 고딕", Font.BOLD, 30)
);
        title.setForeground(PRIMARY);

        JPanel titlePanel = new JPanel(new GridBagLayout());
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 45)
        );
        titlePanel.add(title);
        registerform.add(titlePanel);
        registerform.add(Box.createVerticalStrut(25));

        registerform.add(nicknameLabel);
        registerform.add(Box.createVerticalStrut(5));
        registerform.add(nicknameField);
        registerform.add(Box.createVerticalStrut(12));

        registerform.add(idLabel);
        registerform.add(Box.createVerticalStrut(5));
        registerform.add(idField);
        registerform.add(Box.createVerticalStrut(12));

        registerform.add(passwordLabel);
        registerform.add(Box.createVerticalStrut(5));
        registerform.add(passwordField);
        registerform.add(Box.createVerticalStrut(12));

        registerform.add(ChpasswordLabel);
        registerform.add(Box.createVerticalStrut(5));
        registerform.add(ChpasswordField);
        registerform.add(Box.createVerticalStrut(12));

        registerform.add(birthLabel);
        registerform.add(Box.createVerticalStrut(5));
        registerform.add(birthSelectPanel);
        registerform.add(Box.createVerticalStrut(12));

        registerform.add(genderLabel);
        registerform.add(Box.createVerticalStrut(5));
        registerform.add(genderSelectPanel);
        registerform.add(Box.createVerticalStrut(20));

        registerform.add(registerButton);
        registerform.add(Box.createVerticalStrut(8));
        registerform.add(backPanel);



        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BACKGROUND);
        page.add(registerform);
        return page;
    }




}
