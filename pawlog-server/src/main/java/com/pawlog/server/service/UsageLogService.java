package com.pawlog.server.service;

import com.pawlog.common.dto.usage.UsageLogSaveDto;
import com.pawlog.server.dao.ProgramDAO;
import com.pawlog.server.dao.UsageLogDAO;
import com.pawlog.server.dao.UserDAO;
import com.pawlog.server.entity.ProgramEntity;
import com.pawlog.server.entity.UsageLogEntity;

import java.sql.Date;
import java.time.LocalDate;

public class UsageLogService {
    private final UsageLogDAO usageLogDAO;
    private final ProgramDAO programDAO;
    private final UserDAO userDAO;

    public UsageLogService() {
        this.usageLogDAO = new UsageLogDAO();
        this.programDAO = new ProgramDAO();
        this.userDAO = new UserDAO();
    }

    public UsageLogSaveDto.Response saveUsageLogs(Long sessionUserId, UsageLogSaveDto.Request req) {
        if (req == null || req.getLogs() == null || req.getLogs().isEmpty()) {
            return new UsageLogSaveDto.Response(false, 400, "저장할 로그 데이터가 없습니다.", 0);
        }

        // 유저 ID 확인
        Long userId = sessionUserId;
        if (userId == null) {
            // 세션 정보가 없거나 미인증 처리
            return new UsageLogSaveDto.Response(false, 401, "인증되지 않은 유저입니다. 로그인 후 이용해주세요.", 0);
        }

        // Request 레벨 fallback 날짜
        LocalDate fallbackDate;
        try {
            fallbackDate = LocalDate.parse(req.getUsageDate());
        } catch (Exception e) {
            fallbackDate = LocalDate.now();
        }

        int savedCount = 0;

        // 사용 기록 리스트 순회 저장
        for (UsageLogSaveDto.LogItem item : req.getLogs()) {
            if (item.getProgramName() == null || item.getProgramName().trim().isEmpty()) {
                continue;
            }

            String processName = item.getProgramName().trim();

            // 프로그램 조회 및 없으면 자동 생성
            ProgramEntity program = programDAO.getProgramByProcessName(processName);
            if (program == null) {
                String displayName = processName.replaceFirst("(?i)\\.exe$", "");
                programDAO.insertProgram(processName, displayName);
                program = programDAO.getProgramByProcessName(processName);
            }

            if (program == null) {
                // 프로그램 등록 실패 시 해당 건 건너뜀
                continue;
            }

            // 날짜 결정: LogItem에 usageDate가 있으면 사용, 없으면 Request 레벨 fallback
            LocalDate itemDate;
            if (item.getUsageDate() != null && !item.getUsageDate().isEmpty()) {
                try {
                    itemDate = LocalDate.parse(item.getUsageDate());
                } catch (Exception e) {
                    itemDate = fallbackDate;
                }
            } else {
                itemDate = fallbackDate;
            }

            int hour = Math.max(0, Math.min(23, item.getHour()));

            // UsageLogEntity 생성 및 DB UPSERT
            UsageLogEntity logEntity = new UsageLogEntity();
            logEntity.setUserId(userId);
            logEntity.setProgramId(program.getProgramId());
            logEntity.setUsageDate(Date.valueOf(itemDate));
            logEntity.setUsageHour(hour);
            logEntity.setDurationSec((int) item.getDurationSec());

            boolean success = usageLogDAO.upsertUsageLog(logEntity);
            if (success) {
                savedCount++;
            }
        }

        if (savedCount > 0) {
            return new UsageLogSaveDto.Response(true, 200, "사용 기록이 성공적으로 저장되었습니다.", savedCount);
        } else {
            return new UsageLogSaveDto.Response(false, 500, "사용 기록 저장에 실패하였습니다.", 0);
        }
    }

    /**
     * 테스트 시나리오:
     *
     * [8/19] 23:00~23:10  크롬 ON  (600초)
     * [8/19] 23:10~23:20  크롬 OFF
     * [8/19] 23:20~23:30  크롬 ON  (600초, 누적 1200초)
     *   → 첫 번째 저장 (23:30)
     *
     * [8/19] 23:30~23:40  크롬 OFF
     * [8/19] 23:40~23:50  크롬 ON  (600초, 누적 1800초)
     * [8/19] 23:50~23:55  크롬 OFF
     * [8/19] 23:55~00:00  크롬 ON  (300초, 누적 2100초)
     * [8/20] 00:00~01:00  크롬 ON  (3600초)
     * [8/20] 01:00~01:05  크롬 ON  (300초)
     *   → 두 번째 저장 (01:05)
     *
     * 기대 결과:
     *   (2026-08-19, 23) → 2100초 (35분)
     *   (2026-08-20,  0) → 3600초 (60분)
     *   (2026-08-20,  1) →  300초 (5분)
     */
    /*
    public static void main(String[] args) {
        System.out.println("=== UPSERT 테스트 시작 ===\n");
        UsageLogService service = new UsageLogService();

        // 테스트용 유저 ID (DB에 존재하는 유저 ID로 변경 필요)
        Long testUserId = 12L;

        // ──────────────────────────────────────────────
        // 1차 저장: 23:30 시점 — hourlyUsageMap 누적값
        //   (08-19, 23, chrome.exe) = 1200초 (23:00~23:10 + 23:20~23:30)
        // ──────────────────────────────────────────────
        System.out.println("── 1차 저장 (23:30 시점) ──");

        java.util.List<UsageLogSaveDto.LogItem> firstLogs = new java.util.ArrayList<>();
        firstLogs.add(new UsageLogSaveDto.LogItem(23, "chrome.exe", 1200L, "2026-08-19"));

        UsageLogSaveDto.Request firstRequest = new UsageLogSaveDto.Request(
                "test@test.com", "2026-08-19", firstLogs
        );

        UsageLogSaveDto.Response firstResponse = service.saveUsageLogs(testUserId, firstRequest);
        System.out.println("  Success: " + firstResponse.isSuccess());
        System.out.println("  Message: " + firstResponse.getMessage());
        System.out.println("  SavedCount: " + firstResponse.getSavedCount());

        // ──────────────────────────────────────────────
        // 2차 저장: 01:05 시점 — hourlyUsageMap 누적값 (클리어 안 됨)
        //   (08-19, 23, chrome.exe) = 2100초 (기존 1200 + 23:40~23:50(600) + 23:55~00:00(300))
        //   (08-20,  0, chrome.exe) = 3600초 (00:00~01:00)
        //   (08-20,  1, chrome.exe) =  300초 (01:00~01:05)
        // ──────────────────────────────────────────────
        System.out.println("\n── 2차 저장 (다음날 01:05 시점) ──");

        java.util.List<UsageLogSaveDto.LogItem> secondLogs = new java.util.ArrayList<>();
        secondLogs.add(new UsageLogSaveDto.LogItem(23, "chrome.exe", 2100L, "2026-08-19"));
        secondLogs.add(new UsageLogSaveDto.LogItem(0,  "chrome.exe", 3600L, "2026-08-20"));
        secondLogs.add(new UsageLogSaveDto.LogItem(1,  "chrome.exe",  300L, "2026-08-20"));

        UsageLogSaveDto.Request secondRequest = new UsageLogSaveDto.Request(
                "test@test.com", "2026-08-20", secondLogs
        );

        UsageLogSaveDto.Response secondResponse = service.saveUsageLogs(testUserId, secondRequest);
        System.out.println("  Success: " + secondResponse.isSuccess());
        System.out.println("  Message: " + secondResponse.getMessage());
        System.out.println("  SavedCount: " + secondResponse.getSavedCount());

        // ──────────────────────────────────────────────
        // 기대 결과 출력
        // ──────────────────────────────────────────────
        System.out.println("\n── 기대 결과 ──");
        System.out.println("  DB에 3개 row가 존재해야 함:");
        System.out.println("  (2026-08-19, 23시) → 2100초 (35분) — 1차의 1200이 2100으로 UPSERT됨");
        System.out.println("  (2026-08-20,  0시) → 3600초 (60분) — 신규 INSERT");
        System.out.println("  (2026-08-20,  1시) →  300초 ( 5분) — 신규 INSERT");
        System.out.println("\n  확인 SQL:");
        System.out.println("  SELECT * FROM usage_logs WHERE user_id = " + testUserId + " ORDER BY usage_date, usage_hour;");
        System.out.println("\n=== 테스트 종료 ===");
    }
    */
}
