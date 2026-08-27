package com.pawlog.common.dto.board;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RecentPopularProgramDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private int count;

    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Hotprogram {
        private String programid;
        private String programname;
        private int recommmendcount;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private boolean success;
        private String message;
        private int code;
        private List<Hotprogram> data;
    }
}
