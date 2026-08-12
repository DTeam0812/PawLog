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

import org.w3c.dom.Text;




public class Login extends JFrame{
    private static final Color BACKGROUND = new Color(247, 244, 239);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color PRIMARY = new Color(255, 159, 67);
    private static final Color TEXT = new Color(45, 52, 54);
    private static final Color SUB_TEXT = new Color(99, 110, 114);
    private static final Color BORDER = new Color(225, 225, 225);
    private Point headerDragOffset;
    public Login() {
        setSize(590, 620);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(topPanel(), BorderLayout.NORTH);
        add(loginPanel(), BorderLayout.CENTER);

        setVisible(true);
    }
    private JPanel topPanel(){
        JPanel headerpanel = new JPanel(new BorderLayout());
        headerpanel.setPreferredSize(new Dimension(0, 50));
        headerpanel.setBackground(new Color(255, 159, 67));
        headerpanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        JLabel titleLabel = new JLabel("Pawlog");
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(231, 76, 60));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setPreferredSize(new Dimension(38, 38));
        closeButton.setMargin(new Insets(0, 0, 2, 0));
        closeButton.addActionListener(event -> setVisible(false));
        titleLabel.setText("🐾 Pawlog");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        JPanel closeButtonPanel = new JPanel(new GridBagLayout());
        closeButtonPanel.setOpaque(false);
        closeButtonPanel.add(closeButton);

        headerpanel.add(titleLabel, BorderLayout.WEST);
        headerpanel.add(closeButtonPanel, BorderLayout.EAST);
        addHeaderDrag(headerpanel);
        addHeaderDrag(titleLabel);
        headerpanel.setPreferredSize(new Dimension(0, 55));
        
        return headerpanel;
    }
    private JPanel loginPanel(){
        JPanel loginform = new JPanel();
        loginform.setLayout(new BoxLayout(loginform, BoxLayout.Y_AXIS));
        loginform.setBackground(BACKGROUND);
        loginform.setBorder(
            BorderFactory.createEmptyBorder(70, 38, 30, 38)
        );
        
    
        JLabel emailLabel = new JLabel("이메일");
        JTextField emailField = new JTextField();

        JLabel passwordLabel = new JLabel("비밀번호");
        JPasswordField passwordField = new JPasswordField();
        
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

   

        emailField.setPreferredSize(new Dimension(500, 44));
        emailField.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 44)
        );

        passwordField.setPreferredSize(new Dimension(500, 44));
        passwordField.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 44)
        );
        JButton loginButton = new JButton("로그인");

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


        JLabel warn = new JLabel("");
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
        loginform.add(Box.createVerticalStrut(5));
        
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

     private void addHeaderDrag(Component component) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                headerDragOffset = event.getLocationOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (headerDragOffset == null) {
                    return;
                }

                Point currentPoint = event.getLocationOnScreen();

                int movedX = currentPoint.x - headerDragOffset.x;
                int movedY = currentPoint.y - headerDragOffset.y;

                setLocation(getX() + movedX, getY() + movedY);
                headerDragOffset = currentPoint;
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                headerDragOffset = null;
            }
        };
    }
    public static void main(String[] args) {
        new Login();
    


    }
}
