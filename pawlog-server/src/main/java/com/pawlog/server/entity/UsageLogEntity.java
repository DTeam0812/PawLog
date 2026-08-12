package com.pawlog.server.entity;

import java.sql.Timestamp;

public class UsageLogEntity {
    private long logId;
    private long userId;
    private long programId;
    private boolean isForeground;
    private Timestamp startTime;
    private Timestamp endTime;
    private int durationSec;

    public UsageLogEntity() {
    }

    public UsageLogEntity(long userId, long programId, boolean isForeground, Timestamp startTime, Timestamp endTime, int durationSec) {
        this.userId = userId;
        this.programId = programId;
        this.isForeground = isForeground;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSec = durationSec;
    }

    public UsageLogEntity(long logId, long userId, long programId, boolean isForeground, Timestamp startTime, Timestamp endTime, int durationSec) {
        this.logId = logId;
        this.userId = userId;
        this.programId = programId;
        this.isForeground = isForeground;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSec = durationSec;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getProgramId() {
        return programId;
    }

    public void setProgramId(long programId) {
        this.programId = programId;
    }

    public boolean isForeground() {
        return isForeground;
    }

    public void setForeground(boolean isForeground) {
        this.isForeground = isForeground;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

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
