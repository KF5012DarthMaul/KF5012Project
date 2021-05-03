package domain;

import java.awt.Color;
import java.time.Duration;

import kf5012darthmaulapplication.User;
import temporal.ChartableEvent;
import temporal.Period;

public class VerificationExecution implements ChartableEvent {
	private static final Color color = Color.RED;

	private Verification verification;
	private TaskExecution taskExec;
	
	private String notes;
	private Duration deadline;
	private User allocation; // Nullable
	private Completion completion; // Nullable
	
	public VerificationExecution(
			Verification verification,
			TaskExecution taskExec,
			
			String notes,
			Duration deadline,
			User allocation,
			Completion completion
	) {
		this.verification = verification;
		this.taskExec = taskExec;
		
		this.notes = notes;
		this.deadline = deadline;
		this.allocation = allocation;
		this.completion = completion;
	}
	
	public Verification getVerification() {
		return this.verification;
	}
	public TaskExecution getTaskExec() {
		return this.taskExec;
	}
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
}
