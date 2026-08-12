package server.db.dto;

import java.sql.Date;

public class DailyUsageSummaryDTO {
	private long summaryId;
    private long userId;
    private long programId;
    private Date summaryDate;

    private long foregroundDurationSec;
    private long backgroundDurationSec;
    private long totalDurationSec;

    // 기본 생성자
    public DailyUsageSummaryDTO() {
    }

    // 데이터 입력용 생성자
    public DailyUsageSummaryDTO(long userId,
                                long programId,
                                Date summaryDate,
                                long foregroundDurationSec,
                                long backgroundDurationSec,
                                long totalDurationSec) {

        this.userId = userId;
        this.programId = programId;
        this.summaryDate = summaryDate;
        this.foregroundDurationSec = foregroundDurationSec;
        this.backgroundDurationSec = backgroundDurationSec;
        this.totalDurationSec = totalDurationSec;
    }

    // 전체 생성자
    public DailyUsageSummaryDTO(long summaryId,
                                long userId,
                                long programId,
                                Date summaryDate,
                                long foregroundDurationSec,
                                long backgroundDurationSec,
                                long totalDurationSec) {

        this.summaryId = summaryId;
        this.userId = userId;
        this.programId = programId;
        this.summaryDate = summaryDate;
        this.foregroundDurationSec = foregroundDurationSec;
        this.backgroundDurationSec = backgroundDurationSec;
        this.totalDurationSec = totalDurationSec;
    }
	
    public long getSummaryId() {
		return summaryId;
	}


	public void setSummaryId(long summaryId) {
		this.summaryId = summaryId;
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


	public Date getSummaryDate() {
		return summaryDate;
	}


	public void setSummaryDate(Date summaryDate) {
		this.summaryDate = summaryDate;
	}


	public long getForegroundDurationSec() {
		return foregroundDurationSec;
	}


	public void setForegroundDurationSec(long foregroundDurationSec) {
		this.foregroundDurationSec = foregroundDurationSec;
	}


	public long getBackgroundDurationSec() {
		return backgroundDurationSec;
	}


	public void setBackgroundDurationSec(long backgroundDurationSec) {
		this.backgroundDurationSec = backgroundDurationSec;
	}


	public long getTotalDurationSec() {
		return totalDurationSec;
	}


	public void setTotalDurationSec(long totalDurationSec) {
		this.totalDurationSec = totalDurationSec;
	}


	@Override
    public String toString() {
        return "DailyUsageSummaryDTO ["
                + "summaryId=" + summaryId
                + ", userId=" + userId
                + ", programId=" + programId
                + ", summaryDate=" + summaryDate
                + ", foregroundDurationSec=" + foregroundDurationSec
                + ", backgroundDurationSec=" + backgroundDurationSec
                + ", totalDurationSec=" + totalDurationSec
                + "]";
    }
}
