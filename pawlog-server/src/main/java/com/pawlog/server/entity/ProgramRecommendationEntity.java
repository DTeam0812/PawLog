package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@AllArgsConstructor
@Getter
@Setter

public class ProgramRecommendationEntity {
    private long recId;
    private long userId;
    private long programId;
    private String reason; // NULLABLE
    private Timestamp createdAt;


    @Override
    public String toString() {
        return "ProgramRecommendationEntity ["
                + "recId=" + recId
                + ", userId=" + userId
                + ", programId=" + programId
                + ", reason=" + reason
                + ", createdAt=" + createdAt
                + "]";
    }
}