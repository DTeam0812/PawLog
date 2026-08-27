package com.pawlog.common.dto.recommend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class RecommendProgramDto {

    // 1. 요청 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private int count; // 조회할 프로그램 개수
    }

    // 2. 단일 추천 프로그램 아이템 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String program;
        private int usercount;
    }

    // 3. 응답 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private List<Item> data;

        public Response(boolean success, int code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.data = null;
        }

        public Response(boolean success, String message) {
            this.success = success;
            this.code = success ? 200 : 400;
            this.message = message;
            this.data = null;
        }
    }
}
