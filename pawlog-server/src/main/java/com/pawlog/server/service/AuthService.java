package com.pawlog.server.service;

import com.pawlog.common.dto.user.UserLoginDto;
import com.pawlog.common.dto.user.UserRegisterDto;
import com.pawlog.server.dao.UserDAO;
import com.pawlog.server.entity.UserEntity;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    // 회원가입
    public UserRegisterDto.Response register(UserRegisterDto.Request dto) {
        if (dto == null) {
            return new UserRegisterDto.Response(false, 400, "유효하지 않은 요청입니다.");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty() ||
            dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            return new UserRegisterDto.Response(false, 400, "이메일과 비밀번호는 필수 입력항목입니다.");
        }

        // 이메일 중복 체크
        if (userDAO.checkEmailExists(dto.getEmail())) {
            return new UserRegisterDto.Response(false, 400, "이미 등록된 이메일 계정입니다.");
        }

        java.sql.Date birthDate = null;
        if (dto.getBirth() != null && !dto.getBirth().trim().isEmpty()) {
            try {
                birthDate = java.sql.Date.valueOf(dto.getBirth().trim());
            } catch (Exception e) {
                // 날짜 포맷 이상 시 무시하거나 처리
            }
        }

        UserEntity user = new UserEntity();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPassword());
        user.setNickname(dto.getNickname() != null && !dto.getNickname().trim().isEmpty() ? dto.getNickname() : dto.getEmail().split("@")[0]);
        user.setGender(dto.getGender());
        user.setBirthDate(birthDate);

        try {
            boolean success = userDAO.insertUser(user);
            if (success) {
                return new UserRegisterDto.Response(true, 200, "회원가입에 성공하였습니다.");
            } else {
                return new UserRegisterDto.Response(false, 500, "회원가입 DB 처리 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new UserRegisterDto.Response(false, 500, "회원가입 처리 중 서버 오류가 발생하였습니다.");
        }
    }

    // 로그인
    public UserLoginDto.Response login(UserLoginDto.Request dto) {
        if (dto == null) {
            return new UserLoginDto.Response(false, "유효하지 않은 요청입니다.", null);
        }

        try {
            userDAO.loginChk(dto.getEmail(), dto.getPassword());
            return new UserLoginDto.Response(true, "로그인 성공", dto.getEmail());
        } catch (Exception e) {
            return new UserLoginDto.Response(false, "이메일 또는 비밀번호가 올바르지 않습니다.", null);
        }
    }
}
