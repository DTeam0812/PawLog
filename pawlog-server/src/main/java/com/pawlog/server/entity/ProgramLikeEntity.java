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
    private long programId;
    private long userId;

    @Override
    public String toString() {
        return "ProgramLikeEntity ["
                + "programId=" + programId
                + ", userId=" + userId
                + "]";
    }
}
