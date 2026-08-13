package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ProgramRankingEntity {
    private long rankingId;
    private long programId;
    private String periodType;
    private Date rankingDate;
    private int rankPosition;
    private long totalDurationSec;
    private String gender;
    private String ageGroup;


    @Override
    public String toString() {
        return "ProgramRankingDTO ["
                + "rankingId=" + rankingId
                + ", programId=" + programId
                + ", periodType=" + periodType
                + ", rankingDate=" + rankingDate
                + ", rankPosition=" + rankPosition
                + ", totalDurationSec=" + totalDurationSec
                + ", gender=" + gender
                + ", ageGroup=" + ageGroup
                + "]";
    }
}
