package guicomponents.formatters;

import domain.TaskExecution;

public class NamedTaskExecutionFormatter
		implements DomainObjectFormatter<TaskExecution>
{
	private static TaskExecutionFormatter formatter = new TaskExecutionFormatter();

	@Override
	public String apply(TaskExecution t) {
		return t.getName()+" "+formatter.apply(t);
	}
}
