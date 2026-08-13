package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class PredictionVoteEntity {
    private long voteId;
    private long eventId;
    private long userId;
    private long programId;


    @Override
    public String toString() {
        return "PredictionVoteEntity ["
                + "voteId=" + voteId
                + ", eventId=" + eventId
                + ", userId=" + userId
                + ", programId=" + programId
                + "]";
    }
}