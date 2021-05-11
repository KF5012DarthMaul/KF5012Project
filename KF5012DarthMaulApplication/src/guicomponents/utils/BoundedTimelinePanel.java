package guicomponents.utils;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import temporal.ChartableEvent;
import temporal.Timeline;

@SuppressWarnings("serial")
public class BoundedTimelinePanel extends JPanel {
	private TimelinePanel timelinePanel;
	private DateRangePicker dateRangePicker;
	private boolean showLabelOnce;

	/**
	 * Create the bounded timeline panel.
	 * 
	 * You should keep a reference to the passed-in timelinePanel if you need to
	 * change the Timeline it uses. This component registers a change listener
	 * on the panel, so will update automatically if the timeline changes.
	 * 
	 * @param timelinePanel The timeline panel to embed.
	 * @param showLabelOnce Whether the event label should only be shown for the
	 * first event in each map of the timeline.
	 */
	public BoundedTimelinePanel(
			TimelinePanel timelinePanel, DateRangePicker dateRangePicker,
			boolean showLabelOnce
	) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.showLabelOnce = showLabelOnce;
		this.timelinePanel = timelinePanel;
		timelinePanel.addChangeListener((e) -> this.refresh());
		add(this.timelinePanel);

		this.dateRangePicker = dateRangePicker;
		this.dateRangePicker.addChangeListener((e) -> this.refresh());
		add(this.dateRangePicker);
	}

	/**
	 * Create the panel.
	 */
	public BoundedTimelinePanel(
			TimelinePanel timelinePanel, DateRangePicker dateRangePicker
	) {
		this(timelinePanel, dateRangePicker, false);
	}
	
	private void refresh() {
		timelinePanel.showBetween(
			dateRangePicker.getStartDateTime(),
			dateRangePicker.getEndDateTime(),
			this.showLabelOnce
		);
	}
}
