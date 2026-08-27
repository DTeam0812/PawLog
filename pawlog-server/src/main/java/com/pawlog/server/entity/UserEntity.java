package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.sql.Date;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class UserEntity {
    private long userId;
    private String id;
    private String passwordHash;
    private String nickname;
    private String gender;
    private Date birthDate;
    private Timestamp createdAt;

    //passwordHash 제외
    @Override
    public String toString() {
        return "UserEntity ["
                + "userId=" + userId
                + ", id=" + id
                + ", nickname=" + nickname
                + ", gender=" + gender
                + ", birthDate=" + birthDate
                + ", createdAt=" + createdAt
                + "]";
    }
}
