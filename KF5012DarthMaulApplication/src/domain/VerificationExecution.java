package domain;

import java.awt.Color;
import java.time.Duration;

import temporal.ChartableEvent;
import temporal.Period;

public class VerificationExecution implements ChartableEvent {
	private static final Color color = Color.RED;
	
	private TaskExecution task;
	private Duration deadline;
	
	public VerificationExecution(TaskExecution task, Duration deadline) {
		this.task = task;
		this.deadline = deadline;
	}

	@Override
	public Period getPeriod() {
		return new Period(this.task.getPeriod().end(), this.deadline);
	}

	@Override
	public String getName() {
		return this.task.getName() + " verification";
	}

	@Override
	public Color getColor() {
		return VerificationExecution.color;
	}
}
