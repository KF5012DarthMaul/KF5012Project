package domain;

import java.awt.Color;
import java.time.Duration;

import kf5012darthmaulapplication.User;
import temporal.ChartableEvent;
import temporal.Period;

/**
 * An instance of verifying a task.
 * 
 * Some tasks may not be verified regularly, so these can be made for arbitrary
 * tasks without a consistent {@link Verification} associated with them.
 * 
 * @author William Taylor
 */
public class VerificationExecution implements ChartableEvent {
	private static final Color color = Color.RED;

	private Integer id; // Nullable
	private Verification verification; // Nullable
	private TaskExecution taskExec;
	
	private String notes;
	private Duration deadline;
	private User allocation; // Nullable
	private Completion completion; // Nullable
	
	/**
	 * Create a new VerificationExecution, or populate a VerificationExecution
	 * object from data storage.
	 * 
	 * @param id The ID of the verification execution in the database. Null if
	 * not in the DB.
	 * @param verification The verification this is an execution of. Null if the
	 * task of this verification execution (this -> task execution -> task) is
	 * not normally verified.
	 * @param taskExec The task execution this is a verification of.
	 * @param notes Notes associated with the verification execution.
	 * @param deadline The latest point after the task is
	 * {@link Completion completed} that it must be verified by.
	 * @param allocation The caretaker or manager this verification is allocated
	 * to. Null if not allocated.
	 * @param completion The completion information associated with this
	 * verification execution. Null if not completed.
	 */
	public VerificationExecution(
			Integer id,
			
			Verification verification,
			TaskExecution taskExec,
			
			String notes,
			Duration deadline,
			User allocation,
			Completion completion
	) {
		this.id = id; // May have no ID assigned yet (ie. not in DB)
		this.verification = verification;
		this.taskExec = taskExec;
		
		this.notes = notes;
		this.deadline = deadline;
		this.allocation = allocation;
		this.completion = completion;
	}

	/**
	 * 
	 * 
	 * @param verification2
	 */
	public VerificationExecution(VerificationExecution v, TaskExecution t) {
		this.id = v.id;
		this.verification = v.verification; // Backref the same verification - that isn't being copied
		this.taskExec = t;
		
		this.notes = v.notes;
		this.deadline = v.deadline;
		this.allocation = v.allocation;
		
		if (v.completion == null) {
			this.completion = null;
		} else {
			this.completion = new Completion(v.completion);
		}
	}

	// ID and reference management
	public Integer getID() {
		return this.id;
	}
	public void setID(Integer id) {
		this.id = id;
	}

	public Verification getVerification() {
		return this.verification;
	}
	public void setVerification(Verification verification) {
		this.verification = verification;
	}
	
	public TaskExecution getTaskExec() {
		return this.taskExec;
	}
	public void setTaskExec(TaskExecution taskExec) {
		this.taskExec = taskExec;
	}
	
	// Getters
	public String getNotes() {
		return this.notes;
	}
	public Duration getDeadline() { // Mainly for DB code
		return this.deadline;
	}
	public User getAllocation() {
		return this.allocation;
	}
	public Completion getCompletion() {
		return this.completion;
	}

	// Implemented interfaces
	@Override
	public Period getPeriod() {
		return new Period(this.taskExec.getPeriod().end(), this.deadline);
	}

	@Override
	public String getName() {
		return this.taskExec.getName() + " verification";
	}

	@Override
	public Color getColor() {
		return VerificationExecution.color;
	}
	
	// Setters
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public void setDeadline(Duration deadline) {
		this.deadline = deadline;
	}
	public void setAllocation(User allocation) {
		this.allocation = allocation;
	}
	public void setCompletion(Completion completion) {
		this.completion = completion;
	}
}
