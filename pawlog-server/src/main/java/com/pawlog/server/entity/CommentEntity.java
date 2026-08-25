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

public class CommentEntity {
    private long commentId;
    private long postId;
    private long userId;
    private String content;
    private Timestamp createdAt;
    private Boolean isActive;



    @Override
    public String toString() {
        return "CommentEntity ["
                + "commentId=" + commentId
                + ", postId=" + postId
                + ", userId=" + userId
                + ", content=" + content
                + ", createdAt=" + createdAt
                + ", isActive=" + isActive
                + "]";
    }
}