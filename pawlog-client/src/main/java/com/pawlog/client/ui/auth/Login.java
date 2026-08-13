package com.pawlog.client.ui.auth;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Year;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.pawlog.client.network.PawLogClient;

import org.w3c.dom.Text;




public class Login extends JPanel{
    private static final Color BACKGROUND = new Color(247, 244, 239);

    private static final Color PRIMARY = new Color(255, 159, 67);
    private static final Color TEXT = new Color(45, 52, 54);
    private final PawLogClient pawLogClient = new PawLogClient();
    
    public Login(Runnable showRegister , Runnable loginsuccess) {
        setLayout(new BorderLayout());
        add(loginPanel(showRegister , loginsuccess), BorderLayout.CENTER);
    }

    private JPanel loginPanel(Runnable showRegister , Runnable loginsuccess){
        JPanel loginform = new JPanel();
        loginform.setLayout(new BoxLayout(loginform, BoxLayout.Y_AXIS));
        loginform.setBackground(BACKGROUND);
        loginform.setBorder(
            BorderFactory.createEmptyBorder(70, 38, 30, 38)
        );
        JLabel nickname = new JLabel("닉네임");
        JTextField nicknamefield = new JTextField();
    
        JLabel emailLabel = new JLabel("이메일");
        JTextField emailField = new JTextField();

        JLabel passwordLabel = new JLabel("비밀번호");
        JPasswordField passwordField = new JPasswordField();
        nickname.setAlignmentX(Component.LEFT_ALIGNMENT);
        nicknamefield.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nicknamefield.setPreferredSize(new Dimension(500, 44));
        nicknamefield.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 44)
        );
        

        emailField.setPreferredSize(new Dimension(500, 44));
        emailField.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 44)
        );

        passwordField.setPreferredSize(new Dimension(500, 44));
        passwordField.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 44)
        );
        JLabel warn = new JLabel("");
        JButton loginButton = new JButton("로그인");

        loginButton.addActionListener(event -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                warn.setText("이메일과 비밀번호를 입력해 주세요.");
                return;
            }

            try {
                PawLogClient.Response response = pawLogClient.login(email, password);

                if (response.success()) {
                    warn.setText("");
                    loginsuccess.run();
                } else {
                    warn.setText(response.message());
                }
            } catch (Exception exception) {
                warn.setText("서버에 연결할 수 없습니다.");
            }
        });


        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(PRIMARY);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginButton.setPreferredSize(new Dimension(500, 49));
        loginButton.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 49)
        );
        JButton register = new JButton("회원가입");
        register.addActionListener(event -> {
            showRegister.run();
        });

        register.setAlignmentX(Component.LEFT_ALIGNMENT);
        register.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        register.setForeground(Color.WHITE);
        register.setBackground(PRIMARY);
        register.setFocusPainted(false);
        register.setBorderPainted(false);
        register.setAlignmentX(Component.LEFT_ALIGNMENT);
        register.setPreferredSize(new Dimension(500, 49));
        register.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 49)
        );


        JLabel title = new JLabel("🐾 Pawlog");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel titlePanel = new JPanel(new GridBagLayout());
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 50)
        );

        titlePanel.add(title);
        Font labelFont = new Font("맑은 고딕", Font.BOLD, 14);

        emailLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        emailLabel.setForeground(TEXT);
        passwordLabel.setForeground(TEXT);
      
        
        loginform.add(titlePanel);
        
        loginform.add(Box.createVerticalStrut(28));
        loginform.add(emailLabel);
        loginform.add(Box.createVerticalStrut(7));
        loginform.add(emailField);

        loginform.add(Box.createVerticalStrut(18));

        loginform.add(passwordLabel);
        loginform.add(Box.createVerticalStrut(7));
        loginform.add(passwordField);
        loginform.add(Box.createVerticalStrut(7));
        loginform.add(warn);
        loginform.add(Box.createVerticalStrut(50));
        loginform.add(loginButton);
        loginform.add(Box.createVerticalStrut(30));
        loginform.add(register);
      
        return loginform;
    }

    
    
}
