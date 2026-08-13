package com.pawlog.common.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Date;

public class UserRegisterDto {

    // 1. 회원가입 요청 DTO
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Request {
        private String email;
        private String password;
        private String nickname;
        private String gender;
        private Date birthDate;
    }

    // 2. 회원가입 응답 DTO
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        private boolean success;
        private String message;
    }
}