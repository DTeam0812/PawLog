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

public class ProgramUsageStatEntity {
    private long statId;
    private long programId;
    private String periodType;
    private Date statDate;
    private float avgDurationSec;
    private int totalUsers;
    private String gender;
    private String ageGroup;

    @Override
    public String toString() {
        return "ProgramUsageStatEntity ["
                + "statId=" + statId
                + ", programId=" + programId
                + ", periodType=" + periodType
                + ", statDate=" + statDate
                + ", avgDurationSec=" + avgDurationSec
                + ", totalUsers=" + totalUsers
                + ", gender=" + gender
                + ", ageGroup=" + ageGroup
                + "]";
    }
}
