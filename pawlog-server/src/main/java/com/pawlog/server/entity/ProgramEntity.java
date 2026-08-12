package com.pawlog.server.entity;

public class ProgramEntity {
    private long programId;
    private String processName;
    private String displayName;
    private Integer categoryId;

    public ProgramEntity() {
    }

    public ProgramEntity(long programId, String processName, String displayName, Integer categoryId) {
        this.programId = programId;
        this.processName = processName;
        this.displayName = displayName;
        this.categoryId = categoryId;
    }

    public long getProgramId() {
        return programId;
    }

    public void setProgramId(long programId) {
        this.programId = programId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "ProgramEntity ["
                + "programId=" + programId
                + ", processName=" + processName
                + ", displayName=" + displayName
                + ", categoryId=" + categoryId
                + "]";
    }
}
