package com.pawlog.common.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthRegisterDto {

    // 1. 회원가입 요청 DTO
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Request {
        private String nickname;
        private String id;
        private String password;
        private String birth;
        private String gender;
    }

    // 2. 회원가입 응답 DTO
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Response {
        private boolean success;
        private int code;
        private String message;

        public Response(boolean success, String message) {
            this.success = success;
            this.code = success ? 200 : 400;
            this.message = message;
        }
    }
}
