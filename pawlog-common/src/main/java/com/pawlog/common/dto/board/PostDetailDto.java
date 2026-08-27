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
        private long commentId;//삭제용 식별자
        private long userId;//작성자 ID
        private String commentAuthor;//댓글 단사람 닉네임
        private String commenttime;//작성 시간
        private String commentcontent;//내용
        private boolean mine;//본인 댓글 여부
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
