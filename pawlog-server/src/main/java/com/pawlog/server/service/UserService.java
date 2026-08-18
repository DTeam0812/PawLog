package com.pawlog.server.service;

import com.pawlog.common.dto.user.UserLoginDto;
import com.pawlog.common.dto.user.UserRegisterDto;
import com.pawlog.server.dao.UserDAO;
import com.pawlog.server.entity.UserEntity;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    // 회원가입
    public UserRegisterDto.Response register(UserRegisterDto.Request dto) {
        if (dto == null) {
            return new UserRegisterDto.Response(false, "유효하지 않은 요청입니다.");
        }
       
        UserEntity user = new UserEntity();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setGender(dto.getGender());
        user.setBirthDate(dto.getBirthDate());
        
        try {
            userDAO.insertUser(user);
            return new UserRegisterDto.Response(true, "회원가입 성공");
        } catch (Exception e) {
            return new UserRegisterDto.Response(false, "회원가입 실패");
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
