package guicomponents.formatters;

import domain.Task;

/**
 * Formats a task.
 * 
 * @author William Taylor
 */
public class TaskFormatter implements Formatter<Task> {
	@Override
	public String apply(Task task) {
		String name = task.getName();
		String priority = task.getStandardPriority().toString();
		
		return name+" (standard priority: "+priority+")";
	}
}
