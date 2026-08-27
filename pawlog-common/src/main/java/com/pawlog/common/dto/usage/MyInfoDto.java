package com.pawlog.common.dto.usage;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MyInfoDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        private String id;
        private String usageDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TodayInfo {
        private long totalUsageSeconds;
        private String mostUsedProgram;
        private int programCount;
        private List<ProgramStatistics> programs;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProgramStatistics {
        private String programName;
        private long usageSeconds;  
        private int usageRatio;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private TodayInfo data;
    }
}
