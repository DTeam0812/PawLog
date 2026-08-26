package com.pawlog.server.test;

import com.pawlog.common.dto.ranking.PopularRankingDto;
import com.pawlog.server.service.PopularRankingService;

import java.time.LocalDateTime;

public class RealtimeRankingTest {

    public static void main(String[] args) {
        PopularRankingService service = new PopularRankingService();

        System.out.println("================================================================");
        System.out.println("   [테스트 1] 현재 시각 기준 실시간 Top 5 랭킹");
        System.out.println("================================================================");
        LocalDateTime now = LocalDateTime.now();
        testRealtimeRanking(service, now);

        System.out.println("\n================================================================");
        System.out.println("   [테스트 2] 지정 시각 (2026-08-26 13:01 -> 11시, 12시 기준) 시뮬레이션");
        System.out.println("================================================================");
        LocalDateTime customTime = LocalDateTime.of(2026, 8, 26, 13, 1, 0);
        testRealtimeRanking(service, customTime);
    }

    private static void testRealtimeRanking(PopularRankingService service, LocalDateTime baseTime) {
        LocalDateTime dt1 = baseTime.minusHours(2);
        LocalDateTime dt2 = baseTime.minusHours(1);

        System.out.printf(">> 기준 시각: %s%n", baseTime);
        System.out.printf(">> 조회 대상 시간대: [%s %d시] 및 [%s %d시]%n",
                dt1.toLocalDate(), dt1.getHour(),
                dt2.toLocalDate(), dt2.getHour());

        PopularRankingDto.Response response = service.getRealtimeTop5Rankings(baseTime);

        System.out.println("성공 여부: " + response.isSuccess());
        System.out.println("응답 코드: " + response.getCode());
        System.out.println("메시지: " + response.getMessage());

        if (response.getRankings() != null && !response.getRankings().isEmpty()) {
            System.out.println("----------------------------------------------------------------");
            System.out.printf("%-6s | %-20s | %-12s | %-12s | %-8s%n",
                    "순위", "프로그램명", "카테고리", "총 사용시간", "점유율");
            System.out.println("----------------------------------------------------------------");

            int totalPercent = 0;
            for (PopularRankingDto.RankingItem item : response.getRankings()) {
                System.out.printf("%-6d | %-20s | %-12s | %-12s | %d%%%n",
                        item.getRank(),
                        item.getProgramName(),
                        item.getCategoryName(),
                        item.getAvgUsageFormatted(),
                        item.getUsagePercentage());
                totalPercent += item.getUsagePercentage();
            }
            System.out.println("----------------------------------------------------------------");
            System.out.printf("총 점유율 합계: %d%%%n", totalPercent);
        } else {
            System.out.println(">> 해당 시간대에 사용 기록(usage_logs)이 없습니다.");
        }
    }
}
