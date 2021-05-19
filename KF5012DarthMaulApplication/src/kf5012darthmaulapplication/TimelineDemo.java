package kf5012darthmaulapplication;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import domain.Task;
import domain.TaskExecution;
import domain.TaskPriority;
import domain.Verification;
import domain.VerificationExecution;
import guicomponents.utils.TimelinePanel;
import temporal.ChartableEvent;
import temporal.ChartableTemporalMap;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.IntervaledPeriodSet;
import temporal.Period;
import temporal.TemporalList;
import temporal.TemporalMap;
import temporal.Timeline;

@SuppressWarnings("serial")
public class TimelineDemo extends JFrame {
	private static final DateTimeFormatter formatter =
			DateTimeFormatter.ofPattern("h:mma d/M/yyyy");
	
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TimelineDemo frame = new TimelineDemo();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TimelineDemo() {
		setTitle("Time Line Plot");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		Timeline<Integer, ChartableEvent> timeline = generateTimeline();
		TimelinePanel timelinePanel = new TimelinePanel(timeline);
		contentPane.add(timelinePanel, BorderLayout.CENTER);

		timelinePanel.showBetween(
			dt("9:00am 9/5/2021"), dt("5:00pm 10/5/2021"), true
		);
	}

	private List<List<ChartableEvent>> generateTaskExecs() {
		List<List<ChartableEvent>> eventLists = new ArrayList<>();
		
		// A recurring task
		List<ChartableEvent> task1Events = new ArrayList<>();
		eventLists.add(task1Events);
		
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
		task1Events.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("9:45am 9/5/2021"), dt("10:00am 9/5/2021")),
			null, null, null
		));
		task1Events.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("11:45am 9/5/2021"), dt("12:00pm 9/5/2021")),
			null, null, null
		));
		task1Events.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("1:45pm 9/5/2021"), dt("2:00pm 9/5/2021")),
			null, null, null
		));

		// Some on the 10th
		task1Events.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("9:45am 10/5/2021"), dt("10:00am 10/5/2021")),
			null, null, null
		));
		task1Events.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("11:45am 10/5/2021"), dt("12:00pm 10/5/2021")),
			null, null, null
		));
		task1Events.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("1:45pm 10/5/2021"), dt("2:00pm 10/5/2021")),
			null, null, null
		));

		// A low-priority one-off task without a deadline
		List<ChartableEvent> task2Events = new ArrayList<>();
		eventLists.add(task2Events);
		
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
			null, null
		);
		
		task2Events.add(new TaskExecution(
			null, t2, "", TaskPriority.LOW,
			new Period(dt("1:00pm 9/5/2021"), dt("3:00pm 9/5/2021")),
			null, null, null
		));

		// A high-priority one-off task with deadline and verification.
		List<ChartableEvent> task3Events = new ArrayList<>();
		eventLists.add(task3Events);
		
		User myUser = new User("myuser", PermissionManager.AccountType.CARETAKER);

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
			null
		);
		Verification verification = new Verification(null, t3, "", TaskPriority.HIGH, Duration.ofHours(3), null);
		t3.setVerification(verification);
		
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
		task3Events.add(t3Exec);
		task3Events.add(t3VerExec);
		
		return eventLists;
	}
	
	private Timeline<Integer, ChartableEvent> generateTimeline() {
		List<List<ChartableEvent>> eventLists = this.generateTaskExecs();

		List<TemporalMap<Integer, ChartableEvent>> maps = new ArrayList<>();
		for (List<ChartableEvent> eventList : eventLists) {
			maps.add(
				new ChartableTemporalMap<Integer, ChartableEvent>(
					new TemporalList<ChartableEvent>(eventList)
				)
			);
		}
		
		Timeline<Integer, ChartableEvent> timeline = new Timeline<>(maps);
		return timeline;
	}

	private LocalDateTime dt(String dateTimeString) {
		return LocalDateTime.parse(dateTimeString, formatter);
	}
}
