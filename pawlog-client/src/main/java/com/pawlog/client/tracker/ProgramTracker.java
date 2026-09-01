package com.pawlog.client.tracker;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.pawlog.client.Storage.SQLiteInitializer;
import com.pawlog.client.Storage.UsageAutoSaveScheduler;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

public class ProgramTracker {
    private static final Set<String> EXCLUDED = new HashSet<>();
    private static final Map<String, Long> usageMap = new HashMap<>();// 전체 통계
    private static final Map<UsageKey, Long> pendingUsageMap = new HashMap<>();//sqllite 기록용 통계
    private static final Map<UsageKey, Long> hourlyUsageMap = new HashMap<>();//시간별 통계
    private static ScheduledExecutorService scheduler;//기본 단위 갱신 스레드
    private static LocalDate trackingDate = LocalDate.now();
    private static String previousProcessName;

    private static volatile String currentProgram = "확인 중...";
    private static volatile String currentProcessName = "";
    private static volatile String currentWindowTitle = "활성 창을 확인하고 있습니다.";
    private static volatile long usageSeconds;//현재세션 사용시간
    private static volatile long totalSeconds;//총 활성 시간

    static {//예외 프로그램 
        EXCLUDED.add("explorer.exe");
        EXCLUDED.add("ShellExperienceHost.exe");
        EXCLUDED.add("SearchHost.exe");
        EXCLUDED.add("java.exe");
        EXCLUDED.add(" javaw.exe");
    }

    public static synchronized void startTracking() {//트래킹 시작
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
      
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(ProgramTracker::trackForegroundWindow, 0, 1, TimeUnit.SECONDS);
        
    }
    
    public static synchronized void stopTracking() {//중지
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    public static synchronized void resetTrackingData() {
        usageMap.clear();
        hourlyUsageMap.clear();
        pendingUsageMap.clear();
        trackingDate = LocalDate.now();
        previousProcessName = null;
        currentProgram = "확인 중...";
        currentProcessName = "";
        currentWindowTitle = "활성 창을 확인하고 있습니다.";
        usageSeconds = 0;
        totalSeconds = 0;
    }

    private static void trackForegroundWindow() {//프로그램 추적 알고리즘
        try {
            HWND window = User32.INSTANCE.GetForegroundWindow();

            if (window == null) {
                return;
            }

            char[] titleBuffer = new char[512];
            User32.INSTANCE.GetWindowText(window, titleBuffer, titleBuffer.length);
            String windowTitle = Native.toString(titleBuffer);

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

            String processName = Path.of(command.get()).getFileName().toString();
            String displayName = formatProcessNameForDisplay(processName);

            if (EXCLUDED.contains(processName)) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            int currentHour = now.getHour();

            if (!today.equals(trackingDate)) {
                trackingDate = today;
                totalSeconds = 0;
            }

            if (!processName.equals(previousProcessName)) {
                previousProcessName = processName;
                usageSeconds = 1;
            } else {
                usageSeconds++;
            }

            totalSeconds++;
            
            UsageKey usageKey = new UsageKey(
                today,
                currentHour,
                processName
            );

            addUsage(processName, usageKey);

            currentProcessName = processName;
            currentProgram = displayName;
            currentWindowTitle = windowTitle;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
    private static synchronized void addUsage(String processName, UsageKey usageKey) {//이함수로 통계 병합 , 관리
        usageMap.merge(processName, 1L, Long::sum);
        hourlyUsageMap.merge(usageKey, 1L, Long::sum);
        pendingUsageMap.merge(usageKey, 1L, Long::sum);
    }
    public static String getCurrentProgram() {
        return currentProgram;
    }

    public static String getCurrentProcessName() {
        return currentProcessName;
    }

    public static String getCurrentWindowTitle() {
        return formatWindowTitleForDisplay(currentWindowTitle);
    }

    private static String formatWindowTitleForDisplay(String windowTitle) {
        if(windowTitle == null || windowTitle.isBlank()) {
            return "";
        }

        String title = windowTitle.trim();
        String[] titleParts = title.split("\\s+[-–—]\\s+");

        if(titleParts.length > 1) {
            title = titleParts[titleParts.length - 1].trim();
        }

        String[] pathParts = title.split("[\\\\/₩¥]");

        if(pathParts.length > 1) {
            title = pathParts[pathParts.length - 1].trim();
        }

        return title.replaceFirst("\\s+\\d+(?:\\.\\d+)+$", "").trim();
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

    public static Map<UsageKey, Long> getHourlyUsageMap() {
        return new HashMap<>(hourlyUsageMap);
    }

    public static synchronized Map<UsageKey, Long> drainPendingUsage() {//복사후 초기화 
    Map<UsageKey, Long> snapshot = new HashMap<>(pendingUsageMap);
    pendingUsageMap.clear();
    return snapshot;
    }

    public static synchronized void restorePendingUsage(Map<UsageKey, Long> data) {//요청 실패시 데이터 병합
    if (data == null || data.isEmpty()) {
        return;
        }

    for(Map.Entry<UsageKey, Long> entry : data.entrySet()) {
        UsageKey key = entry.getKey();
        long seconds = entry.getValue();

        pendingUsageMap.merge(key, seconds, Long::sum);
        }
    }
    

    int currentHour = LocalDateTime.now().getHour();

    public static List<Map.Entry<String, Long>> getTopPrograms() {
        //KEY [DATE ,PROGRAMNAME , HOUR(시간대)]  VALUE 로 SECONDS가 오는 구조
        return usageMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .toList();//시간 (value) 내림차순 정렬로 최근 활성 프로그램 정렬
    }



    public static String getMostProgram() {
        List<Map.Entry<String, Long>> rankList = getTopPrograms();

        if (rankList.isEmpty()) {
            return "기록 없음";
        }

        return formatProcessNameForDisplay(rankList.get(0).getKey());
    }

    public static String formatProcessNameForDisplay(String processName) {
        if (processName == null || processName.isBlank()) {
            return "알 수 없음";
        }

        return processName.replaceFirst("(?i)\\.exe$", "");
    }

    public static long amount() {
        List<Map.Entry<String, Long>> rankList = getTopPrograms();
        long count = rankList.size();

        return count;
    }

}
