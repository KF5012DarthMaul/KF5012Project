package guicomponents.formatters;

import domain.Task;

public class TaskFormatter implements DomainObjectFormatter<Task> {
	@Override
	public String apply(Task task) {
		String name = task.getName();
		String priority = task.getStandardPriority().toString();
		
		return name+" (standard priority: "+priority+")";
	}
}
