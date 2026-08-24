package com.pawlog.common.dto.ranking;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MyStatisticsDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String id;
        private String startDate;
        private String endDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProgramUsageRatio {
        private String programName;
        private String programTime;
        private int programRatio;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MyStatistics {
        private String totalTime;
        private String mostUsedItem;
        private int amountUsedItem;
        private List<ProgramUsageRatio> ratio;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private MyStatistics data;
    }
}
