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
        private long programId;//조회할 프로그램 id
        private int page;//조회할 페이지번호
        private int pageSize;//조회할 페이지 크기
        private String sort;//정렬 방식
        private String keyword;//게시글 검색어
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostItem {
        private long postId;//포스트
        private long userId;//유저아이디
        private long programId;//프로그램 id(키워드)
        private String authorNickname;//저자
        private String title;//제목
        private String createdAt;
        private int likeCount;//좋아요 갯수
        private int commentCount;//댓글수

       private int likecount;//좋아요 갯수
        private boolean liked;//추천 여부

        

        public int getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
        }

        public int getLikecount() {
            return likecount;
        }

        public void setLikecount(int likecount) {
            this.likecount = likecount;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageData {
        private List<PostItem> posts;//현재 페이지에 표시할글
        private int currentPage;//현재페이지
        private int totalPages;//총 페이지
        private long totalCount;//조건에맞는 게시글수
        
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
