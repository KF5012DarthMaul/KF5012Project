package domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import kf5012darthmaulapplication.User;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.IntervaledPeriodSet;
import temporal.Period;

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
	
	/**
	 * Copy constructor.
	 * 
	 * Use carefully, as it copies the ID. Make sure to delete the old object
	 * after confirming changes to the new one.
	 * 
	 * @param obj The Task object to copy.
	 */
	public Task(Task obj) {
		this.id = obj.id;
		this.name = obj.name;
		this.notes = obj.notes;
		this.preferences = obj.preferences;
		this.efficiency = obj.efficiency;
		this.effectiveness = obj.effectiveness;
		this.standardPriority = obj.standardPriority;
		this.scheduleConstraint = obj.scheduleConstraint;
		this.allocationConstraint = obj.allocationConstraint;
		this.verification = obj.verification;
	}
	
	// Used when creating a task, just before its details are filled in by the
	// user.
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
	public Integer getID() {
		return this.id;
	}
	public void setID(Integer id) {
		this.id = id;
	}
	
	// Getters
	public String getName() {
		return this.name;
	}
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
	
	public TaskPriority getStandardPriority() {
		return this.standardPriority;
	}
	public ConstrainedIntervaledPeriodSet getScheduleConstraint() {
		return this.scheduleConstraint;
	}
	public User getAllocationConstraint() {
		return this.allocationConstraint;
	}
	public Verification getVerification() {
		return this.verification;
	}

	// Setters
	public void setName(String name) {
		this.name = name;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	// TODO: make methods for preferences, efficiency, and effectiveness
	
	public void setStandardPriority(TaskPriority standardPriority) {
		this.standardPriority = standardPriority;
	}
	public void setScheduleConstraint(
			ConstrainedIntervaledPeriodSet scheduleConstraint) {
		this.scheduleConstraint = scheduleConstraint;
	}
	public void setAllocationConstraint(User allocationConstraint) {
		this.allocationConstraint = allocationConstraint;
	}
	public void setVerification(Verification verification) {
		this.verification = verification;
	}
}
