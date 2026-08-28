package com.pawlog.common.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CommentDeleteDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private long commentId;
        private long postId; // 삭제 후 게시글의 총 댓글 수 갱신을 위해 전달
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private int totalCommentCount; // 갱신된 게시글의 남은 댓글 수

        public Response(boolean success, int code, String message) {
            this(success, code, message, 0);
        }
    }
}