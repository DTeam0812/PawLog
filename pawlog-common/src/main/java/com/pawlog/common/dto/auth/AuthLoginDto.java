package com.pawlog.common.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthLoginDto {

    // 1. 로그인 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String email;
        private String password;
    }

    // 2. 로그인 응답 DTO 데이터 (User Summary)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private long userId;
        private String email;
        private String nickname;
        private String birthDate;

        public UserInfo(Long userId, String email, String nickname) {
            this(userId, email, nickname, "");
        }
    }

    // 3. 로그인 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private UserInfo user; // 성공 시 유저 데이터, 실패 시 null

        public Response(boolean success, int code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.user = null;
        }

        public Response(boolean success, String message, String nickname) {
            this.success = success;
            this.code = success ? 200 : 401;
            this.message = message;
            this.user = null;
        }
    }
}
