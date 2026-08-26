package com.pawlog.common.dto.analytics;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ProgramAnalyticsDto {

    // 1. 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private long programId;
    }

    // 2. 연령대별 사용량 아이템 DTO (8개 구간)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgeGroupItem {
        private String ageGroupName; // "10대 미만", "10대", "20대", ..., "70대 이상"
        private String timeFormatted; // 예: "2시간 15분", "0초"
        private int percentage;       // 100% 보정된 정수 비율
    }

    // 3. 분석 상세 데이터 페이로드
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsData {
        private long programId;
        private String programName;

        // 요약 카드 3종
        private String avgDailyUsage;       // "3시간 12분"
        private String totalRank;           // "2위" (사용자 없을 시 "순위 없음")
        private String dayOverDayDiff;      // "+28분", "-15분", "0초"
        private String dayOverDaySubText;   // "전날보다 14% 더 오래 사용", "전날(D-2) 기록 없음 (신규 활성화)" 등
        private String dayOverDayLabel;     // "2026-08-25 기준 전날 대비"

        // 차트 1: 4시간 단위 6구간 시간대별 평균 사용 시간(초 단위 정수)
        // [0~3시, 4~7시, 8~11시, 12~15시, 16~19시, 20~23시]
        private int[] hourlyUsage;

        // 차트 2: 연령대별 사용 통계 (8개 구간)
        private List<AgeGroupItem> ageGroupStats;
    }

    // 4. 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private AnalyticsData data;

        public Response(boolean success, int code, String message) {
            this(success, code, message, null);
        }
    }
}