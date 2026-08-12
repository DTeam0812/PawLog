package com.pawlog.server.service;

import com.pawlog.server.dao.UserDAO;
import com.pawlog.server.entity.UserEntity;
import java.sql.Date;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public boolean register(String email, String password, String nickname, String gender, Date birthDate) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(password); // (비밀번호)
        user.setNickname(nickname);
        user.setGender(gender);
        user.setBirthDate(birthDate);

        return userDAO.insertUser(user);
    }

    public static void main(String[] args) {
        UserService userService = new UserService();
        Date date = new Date(2026, 8, 12);
        userService.register("teste1", "testp2", "테스트중", "testg3", date);
    }
}

