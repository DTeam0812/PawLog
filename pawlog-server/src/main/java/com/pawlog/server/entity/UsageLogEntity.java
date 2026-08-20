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

public class UsageLogEntity {
    private long logId;
    private long userId;
    private long programId;
    private Date usageDate;
    private int usageHour;
    private int durationSec;


    @Override
    public String toString() {
        return "UsageLogEntity ["
                + "logId=" + logId
                + ", userId=" + userId
                + ", programId=" + programId
                + ", usageDate=" + usageDate
                + ", usageHour=" + usageHour
                + ", durationSec=" + durationSec
                + "]";
    }
}
