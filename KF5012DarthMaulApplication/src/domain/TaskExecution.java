package domain;

import java.awt.Color;

import kf5012darthmaulapplication.User;
import temporal.ChartableEvent;
import temporal.Period;

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
	public Integer getID() {
		return this.id;
	}
	public void setID(Integer id) {
		this.id = id;
	}
	
	public Task getTask() {
		return this.task;
	}
	// You can't set the task

	// Getters
	@Override
	public String getName() {
		return this.task.getName();
	}
	public String getNotes() {
		return this.notes;
	}

	@Override
	public Period getPeriod() {
		return this.period;
	}

	public TaskPriority getPriority() {
		return this.priority;
	}
	public User getAllocation() {
		return this.allocation;
	}
	public Completion getCompletion() {
		return this.completion;
	}
	public VerificationExecution getVerification() {
		return this.verification;
	}

	@Override
	public Color getColor() {
		return TaskExecution.color;
	}

	// Setters
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public void setPriority(TaskPriority priority) {
		this.priority = priority;
	}
	public void setAllocation(User allocation) {
		this.allocation = allocation;
	}
	public void setCompletion(Completion completion) {
		this.completion = completion;
	}
	public void setVerification(VerificationExecution verification) {
		this.verification = verification;
	}
}
