package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class HomePanel extends JPanel {

    private static final Color BACKGROUND = new Color(247, 244, 239);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color PRIMARY = new Color(255, 159, 67);
    private static final Color TEXT = new Color(45, 52, 54);
    private static final Color SUB_TEXT = new Color(99, 110, 114);
    private static final Color BORDER = new Color(225, 225, 225);
    
    private final JLabel totalTimeValue = new JLabel("4시간 20분");//임시1
    private final JLabel currentProgramValue = new JLabel("VS Code");//임시2
    private final JLabel topProgramValue = new JLabel("Chrome");

    public HomePanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createBodyPanel(), BorderLayout.CENTER);
        
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        return topPanel;
    }

    private JPanel createBodyPanel() {
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 20));
        bodyPanel.setOpaque(false);

        bodyPanel.add(createSummaryPanel(), BorderLayout.NORTH);
        bodyPanel.add(createUsagePanel(), BorderLayout.CENTER);

        return bodyPanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);//기존 사용안함
        summaryPanel.setPreferredSize(new Dimension(0, 120));//120까지 유예?

        summaryPanel.add(createSummaryCard("오늘 사용 시간", totalTimeValue));
        summaryPanel.add(createSummaryCard("현재 프로그램", currentProgramValue));
        summaryPanel.add(createSummaryCard("가장 많이 사용", topProgramValue));

        return summaryPanel;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        JPanel cardPanel = new JPanel(new GridLayout(2, 1));
        cardPanel.setBackground(CARD_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(15, 18, 15, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        titleLabel.setForeground(SUB_TEXT);

        valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        valueLabel.setForeground(TEXT);

        cardPanel.add(titleLabel);
        cardPanel.add(valueLabel);

        return cardPanel;
    }

    private JPanel createUsagePanel() {
        JPanel usagePanel = new JPanel();
        usagePanel.setLayout(new BoxLayout(usagePanel, BoxLayout.Y_AXIS));
        usagePanel.setBackground(CARD_BACKGROUND);
        usagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel titleLabel = new JLabel("오늘의 프로그램 사용량");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        usagePanel.add(titleLabel);
        usagePanel.add(Box.createVerticalStrut(18));
        usagePanel.add(createUsageRow("VS Code", 50, "2시간 10분"));
        usagePanel.add(Box.createVerticalStrut(15));
        usagePanel.add(createUsageRow("Chrome", 35, "1시간 30분"));
        usagePanel.add(Box.createVerticalStrut(15));
        usagePanel.add(createUsageRow("Discord", 15, "40분"));

        return usagePanel;
    }

    private JPanel createUsageRow(String programName, int percent, String time) {
        JPanel rowPanel = new JPanel(new BorderLayout(15, 0));
        rowPanel.setOpaque(false);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        rowPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(programName);
        nameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        nameLabel.setForeground(TEXT);
        nameLabel.setPreferredSize(new Dimension(100, 30));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(percent);
        progressBar.setForeground(PRIMARY);
        progressBar.setBackground(new Color(235, 235, 235));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 14));

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        progressPanel.add(progressBar, BorderLayout.CENTER);

        JLabel timeLabel = new JLabel(time, SwingConstants.RIGHT);
        timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        timeLabel.setForeground(SUB_TEXT);
        timeLabel.setPreferredSize(new Dimension(90, 30));

        rowPanel.add(nameLabel, BorderLayout.WEST);
        rowPanel.add(progressPanel, BorderLayout.CENTER);
        rowPanel.add(timeLabel, BorderLayout.EAST);

        return rowPanel;
    }

    private JPanel createGoalPanel() {
        JPanel goalPanel = new JPanel(new BorderLayout(15, 8));
        goalPanel.setBackground(CARD_BACKGROUND);
        goalPanel.setPreferredSize(new Dimension(0, 85));
        goalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(13, 20, 13, 20)
        ));


      

       

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        goalPanel.add(titlePanel, BorderLayout.NORTH);
        

        return goalPanel;
    }

    public void updateSummary(
            String totalTime,
            String currentProgram,
            String topProgram
    ) {
        totalTimeValue.setText(totalTime);
        currentProgramValue.setText(currentProgram);
        topProgramValue.setText(topProgram);
    }
}