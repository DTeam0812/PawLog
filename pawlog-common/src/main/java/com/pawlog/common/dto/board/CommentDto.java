package com.pawlog.common.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CommentDto {

    // 1. 댓글 작성 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private long postId;
        private String content;
    }

    // 2. 댓글 작성 응답 데이터 페이로드 (UI 즉시 렌더링용)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentData {
        private long commentId;
        private long postId;
        private long userId;
        private String authorNickname;
        private String content;
        private String createdAt;
        private int totalCommentCount; // 갱신된 총 댓글 수
    }

    // 3. 통신 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private CommentData data;

        public Response(boolean success, int code, String message) {
            this(success, code, message, null);
        }
    }
}