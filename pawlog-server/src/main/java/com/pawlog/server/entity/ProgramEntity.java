package server.db.dto;

public class ProgramDTO {
	private long programId;
	private String processName;
	private String displayName;
	private Integer categoryId;
	
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
		return "ProgramDTO ["
				+ "programId=" + programId
                + ", processName=" + processName
                + ", displayName=" + displayName
                + ", categoryId=" + categoryId
                + "]";
	}
}
