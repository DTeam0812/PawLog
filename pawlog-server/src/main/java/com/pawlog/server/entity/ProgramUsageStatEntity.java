package com.pawlog.server.entity;

import java.sql.Date;

public class ProgramUsageStatEntity {
    private long statId;
    private long programId;
    private String periodType;
    private Date statDate;

    private float avgForegroundSec;
    private float avgBackgroundSec;
    private float avgDurationSec;

    private int totalUsers;

    private String gender;
    private String ageGroup;

    // 기본 생성자
    public ProgramUsageStatEntity() {
    }

    // 데이터 입력용 생성자
    public ProgramUsageStatEntity(long programId,
                                String periodType,
                                Date statDate,
                                float avgForegroundSec,
                                float avgBackgroundSec,
                                float avgDurationSec,
                                int totalUsers,
                                String gender,
                                String ageGroup) {

        this.programId = programId;
        this.periodType = periodType;
        this.statDate = statDate;
        this.avgForegroundSec = avgForegroundSec;
        this.avgBackgroundSec = avgBackgroundSec;
        this.avgDurationSec = avgDurationSec;
        this.totalUsers = totalUsers;
        this.gender = gender;
        this.ageGroup = ageGroup;
    }

    // 전체 데이터 생성자
    public ProgramUsageStatEntity(long statId,
                                long programId,
                                String periodType,
                                Date statDate,
                                float avgForegroundSec,
                                float avgBackgroundSec,
                                float avgDurationSec,
                                int totalUsers,
                                String gender,
                                String ageGroup) {

        this.statId = statId;
        this.programId = programId;
        this.periodType = periodType;
        this.statDate = statDate;
        this.avgForegroundSec = avgForegroundSec;
        this.avgBackgroundSec = avgBackgroundSec;
        this.avgDurationSec = avgDurationSec;
        this.totalUsers = totalUsers;
        this.gender = gender;
        this.ageGroup = ageGroup;
    }

    public long getStatId() {
        return statId;
    }

    public void setStatId(long statId) {
        this.statId = statId;
    }

    public long getProgramId() {
        return programId;
    }

    public void setProgramId(long programId) {
        this.programId = programId;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public Date getStatDate() {
        return statDate;
    }

    public void setStatDate(Date statDate) {
        this.statDate = statDate;
    }

    public float getAvgForegroundSec() {
        return avgForegroundSec;
    }

    public void setAvgForegroundSec(float avgForegroundSec) {
        this.avgForegroundSec = avgForegroundSec;
    }

    public float getAvgBackgroundSec() {
        return avgBackgroundSec;
    }

    public void setAvgBackgroundSec(float avgBackgroundSec) {
        this.avgBackgroundSec = avgBackgroundSec;
    }

    public float getAvgDurationSec() {
        return avgDurationSec;
    }

    public void setAvgDurationSec(float avgDurationSec) {
        this.avgDurationSec = avgDurationSec;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    @Override
    public String toString() {
        return "ProgramUsageStatsDTO ["
                + "statId=" + statId
                + ", programId=" + programId
                + ", periodType=" + periodType
                + ", statDate=" + statDate
                + ", avgForegroundSec=" + avgForegroundSec
                + ", avgBackgroundSec=" + avgBackgroundSec
                + ", avgDurationSec=" + avgDurationSec
                + ", totalUsers=" + totalUsers
                + ", gender=" + gender
                + ", ageGroup=" + ageGroup
                + "]";
    }
}
