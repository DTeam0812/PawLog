package com.pawlog.common.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthUpdateDto {

    // 1. 회원 정보 수정 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String nickname;        // 변경할 닉네임 (미변경 시 null 또는 "")
        private String currentPassword; // 현재 비밀번호 (비밀번호 변경 시 필수)
        private String newPassword;     // 새 비밀번호 (비밀번호 변경 시 필수)
    }

    // 2. 회원 정보 수정 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private int code;
        private String message;
        private String updatedNickname; // 변경된 닉네임 (화면 갱신용, 없으면 null)

        // 실패 및 에러 응답용 생성자
        public Response(boolean success, int code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.updatedNickname = null;
        }
    }
}