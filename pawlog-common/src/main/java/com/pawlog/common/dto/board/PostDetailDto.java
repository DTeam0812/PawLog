package com.pawlog.common.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class PostDetailDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private long postId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class commentitem{
        String commentAuthor;//댓글 단사람 닉네임
        String commmenttime;//작성 시간
        String commentcontent;//내용
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostDetailData {
        private long postId;
        private long userId;
        private long programId;
        private String authorNickname;
        private String title;
        private String content;
        private String createdAt;
        private int likeCount;
        private int commentCount;
        List<commentitem> comments;
        private boolean liked;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private PostDetailData data;

        public Response(boolean success, int code, String message) {
            this(success, code, message, null);
        }
    }
}
