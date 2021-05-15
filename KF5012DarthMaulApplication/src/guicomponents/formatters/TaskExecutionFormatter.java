package guicomponents.formatters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import domain.Completion;
import domain.TaskExecution;
import domain.VerificationExecution;
import temporal.Period;

public class TaskExecutionFormatter implements DomainObjectFormatter<TaskExecution> {
	private static final DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofPattern("h:mma d/M/yyyy");
	
	// Based on https://www.logicbig.com/tutorials/java-swing/jtree-renderer.html
	private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";
	
	@Override
	public String apply(TaskExecution taskExec) {
		// Get Objects
		Period period = taskExec.getPeriod();
		Duration duration = period.duration();
		Completion completion = taskExec.getCompletion();
		VerificationExecution verification = taskExec.getVerification();

		// Times
		String start = period.start().format(dateTimeFormatter);
		String durationStr = "continuous";
		if (duration != null) {
			durationStr = formatDuration(duration);
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
			String verDurationStr = formatDuration(verDuration);
			
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
			"<html>%s (%s | priority: %s) - %s%s</html>",
			start, durationStr, priority, completionStatus,
			verificationStr
		);
	}

	private String formatCompletionStatus(
			Period period, Completion completion
	) {
		LocalDateTime now = LocalDateTime.now();
		
		if (completion == null) {
			if (now.isAfter(period.end())) {
				return String.format(SPAN_FORMAT, "red", "overdue!");
			} else if (now.isAfter(period.start())) {
				return String.format(SPAN_FORMAT, "blue", "in progress");
			} else {
				return "upcoming";
			}
		} else {
			return String.format(SPAN_FORMAT, "green", "done ✔️");
		}
	}
	
	// Based on https://stackoverflow.com/a/266970
	private String formatDuration(Duration duration) {
		long totalM = duration.toMinutes();
		long h = (totalM / 60);
		long m = (totalM % 60);
		
		if (h == 0) {
			return String.format("%02dm", m);
		} else {
			return String.format("%dh, %02dm", h, m);
		}
	}
}
