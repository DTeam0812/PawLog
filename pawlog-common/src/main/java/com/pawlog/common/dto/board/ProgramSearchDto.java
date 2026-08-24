package com.pawlog.common.dto.board;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ProgramSearchDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String keyword;//키워드 
        private int limit;//서버 최대 반환값 
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProgramItem {
        private long programId;
        private String processName;
        private String displayName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private List<ProgramItem> programs;
    }
}
