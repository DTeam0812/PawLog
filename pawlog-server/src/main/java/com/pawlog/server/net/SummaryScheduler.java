package com.pawlog.server.net;

import com.pawlog.server.dao.DailyUsageSummaryDAO;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * usage_logs 변경 사항을 추적하여 daily_usage_summary를 5분 주기로 일괄 동기화하는 스케줄러.
 *
 * <p>동작 방식:
 * <ol>
 *   <li>usage_logs에 저장 성공 시 {@link #markDirty}로 동기화 필요 키를 등록한다.</li>
 *   <li>5분마다 dirty 키 목록을 일괄 처리하여 daily_usage_summary를 재집계한다.</li>
 *   <li>동기화 실패한 키는 다음 flush에서 재시도한다.</li>
 * </ol>
 * </p>
 */
public class SummaryScheduler {

    private static final SummaryScheduler INSTANCE = new SummaryScheduler();

    private static final int FLUSH_INTERVAL_MINUTES = 5;

    /** 동기화가 필요한 키 목록 (thread-safe) */
    private final Set<SyncKey> dirtyKeys = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "summary-sync-scheduler");
        t.setDaemon(true); // JVM 종료 시 자동 종료
        return t;
    });

    private final DailyUsageSummaryDAO summaryDAO = new DailyUsageSummaryDAO();

    private SummaryScheduler() {}

    public static SummaryScheduler getInstance() {
        return INSTANCE;
    }

    /**
     * 서버 시작 시 한 번 호출한다.
     * {@value FLUSH_INTERVAL_MINUTES}분 주기로 daily_usage_summary 동기화를 시작한다.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(
            this::flush,
            FLUSH_INTERVAL_MINUTES,
            FLUSH_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        );
        System.out.printf("[SummaryScheduler] 시작 — %d분 주기로 daily_usage_summary 동기화%n", FLUSH_INTERVAL_MINUTES);
    }

    /**
     * usage_logs 저장 성공 시 호출한다.
     * 해당 (userId, programId, date) 조합을 동기화 대기 목록에 등록한다.
     *
     * @param userId    유저 ID
     * @param programId 프로그램 ID
     * @param usageDate 사용 날짜 (usage_logs의 usage_date)
     */
    public void markDirty(long userId, long programId, Date usageDate) {
        dirtyKeys.add(new SyncKey(userId, programId, usageDate.toLocalDate()));
    }

    /**
     * dirty 키 목록을 일괄 처리하여 daily_usage_summary를 동기화한다.
     * 스케줄러에 의해 5분마다 자동 호출된다.
     */
    private void flush() {
        if (dirtyKeys.isEmpty()) {
            return;
        }

        // 현재 dirty 목록 스냅샷 (flush 중 추가되는 키는 다음 flush에서 처리)
        Set<SyncKey> toSync = new HashSet<>(dirtyKeys);
        dirtyKeys.removeAll(toSync);

        System.out.printf("[SummaryScheduler] flush 시작 — %d건%n", toSync.size());
        int success = 0;

        for (SyncKey key : toSync) {
            try {
                summaryDAO.syncFromUsageLogs(key.userId(), Date.valueOf(key.date()), key.programId());
                success++;
            } catch (Exception e) {
                System.err.printf("[SummaryScheduler] 동기화 실패 (userId=%d, programId=%d, date=%s): %s%n",
                    key.userId(), key.programId(), key.date(), e.getMessage());
                dirtyKeys.add(key); // 실패 시 다음 flush에서 재시도
            }
        }

        System.out.printf("[SummaryScheduler] flush 완료 — 성공: %d / 전체: %d%n", success, toSync.size());
    }

    /** dirty 추적 키 (userId + programId + date 조합) */
    private record SyncKey(long userId, long programId, LocalDate date) {}
}
