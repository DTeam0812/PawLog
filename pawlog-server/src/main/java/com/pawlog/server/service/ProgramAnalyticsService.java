package com.pawlog.server.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pawlog.common.dto.analytics.ProgramAnalyticsDto;
import com.pawlog.server.dao.DailyUsageSummaryDAO;
import com.pawlog.server.dao.ProgramDAO;
import com.pawlog.server.dao.UsageLogDAO;
import com.pawlog.server.entity.ProgramEntity;

public class ProgramAnalyticsService {
    private final DailyUsageSummaryDAO summaryDAO;
    private final UsageLogDAO usageLogDAO;
    private final ProgramDAO programDAO;

    public ProgramAnalyticsService() {
        this.summaryDAO = new DailyUsageSummaryDAO();
        this.usageLogDAO = new UsageLogDAO();
        this.programDAO = new ProgramDAO();
    }

    public ProgramAnalyticsService(DailyUsageSummaryDAO summaryDAO, UsageLogDAO usageLogDAO, ProgramDAO programDAO) {
        this.summaryDAO = summaryDAO;
        this.usageLogDAO = usageLogDAO;
        this.programDAO = programDAO;
    }

    public ProgramAnalyticsDto.Response getProgramAnalytics(ProgramAnalyticsDto.Request request) {
        if (request == null || request.getProgramId() <= 0) {
            return new ProgramAnalyticsDto.Response(false, 400, "유효한 프로그램 ID가 필요합니다.", null);
        }

        long programId = request.getProgramId();

        try {
            // 0. 프로그램 기본 정보 확인
            ProgramEntity program = programDAO.getProgramById(programId);
            if (program == null) {
                return new ProgramAnalyticsDto.Response(false, 404, "존재하지 않는 프로그램입니다.", null);
            }
            String programName = program.getDisplayName() != null ? program.getDisplayName() : program.getProcessName();

            // 기준 일자 계산 (D-1 ~ D-7, D-2)
            LocalDate today = LocalDate.now();
            LocalDate d1 = today.minusDays(1);
            LocalDate d2 = today.minusDays(2);
            LocalDate d7 = today.minusDays(7);

            // 1. 일평균 사용 시간 및 전체 순위 조회 (D-7 ~ D-1)
            Object[] rankAndAvg = summaryDAO.getAvgUsageAndRank(programId, d7, d1);
            int rank = (Integer) rankAndAvg[0];
            double avgDailySec = (Double) rankAndAvg[1];

            String avgDailyUsageStr = formatDuration((long) avgDailySec);
            String totalRankStr = rank > 0 ? (rank + "위") : "순위 없음";

            // 2. 전날 대비 (D-1 vs D-2) 계산 및 예외 처리
            long d1Sec = summaryDAO.getDailyDurationByDate(programId, d1);
            long d2Sec = summaryDAO.getDailyDurationByDate(programId, d2);

            String dayOverDayDiff;
            String dayOverDaySubText;
            String dayOverDayLabel = d1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 기준 전날 대비";

            if (d1Sec == 0 && d2Sec == 0) {
                dayOverDayDiff = "0초";
                dayOverDaySubText = "최근 2일간 사용 기록 없음";
            } else if (d2Sec == 0) {
                dayOverDayDiff = "+" + formatDuration(d1Sec);
                dayOverDaySubText = "전날(D-2) 기록 없음 (신규 활성화)";
            } else {
                long diffSec = d1Sec - d2Sec;
                double rate = ((double) Math.abs(diffSec) / d2Sec) * 100.0;
                int rateInt = (int) Math.round(rate);

                if (diffSec > 0) {
                    dayOverDayDiff = "+" + formatDuration(diffSec);
                    dayOverDaySubText = "전날보다 " + rateInt + "% 더 오래 사용";
                } else if (diffSec < 0) {
                    dayOverDayDiff = "-" + formatDuration(Math.abs(diffSec));
                    dayOverDaySubText = "전날보다 " + rateInt + "% 덜 사용";
                } else {
                    dayOverDayDiff = "0초";
                    dayOverDaySubText = "전날과 동일하게 사용";
                }
            }

            // 3. 4시간 단위 6구간 시간대별 평균 사용 시간 조회 (D-7 ~ D-1)
            int[] hourlyUsage = usageLogDAO.getHourlyUsageSlots(programId, d7, d1);

            // 4. 연령대별 사용 통계 및 100% 점유율 보정 (D-7 ~ D-1)
            Map<String, Long> ageDurations = summaryDAO.getAgeGroupDurations(programId, d7, d1);
            List<ProgramAnalyticsDto.AgeGroupItem> ageGroupStats = buildAgeGroupStats(ageDurations);

            // 최종 DTO 조립
            ProgramAnalyticsDto.AnalyticsData data = new ProgramAnalyticsDto.AnalyticsData(
                    programId,
                    programName,
                    avgDailyUsageStr,
                    totalRankStr,
                    dayOverDayDiff,
                    dayOverDaySubText,
                    dayOverDayLabel,
                    hourlyUsage,
                    ageGroupStats
            );

            return new ProgramAnalyticsDto.Response(true, 200, "프로그램 분석 데이터 조회 성공", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ProgramAnalyticsDto.Response(false, 500, "프로그램 분석 처리 중 오류 발생: " + e.getMessage(), null);
        }
    }

    // 연령대 8개 구간 100% 보정 로직 (최대 잔여법)
    private List<ProgramAnalyticsDto.AgeGroupItem> buildAgeGroupStats(Map<String, Long> ageMap) {
        List<ProgramAnalyticsDto.AgeGroupItem> result = new ArrayList<>();
        List<Long> durations = new ArrayList<>();
        long totalSec = 0L;

        for (Map.Entry<String, Long> entry : ageMap.entrySet()) {
            durations.add(entry.getValue());
            totalSec += entry.getValue();
        }

        int size = ageMap.size();
        int[] percentages = new int[size];

        if (totalSec > 0) {
            double[] decimals = new double[size];
            int assignedTotal = 0;

            for (int i = 0; i < size; i++) {
                double exactPercent = ((double) durations.get(i) / totalSec) * 100.0;
                percentages[i] = (int) exactPercent;
                decimals[i] = exactPercent - percentages[i];
                assignedTotal += percentages[i];
            }

            int remainder = 100 - assignedTotal;
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < size; i++) indices.add(i);
            indices.sort((a, b) -> Double.compare(decimals[b], decimals[a]));

            for (int i = 0; i < remainder && i < size; i++) {
                percentages[indices.get(i)]++;
            }
        }

        int idx = 0;
        for (Map.Entry<String, Long> entry : ageMap.entrySet()) {
            result.add(new ProgramAnalyticsDto.AgeGroupItem(
                    entry.getKey(),
                    formatDuration(entry.getValue()),
                    percentages[idx++]
            ));
        }

        return result;
    }

    private String formatDuration(long totalSec) {
        if (totalSec <= 0) return "0초";
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;

        if (hours > 0) return String.format("%d시간 %d분", hours, minutes);
        if (minutes > 0) return String.format("%d분 %d초", minutes, seconds);
        return String.format("%d초", seconds);
    }
}