package com.pawlog.common.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostEnrollDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        private long programId;
        private String title;
        private String content;

    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private boolean success;
        private int code;
        private String message;
        private Long postId;

    }


}
