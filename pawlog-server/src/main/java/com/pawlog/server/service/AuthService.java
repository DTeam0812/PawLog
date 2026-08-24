package com.pawlog.server.service;

import com.pawlog.common.dto.auth.AuthLoginDto;
import com.pawlog.common.dto.auth.AuthRegisterDto;
import com.pawlog.common.dto.auth.AuthUpdateDto;
import com.pawlog.server.dao.UserDAO;
import com.pawlog.server.entity.UserEntity;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    // 회원가입
    public AuthRegisterDto.Response register(AuthRegisterDto.Request dto) {
        if (dto == null) {
            return new AuthRegisterDto.Response(false, 400, "유효하지 않은 요청입니다.");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty() ||
            dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            return new AuthRegisterDto.Response(false, 400, "이메일과 비밀번호는 필수 입력항목입니다.");
        }

        // 이메일 중복 체크
        if (userDAO.checkEmailExists(dto.getEmail())) {
            return new AuthRegisterDto.Response(false, 400, "이미 등록된 이메일 계정입니다.");
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
                return new AuthRegisterDto.Response(true, 200, "회원가입에 성공하였습니다.");
            } else {
                return new AuthRegisterDto.Response(false, 500, "회원가입 DB 처리 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthRegisterDto.Response(false, 500, "회원가입 처리 중 서버 오류가 발생하였습니다.");
        }
    }

    // 로그인
    public AuthLoginDto.Response login(AuthLoginDto.Request dto) {
        if (dto == null || dto.getEmail() == null || dto.getEmail().trim().isEmpty() ||
            dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            return new AuthLoginDto.Response(false, 400, "이메일과 비밀번호를 입력해 주세요.");
        }

        try {
            UserEntity user = userDAO.getUserByEmailAndPassword(dto.getEmail().trim(), dto.getPassword());
            if (user != null) {
                AuthLoginDto.UserInfo userInfo = new AuthLoginDto.UserInfo(user.getUserId(), user.getEmail(), user.getNickname());
                return new AuthLoginDto.Response(true, 200, "로그인에 성공하였습니다.", userInfo);
            } else {
                return new AuthLoginDto.Response(false, 401, "이메일 또는 비밀번호가 올바르지 않습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthLoginDto.Response(false, 500, "로그인 처리 중 서버 오류가 발생하였습니다.");
        }
    }

    // 마이페이지 정보 수정 서비스 (닉네임, 비밀번호 선택적 변경)
    public AuthUpdateDto.Response updateUser(long userId, AuthUpdateDto.Request dto) {
        if (dto == null) {
            return new AuthUpdateDto.Response(false, 400, "수정할 데이터가 없습니다.");
        }

        try {
            UserEntity currentUser = userDAO.getUserById(userId);
            if (currentUser == null) {
                return new AuthUpdateDto.Response(false, 404, "사용자 정보를 찾을 수 없습니다.");
            }

            String newNickname = dto.getNickname();
            String newPassword = dto.getNewPassword();
            String currentPassword = dto.getCurrentPassword();

            // 1. 비밀번호 변경 시 유효성 검증
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (currentPassword == null || currentPassword.trim().isEmpty()) {
                    return new AuthUpdateDto.Response(false, 400, "비밀번호 변경을 위해 현재 비밀번호를 입력해야 합니다.");
                }

                // 현재 비밀번호와 새 비밀번호 동일 여부 체크
                if (currentPassword.equals(newPassword)) {
                    return new AuthUpdateDto.Response(false, 400, "새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다.");
                }

                // DB 비밀번호와 대조
                if (!currentUser.getPasswordHash().equals(currentPassword)) {
                    return new AuthUpdateDto.Response(false, 401, "현재 비밀번호가 일치하지 않습니다.");
                }
            }

            // 2. 수정 대상 필드 존재 여부 체크
            if ((newNickname == null || newNickname.trim().isEmpty()) &&
                    (newPassword == null || newPassword.trim().isEmpty())) {
                return new AuthUpdateDto.Response(false, 400, "수정할 항목이 지정되지 않았습니다.");
            }

            // 3. DB 동적 업데이트 수행
            boolean isUpdated = userDAO.updateUser(userId, newNickname, newPassword);
            if (isUpdated) {
                String updatedNick = (newNickname != null && !newNickname.trim().isEmpty()) ? newNickname.trim() : currentUser.getNickname();
                return new AuthUpdateDto.Response(true, 200, "회원 정보가 성공적으로 변경되었습니다.", updatedNick);
            } else {
                return new AuthUpdateDto.Response(false, 500, "회원 정보 수정 DB 처리 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new AuthUpdateDto.Response(false, 500, "정보 수정 중 서버 오류 발생: " + e.getMessage());
        }
    }

    // 소켓 세션 등록용 사용자 정보 조회
    public UserEntity getUserByEmailAndPassword(String email, String password) {
        return userDAO.getUserByEmailAndPassword(email, password);
    }
}
