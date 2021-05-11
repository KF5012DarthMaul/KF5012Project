package domain;

import java.time.Duration;

import kf5012darthmaulapplication.User;

/**
 * The information about verifying a task that may be done more than once. A
 * Verification implies that the task should be verified every time it is
 * executed.
 * 
 * @author William Taylor
 */
public class Verification {
	private Integer id; // Nullable
	
	private String notes;
	private TaskPriority standardPriority;
	private Duration standardDeadline; // Nullable
	private User allocationConstraint; // Nullable
	
	/**
	 * Create a new Verification, or populate a Verification object from data
	 * storage.
	 * 
	 * @param id The ID of the verification in the database. Null if not in the
	 * DB.
	 * @param notes The notes common to all verifications of this task.
	 * @param standardPriority The usual priority of executions of verifying the
	 * task.
	 * @param standardDeadline The usual deadline for executions of verifying
	 * the task.
	 * @param allocationConstraint The only caretaker that may be assigned this
	 * verification. Null if not constrained to any caretaker.
	 */
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
	
	/**
	 * Create a new verification with sensible default values.
	 */
	public Verification() {
		this(
			null, // No ID assigned yet (ie. not in DB)
			"",
			TaskPriority.NORMAL,
			null, // No deadline
			null // No allocation constraint
		);
	}

	// ID management
	public Integer getID() {
		return this.id;
	}
	public void setID(Integer id) {
		this.id = id;
	}
	
	// Getters
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

	// Setters
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public void setStandardPriority(TaskPriority standardPriority) {
		this.standardPriority = standardPriority;
	}
	public void setStandardDeadline(Duration standardDeadline) {
		this.standardDeadline = standardDeadline;
	}
	public void setAllocationConstraint(User allocationConstraint) {
		this.allocationConstraint = allocationConstraint;
	}
}
