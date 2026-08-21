package com.pawlog.server.service;

import com.pawlog.common.dto.ranking.PopularRankingDto;
import com.pawlog.server.dao.ProgramUsageStatDAO;

import java.util.ArrayList;
import java.util.List;

public class PopularRankingService {

    private final ProgramUsageStatDAO statsDAO = new ProgramUsageStatDAO();

    public PopularRankingDto.Response getPopularRankings(PopularRankingDto.Request request) {
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
                item.setCategoryName(raw.categoryName);
                item.setAvgUsageFormatted(formatDuration((long) raw.avgDurationSec));

                rawDurations.add(raw.avgDurationSec);
                totalDuration += raw.avgDurationSec;
                rankingItems.add(item);
            }

            // 100% 정수 비율 보정 (Service 책임)
            if (totalDuration > 0 && !rankingItems.isEmpty()) {
                applyExact100Percentage(rankingItems, rawDurations, totalDuration);
            }

            return new PopularRankingDto.Response(true, "인기 순위 조회 성공", rankingItems);

        } catch (Exception e) {
            e.printStackTrace();
            return new PopularRankingDto.Response(false, "조회 중 오류 발생: " + e.getMessage(), null);
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
//                System.out.printf("[%d위] %s (%s) | 평균: %s | 점유율: %d%%\n",
//                        item.getRank(),
//                        item.getProgramName(),
//                        item.getCategoryName(),
//                        item.getAvgUsageFormatted(),
//                        item.getUsagePercentage());
//                totalPercent += item.getUsagePercentage();
//            }
//            System.out.println("--------------------------------");
//            System.out.println("총 점유율 합계(100% 검증): " + totalPercent + "%");
//        }
//    }
}