package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class ProgramEntity {
    private long programId;
    private String processName;
    private String displayName;


    @Override
    public String toString() {
        return "ProgramEntity ["
                + "programId=" + programId
                + ", processName=" + processName
                + ", displayName=" + displayName
                + "]";
    }
}
