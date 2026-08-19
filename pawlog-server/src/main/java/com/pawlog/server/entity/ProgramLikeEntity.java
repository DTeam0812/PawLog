package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ProgramLikeEntity {
    private long userId;
    private long programId;

    @Override
    public String toString() {
        return "ProgramLikeEntity ["
                + "userId=" + userId
                + ", programId=" + programId
                + "]";
    }
}
