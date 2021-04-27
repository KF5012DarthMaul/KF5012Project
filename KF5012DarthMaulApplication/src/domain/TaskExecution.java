package domain;

import java.awt.Color;

import temporal.ChartableEvent;
import temporal.Period;

public class TaskExecution implements ChartableEvent {
	private static final Color color = Color.BLUE;
	
	private String name;
	private Period period;
	
	public TaskExecution(String name, Period period) {
		this.name = name;
		this.period = period;
	}

	@Override
	public Period getPeriod() {
		return this.period;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Color getColor() {
		return TaskExecution.color;
	}
}
