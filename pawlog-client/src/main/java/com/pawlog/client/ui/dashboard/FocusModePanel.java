package com.pawlog.client.ui.dashboard;

import static com.pawlog.client.ui.dashboard.UiComponents.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.Timer;
import com.pawlog.client.network.PawLogClient;
import com.pawlog.client.tracker.ProgramTracker;
import com.pawlog.client.ui.common.PawLogDialog;
import com.pawlog.client.ui.dashboard.UiComponents.RoundedPanel;
import com.pawlog.client.ui.widget.WidgetAlertService;
import com.pawlog.common.dto.board.ProgramSearchDto;

public class FocusModePanel extends JPanel {
    private Timer focusTimer;
    private int remainingSeconds;
    private int totalFocusSeconds;
    private boolean timerRunning;
    private final PawLogClient pawLogClient = PawLogClient.getInstance();
    private final JTextField durationField = textField("예: 50");
    private final JTextField programSearchField = textField("프로그램 이름을 검색하세요");
    private final JPanel programSearchResults = transparentPanel(new GridLayout(0, 2, 6, 6));
    private final JLabel selectedProgramLabel = label("선택된 프로그램이 없어요.", Font.PLAIN, 11, SUB_TEXT);
    private final JLabel focusProgramLabel = label("프로그램을 선택해주세요", Font.BOLD, 29, PRIMARY);
    private final JLabel timerLabel = label("00:00", Font.BOLD, 54, TEXT);
    private final JLabel deviationCountLabel = label("0회", Font.BOLD, 20, PRIMARY);
    private final JLabel deviationTimeLabel = label("00:00", Font.BOLD, 20, PRIMARY);
    private final DogProgressBar focusProgressBar = new DogProgressBar();
    private final JButton startButton = primaryButton("집중 시작");
    private final JButton pauseButton = outlineButton("일시정지");
    private final JButton stopButton = outlineButton("중단하기");
    private String selectedProcessName;
    private boolean outsideTargetProgram;
    private boolean deviationCounted;
    private int deviationCount;
    private int deviationSeconds;
    private long deviationStartedAt;
    private long selectedProgramId = -1;
    private boolean timerPaused;

    public FocusModePanel() {
        configureDurationInput();
        configureProgramSearch();
        alertCheck.addActionListener(event -> {
            if(alertCheck.isSelected() && outsideTargetProgram && deviationCounted && timerRunning && !timerPaused) {
                WidgetAlertService.startBarking();
            } else {
                WidgetAlertService.stopBarking();
            }
        });

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 30, 32));

        JPanel content = transparentPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel heading = titlePanel("집중 모드" , "" );
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(heading);
        content.add(Box.createVerticalStrut(20));
        content.add(createSessionCard());
        content.add(Box.createVerticalStrut(16));
    

        add(content, BorderLayout.CENTER);
    }

    private JPanel createSessionCard() {
        RoundedPanel card = card();
        card.setLayout(new BorderLayout(28, 0));
        card.setBorder(BorderFactory.createEmptyBorder(25, 28, 25, 28));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 540));
        card.setPreferredSize(new Dimension(850, 540));
        card.add(createTimerArea(), BorderLayout.CENTER);
        card.add(createSessionSettings(), BorderLayout.EAST);
        return card;
    }

    private JPanel createTimerArea() {
        JPanel timerArea = transparentPanel();
        timerArea.setLayout(new BoxLayout(timerArea, BoxLayout.Y_AXIS));

        JLabel badge = label("●  집중할 프로그램", Font.BOLD, 12, SUB_TEXT);
        JLabel description = label("시간과 프로그램을 선택하면 시작할 수 있어요.", Font.PLAIN, 13, SUB_TEXT);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        focusProgramLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        focusProgressBar.setValue(0);
        focusProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 102));
        focusProgressBar.setPreferredSize(new Dimension(420, 102));
        focusProgressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel status = createDeviationStatusPanel();
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
       

        timerArea.add(badge);
        timerArea.add(Box.createVerticalStrut(10));
        timerArea.add(focusProgramLabel);
        timerArea.add(Box.createVerticalStrut(14));
        timerArea.add(timerLabel);
        timerArea.add(Box.createVerticalStrut(5));
        timerArea.add(description);
        timerArea.add(Box.createVerticalStrut(24));
        timerArea.add(focusProgressBar);
        timerArea.add(Box.createVerticalStrut(18));
        timerArea.add(status);
        return timerArea;
    }

    private JPanel createDeviationStatusPanel() {
        JPanel status = transparentPanel(new GridLayout(1, 2, 10, 0));
        status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        status.setPreferredSize(new Dimension(420, 72));
        status.add(createDeviationStatusCard("딴짓 횟수", deviationCountLabel));
        status.add(createDeviationStatusCard("딴짓 시간", deviationTimeLabel));
        return status;
    }

    private JPanel createDeviationStatusCard(String title, JLabel valueLabel) {
        RoundedPanel statusCard = new RoundedPanel(18, PRIMARY_SOFT);
        statusCard.setLayout(new BorderLayout(8, 0));
        statusCard.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        statusCard.add(label(title, Font.BOLD, 11, SUB_TEXT), BorderLayout.WEST);
        statusCard.add(valueLabel, BorderLayout.EAST);
        return statusCard;
    }

    private JPanel createSessionSettings() {
        RoundedPanel settings = card();
        settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
        settings.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        settings.setPreferredSize(new Dimension(410, 490));

        
        JLabel timeTitle = label("집중할 시간", Font.BOLD, 12, SUB_TEXT);
        JLabel programTitle = label("집중할 프로그램", Font.BOLD, 12, SUB_TEXT);
        
        timeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        programTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel durationRow = transparentPanel(new BorderLayout(8, 0));
        durationRow.setMinimumSize(new Dimension(350, 46));
        durationRow.setPreferredSize(new Dimension(350, 46));
        durationRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        durationRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        durationField.setHorizontalAlignment(JTextField.CENTER);
        durationRow.add(durationField, BorderLayout.CENTER);
        durationRow.add(label("분", Font.BOLD, 12, SUB_TEXT), BorderLayout.EAST);

        programSearchField.setMinimumSize(new Dimension(350, 46));
        programSearchField.setPreferredSize(new Dimension(350, 46));
        programSearchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        programSearchField.setHorizontalAlignment(JTextField.CENTER);
        programSearchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        programSearchResults.setMinimumSize(new Dimension(350, 70));
        programSearchResults.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        programSearchResults.setPreferredSize(new Dimension(350, 70));
        programSearchResults.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedProgramLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        alertCheck.setFont(font(Font.PLAIN, 11));
        alertCheck.setForeground(SUB_TEXT);
        alertCheck.setOpaque(false);
        alertCheck.setFocusPainted(false);
        alertCheck.setAlignmentX(Component.LEFT_ALIGNMENT);

        startButton.setFont(font(Font.BOLD, 15));
        pauseButton.setFont(font(Font.BOLD, 14));
        stopButton.setFont(font(Font.BOLD, 14));
        stopButton.setForeground(RED);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        JPanel timerButtonRow = transparentPanel(new GridLayout(1, 3, 8, 0));
        timerButtonRow.setMinimumSize(new Dimension(350, 52));
        timerButtonRow.setPreferredSize(new Dimension(350, 52));
        timerButtonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        timerButtonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerButtonRow.add(startButton);
        timerButtonRow.add(pauseButton);
        timerButtonRow.add(stopButton);

        startButton.addActionListener(event -> {
            if(timerRunning) {
                return;
            }

            if(selectedProgramId <= 0) {
                selectedProgramLabel.setText("집중할 프로그램을 선택해주세요.");
                selectedProgramLabel.setForeground(RED);
                return;
            }

            startFocusTimer();
        });

        pauseButton.addActionListener(event -> toggleFocusTimerPause());
        stopButton.addActionListener(event -> stopFocusTimer());
        
        settings.add(Box.createVerticalStrut(12));
        settings.add(timeTitle);
        settings.add(Box.createVerticalStrut(12));
        settings.add(durationRow);
        settings.add(Box.createVerticalStrut(15));
        settings.add(programTitle);
        settings.add(Box.createVerticalStrut(5));
        settings.add(programSearchField);
        settings.add(Box.createVerticalStrut(5));
        settings.add(programSearchResults);
        settings.add(Box.createVerticalStrut(30));
        settings.add(selectedProgramLabel);
        settings.add(Box.createVerticalStrut(7));
        settings.add(alertCheck);
        settings.add(Box.createVerticalGlue());
        settings.add(timerButtonRow);
        return settings;
    }

   
    private void configureDurationInput() {
        ((AbstractDocument)durationField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes) throws BadLocationException {
                String current = bypass.getDocument().getText(0, bypass.getDocument().getLength());
                String next = current.substring(0, offset) + text + current.substring(offset + length);

                if (next.isEmpty() || next.matches("\\d{1,3}")) {
                    super.replace(bypass, offset, length, text, attributes);
                }
            }

            @Override
            public void insertString(FilterBypass bypass, int offset, String text, AttributeSet attributes) throws BadLocationException {
                replace(bypass, offset, 0, text, attributes);
            }
        });

        durationField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateTimerPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateTimerPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateTimerPreview();
            }
        });
    }

    private void updateTimerPreview() {
        if(timerRunning) {
            return;
        }

        String minutes = durationField.getText().trim();
        timerLabel.setText(minutes.isEmpty() ? "00:00" : minutes + ":00");
    }
    private void updateTimerLabel() {
    int minutes = remainingSeconds / 60;
    int seconds = remainingSeconds % 60;

    timerLabel.setText(
        String.format("%02d:%02d", minutes, seconds)
    );
}

    private void finishFocusTimer() {
    if(focusTimer != null) {
        focusTimer.stop();
    }

    remainingSeconds = 0;
    timerRunning = false;
    timerPaused = false;
    focusProgressBar.setValue(focusProgressBar.getMaximum());
    focusProgressBar.setRunning(false);
    startButton.setEnabled(true);
    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
    pauseButton.setText("일시정지");
    durationField.setEnabled(true);
    programSearchField.setEnabled(true);
    updateTimerLabel();
    WidgetAlertService.stopBarking();

    JOptionPane.showMessageDialog(
    this,
    "집중 시간이 끝났어요!"
);
}
    private void startFocusTimer() {
    String input = durationField.getText().trim();

    if(input.isEmpty()) {
        return;
    }

    int minutes = Integer.parseInt(input);

    if(minutes <= 0) {
        return;
    }

    remainingSeconds = minutes * 60;
    totalFocusSeconds = remainingSeconds;
    resetDeviationStats();
    timerRunning = true;
    timerPaused = false;
    focusProgressBar.setMaximum(totalFocusSeconds);
    focusProgressBar.setValue(0);
    focusProgressBar.setRunning(true);
    startButton.setEnabled(false);
    pauseButton.setEnabled(true);
    stopButton.setEnabled(true);
    pauseButton.setText("일시정지");
    durationField.setEnabled(false);
    programSearchField.setEnabled(false);
    updateTimerLabel();

    focusTimer = new Timer(1000, event -> {
        remainingSeconds--;
        updateTimerLabel();
        updateFocusProgress();
        checkProgramDeviation();
        if(remainingSeconds <= 0) {
            finishFocusTimer();
        }
    });

    focusTimer.start();
}

    private void updateFocusProgress() {
        if(totalFocusSeconds <= 0) {
            focusProgressBar.setValue(0);
            return;
        }

        int elapsedSeconds = totalFocusSeconds - remainingSeconds;
        focusProgressBar.setValue(Math.min(totalFocusSeconds, elapsedSeconds));
    }

    private void stopFocusTimer() {
        if(focusTimer != null) {
            focusTimer.stop();
        }

        remainingSeconds = 0;
        totalFocusSeconds = 0;
        timerRunning = false;
        timerPaused = false;
        focusProgressBar.setMaximum(100);
        focusProgressBar.setValue(0);
        focusProgressBar.setRunning(false);
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        pauseButton.setText("일시정지");
        durationField.setEnabled(true);
        programSearchField.setEnabled(true);
        updateTimerPreview();
        WidgetAlertService.stopBarking();
    }

    private void toggleFocusTimerPause() {
        if(focusTimer == null || !timerRunning) {
            return;
        }

        if(timerPaused) {
            focusTimer.start();
            timerPaused = false;
            focusProgressBar.setRunning(true);
            pauseButton.setText("일시정지");
            if(alertCheck.isSelected() && outsideTargetProgram && deviationCounted) {
                WidgetAlertService.startBarking();
            }
        } else {
            focusTimer.stop();
            timerPaused = true;
            focusProgressBar.setRunning(false);
            pauseButton.setText("계속하기");
            WidgetAlertService.stopBarking();
        }
    }
    private void configureProgramSearch() {
        programSearchResults.add(label("검색 결과가 여기에 표시됩니다.", Font.PLAIN, 10, SUB_TEXT));

        programSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateProgramSearchResults();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateProgramSearchResults();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateProgramSearchResults();
            }
        });
    }

    private void updateProgramSearchResults() {
        String keyword = programSearchField.getText().trim();
        programSearchResults.removeAll();

        if (keyword.isBlank()) {
            programSearchResults.add(label("검색 결과가 여기에 표시됩니다.", Font.PLAIN, 10, SUB_TEXT));
            refreshProgramSearchResults();
            return;
        }

        programSearchResults.add(label("프로그램 검색 중...", Font.PLAIN, 10, SUB_TEXT));
        refreshProgramSearchResults();
        loadProgramSearchResults(keyword);
    }

    private void loadProgramSearchResults(String keyword) {
        new SwingWorker<ProgramSearchDto.Response, Void>() {
            @Override
            protected ProgramSearchDto.Response doInBackground() throws Exception {
                return pawLogClient.searchPrograms(keyword, 4);
            }   

            @Override
            protected void done() {
                if (!programSearchField.getText().trim().equalsIgnoreCase(keyword)) {
                    return;
                }

                programSearchResults.removeAll();

                try {
                    ProgramSearchDto.Response response = get();

                    if (!response.isSuccess()) {
                        programSearchResults.add(label(response.getMessage(), Font.PLAIN, 10, RED));
                    } else if (response.getPrograms() == null || response.getPrograms().isEmpty()) {
                        programSearchResults.add(label("일치하는 프로그램이 없어요.", Font.PLAIN, 10, SUB_TEXT));
                    } else {
                        for (ProgramSearchDto.ProgramItem program : response.getPrograms()) {
                            programSearchResults.add(createProgramResultButton(program));
                        }
                    }
                } catch (Exception exception) {
                    programSearchResults.add(label("서버 연결에 실패했어요.", Font.PLAIN, 10, RED));
                    exception.printStackTrace();
                }

                refreshProgramSearchResults();
            }
        }.execute();
    }

    private JButton createProgramResultButton(ProgramSearchDto.ProgramItem program) {
        JButton button = outlineButton(program.getDisplayName());
        button.setPreferredSize(new Dimension(160, 30));
        button.addActionListener(event -> selectProgram(program));
        return button;
    }

    private void selectProgram(ProgramSearchDto.ProgramItem program) {
        selectedProgramId = program.getProgramId();
        selectedProcessName = program.getProcessName();
        focusProgramLabel.setText(program.getDisplayName());
        selectedProgramLabel.setText("선택됨  ·  " + program.getDisplayName());
        selectedProgramLabel.setForeground(PRIMARY);
        programSearchField.setText("");
        programSearchResults.removeAll();
        programSearchResults.add(label("프로그램 선택이 완료됐어요.", Font.PLAIN, 10, SUB_TEXT));
        refreshProgramSearchResults();
    }
    private void checkProgramDeviation() {
    if(!timerRunning || timerPaused) {
        return;
    }

    String currentProcessName = ProgramTracker.getCurrentProcessName();

    if(currentProcessName == null || currentProcessName.isBlank() || selectedProcessName == null) {
        return;
    }

    boolean sameProgram =
        normalizeProcessName(selectedProcessName).equals(normalizeProcessName(currentProcessName));

    if(sameProgram) {
        WidgetAlertService.stopBarking();
        outsideTargetProgram = false;
        deviationCounted = false;
        deviationStartedAt = 0;
        selectedProgramLabel.setText(
        focusProgramLabel.getText() + "에 잘 집중하고 있어요."
    );
        selectedProgramLabel.setForeground(PRIMARY);
        return;
    }

    deviationSeconds++;
    updateDeviationStatus();

    if(!outsideTargetProgram) {
        outsideTargetProgram = true;
        deviationCounted = false;
        deviationStartedAt = System.currentTimeMillis();
    }

    long outsideMillis =
        System.currentTimeMillis() - deviationStartedAt;

    if(outsideMillis >= 3000 && !deviationCounted) {
        deviationCount++;
        deviationCounted = true;
        updateDeviationStatus();
        showDeviationWarning(ProgramTracker.getCurrentProgram());
    }
}

    private void resetDeviationStats() {
        outsideTargetProgram = false;
        deviationCounted = false;
        deviationStartedAt = 0;
        deviationCount = 0;
        deviationSeconds = 0;
        updateDeviationStatus();
    }

    private void updateDeviationStatus() {
        int minutes = deviationSeconds / 60;
        int seconds = deviationSeconds % 60;
        deviationCountLabel.setText(deviationCount + "회");
        deviationTimeLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private String normalizeProcessName(String processName) {
        String normalized = processName.trim().replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');

        if(slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }

        if(normalized.toLowerCase().endsWith(".exe")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }

        return normalized.toLowerCase();
    }
    private void refreshProgramSearchResults() {
        programSearchResults.revalidate();
        programSearchResults.repaint();
    }
    private void showDeviationWarning(String currentProgram) {
    if(!alertCheck.isSelected()) {
        return;
    }
   
    selectedProgramLabel.setText(
        currentProgram + "으로 이탈했어요! 집중 프로그램으로 돌아와 주세요."
    );

    selectedProgramLabel.setForeground(RED);
    WidgetAlertService.startBarking();
}
private final JCheckBox alertCheck =
    new JCheckBox("다른 프로그램으로 이탈하면 알려주기", true);
    private JLabel createSoftBadge(String text) {
        JLabel badge = label(text, Font.BOLD, 10, PRIMARY);
        badge.setOpaque(true);
        badge.setBackground(PRIMARY_SOFT);
        badge.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));
        return badge;
    }

    private static class DogProgressBar extends JProgressBar {
        private final ImageIcon[] runningFrames = new ImageIcon[8];
        private final ImageIcon idleDogIcon;
        private final Timer frameTimer;
        private boolean running;
        private int frameIndex;

        private DogProgressBar() {
            super(0, 100);
            setOpaque(true);
            setBackground(CARD);
            setBorderPainted(false);

            for(int index = 0; index < runningFrames.length; index++) {
                String path = String.format("/images/dog-run/pawlog_dog_run_%02d.png", index);
                java.net.URL frameUrl = FocusModePanel.class.getResource(path);
                runningFrames[index] = frameUrl == null ? null : new ImageIcon(frameUrl);
            }

            java.net.URL idleIconUrl = FocusModePanel.class.getResource("/images/pawlog_dog_idle_front_matched.png");
            idleDogIcon = idleIconUrl == null ? null : new ImageIcon(idleIconUrl);

            frameTimer = new Timer(80, event -> {
                frameIndex = (frameIndex + 1) % runningFrames.length;
                repaint();
            });
            frameTimer.setCoalesce(true);
        }

        private void setRunning(boolean running) {
            this.running = running;

            if(running) {
                frameTimer.start();
            } else {
                frameTimer.stop();
                frameIndex = 0;
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D)graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            int trackX = 12;
            int trackWidth = Math.max(1, getWidth() - 24);
            int trackY = getHeight() - 18;
            int trackHeight = 11;
            int fillWidth = (int)(trackWidth * getPercentComplete());

            drawFinishArea(g2, trackX, trackWidth, trackY, trackHeight);

            g2.setColor(new Color(226, 203, 168));
            g2.fillRoundRect(trackX, trackY, trackWidth, trackHeight, 12, 12);

            g2.setColor(new Color(177, 198, 132));
            g2.fillRoundRect(trackX, trackY, trackWidth, 4, 8, 8);

            g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 95));
            g2.fillRoundRect(trackX, trackY + 3, fillWidth, trackHeight - 3, 8, 8);

            drawGrass(g2, trackX, trackWidth, trackY);
            drawFootprints(g2, trackX, fillWidth, trackY);

            ImageIcon dogIcon = running ? runningFrames[frameIndex] : idleDogIcon;
            int dogWidth = dogIcon == null ? 28 : dogIcon.getIconWidth();
            int dogHeight = dogIcon == null ? 28 : dogIcon.getIconHeight();
            int dogX = trackX + (int)((trackWidth - dogWidth) * getPercentComplete());
            int dogY = trackY + trackHeight - dogHeight;

            if(dogIcon != null) {
                dogIcon.paintIcon(this, g2, dogX, dogY);
            } else {
                String dog = "🐕";
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 23));
                FontMetrics metrics = g2.getFontMetrics();
                g2.setColor(TEXT);
                g2.drawString(dog, dogX, dogY + metrics.getAscent());
            }

            g2.dispose();
        }

        private void drawGrass(Graphics2D g2, int trackX, int trackWidth, int trackY) {
            g2.setColor(new Color(116, 157, 91));

            for(int x = trackX + 18; x < trackX + trackWidth - 45; x += 42) {
                g2.drawLine(x, trackY + 1, x - 3, trackY - 4);
                g2.drawLine(x, trackY + 1, x + 1, trackY - 6);
                g2.drawLine(x, trackY + 1, x + 4, trackY - 3);
            }
        }

        private void drawFootprints(Graphics2D g2, int trackX, int fillWidth, int trackY) {
            g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 145));

            for(int x = trackX + 18; x < trackX + fillWidth - 12; x += 30) {
                int y = trackY + 5 + ((x / 30) % 2 == 0 ? 1 : -1);
                g2.fillOval(x, y, 5, 4);
                g2.fillOval(x + 5, y - 2, 3, 3);
                g2.fillOval(x + 8, y, 3, 3);
            }
        }

        private void drawFinishArea(Graphics2D g2, int trackX, int trackWidth, int trackY, int trackHeight) {
            int finishWidth = 52;
            int finishX = trackX + trackWidth - finishWidth;
            int flagX = trackX + trackWidth - 8;
            int flagY = 8;
            int cellSize = 6;

            g2.setColor(PRIMARY_SOFT);
            g2.fillRoundRect(finishX, 3, finishWidth, trackY + trackHeight - 3, 16, 16);

            g2.setColor(TEXT);
            g2.fillRoundRect(flagX, flagY, 3, trackY - flagY + 7, 3, 3);

            for(int row = 0; row < 3; row++) {
                for(int column = 0; column < 4; column++) {
                    g2.setColor((row + column) % 2 == 0 ? CARD : TEXT);
                    int x = flagX - 24 + column * cellSize;
                    int y = flagY + row * cellSize;
                    g2.fillRect(x, y, cellSize, cellSize);
                }
            }
        }
    }
}
