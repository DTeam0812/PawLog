package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class UsageLogEntity {
    private long logId;
    private long userId;
    private long programId;
    private boolean isForeground;
    private Timestamp startTime;
    private Timestamp endTime;
    private int durationSec;


    @Override
    public String toString() {
        return "UsageLogEntity ["
                + "logId=" + logId
                + ", userId=" + userId
                + ", programId=" + programId
                + ", isForeground=" + isForeground
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", durationSec=" + durationSec
                + "]";
    }
}
