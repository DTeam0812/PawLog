package com.pawlog.common.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostLikeDto {

    // 1. 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private long postId;
    }

    // 2. 실제 데이터 페이로드 (data 필드에 들어갈 순수 데이터)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeData {
        private boolean liked;
        private int likeCount;
    }

    // 3. 통신 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private LikeData data;

        public Response(boolean success, int code, String message) {
            this(success, code, message, null);
        }
    }
}
