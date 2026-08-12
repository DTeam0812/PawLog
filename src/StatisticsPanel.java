import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatisticsPanel extends JPanel {
    private final CardLayout periodCardLayout = new CardLayout();
    private final JPanel periodContentPanel = new JPanel(periodCardLayout);
    private static final Color BACKGROUND = new Color(247, 244, 239);
    private static final Color PRIMARY = new Color(255, 159, 67);
    private static final Color TEXT = new Color(45, 52, 54);
    private static final Color SUB_TEXT = new Color(99, 110, 114);
    private static final Color BORDER = new Color(225, 225, 225);

    public StatisticsPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(createTopPanel(), BorderLayout.NORTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setPreferredSize(new Dimension(0, 29));

        topPanel.add(createTitlePanel(), BorderLayout.WEST);
        topPanel.add(createPeriodButtonPanel(), BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("사용 통계");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setForeground(TEXT);
        titlePanel.add(titleLabel);
        return titlePanel;
    }

    private JPanel createPeriodButtonPanel() {
        JPanel buttonPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton dailyButton = createPeriodButton("일간", true);
        JButton weeklyButton = createPeriodButton("주간", false);
        JButton monthlyButton = createPeriodButton("월간", false);

        buttonPanel.add(dailyButton);
        buttonPanel.add(weeklyButton);
        buttonPanel.add(monthlyButton);

        return buttonPanel;
    }

    private JButton createPeriodButton(String text, boolean selected) {
        JButton button = new JButton(text);

        button.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(56, 28));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusPainted(false);

        if (selected) {
            button.setForeground(Color.WHITE);
            button.setBackground(PRIMARY);
            button.setBorder(BorderFactory.createLineBorder(PRIMARY));
        } else {
            button.setForeground(TEXT);
            button.setBackground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(BORDER));
        }

        return button;
    }
}