package domain;

import java.time.Duration;

import kf5012darthmaulapplication.User;

public class Verification {
	private Task task;
	private String notes;
	private TaskPriority standardPriority;
	private Duration standardDeadline; // Nullable
	private User allocationConstraint; // Nullable
	
	public Verification(
			Task task,
			String notes,
			TaskPriority standardPriority,
			Duration standardDeadline,
			User allocationConstraint
	) {
		this.task = task;
		this.notes = notes;
		this.standardPriority = standardPriority;
		this.standardDeadline = standardDeadline;
		this.allocationConstraint = allocationConstraint;
	}
	
	public Verification(Task task) {
		this(
			task,
			"",
			TaskPriority.NORMAL,
			null, // No deadline
			null // No allocation constraint
		);
	}

	public Task getTask() {
		return this.task;
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
