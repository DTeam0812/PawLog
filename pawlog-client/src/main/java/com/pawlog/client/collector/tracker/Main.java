package com.pawlog.client.collector.tracker;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import com.pawlog.client.ui.widget.Widget;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

public class Main {
    private static final Set<String> EXCLUDED = new HashSet<>();
    private static final Map<String, Long> usageMap = new HashMap<>();

    private static ScheduledExecutorService scheduler;
    private static LocalDate trackingDate = LocalDate.now();
    private static String previousProgram;

    private static volatile String currentProgram = "확인 중...";
    private static volatile String currentWindowTitle = "활성 창을 확인하고 있습니다.";
    private static volatile long usageSeconds;
    private static volatile long totalSeconds;

    static {
        EXCLUDED.add("explorer.exe");
        EXCLUDED.add("ShellExperienceHost.exe");
        EXCLUDED.add("SearchHost.exe");
        EXCLUDED.add("java.exe");
    }

    public static synchronized void startTracking() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(Main::trackForegroundWindow, 0, 1, TimeUnit.SECONDS);
    }

    public static synchronized void stopTracking() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private static void trackForegroundWindow() {
        try {
            HWND window = User32.INSTANCE.GetForegroundWindow();

            if (window == null) {
                return;
            }

            IntByReference processId = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(window, processId);
            long pid = processId.getValue() & 0xFFFFFFFFL;

            Optional<ProcessHandle> process = ProcessHandle.of(pid);

            if (process.isEmpty()) {
                return;
            }

            Optional<String> command = process.get().info().command();

            if (command.isEmpty()) {
                return;
            }

            String programName = Path.of(command.get()).getFileName().toString();
            String displayName = programName.replaceFirst("(?i)\\.exe$", "");

            if (EXCLUDED.contains(programName)) {
                return;
            }

            LocalDate today = LocalDate.now();

            if (!today.equals(trackingDate)) {
                trackingDate = today;
                totalSeconds = 0;
            }

            if (!programName.equals(previousProgram)) {
                previousProgram = programName;
                usageSeconds = 1;
            } else {
                usageSeconds++;
            }

            totalSeconds++;
            usageMap.merge(programName, 1L, Long::sum);

            currentProgram = displayName;
            currentWindowTitle = programName;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static String getCurrentProgram() {
        return currentProgram;
    }

    public static String getCurrentWindowTitle() {
        return currentWindowTitle;
    }

    public static long getUsageSeconds() {
        return usageSeconds;
    }

    public static long getTotalSeconds() {
        return totalSeconds;
    }

    public static Map<String, Long> getUsageMap() {
        return new HashMap<>(usageMap);
    }

    public static List<Map.Entry<String, Long>> getTopPrograms() {
        return usageMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .toList();
    }

    public static String getMostProgram() {
        List<Map.Entry<String, Long>> rankList = getTopPrograms();

        if (rankList.isEmpty()) {
            return "기록 없음";
        }

        return rankList.get(0).getKey();
    }

    public static long amount() {
        List<Map.Entry<String, Long>> rankList = getTopPrograms();
        long count = rankList.size();

        return count;
    }

    public static void main(String[] args) {
        startTracking();

        SwingUtilities.invokeLater(() -> {
            Widget widget = new Widget();
            widget.show();
        });
    }
}
