package com.pawlog.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class PredictionEventEntity {
    private long eventId;
    private String title;
    private Timestamp createdAt;
    private Timestamp votingDeadline;
    private String status; // 'OPEN', 'CLOSED', 'ANNOUNCED'
    private Date targetDate;
    private String eventType; // 'WEEKLY', 'MONTHLY'


    @Override
    public String toString() {
        return "PredictionEventEntity ["
                + "eventId=" + eventId
                + ", title=" + title
                + ", createdAt=" + createdAt
                + ", votingDeadline=" + votingDeadline
                + ", status=" + status
                + ", targetDate=" + targetDate
                + ", eventType=" + eventType
                + "]";
    }
}