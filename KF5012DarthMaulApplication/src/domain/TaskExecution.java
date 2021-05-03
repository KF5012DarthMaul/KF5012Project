package domain;

import java.awt.Color;

import kf5012darthmaulapplication.User;
import temporal.ChartableEvent;
import temporal.Period;

public class TaskExecution implements ChartableEvent {
	private static final Color color = Color.BLUE;
	
	private Task task;
	
	private String notes;
	private TaskPriority priority;
	private Period period;
	private User allocation; // Nullable
	private Completion completion; // Nullable
	private VerificationExecution verification; // Nullable
	
	public TaskExecution(
			Task task,
			String notes,
			TaskPriority priority,
			Period period,
			User allocation,
			Completion completion,
			VerificationExecution verification
	) {
		this.task = task;
		this.notes = notes;
		this.period = period;
		this.allocation = allocation;
		this.verification = verification;
		this.completion = completion;
		this.priority = priority;
	}

	public Task getTask() {
		return this.task;
	}

	@Override
	public String getName() {
		return this.task.getName();
	}
	
	public String getNotes() {
		return this.notes;
	}

	public TaskPriority getPriority() {
		return this.priority;
	}

	@Override
	public Period getPeriod() {
		return this.period;
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
}
