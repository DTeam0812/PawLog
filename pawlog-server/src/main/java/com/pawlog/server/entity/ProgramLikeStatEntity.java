package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProgramLikeStatEntity {
    private long programId;
    private long likeCount;

    @Override
    public String toString() {
        return "ProgramLikeStatEntity ["
                + "programId=" + programId
                + ", likeCount=" + likeCount
                + "]";
    }
}