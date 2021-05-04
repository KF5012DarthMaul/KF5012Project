package domain;

import java.time.Duration;

import kf5012darthmaulapplication.User;

public class Verification {
	private String notes;
	private TaskPriority standardPriority;
	private Duration standardDeadline; // Nullable
	private User allocationConstraint; // Nullable
	
	public Verification(
			String notes,
			TaskPriority standardPriority,
			Duration standardDeadline,
			User allocationConstraint
	) {
		this.notes = notes;
		this.standardPriority = standardPriority;
		this.standardDeadline = standardDeadline;
		this.allocationConstraint = allocationConstraint;
	}
	
	public Verification() {
		this(
			"",
			TaskPriority.NORMAL,
			null, // No deadline
			null // No allocation constraint
		);
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
