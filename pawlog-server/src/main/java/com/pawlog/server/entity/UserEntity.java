package com.pawlog.server.entity;

import java.sql.Date;
import java.sql.Timestamp;

public class UserEntity {
    private long userId;
    private String email;
    private String passwordHash;
    private String nickname;
    private String gender;
    private Date birthDate;
    private Timestamp createdAt;

    public UserEntity() {
    }

    // 회원가입용 생성자
    public UserEntity(String email, String passwordHash, String nickname, String gender, Date birthDate) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    // 전체 데이터 생성자
    public UserEntity(long userId, String email, String passwordHash, String nickname, String gender, Date birthDate, Timestamp createdAt) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserEntity ["
                + "userId=" + userId
                + ", email=" + email
                + ", nickname=" + nickname
                + ", gender=" + gender
                + ", birthDate=" + birthDate
                + ", createdAt=" + createdAt
                + "]";
    }
}
