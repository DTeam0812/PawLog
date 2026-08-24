package com.pawlog.common.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class MyStatisticsDto {

    // 1. 개인 통계 조회 요청 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String id;              // 유저 PK (예: "123")
        private LocalDate startDate;    // 통계 시작 날짜 (예: "2026-08-01")
        private LocalDate endDate;      // 통계 종료 날짜 (예: "2026-08-24")
    }

    // 2. 프로그램별 점유율 아이템 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProgramRatioItem {
        private String programName;     // 프로그램 이름
        private String programTime;     // 사용 시간 (초 단위 문자열, 예: "1600")
        private int programRatio;       // 점유 비율 (정수 백분율 %, 예: 48)
    }

    // 3. 응답 데이터 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataPayload {
        private String totalTime;               // 총 사용 시간 (초 단위 문자열, 예: "2720")
        private String mostUsedItem;            // 최다 사용 프로그램명 (예: "Chrome")
        private int amountUsedItem;             // 사용된 프로그램 개수 (예: 3)
        private List<ProgramRatioItem> ratio;  // 프로그램별 점유율 목록
    }

    // 4. 개인 통계 조회 응답 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private DataPayload data;

        // 실패/에러 응답용 편의 생성자
        public Response(boolean success, int code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.data = null;
        }

        // 기본 성공/실패 응답 생성자
        public Response(boolean success, String message) {
            this.success = success;
            this.code = success ? 200 : 400;
            this.message = message;
            this.data = null;
        }
    }
}
