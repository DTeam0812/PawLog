package com.pawlog.common.dto.board;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostListDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private long programId;
        private int page;
        private int pageSize;
        private String sort;
        private String keyword;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostItem {
        private long postId;
        private long userId;
        private long programId;
        private String authorNickname;
        private String title;
        
        private String createdAt;
        private int likeCount;
        private int commentCount;
        private boolean liked;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageData {
        private List<PostItem> posts;
        private int currentPage;
        private int totalPages;
        private long totalCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private PageData data;
    }
}
