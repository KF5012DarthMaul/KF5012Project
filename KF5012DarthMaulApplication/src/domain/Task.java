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

	public Integer getID() {
		return this.id;
	}
	public void setID(Integer id) {
		this.id = id;
	}
	
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
}
