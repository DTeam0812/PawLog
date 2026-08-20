package com.pawlog.common.dto.ranking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

public class PopularRankingDto {

    // 1. 요청 DTO
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Request {
        private String ageGroup;   // "ALL", "10s", "20s", "30s" 등
        private String gender;     // "ALL", "M", "F"
        private LocalDate startDate;
        private LocalDate endDate;
    }

    // 2. 단일 순위 아이템
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class RankingItem {
        private int rank;
        private String programName;
        private String categoryName;
        private String avgUsageFormatted;
        private int usagePercentage;
    }

    // 3. 응답 DTO
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Response {
        private boolean success;
        private String message;
        private List<RankingItem> rankings;
    }
}
