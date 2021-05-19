package domain;

import java.time.Duration;
import java.time.LocalDateTime;

import kf5012darthmaulapplication.User;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.EventDescriptor;
import temporal.IntervaledPeriodSet;
import temporal.Period;

/**
 * The information about verifying a task that may be done more than once. A
 * Verification implies that the task should be verified every time it is
 * executed.
 * 
 * @author William Taylor
 */
public class Verification implements EventDescriptor {
	private Integer id; // Nullable
	private Task task;
	
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
			Task task,
			
			String notes,
			TaskPriority standardPriority,
			Duration standardDeadline,
			User allocationConstraint
	) {
		this.id = id;
		this.task = task;
		
		this.notes = notes;
		this.standardPriority = standardPriority;
		this.standardDeadline = standardDeadline;
		this.allocationConstraint = allocationConstraint;
	}
	
	/**
	 * Create a new verification with sensible default values.
	 */
	public Verification(Task task) {
		this(
			null, // No ID assigned yet (ie. not in DB)
			task,
			"",
			TaskPriority.NORMAL,
			null, // No deadline
			null // No allocation constraint
		);
	}

	// ID and reference management
	public Integer getID() {
		return this.id;
	}
	public void setID(Integer id) {
		this.id = id;
	}

	public Task getTask() {
		return this.task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	
	// Getters
	public String getName() {
		return this.task.getName() + " verification";
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
	
	// Implement EventDescriptor
	@Override
	public ConstrainedIntervaledPeriodSet getSchedule() {
		ConstrainedIntervaledPeriodSet schedule = this.getTask().getSchedule();
		
		Duration setRefDur = schedule.periodSet().referencePeriod().duration();
		
		// Push back the start to the end of the task, or leave it at the
		// start if there is no end.
		
		LocalDateTime verSetRefStart = schedule.periodSet().referencePeriod().start();
		if (setRefDur != null) {
			verSetRefStart = verSetRefStart.plus(setRefDur);
		};
		
		// Deal with verCSet

		IntervaledPeriodSet cSet = schedule.periodSetConstraint();
		IntervaledPeriodSet verCSet;
		if (cSet == null) {
			// If the task set has no constraint, then the verification set
			// has no constraint.
			verCSet = null;
			
		} else {
			LocalDateTime cSetRefStart = schedule.periodSetConstraint().referencePeriod().start();
			Duration cSetRefDur = schedule.periodSetConstraint().referencePeriod().duration();
			Duration cSetInterval = schedule.periodSetConstraint().interval();

			Duration verStdDeadline = this.getStandardDeadline();
			
			if (setRefDur != null) {
				// Push forward the constraint set start by the length of
				// the task.
				verCSet = new IntervaledPeriodSet(
					new Period(cSetRefStart.plus(setRefDur), cSetRefDur),
					cSetInterval
				);
			
			} else if (verStdDeadline != null) {
				// If the task has no deadline, then push forward by the
				// duration of the verification.
				verCSet = new IntervaledPeriodSet(
					new Period(cSetRefStart.plus(verStdDeadline), cSetRefDur),
					cSetInterval
				);
				
			} else {
				// If the verification has no deadline, then use the current
				// cSet start, but add a second to the end to make the last
				// event in each constraint period be generated.
				verCSet = new IntervaledPeriodSet(
					new Period(cSetRefStart, cSetRefDur.plusSeconds(1)),
					cSetInterval
				);
			}
		}
		
		ConstrainedIntervaledPeriodSet verSchedule = new ConstrainedIntervaledPeriodSet(
			new IntervaledPeriodSet(
				new Period(verSetRefStart, this.getStandardDeadline()),
				schedule.periodSet().interval()
			),
			verCSet
		);
		
		return verSchedule;
	}
}
