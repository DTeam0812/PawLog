package com.pawlog.server.service;

import com.pawlog.common.dto.statistics.MyStatisticsDto;
import com.pawlog.common.dto.statistics.TodayStatisticsDto;
import com.pawlog.server.dao.DailyUsageSummaryDAO;
import com.pawlog.server.dao.UsageLogDAO;
import com.pawlog.server.dao.UserDAO;
import com.pawlog.server.entity.UserEntity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsService {

    private final DailyUsageSummaryDAO summaryDAO;
    private final UsageLogDAO usageLogDAO;
    private final UserDAO userDAO;

    public StatisticsService() {
        this.summaryDAO = new DailyUsageSummaryDAO();
        this.usageLogDAO = new UsageLogDAO();
        this.userDAO = new UserDAO();
    }

    public StatisticsService(DailyUsageSummaryDAO summaryDAO, UsageLogDAO usageLogDAO, UserDAO userDAO) {
        this.summaryDAO = summaryDAO;
        this.usageLogDAO = usageLogDAO;
        this.userDAO = userDAO;
    }

    // 1. 개인 기간별 통계 조회 (MY_STATISTICS_GET)
    public MyStatisticsDto.Response getMyStatistics(MyStatisticsDto.Request request) {
        // 1. 요청 파라미터 검증
        if (request == null) {
            return new MyStatisticsDto.Response(false, 400, "요청 데이터가 없습니다.");
        }

        if (request.getId() == null || request.getId().trim().isEmpty()) {
            return new MyStatisticsDto.Response(false, 400, "유저 ID가 필요합니다.");
        }

        long userId;
        try {
            userId = Long.parseLong(request.getId().trim());
        } catch (NumberFormatException e) {
            return new MyStatisticsDto.Response(false, 400, "유효하지 않은 유저 ID 형식입니다.");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return new MyStatisticsDto.Response(false, 400, "시작일과 종료일이 필요합니다.");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            return new MyStatisticsDto.Response(false, 400, "시작일은 종료일보다 늦을 수 없습니다.");
        }

        try {
            // 2. 기간 내 사용자 일간 통계 집계 조회
            Date startDate = Date.valueOf(request.getStartDate());
            Date endDate = Date.valueOf(request.getEndDate());
            List<Object[]> rows = summaryDAO.getSummaryByUserAndPeriod(userId, startDate, endDate);

            if (rows == null || rows.isEmpty()) {
                MyStatisticsDto.DataPayload emptyPayload = new MyStatisticsDto.DataPayload(
                        "0",
                        "",
                        0,
                        Collections.emptyList()
                );
                return new MyStatisticsDto.Response(true, 200, "개인 통계 조회 성공", emptyPayload);
            }

            // 3. 통계 가공
            long totalSec = 0L;
            List<MyStatisticsDto.ProgramRatioItem> ratioItems = new ArrayList<>();
            List<Long> durations = new ArrayList<>();

            for (Object[] row : rows) {
                String programName = (String) row[1];
                long durationSec = (Long) row[2];

                totalSec += durationSec;
                durations.add(durationSec);

                MyStatisticsDto.ProgramRatioItem item = new MyStatisticsDto.ProgramRatioItem();
                item.setProgramName(programName != null ? programName : "알 수 없음");
                item.setProgramTime(String.valueOf(durationSec));
                item.setProgramRatio(0);
                ratioItems.add(item);
            }

            // 4. 프로그램별 점유율 계산 (합계가 정확히 100%가 되도록 최대 잔여법 적용)
            if (totalSec > 0 && !ratioItems.isEmpty()) {
                applyExact100Percentage(ratioItems, durations, totalSec);
            }

            String mostUsedItem = ratioItems.get(0).getProgramName();
            int amountUsedItem = ratioItems.size();

            MyStatisticsDto.DataPayload payload = new MyStatisticsDto.DataPayload(
                    String.valueOf(totalSec),
                    mostUsedItem,
                    amountUsedItem,
                    ratioItems
            );

            return new MyStatisticsDto.Response(true, 200, "개인 통계 조회 성공", payload);

        } catch (Exception e) {
            e.printStackTrace();
            return new MyStatisticsDto.Response(false, 500, "개인 통계 조회 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 각 항목의 백분율 합이 정확히 100이 되도록 소수점 잔여값을 기준으로 보정
     */
    private void applyExact100Percentage(List<MyStatisticsDto.ProgramRatioItem> items, List<Long> durations, long totalSec) {
        int size = items.size();
        int[] basePercentages = new int[size];
        double[] decimals = new double[size];
        int assignedTotal = 0;

        for (int i = 0; i < size; i++) {
            double exactPercent = ((double) durations.get(i) / totalSec) * 100.0;
            basePercentages[i] = (int) exactPercent;
            decimals[i] = exactPercent - basePercentages[i];
            assignedTotal += basePercentages[i];
        }

        int remainder = 100 - assignedTotal;

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < size; i++) indices.add(i);
        indices.sort((a, b) -> Double.compare(decimals[b], decimals[a]));

        for (int i = 0; i < remainder && i < size; i++) {
            basePercentages[indices.get(i)]++;
        }

        for (int i = 0; i < size; i++) {
            items.get(i).setProgramRatio(basePercentages[i]);
        }
    }

    // 2. 홈화면 오늘 통계 조회 (TODAY_STATISTICS_GET)
    public TodayStatisticsDto.Response getTodayStatistics(TodayStatisticsDto.Request request) {
        // 1. 요청 파라미터 검증
        if (request == null) {
            return new TodayStatisticsDto.Response(false, 400, "요청 데이터가 없습니다.", null);
        }

        if (request.getId() == null || request.getId().trim().isEmpty()) {
            return new TodayStatisticsDto.Response(false, 400, "아이디가 필요합니다.", null);
        }

        if (request.getUsageDate() == null || request.getUsageDate().trim().isEmpty()) {
            return new TodayStatisticsDto.Response(false, 400, "대상 날짜가 필요합니다.", null);
        }

        Date usageDate;
        try {
            usageDate = Date.valueOf(request.getUsageDate().trim());
        } catch (IllegalArgumentException e) {
            return new TodayStatisticsDto.Response(false, 400, "유효하지 않은 날짜 형식입니다. (yyyy-MM-dd)", null);
        }

        try {
            // 2. 로그인 아이디로 사용자 조회
            UserEntity user = userDAO.getUserByLoginId(request.getId().trim());
            if (user == null) {
                return new TodayStatisticsDto.Response(false, 400, "존재하지 않는 유저입니다.", null);
            }

            // 3. 당일 프로그램별 사용 기록 조회
            List<Object[]> rows = usageLogDAO.getDailyProgramUsageByUserIdAndDate(user.getUserId(), usageDate);

            if (rows == null || rows.isEmpty()) {
                TodayStatisticsDto.DataPayload emptyPayload = new TodayStatisticsDto.DataPayload(
                        0L,
                        "",
                        0,
                        Collections.emptyList()
                );
                return new TodayStatisticsDto.Response(true, 200, "오늘 사용 통계 조회에 성공했습니다.", emptyPayload);
            }

            // 4. 통계 가공
            long totalUsageSeconds = 0L;
            List<TodayStatisticsDto.ProgramItem> programItems = new ArrayList<>();
            List<Long> durations = new ArrayList<>();

            for (Object[] row : rows) {
                String programName = (String) row[0];
                long sec = (Long) row[1];

                totalUsageSeconds += sec;
                durations.add(sec);

                TodayStatisticsDto.ProgramItem item = new TodayStatisticsDto.ProgramItem();
                item.setProgramName(programName != null ? programName : "알 수 없음");
                item.setUsageSeconds(sec);
                item.setUsageRatio(0);
                programItems.add(item);
            }

            // 5. 점유율 백분율 계산 및 100% 보정 (최대 잔여법)
            if (totalUsageSeconds > 0 && !programItems.isEmpty()) {
                applyExact100PercentageForToday(programItems, durations, totalUsageSeconds);
            }

            String mostUsedProgram = programItems.get(0).getProgramName();
            int programCount = programItems.size();

            TodayStatisticsDto.DataPayload payload = new TodayStatisticsDto.DataPayload(
                    totalUsageSeconds,
                    mostUsedProgram,
                    programCount,
                    programItems
            );

            return new TodayStatisticsDto.Response(true, 200, "오늘 사용 통계 조회에 성공했습니다.", payload);

        } catch (Exception e) {
            e.printStackTrace();
            return new TodayStatisticsDto.Response(false, 500, "오늘 통계 조회 중 오류 발생: " + e.getMessage(), null);
        }
    }

    /**
     * 오늘 통계 각 항목의 백분율 합이 정확히 100이 되도록 소수점 잔여값을 기준으로 보정
     */
    private void applyExact100PercentageForToday(List<TodayStatisticsDto.ProgramItem> items, List<Long> durations, long totalSec) {
        int size = items.size();
        int[] basePercentages = new int[size];
        double[] decimals = new double[size];
        int assignedTotal = 0;

        for (int i = 0; i < size; i++) {
            double exactPercent = ((double) durations.get(i) / totalSec) * 100.0;
            basePercentages[i] = (int) exactPercent;
            decimals[i] = exactPercent - basePercentages[i];
            assignedTotal += basePercentages[i];
        }

        int remainder = 100 - assignedTotal;

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < size; i++) indices.add(i);
        indices.sort((a, b) -> Double.compare(decimals[b], decimals[a]));

        for (int i = 0; i < remainder && i < size; i++) {
            basePercentages[indices.get(i)]++;
        }

        for (int i = 0; i < size; i++) {
            items.get(i).setUsageRatio(basePercentages[i]);
        }
    }

    public static void main(String[] args) {
        StatisticsService service = new StatisticsService();

        System.out.println("========== [TODAY_STATISTICS_GET 테스트] ==========");
        TodayStatisticsDto.Request req = new TodayStatisticsDto.Request("testuser", "2026-08-25");
        TodayStatisticsDto.Response res = service.getTodayStatistics(req);

        System.out.println("성공 여부: " + res.isSuccess());
        System.out.println("상태 코드: " + res.getCode());
        System.out.println("응답 메시지: " + res.getMessage());

        if (res.getData() != null) {
            TodayStatisticsDto.DataPayload payload = res.getData();
            System.out.printf("총 사용시간(초): %d | 최다 사용 프로그램: %s | 프로그램 수: %d\n",
                    payload.getTotalUsageSeconds(), payload.getMostUsedProgram(), payload.getProgramCount());
            System.out.println("----------------------------------------------------------------------");
            for (TodayStatisticsDto.ProgramItem item : payload.getPrograms()) {
                System.out.printf("프로그램: %s | 사용시간: %d초 | 비율: %d%%\n",
                        item.getProgramName(), item.getUsageSeconds(), item.getUsageRatio());
            }
            System.out.println("----------------------------------------------------------------------");
        }
    }
}
