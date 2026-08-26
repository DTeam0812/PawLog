package com.pawlog.server.service;

import com.pawlog.common.dto.ranking.PopularRankingDto;
import com.pawlog.server.dao.ProgramUsageStatDAO;
import com.pawlog.server.dao.UsageLogDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PopularRankingService {

    private final ProgramUsageStatDAO statsDAO = new ProgramUsageStatDAO();
    private final UsageLogDAO usageLogDAO = new UsageLogDAO();

    /**
     * 현 시점 기준 직전 2개 시간대(예: 13:01 -> 11시, 12시)의 실시간 인기 프로그램 상위 5개 조회
     *
     * @param baseTime 기준 시각 (null인 경우 현재 시각 LocalDateTime.now() 기준)
     * @return 실시간 Top 5 랭킹 응답 DTO
     */
    public PopularRankingDto.Response getRealtimeTop5Rankings(LocalDateTime baseTime) {
        LocalDateTime now = (baseTime != null) ? baseTime : LocalDateTime.now();

        LocalDateTime dt1 = now.minusHours(2);
        LocalDateTime dt2 = now.minusHours(1);

        LocalDate date1 = dt1.toLocalDate();
        int hour1 = dt1.getHour();

        LocalDate date2 = dt2.toLocalDate();
        int hour2 = dt2.getHour();

        try {
            List<UsageLogDAO.RealtimeRankRawData> rawList = usageLogDAO.getTop5RealtimePrograms(
                    date1, hour1, date2, hour2
            );

            if (rawList.isEmpty()) {
                return new PopularRankingDto.Response(true, 200, "실시간 사용 데이터가 없습니다.", new ArrayList<>());
            }

            List<PopularRankingDto.RankingItem> rankingItems = new ArrayList<>();
            List<Double> rawDurations = new ArrayList<>();
            double totalDuration = 0.0;
            int rank = 1;

            for (UsageLogDAO.RealtimeRankRawData raw : rawList) {
                PopularRankingDto.RankingItem item = new PopularRankingDto.RankingItem();
                item.setRank(rank++);
                item.setProgramName(raw.programName);
                item.setCategoryName(raw.categoryName != null ? raw.categoryName : "기타");
                item.setAvgUsageFormatted(formatDuration(raw.totalDurationSec));

                double duration = (double) raw.totalDurationSec;
                rawDurations.add(duration);
                totalDuration += duration;
                rankingItems.add(item);
            }

            // 100% 정수 비율 보정
            if (totalDuration > 0 && !rankingItems.isEmpty()) {
                applyExact100Percentage(rankingItems, rawDurations, totalDuration);
            }

            String msg = String.format("실시간 인기 순위 조회 성공 (%s %d시, %s %d시 기준)",
                    date1, hour1, date2, hour2);
            return new PopularRankingDto.Response(true, 200, msg, rankingItems);

        } catch (Exception e) {
            e.printStackTrace();
            return new PopularRankingDto.Response(false, 500, "실시간 순위 조회 중 오류 발생: " + e.getMessage());
        }
    }

    public PopularRankingDto.Response getPopularRankings(PopularRankingDto.Request request) {
        // 요청 파라미터 검증
        if (request == null) {
            return new PopularRankingDto.Response(false, 400, "요청 데이터가 없습니다.");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return new PopularRankingDto.Response(false, 400, "시작일과 종료일이 필요합니다.");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return new PopularRankingDto.Response(false, 400, "시작일은 종료일보다 늦을 수 없습니다.");
        }
        try {
            List<ProgramUsageStatDAO.RawRankData> rawList = statsDAO.getTop10PopularRankingsRaw(
                    request.getGender(),
                    request.getAgeGroup(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            List<PopularRankingDto.RankingItem> rankingItems = new ArrayList<>();
            List<Double> rawDurations = new ArrayList<>();
            double totalDuration = 0.0;
            int rank = 1;

            for (ProgramUsageStatDAO.RawRankData raw : rawList) {
                PopularRankingDto.RankingItem item = new PopularRankingDto.RankingItem();
                item.setRank(rank++);
                item.setProgramName(raw.programName);
                item.setAvgUsageFormatted(formatDuration((long) raw.avgDurationSec));

                rawDurations.add(raw.avgDurationSec);
                totalDuration += raw.avgDurationSec;
                rankingItems.add(item);
            }

            // 100% 정수 비율 보정
            if (totalDuration > 0 && !rankingItems.isEmpty()) {
                applyExact100Percentage(rankingItems, rawDurations, totalDuration);
            }

            return new PopularRankingDto.Response(true, 200, "인기 순위 조회 성공", rankingItems);

        } catch (Exception e) {
            e.printStackTrace();
            return new PopularRankingDto.Response(false, 500, "조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 100% 보정 알고리즘
    private void applyExact100Percentage(List<PopularRankingDto.RankingItem> items, List<Double> rawDurations, double total) {
        int size = items.size();
        int[] basePercentages = new int[size];
        double[] decimals = new double[size];
        int assignedTotal = 0;

        for (int i = 0; i < size; i++) {
            double exactPercent = (rawDurations.get(i) / total) * 100.0;
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
            items.get(i).setUsagePercentage(basePercentages[i]); //[cite: 3]
        }
    }

    // 시간 포맷 변환
    private String formatDuration(long totalSec) {
        if (totalSec <= 0) return "0초";
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;

        if (hours > 0) return String.format("%d시간 %d분", hours, minutes);
        if (minutes > 0) return String.format("%d분 %d초", minutes, seconds);
        return String.format("%d초", seconds);
    }
//    public static void main(String[] args) {
//        PopularRankingService service = new PopularRankingService();
//
//        // 테스트 요청 객체 생성 (전체 성별, 전체 연령대, 최근 30일)
//        PopularRankingDto.Request testRequest = new PopularRankingDto.Request(
//                "ALL",
//                "ALL",
//                java.time.LocalDate.now().minusDays(30),
//                java.time.LocalDate.now()
//        );
//
//        PopularRankingDto.Response response = service.getPopularRankings(testRequest);
//
//        System.out.println("=== result ===");
//        System.out.println("성공 여부: " + response.isSuccess());
//        System.out.println("응답 메시지: " + response.getMessage());
//
//        if (response.getRankings() != null) {
//            int totalPercent = 0;
//            for (PopularRankingDto.RankingItem item : response.getRankings()) {
//                System.out.printf("[%d위] %s | 평균: %s | 점유율: %d%%\n",
//                        item.getRank(),
//                        item.getProgramName(),
//                        item.getAvgUsageFormatted(),
//                        item.getUsagePercentage());
//                totalPercent += item.getUsagePercentage();
//            }
//            System.out.println("--------------------------------");
//            System.out.println("총 점유율 합계(100% 검증): " + totalPercent + "%");
//        }
//    }
}