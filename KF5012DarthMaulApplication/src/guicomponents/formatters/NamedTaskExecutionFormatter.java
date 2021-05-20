package guicomponents.formatters;

import domain.TaskExecution;

/**
 * Formats a task execution including the name of its linked task.
 * 
 * Uses TaskExecutionFormatter to format tasks.
 * 
 * @author William Taylor
 */
public class NamedTaskExecutionFormatter implements Formatter<TaskExecution> {
	private static final Formatter<TaskExecution> formatter = new TaskExecutionFormatter();
	
	@Override
	public String apply(TaskExecution t) {
		return t.getName()+" - "+formatter.apply(t);
	}
}
