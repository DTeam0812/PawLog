package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter

public class PostLikeEntity {
    private long userId;
    private long postId;



    @Override
    public String toString() {
        return "PostLikeEntity ["
                + "userId=" + userId
                + ", postId=" + postId
                + "]";
    }
}