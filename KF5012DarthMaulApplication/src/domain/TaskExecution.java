package domain;

import java.awt.Color;

import kf5012darthmaulapplication.User;
import temporal.ChartableEvent;
import temporal.Period;

/**
 * An instance of doing a task.
 * 
 * @author William Taylor
 */
public class TaskExecution implements ChartableEvent {
	private static final Color color = Color.BLUE;
	
	private Integer id; // Nullable
	private Task task;
	
	private String notes;
	private TaskPriority priority;
	private Period period;
	private User allocation; // Nullable
	private Completion completion; // Nullable
	private VerificationExecution verification; // Nullable
	
	/**
	 * Create a new TaskExecution, or populate a TaskExecution object from data
	 * storage.
	 * 
	 * @param id The ID of the task execution in the database. Null if not in
	 * the DB.
	 * @param task The task this is an execution of.
	 * @param notes Notes associated with only this execution of the task.
	 * @param priority The priority of this execution of the task.
	 * @param period The period over which this task is to be done. Should
	 * default to the period from the earliest time it can start to the latest
	 * time it can finish from the Task's schedule constraint.
	 * @param allocation The caretaker allocated this task execution. Null if
	 * no-one is allocated this task.
	 * @param completion The completion information for this task execution.
	 * Null if this task execution is not complete.
	 * @param verification The verification execution associated with this task
	 * execution. Null if this task execution does not need to be verified and
	 * has not been verified.
	 */
	public TaskExecution(
			Integer id,
			Task task,
			
			String notes,
			TaskPriority priority,
			Period period,
			User allocation,
			Completion completion,
			VerificationExecution verification
	) {
		this.id = id;
		this.task = task;
		
		this.notes = notes;
		this.period = period;
		this.allocation = allocation;
		this.verification = verification;
		this.completion = completion;
		this.priority = priority;
	}

	// ID and reference management
	/**
	 * Get the ID for this task execution.
	 * @return The ID for this task execution.
	 */
	public Integer getID() {
		return this.id;
	}
	
	/**
	 * Set the ID for this task execution.
	 * @param id The ID for this task execution.
	 */
	public void setID(Integer id) {
		this.id = id;
	}

	/**
	 * Get the task this is an execution of.
	 * @return The task this is an execution of.
	 */
	public Task getTask() {
		return this.task;
	}
	
	// You can't set the task

	// Getters
	/**
	 * Get the name of the task this is an execution of.
	 * @return The name of the task this is an execution of.
	 */
	@Override
	public String getName() {
		return this.task.getName();
	}

	/**
	 * Get the notes for this task execution.
	 * @return The notes for this task execution.
	 */
	public String getNotes() {
		return this.notes;
	}

	/**
	 * Get the period over which this task execution runs (previously, now, or
	 * in the future).
	 * @return The period over which this task execution runs.
	 */
	@Override
	public Period getPeriod() {
		return this.period;
	}

	/**
	 * Get the priority of this task execution. This may be different to the
	 * standard priority of the task this is an execution of.
	 * @return The priority of this task execution.
	 */
	public TaskPriority getPriority() {
		return this.priority;
	}
	
	/**
	 * Get the user this task execution is allocated to. Null if is not
	 * allocated.
	 * @return The user this task execution is allocated to.
	 */
	public User getAllocation() {
		return this.allocation;
	}
	
	/**
	 * Get the completion information of this task execution. Null if this task
	 * execution is not completed.
	 * @return The completion information of this task execution.
	 */
	public Completion getCompletion() {
		return this.completion;
	}
	
	/**
	 * Get the verification execution associated with this task execution. Null
	 * if this task execution does not need to be verified and has not been
	 * verified.
	 * @return The verification execution associated with this task execution.
	 */
	public VerificationExecution getVerification() {
		return this.verification;
	}

	/**
	 * Get the colour to use when displaying this task execution graphically.
	 * @return The colour for display.
	 */
	@Override
	public Color getColor() {
		return TaskExecution.color;
	}

	// Setters
	/**
	 * Set the notes for this task execution.
	 * @param notes The notes for this task execution.
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * Set the period that this task execution runs over (previously, now, or in
	 * the future). This should be constrained by the schedule constraint of the
	 * task this is an execution of.
	 * @param period The period over which this task execution runs.
	 */
	public void setPeriod(Period period) {
		this.period = period;
	}

	/**
	 * Set the priority of this task execution.
	 * @param priority The priority of this task execution.
	 */
	public void setPriority(TaskPriority priority) {
		this.priority = priority;
	}
	
	/**
	 * Allocate this task execution to the given caretaker. Pass null to
	 * deallocate this task.
	 * @param allocation The caretaker to allocate this task execution to.
	 */
	public void setAllocation(User allocation) {
		this.allocation = allocation;
	}
	
	/**
	 * Set the completion information for this task execution. Give null to mark
	 * this task execution as incomplete.
	 * @param completion The completion information for this task execution.
	 */
	public void setCompletion(Completion completion) {
		this.completion = completion;
	}
	
	/**
	 * Provide the verification execution for this task execution. Provide null
	 * to set this task execution as not requiring verification or having been
	 * verified.
	 * 
	 * @param verification The verification execution for this task execution.
	 */
	public void setVerification(VerificationExecution verification) {
		this.verification = verification;
	}
}
