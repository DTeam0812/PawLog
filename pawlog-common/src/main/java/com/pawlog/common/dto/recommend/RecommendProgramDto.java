package com.pawlog.common.dto.recommend;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RecommendProgramDto {

    // 1. 요청 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private int count; // 조회할 프로그램 개수
    }

    // 2. 단일 핫 프로그램 아이템 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        @JsonProperty("programid")
        @JsonAlias({"programid", "programId"})
        private String programid;

        @JsonProperty("programname")
        @JsonAlias({"programname", "programName"})
        private String programname;

        @JsonProperty("recommmendcount")
        @JsonAlias({"recommmendcount", "recommendCount", "recommendcount"})
        private int recommmendcount;
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
            this(success, code, message, null);
        }

        public Response(boolean success, String message) {
            this(success, success ? 200 : 400, message, null);
        }
    }
}
