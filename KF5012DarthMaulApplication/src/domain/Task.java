package domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import kf5012darthmaulapplication.User;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.IntervaledPeriodSet;
import temporal.Period;

/**
 * The information about a task that may be done more than once.
 * 
 * @author William Taylor
 */
public class Task {
	private Integer id; // Nullable
	
	private String name;
	private String notes;
	
	private Map<User, Integer> preferences;
	private Map<User, Duration> efficiency;
	private Map<User, Integer> effectiveness;

	private TaskPriority standardPriority;
	private ConstrainedIntervaledPeriodSet scheduleConstraint;
	private User allocationConstraint; // Nullable
	private Verification verification; // Nullable
	
	/**
	 * Create a new Task, or populate a Task object from data storage.
	 * 
	 * @param id The ID of the task in the database. Null if not in the DB.
	 * @param name The human-readable name of the task.
	 * @param notes Notes associated with all executions of this task.
	 * @param preferences ?
	 * @param efficiency ?
	 * @param effectiveness ?
	 * @param standardPriority The standard priority for this task. Used to
	 * create executions of it.
	 * @param scheduleConstraint The schedule for this task.
	 * @param allocationConstraint The only caretaker that may be assigned this
	 * task. Null if not constrained to any caretaker.
	 * @param verification The verification associated with this task. Null if
	 * this task does not usually require verification.
	 */
	public Task(
			Integer id,
			
			String name,
			String notes,
			
			Map<User, Integer> preferences,
			Map<User, Duration> efficiency,
			Map<User, Integer> effectiveness,
			
			TaskPriority standardPriority,
			ConstrainedIntervaledPeriodSet scheduleConstraint,
			User allocationConstraint,
			Verification verification
	) {
		this.id = id;
		this.name = name;
		this.notes = notes;
		this.preferences = preferences;
		this.efficiency = efficiency;
		this.effectiveness = effectiveness;
		this.standardPriority = standardPriority;
		this.scheduleConstraint = scheduleConstraint;
		this.allocationConstraint = allocationConstraint;
		this.verification = verification;
	}
	
	// Used when creating a task, just before its details are filled in by the
	// user.
	/**
	 * Create a new task with sensible default values.
	 */
	public Task() {
		this(
			null, // No ID assigned yet (ie. not in DB)
			"Task Name", // Default name
			"", // Empty description
			
			// Users must fill these in next
			new HashMap<>(),
			new HashMap<>(),
			new HashMap<>(),
			
			// Normal priority
			TaskPriority.NORMAL,
			
			// One-off task with an earliest start time of now
			new ConstrainedIntervaledPeriodSet(
				new IntervaledPeriodSet(
					new Period(LocalDateTime.now(), (Duration) null), null
				),
				new IntervaledPeriodSet(
					new Period(LocalDateTime.now(), (Duration) null), null
				)
			),
			
			null, // No allocation constraint
			null // No verification required
		);
	}

	// ID management
	/**
	 * Get the ID for this task.
	 * @return The ID for this task.
	 */
	public Integer getID() {
		return this.id;
	}
	
	/**
	 * Set the ID for this task.
	 */
	public void setID(Integer id) {
		this.id = id;
	}
	
	// Getters
	/**
	 * Get the name of this task.
	 * @return The name of this task.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the notes for this task.
	 * @return The notes for this task.
	 */
	public String getNotes() {
		return this.notes;
	}
	
	public Map<User, Integer> getPreferences() {
		return this.preferences;
	}
	public Map<User, Duration> getEfficiency() {
		return this.efficiency;
	}
	public Map<User, Integer> getEffectiveness() {
		return this.effectiveness;
	}

	/**
	 * Get the standard priority of this task.
	 * @return The standard priority of this task.
	 */
	public TaskPriority getStandardPriority() {
		return this.standardPriority;
	}
	
	/**
	 * Get the schedule for this task.
	 * @return The schedule for this task.
	 */
	public ConstrainedIntervaledPeriodSet getScheduleConstraint() {
		return this.scheduleConstraint;
	}

	/**
	 * Get the allocation constraint for this task, if any.
	 * @return The allocation constraint for this task, if any.
	 */
	public User getAllocationConstraint() {
		return this.allocationConstraint;
	}
	
	/**
	 * Get the verification information for this task, if any.
	 * @return The verification information for this task, if any.
	 */
	public Verification getVerification() {
		return this.verification;
	}

	// Setters
	/**
	 * Set the name of this task.
	 * @param name The name of this task.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Set the notes for this task.
	 * @param notes The notes for this task.
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	// TODO: make methods for preferences, efficiency, and effectiveness

	/**
	 * Set the standard priority for this task.
	 * @param standardPriority The standard priority for this task.
	 */
	public void setStandardPriority(TaskPriority standardPriority) {
		this.standardPriority = standardPriority;
	}
	
	/**
	 * Set the schedule for this task.
	 * @param scheduleConstraint The schedule for this task.
	 */
	public void setScheduleConstraint(
			ConstrainedIntervaledPeriodSet scheduleConstraint) {
		this.scheduleConstraint = scheduleConstraint;
	}

	/**
	 * Set the allocation constraint for this task.
	 * @param allocationConstraint The allocation constraint for this task. Null
	 * if no allocation constraint.
	 */
	public void setAllocationConstraint(User allocationConstraint) {
		this.allocationConstraint = allocationConstraint;
	}

	/**
	 * Add or set the verification associated with this task.
	 * @param verification The verification associated with this task. Null if
	 * this task should not normally need verification.
	 */
	public void setVerification(Verification verification) {
		this.verification = verification;
	}
}
