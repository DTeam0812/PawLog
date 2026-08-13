package com.pawlog.common.dto.user;

public class UserLoginDto {

    // 1. 로그인 요청 DTO
    public static class Request {
        private String email;
        private String password;

        public Request() {
        }

        public Request(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // 2. 로그인 응답 DTO
    public static class Response {
        private boolean success;
        private String message;
        private String nickname;

        public Response() {
        }

        public Response(boolean success, String message, String nickname) {
            this.success = success;
            this.message = message;
            this.nickname = nickname;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}
