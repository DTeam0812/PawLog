package com.pawlog.server.net;

import com.pawlog.server.dao.ProgramUsageStatDAO;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 매일 자정(00:00)에 어제 하루치 daily_usage_summary를 집계하여
 * program_usage_stats 테이블을 자동 업데이트하는 일일 배치 스케줄러.
 */
public class DailyStatsScheduler {

    private static final DailyStatsScheduler INSTANCE = new DailyStatsScheduler();

    private final ProgramUsageStatDAO statsDAO = new ProgramUsageStatDAO();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "daily-stats-batch-scheduler");
        t.setDaemon(true); // JVM 종료 시 자동 종료
        return t;
    });

    private DailyStatsScheduler() {}

    public static DailyStatsScheduler getInstance() {
        return INSTANCE;
    }

    /**
     * 서버 시작 시 호출되어 스케줄러를 가동합니다.
     * 1. 어제자 통계 데이터가 누락되어 있다면 즉시 1회 집계(보정) 수행
     * 2. 매일 자정(00:00:00)에 정기 실행되도록 스케줄 등록
     */
    public void start() {
        // 1. 서버 시작 시 어제치 통계 누락 여부 확인 및 보정
        checkAndCompensateYesterdayStats();

        // 2. 다음 자정(00:00:00)까지 남은 초 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIDNIGHT);
        long initialDelaySec = Duration.between(now, nextMidnight).getSeconds();

        // 3. 매일 24시간 주기로 실행
        long periodSec = TimeUnit.DAYS.toSeconds(1);

        scheduler.scheduleAtFixedRate(
                this::runDailyBatch,
                initialDelaySec,
                periodSec,
                TimeUnit.SECONDS
        );

        long initialDelayMin = initialDelaySec / 60;
        System.out.printf("[DailyStatsScheduler] 시작 — 다음 자정(약 %d분 후)부터 매일 00:00에 자동 통계 집계 실행%n", initialDelayMin);
    }

    /**
     * 어제 날짜의 통계 데이터 누락 여부를 확인하고, 없으면 즉시 집계합니다.
     */
    private void checkAndCompensateYesterdayStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            if (!statsDAO.hasStatsForDate(yesterday)) {
                System.out.printf("[DailyStatsScheduler] 어제(%s) 통계 데이터가 존재하지 않아 보정 집계를 시작합니다...%n", yesterday);
                int count = statsDAO.aggregateAndSaveDailyStats(yesterday);
                System.out.printf("[DailyStatsScheduler] 어제(%s) 보정 집계 완료 — %d건 등록%n", yesterday, count);
            } else {
                System.out.printf("[DailyStatsScheduler] 어제(%s) 통계 데이터가 이미 존재합니다.%n", yesterday);
            }
        } catch (Exception e) {
            System.err.printf("[DailyStatsScheduler] 어제(%s) 통계 보정 실패: %s%n", yesterday, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 매일 자정 실행되는 배치 작업.
     * 어제 하루치(00:00 ~ 23:59) 통계를 집계하여 적재합니다.
     */
    private void runDailyBatch() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        System.out.printf("[DailyStatsScheduler] 자정 통계 배치 시작 — 대상 일자: %s%n", yesterday);

        try {
            int insertedRows = statsDAO.aggregateAndSaveDailyStats(yesterday);
            if (insertedRows >= 0) {
                System.out.printf("[DailyStatsScheduler] 자정 통계 배치 완료 — %s 통계 %d건 갱신 성공%n", yesterday, insertedRows);
            } else {
                System.err.printf("[DailyStatsScheduler] 자정 통계 배치 처리 중 오류 발생 (대상 일자: %s)%n", yesterday);
            }
        } catch (Exception e) {
            System.err.printf("[DailyStatsScheduler] 자정 통계 배치 실행 중 예외 발생: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }
}
