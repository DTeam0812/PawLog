package com.pawlog.common.dto.board;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostJoinDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        long postid;
        //로그인 세션확인은 서버에서
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class commentitem{
        String commentAuthor;//댓글 단사람 닉네임
        String commenttime;//작성 시간
        String commentcontent;//내용
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        long postId;//게시글id
        String Authorname;//작성자
        String title;//제목
        String content;//내용
        String CreatedAt;
        List<commentitem> comments;//댓글
        int like;//추천

    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        boolean success;
        int code;
        String message;
        Post data;
    }
}
