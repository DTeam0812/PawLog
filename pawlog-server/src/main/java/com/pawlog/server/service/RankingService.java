package com.pawlog.server.service;

import com.pawlog.common.dto.ranking.PopularRankingDto;

import java.util.ArrayList;
import java.util.List;

public class RankingService {

    // 인기 프로그램 순위 데이터 조회 (더미 데이터 반환)
    public PopularRankingDto.Response getPopularRankings(PopularRankingDto.Request dto) {
        if (dto == null) {
            return new PopularRankingDto.Response(false, 400, "유효하지 않은 요청입니다.", null);
        }

        List<PopularRankingDto.RankingItem> items = new ArrayList<>();

        // 이미지 UI에 맞춘 상위 10개 프로그램 순위 데이터
       

        String filterInfo = String.format("(연령대: %s, 성별: %s)",
                dto.getAgeGroup() != null ? dto.getAgeGroup() : "전체",
                dto.getGender() != null ? dto.getGender() : "전체");

        return new PopularRankingDto.Response(true, 200, "인기 순위 조회 성공 " + filterInfo, items);
    }

    // 테스트용
    /*public static void main(String[] args) {
        RankingService rankingService = new RankingService();

        // 1. 전체 조건 요청 테스트
        PopularRankingDto.Request req = new PopularRankingDto.Request("ALL", "ALL", "DAILY");
        PopularRankingDto.Response res = rankingService.getPopularRankings(req);

        System.out.println("==============================================");
        System.out.println("Status  : " + (res.isSuccess() ? "SUCCESS" : "FAIL"));
        System.out.println("Message : " + res.getMessage());
        System.out.println("==============================================");
        System.out.printf("%-4s | %-12s | %-10s | %-10s | %s\n", "순위", "프로그램명", "카테고리", "평균 사용시간", "점유율");
        System.out.println("----------------------------------------------");

        if (res.getRankings() != null) {
            for (PopularRankingDto.RankingItem item : res.getRankings()) {
                System.out.printf("%-4d | %-12s | %-10s | %-12s | %d%%\n",
                        item.getRank(),
                        item.getProgramName(),
                        item.getCategoryName(),
                        item.getAvgUsageFormatted(),
                        item.getUsagePercentage());
            }
        }
        System.out.println("==============================================");
    }*/
}
