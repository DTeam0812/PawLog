package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class PostEntity {
    private long postId;
    private long userId;
    private Long programId;
    private String title;
    private String content;
    private Timestamp createdAt;

    @Override
    public String toString() {
        return "PostEntity ["
                + "postId=" + postId
                + ", userId=" + userId
                + ", programId=" + programId
                + ", title=" + title
                + ", content=" + content
                + ", createdAt=" + createdAt
                + "]";
    }
}