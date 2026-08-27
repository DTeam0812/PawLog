package com.pawlog.common.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostDeleteDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private long postId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;

        public Response(boolean success, String message) {
            this(success, success ? 200 : 400, message);
        }
    }
}