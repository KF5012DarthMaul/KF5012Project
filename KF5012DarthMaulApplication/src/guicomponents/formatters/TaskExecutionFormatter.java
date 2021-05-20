package guicomponents.formatters;

import java.time.Duration;
import java.time.LocalDateTime;

import domain.Completion;
import domain.TaskExecution;
import domain.VerificationExecution;
import temporal.Period;

/**
 * Formats a task execution, including all linked verification execution and
 * completion objects.
 * 
 * @author William Taylor
 */
public class TaskExecutionFormatter implements Formatter<TaskExecution> {
	// Formatters for java standard library objects
	private static final Formatter<LocalDateTime> dtFormatter =
			new LocalDateTimeFormatter("h:mma d/M/yyyy");
	private static final Formatter<Duration> durFormatter = new DurationFormatter();
	
	// Standard formatters for task statuses
	private static final Formatter<String> taskStatusNormal = new IdentityFormatter();
	private static final Formatter<String> taskStatusNotice = new ColorFormatter("blue");
	private static final Formatter<String> taskStatusDone = new ColorFormatter("green");
	private static final Formatter<String> taskStatusOverdue = new ColorFormatter("red");
	
	@Override
	public String apply(TaskExecution taskExec) {
		// Get Objects
		Period period = taskExec.getPeriod();
		Duration duration = period.duration();
		Completion completion = taskExec.getCompletion();
		VerificationExecution verification = taskExec.getVerification();

		// Times
		String start = dtFormatter.apply(period.start());
		String durationStr = "no deadline";
		if (duration != null) {
			durationStr = durFormatter.apply(duration);
		}
		
		// Priority
		String priority = taskExec.getPriority().toString();
		
		// Completion / Overdue
		String completionStatus = formatCompletionStatus(
			period, completion);

		// Verification
		String verificationStr = "";
		if (verification != null) {
			Period verPeriod = taskExec.getPeriod();
			Duration verDuration = verPeriod.duration();
			Completion verCompletion = taskExec.getCompletion();

			// Times
			String verDurationStr = "no deadline";
			if (verDuration != null) {
				verDurationStr = durFormatter.apply(verDuration);
			}
			
			// Completion / Overdue
			String verCompletionStatus = formatCompletionStatus(
				verPeriod, verCompletion);
			
			// Glue together
			verificationStr = String.format(
				" [verification (%s) - %s]",
				verDurationStr, verCompletionStatus
			);
		}

		// Glue together
		return String.format(
			"%s (%s | priority: %s) - %s%s",
			start, durationStr, priority, completionStatus,
			verificationStr
		);
	}

	// Keep this in TaskExecutionFormatter to reduce the number of classes, and
	// there is no other reason to separate it.
	private String formatCompletionStatus(
			Period period, Completion completion
	) {
		LocalDateTime now = LocalDateTime.now();
		
		if (completion == null) {
			if (now.isAfter(period.end())) {
				return taskStatusOverdue.apply("overdue!");
			} else if (now.isAfter(period.start())) {
				return taskStatusNotice.apply("in progress");
			} else {
				return taskStatusNormal.apply("upcoming");
			}
		} else {
			return taskStatusDone.apply("done ✔️");
		}
	}
}
