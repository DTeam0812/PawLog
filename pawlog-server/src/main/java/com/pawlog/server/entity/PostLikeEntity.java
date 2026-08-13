package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
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