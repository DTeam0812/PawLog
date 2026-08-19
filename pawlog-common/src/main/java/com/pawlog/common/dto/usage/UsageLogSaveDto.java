package com.pawlog.common.dto.usage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class UsageLogSaveDto {

    // 1. 사용 기록 저장 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String email;
        private String usageDate; // "yyyy-MM-dd"
        private List<LogItem> logs;
    }

    // 2. 시간대별 개별 사용 기록 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogItem {
        private int hour;
        private String programName;
        private long durationSec;
    }

    // 3. 사용 기록 저장 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private Integer savedCount;

        public Response(boolean success, int code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.savedCount = 0;
        }
    }
}
