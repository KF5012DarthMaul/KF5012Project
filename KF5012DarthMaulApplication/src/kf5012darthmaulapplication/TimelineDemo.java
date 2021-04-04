package kf5012darthmaulapplication;

import java.awt.BorderLayout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import temporal.ChartableEvent;
import temporal.ChartableTemporalList;
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
	
	private Timeline<Integer, ChartableEvent> generateTimeline() {
		List<ChartableEvent> events;
		
		// ----------
		
		events = new ArrayList<>();
		
		events.add(new TaskExecution("First task",
			new Period(dt("11:45am 5/2/2021"), dt("12:15pm 5/2/2021"))
		));
		
		TaskExecution repairAntennaTask = new TaskExecution("Repair antenna",
			new Period(dt("1:00pm 5/2/2021"), dt("4:00pm 5/2/2021"))
		);
		events.add(repairAntennaTask);
		events.add(new VerificationExecution(
				repairAntennaTask, Duration.ofMinutes(45)
		));
		
		events.add(new TaskExecution("Clean downstairs windows",
			new Period(dt("2:30pm 5/2/2021"), dt("3:15pm 5/2/2021"))
		));
		events.add(new TaskExecution("Empty bins",
			new Period(dt("9:45am 5/2/2021"), dt("10:00am 5/2/2021"))
		));
		
		TemporalMap<Integer, ChartableEvent> map1 =
			new ChartableTemporalList<ChartableEvent>(
				new TemporalList<ChartableEvent>(events));
		
		// ----------

		events = new ArrayList<>();

		events.add(new TaskExecution("Second task",
			new Period(dt("11:45am 5/2/2021"), dt("12:15pm 5/2/2021"))
		));
		
		TaskExecution repairBasinTask = new TaskExecution("Repair basin",
			new Period(dt("9:31am 5/2/2021"), dt("12:00pm 5/2/2021"))
		);
		events.add(repairBasinTask);
		events.add(new VerificationExecution(
			repairBasinTask, Duration.ofMinutes(15)
		));
		
		events.add(new TaskExecution("Clean upstairs windows",
			new Period(dt("1:00pm 5/2/2021"), dt("5:00pm 5/2/2021"))
		));
		events.add(new TaskExecution("Empty bins",
			new Period(dt("9:45am 6/2/2021"), dt("10:00am 6/2/2021"))
		));
		
		TemporalMap<Integer, ChartableEvent> map2 =
			new ChartableTemporalList<ChartableEvent>(
				new TemporalList<ChartableEvent>(events));
		
		// ----------
		
		List<TemporalMap<Integer, ChartableEvent>> maps = new ArrayList<>();
		maps.add(map1);
		maps.add(map2);
		
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
		timelinePanel.showBetween(dt("9:30am 5/2/2021"), dt("4:30pm 5/2/2021"));
		
		frame.add(timelinePanel, BorderLayout.CENTER);

		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}
}
