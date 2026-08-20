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

public class DailyUsageSummaryEntity {
    private long summaryId;
    private long userId;
    private long programId;
    private Date summaryDate;
    private long totalDurationSec;

    @Override
    public String toString() {
        return "DailyUsageSummaryEntity ["
                + "summaryId=" + summaryId
                + ", userId=" + userId
                + ", programId=" + programId
                + ", summaryDate=" + summaryDate
                + ", totalDurationSec=" + totalDurationSec
                + "]";
    }
}
