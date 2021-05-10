package kf5012darthmaulapplication;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import guicomponents.utils.TimelinePanel;
import domain.Task;
import domain.TaskExecution;
import domain.TaskPriority;
import domain.Verification;
import domain.VerificationExecution;

import temporal.ChartableEvent;
import temporal.ChartableTemporalMap;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.IntervaledPeriodSet;
import temporal.Period;
import temporal.TemporalList;
import temporal.TemporalMap;
import temporal.Timeline;

public class TimelineDemo implements Runnable {
	private static final DateTimeFormatter formatter =
			DateTimeFormatter.ofPattern("h:mma d/M/yyyy");
	
	private final Timeline<Integer, ChartableEvent> timeline;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new TimelineDemo());
	}

	public TimelineDemo() {
		timeline = generateTimeline();
	}
	
	private List<ChartableEvent> generateTaskExecs() {
		List<ChartableEvent> taskExecs = new ArrayList<>();
		
		// A recurring task
		Task t1 = new Task(
			null,
			"Check toilets", "",
			null, null, null,
			TaskPriority.NORMAL,
			new ConstrainedIntervaledPeriodSet(
				new IntervaledPeriodSet(
					new Period(dt("9:45am 9/5/2021"), dt("10:00am 9/5/2021")),
					Duration.ofHours(2)
				),
				new IntervaledPeriodSet(
					new Period(dt("9:00am 9/5/2021"), dt("5:00pm 9/5/2021")),
					Duration.ofDays(1)
				)
			),
			null, null
		);
		
		// Some on the 9th
		taskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("9:45am 9/5/2021"), dt("10:00am 9/5/2021")),
			null, null, null
		));
		taskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("11:45am 9/5/2021"), dt("12:00pm 9/5/2021")),
			null, null, null
		));
		taskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("1:45pm 9/5/2021"), dt("2:00pm 9/5/2021")),
			null, null, null
		));

		// Some on the 10th
		taskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("9:45am 10/5/2021"), dt("10:00am 10/5/2021")),
			null, null, null
		));
		taskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("11:45am 10/5/2021"), dt("12:00pm 10/5/2021")),
			null, null, null
		));
		taskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("1:45pm 10/5/2021"), dt("2:00pm 10/5/2021")),
			null, null, null
		));

		// A low-priority one-off task without a deadline
		Task t2 = new Task(
				null,
				"Fix Window on bike shed", "",
				null, null, null,
				TaskPriority.LOW,
				new ConstrainedIntervaledPeriodSet(
					new IntervaledPeriodSet(
						new Period(dt("9:45am 9/5/2021"), (Duration) null), null
					),
					null
				),
				null, null);
		taskExecs.add(new TaskExecution(
			null, t2, "", TaskPriority.LOW,
			new Period(dt("1:00pm 9/5/2021"), dt("3:00pm 9/5/2021")),
			null, null, null
		));

		// A high-priority one-off task with deadline and verification.
		User myUser = new User("myuser", PermissionManager.AccountType.CARETAKER);
		
		Verification verification = new Verification(null, "", TaskPriority.HIGH, Duration.ofHours(3), null);
		Task t3 = new Task(
			null,
			"Fix Broken Pipe",
			"The waste pipe outside of the toilets on the 3rd floor of Big Building is broken and leaking. Health hazard - fix ASAP.",
			null, null, null,
			TaskPriority.HIGH,
			new ConstrainedIntervaledPeriodSet(
				new IntervaledPeriodSet(
					new Period(dt("1:32pm 10/5/2021"), dt("5:00pm 10/5/2021")), null
				),
				null
			),
			null,
			verification
		);
		
		// The task execution has been allocated to myUser
		TaskExecution t3Exec = new TaskExecution(
			null, t3, "", TaskPriority.HIGH,
			new Period(dt("3:30pm 9/5/2021"), dt("4:15pm 9/5/2021")),
			myUser,
			null, null
		);
		
		// The verification execution
		VerificationExecution t3VerExec = new VerificationExecution(
			null, verification, t3Exec, "", Duration.ofHours(3), null, null
		);

		// Add both
		taskExecs.add(t3Exec);
		taskExecs.add(t3VerExec);
		
		return taskExecs;
	}
	
	private Timeline<Integer, ChartableEvent> generateTimeline() {
		List<ChartableEvent> events = this.generateTaskExecs();
		
		TemporalMap<Integer, ChartableEvent> map =
			new ChartableTemporalMap<Integer, ChartableEvent>(
				new TemporalList<ChartableEvent>(events));
		
		List<TemporalMap<Integer, ChartableEvent>> maps = new ArrayList<>();
		maps.add(map);
		
		Timeline<Integer, ChartableEvent> timeline = new Timeline<>(maps);
		return timeline;
	}

	private LocalDateTime dt(String dateTimeString) {
		return LocalDateTime.parse(dateTimeString, formatter);
	}
	
	@Override
	public void run() {
		JFrame frame = new JFrame("Time Line Plot");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		TimelinePanel timelinePanel = new TimelinePanel(timeline, 1000, 300);
		timelinePanel.showBetween(dt("9:00am 9/5/2021"), dt("5:00pm 10/5/2021"));
		
		frame.add(timelinePanel, BorderLayout.CENTER);

		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}
}
