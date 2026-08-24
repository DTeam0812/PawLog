package com.pawlog.server.service;

import com.pawlog.common.dto.statistics.MyStatisticsDto;
import com.pawlog.server.dao.DailyUsageSummaryDAO;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsService {

    private final DailyUsageSummaryDAO summaryDAO;

    public StatisticsService() {
        this.summaryDAO = new DailyUsageSummaryDAO();
    }

    public StatisticsService(DailyUsageSummaryDAO summaryDAO) {
        this.summaryDAO = summaryDAO;
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
}
