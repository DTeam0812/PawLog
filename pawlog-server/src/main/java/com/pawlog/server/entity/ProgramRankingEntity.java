package com.pawlog.server.entity;

import java.sql.Date;

public class ProgramRankingEntity {
    private long rankingId;
    private long programId;
    private String periodType;
    private Date rankingDate;
    private int rankPosition;
    private long totalDurationSec;
    private String gender;
    private String ageGroup;

    // 기본 생성자
    public ProgramRankingEntity() {
    }

    // 데이터 입력용 생성자
    public ProgramRankingEntity(long programId,
                             String periodType,
                             Date rankingDate,
                             int rankPosition,
                             long totalDurationSec,
                             String gender,
                             String ageGroup) {

        this.programId = programId;
        this.periodType = periodType;
        this.rankingDate = rankingDate;
        this.rankPosition = rankPosition;
        this.totalDurationSec = totalDurationSec;
        this.gender = gender;
        this.ageGroup = ageGroup;
    }

    // 전체 데이터 생성자
    public ProgramRankingEntity(long rankingId,
                             long programId,
                             String periodType,
                             Date rankingDate,
                             int rankPosition,
                             long totalDurationSec,
                             String gender,
                             String ageGroup) {

        this.rankingId = rankingId;
        this.programId = programId;
        this.periodType = periodType;
        this.rankingDate = rankingDate;
        this.rankPosition = rankPosition;
        this.totalDurationSec = totalDurationSec;
        this.gender = gender;
        this.ageGroup = ageGroup;
    }

    // Getter / Setter

    public long getRankingId() {
        return rankingId;
    }

    public void setRankingId(long rankingId) {
        this.rankingId = rankingId;
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

    public Date getRankingDate() {
        return rankingDate;
    }

    public void setRankingDate(Date rankingDate) {
        this.rankingDate = rankingDate;
    }

    public int getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(int rankPosition) {
        this.rankPosition = rankPosition;
    }

    public long getTotalDurationSec() {
        return totalDurationSec;
    }

    public void setTotalDurationSec(long totalDurationSec) {
        this.totalDurationSec = totalDurationSec;
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
