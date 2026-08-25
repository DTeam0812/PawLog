package com.pawlog.common.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostLikeDto {

    // 게시글 좋아요 토글 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private long postId;
    }

    // 게시글 좋아요 토글 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private boolean liked;       // 본인의 변경된 좋아요 상태 (true: 추천됨, false: 취소됨)
        private int likeCount;       // 변경 후 최신 총 좋아요 수 (COUNT(*) 결과)

        public Response(boolean success, int code, String message) {
            this(success, code, message, false, 0);
        }
    }
}