package com.pawlog.client.ui.auth;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.*;
import java.net.URL;

import javax.swing.*;

import com.pawlog.client.AuthStore.AuthStore;
import com.pawlog.client.network.PawLogClient;
import com.pawlog.common.dto.auth.AuthLoginDto;




public class Login extends JPanel{
    private final PawLogClient pawLogClient = PawLogClient.getInstance();
    AuthStore authStore = AuthStore.getInstance();
    public Login(Runnable showRegister , Runnable loginsuccess) {
        setLayout(new BorderLayout());
        add(loginPanel(showRegister , loginsuccess), BorderLayout.CENTER);
    }

    private JPanel loginPanel(Runnable showRegister , Runnable loginsuccess){
        JPanel loginform = new JPanel();
        loginform.setLayout(new BoxLayout(loginform, BoxLayout.Y_AXIS));
        loginform.setOpaque(false);
        loginform.setPreferredSize(new Dimension(460, 525));
        JLabel idLabel = new JLabel("아이디");
        JTextField idField = textField("아이디를 입력하세요");

        JLabel passwordLabel = new JLabel("비밀번호");
        JPasswordField passwordField = passwordField("비밀번호를 입력하세요");
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        idField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        idField.setPreferredSize(new Dimension(460, 46));
        idField.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 44)
        );

        passwordField.setPreferredSize(new Dimension(460, 46));
        passwordField.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 44)
        );
        JLabel warn = new JLabel(" ");
        warn.setFont(font(Font.PLAIN, 12));
        warn.setForeground(RED);
        warn.setAlignmentX(Component.LEFT_ALIGNMENT);
        warn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JButton loginButton = primaryButton("로그인");

        loginButton.addActionListener(event -> {
            String id = idField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (id.isEmpty() || password.isEmpty()) {
                warn.setText("아이디와 비밀번호를 입력해 주세요.");
                return;
            }

            AuthLoginDto.Response response;

            try {
                response = pawLogClient.login(id, password);
            } catch (Exception exception) {
                warn.setText("서버에 연결할 수 없습니다.");
                exception.printStackTrace();
                return;
            }

            if (response.isSuccess() && response.getUser() != null) {
                warn.setText(" ");
                authStore.login(
                    response.getUser().getUserId(),
                    response.getUser().getId(),
                    response.getUser().getNickname(),
                    response.getUser().getBirthDate()
                );
                loginsuccess.run();
            } else {
                warn.setText(response.getMessage());
            }
        });

        loginButton.setFont(font(Font.BOLD, 15));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setPreferredSize(new Dimension(460, 50));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton register = outlineButton("회원가입");
        register.addActionListener(event -> {
            showRegister.run();
        });

        register.setAlignmentX(Component.LEFT_ALIGNMENT);
        register.setFont(font(Font.BOLD, 15));
        register.setPreferredSize(new Dimension(460, 50));
        register.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

//----------------원본-----------------------
//        JLabel title = new JLabel("🐾 Pawlog");
//        title.setFont(new Font("맑은 고딕", Font.BOLD, 32));
//        title.setForeground(PRIMARY);
//        title.setAlignmentX(Component.CENTER_ALIGNMENT);
//        JPanel titlePanel = new JPanel(new GridBagLayout());
//        titlePanel.setOpaque(false);
//        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        titlePanel.setMaximumSize(
//            new Dimension(Integer.MAX_VALUE, 50)
//        );
//
//        titlePanel.add(title);
//---------------------------------------


//----------------수정본-----------------------
        JLabel title;
        ImageIcon icon = null;
        URL imgUrl = getClass().getResource("/images/pawIcon_primary.png");
        if (imgUrl == null) {
            title = new JLabel("🐾 Pawlog");
        }else{
            Image img = new ImageIcon(imgUrl).getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
            title = new JLabel("Pawlog", icon, SwingConstants.CENTER);
            title.setIconTextGap(10); // 이미지와 텍스트 사이 간격
        }
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
//---------------------------------------

        JLabel subtitle = new JLabel("프로그램 사용 시간을 기록하고 더 나은 습관을 만들어보세요");
        subtitle.setFont(font(Font.PLAIN, 13));
        subtitle.setForeground(SUB_TEXT);
        JPanel subtitlePanel = new JPanel(new GridBagLayout());
        subtitlePanel.setOpaque(false);
        subtitlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        subtitlePanel.add(subtitle);

        Font labelFont = new Font("맑은 고딕", Font.BOLD, 14);

        idLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        idLabel.setForeground(TEXT);
        passwordLabel.setForeground(TEXT);
      
        
        loginform.add(titlePanel);
        loginform.add(Box.createVerticalStrut(10));
        loginform.add(subtitlePanel);
        loginform.add(Box.createVerticalStrut(34));
        loginform.add(idLabel);
        loginform.add(Box.createVerticalStrut(7));
        loginform.add(idField);

        loginform.add(Box.createVerticalStrut(18));

        loginform.add(passwordLabel);
        loginform.add(Box.createVerticalStrut(7));
        loginform.add(passwordField);
        loginform.add(Box.createVerticalStrut(7));
        loginform.add(warn);
        loginform.add(Box.createVerticalStrut(26));
        loginform.add(loginButton);
        loginform.add(Box.createVerticalStrut(14));
        loginform.add(register);
      
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BACKGROUND);
        page.add(loginform);
        return page;
    }

    
    
}
