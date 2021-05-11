package guicomponents.utils;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class BoundedTimelinePanel extends JPanel {
	private TimelinePanel timelinePanel;
	private DateRangePicker dateRangePicker;
	private boolean showLabelOnce;

	/**
	 * Create the panel.
	 */
	public BoundedTimelinePanel(boolean showLabelOnce) {
		this.showLabelOnce = showLabelOnce;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		timelinePanel = new TimelinePanel();
		add(timelinePanel);
		
		dateRangePicker = new DateRangePicker("From", "To");
		dateRangePicker.addChangeListener((e) -> this.refresh());
		add(dateRangePicker);
	}

	/**
	 * Create the panel.
	 */
	public BoundedTimelinePanel() {
		this(false);
	}

	private void refresh() {
		timelinePanel.showBetween(
			dateRangePicker.getStartDateTime(),
			dateRangePicker.getEndDateTime(),
			this.showLabelOnce
		);
	}

	public void addChangeListener(ChangeListener listener) {
		dateRangePicker.addChangeListener(listener);
	}
}
