package com.pawlog.common.dto.statistics;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TodayStatisticsDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        private String id;
        @JsonAlias({"id", "email"})
        private String email;
        private String usageDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProgramItem {
        private String programName;
        private long usageSeconds;
        private int usageRatio;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataPayload {
        private long totalUsageSeconds;
        private String mostUsedProgram;
        private int programCount;
        private List<ProgramItem> programs;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private DataPayload data;

        public Response(boolean success, int code, String message) {
            this(success, code, message, null);
        }
    }
}
