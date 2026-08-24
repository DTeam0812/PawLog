package com.pawlog.common.dto.ranking;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

public class PopularRankingDto {

    // 1. 인기 순위 조회 요청 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String ageGroup;   // "ALL", "10s", "20s", ... , "60s"
        private String gender;     // "ALL", "M", "F"
        private LocalDate startDate;
        private LocalDate endDate;
    }

    // 2. 단일 순위 아이템 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RankingItem {
        private int rank;
        private String programName;
        private String avgUsageFormatted;
        private int usagePercentage;
    }

    // 3. 인기 순위 조회 응답 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private List<RankingItem> rankings; // 실패 시 null

        // 실패/에러 응답용 편의 생성자
        public Response(boolean success, int code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.rankings = null;
        }

        // 기본 성공/실패 메시지용 생성자
        public Response(boolean success, String message) {
            this.success = success;
            this.code = success ? 200 : 400;
            this.message = message;
            this.rankings = null;
        }
    }
}
