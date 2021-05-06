package domain;

import java.time.Duration;

import kf5012darthmaulapplication.User;

public class Verification {
	private Integer id; // Nullable
	
	private String notes;
	private TaskPriority standardPriority;
	private Duration standardDeadline; // Nullable
	private User allocationConstraint; // Nullable
	
	public Verification(
			Integer id,
			String notes,
			TaskPriority standardPriority,
			Duration standardDeadline,
			User allocationConstraint
	) {
		this.id = id;
		this.notes = notes;
		this.standardPriority = standardPriority;
		this.standardDeadline = standardDeadline;
		this.allocationConstraint = allocationConstraint;
	}
	
	public Verification() {
		this(
			null, // No ID assigned yet (ie. not in DB)
			"",
			TaskPriority.NORMAL,
			null, // No deadline
			null // No allocation constraint
		);
	}

	public Integer getID() {
		return this.id;
	}
	public void setID(Integer id) {
		this.id = id;
	}
	
	public String getNotes() {
		return this.notes;
	}
	public TaskPriority getStandardPriority() {
		return this.standardPriority;
	}
	public Duration getStandardDeadline() {
		return this.standardDeadline;
	}
	public User getAllocationConstraint() {
		return this.allocationConstraint;
	}
}
