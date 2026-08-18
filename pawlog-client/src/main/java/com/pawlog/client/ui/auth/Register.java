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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.time.Year;
import javax.swing.JComboBox;

public class Register extends JPanel{
    private JComboBox<String> yearBox;
    private JComboBox<String> monthBox;
    private JComboBox<String> dayBox;

    private JRadioButton maleButton;
    private JRadioButton femaleButton;
    private final PawLogClient pawLogClient = new PawLogClient();

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

        yearBox = new JComboBox<>(years);
        monthBox = new JComboBox<>(months);
        dayBox = new JComboBox<>(days);

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
            add(registerPanel(showLogin), BorderLayout.CENTER);
        }

        private JPanel registerPanel(Runnable showLogin) {
        JPanel registerform = new JPanel();
        registerform.setLayout(new BoxLayout(registerform,BoxLayout.Y_AXIS));
        registerform.setBackground(BACKGROUND);
        registerform.setBorder(BorderFactory.createEmptyBorder(25,38,20,38));
        JLabel title = new JLabel("회원가입");
        JPanel birthSelectPanel = BirthPanel();
        JPanel genderSelectPanel = GenderPanel();
        JLabel emailLabel = new JLabel("이메일");
        JTextField emailField = new JTextField();
        JLabel nicknameLabel = new JLabel("닉네임");
        JTextField nicknameField = new JTextField();
        JLabel passwordLabel = new JLabel("비밀번호");
        JPasswordField passwordField = new JPasswordField();
        nicknameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nicknameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nicknameField.setPreferredSize(new Dimension(500, 40));
        nicknameField.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 40)
        );
        JLabel ChpasswordLabel = new JLabel("비밀번호 확인");
        JPasswordField ChpasswordField = new JPasswordField();
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        ChpasswordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ChpasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        passwordField.setMaximumSize( new Dimension(Integer.MAX_VALUE, 40));

        ChpasswordField.setMaximumSize( new Dimension(Integer.MAX_VALUE, 40));
        emailField.setPreferredSize(new Dimension(500, 40));
        passwordField.setPreferredSize(new Dimension(500, 40));
        ChpasswordField.setPreferredSize(new Dimension(500, 40));
        Font labelFont = new Font("맑은 고딕", Font.BOLD, 14);
        nicknameLabel.setFont(labelFont);
        nicknameLabel.setForeground(TEXT);
        emailLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        ChpasswordLabel.setFont(labelFont);
        JLabel birthLabel = new JLabel("생년월일");
        JLabel genderLabel = new JLabel("성별");

        birthLabel.setFont(labelFont);
        genderLabel.setFont(labelFont);

        birthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton registerButton = new JButton("회원가입");
        registerButton.setFont(new Font("맑은 고딕", Font.BOLD, 16) );
        registerButton.setForeground(CARD);
        registerButton.setBackground(PRIMARY);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.setMaximumSize( new Dimension(Integer.MAX_VALUE, 49) );  
        registerButton.addActionListener(event -> {
            System.out.println("회원가입 버튼 클릭됨");

            String nickname = nicknameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String checkPassword = new String(ChpasswordField.getPassword());

            if (nickname.isEmpty() || email.isEmpty() || password.isEmpty() || checkPassword.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "모든 항목을 입력해 주세요.",
                    "입력 확인",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!email.contains("@")) { JOptionPane.showMessageDialog(
                    this,
                    "이메일 형식을 확인해 주세요.",
                    "입력 확인",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!password.equals(checkPassword)) { JOptionPane.showMessageDialog(
                    this,
                    "비밀번호가 서로 일치하지 않습니다.",
                    "입력 확인",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                int year = Year.now().getValue() - yearBox.getSelectedIndex();
                int month = monthBox.getSelectedIndex() + 1;
                int day = dayBox.getSelectedIndex() + 1;
                LocalDate birthDate = LocalDate.of(year, month, day);
                String gender = maleButton.isSelected() ? "M" : "F";

                PawLogClient.Response response = pawLogClient.register(
                    email,
                    password,
                    nickname,
                    gender,
                    birthDate
                );

                if (!response.success()) {
                    JOptionPane.showMessageDialog(
                        this,
                        response.message(),
                        "회원가입 실패",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                JOptionPane.showMessageDialog(
                    this,
                    "회원가입이 완료되었습니다.",
                    "회원가입 성공",
                    JOptionPane.INFORMATION_MESSAGE
                );
                showLogin.run();
            } catch (DateTimeException exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "올바른 생년월일을 선택해 주세요.",
                    "입력 확인",
                    JOptionPane.WARNING_MESSAGE
                );
            } catch (Exception exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    "서버 오류: " + exception.getMessage(),
                    "회원가입 실패",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        JButton backButton = new JButton("로그인으로 돌아가기");
        backButton.setForeground(PRIMARY);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
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

        registerform.add(emailLabel);
        registerform.add(Box.createVerticalStrut(5));
        registerform.add(emailField);
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



        return registerform;
    }




}
