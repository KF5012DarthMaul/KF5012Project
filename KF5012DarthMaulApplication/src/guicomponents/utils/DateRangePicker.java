package guicomponents.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.BoxLayout;

@SuppressWarnings("serial")
public class DateRangePicker extends JPanel {
	private static final String defaultStartLabel = "Start";
	private static final String defaultEndLabel = "End";
	
	private DateTimePicker startDateTimePicker;
	private DateTimePicker endDateTimePicker;

	/**
	 * Create the date range picker.
	 * 
	 * @param startTime The initial date/time for the start DateTimePicker.
	 * @param endTime The initial date/time for the end DateTimePicker.
	 * @param startLabel The label for the start DateTimePicker.
	 * @param endLabel The label for the end DateTimePicker.
	 */
	public DateRangePicker(
			LocalDateTime startTime, LocalDateTime endTime,
			String startLabel, String endLabel
	) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		this.startDateTimePicker = new DateTimePicker(startTime, startLabel);
		this.add(startDateTimePicker);
		
		this.endDateTimePicker = new DateTimePicker(endTime, endLabel);
		this.add(endDateTimePicker);
	}
	
	/**
	 * Create the date range picker.
	 * 
	 * @param startTime The initial date/time for the start DateTimePicker.
	 * @param endTime The initial date/time for the end DateTimePicker.
	 */
	public DateRangePicker(LocalDateTime startTime, LocalDateTime endTime) {
		this(startTime, endTime, defaultStartLabel, defaultEndLabel);
	}

	/**
	 * Create the date range picker.
	 * 
	 * @param startLabel The label for the start DateTimePicker.
	 * @param endLabel The label for the end DateTimePicker.
	 */
	public DateRangePicker(String startLabel, String endLabel) {
		this(
			LocalDateTime.now().truncatedTo(ChronoUnit.DAYS),
			LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1),
			startLabel, endLabel
		);
	}
	
	/**
	 * Create the date range picker.
	 */
	public DateRangePicker() {
		this(defaultStartLabel, defaultEndLabel);
	}

	// Getters
	public LocalDateTime getStartDateTime() {
		return this.startDateTimePicker.getDateTime();
	}
	public LocalDateTime getEndDateTime() {
		return this.endDateTimePicker.getDateTime();
	}
	
	// Setters
	public void setStartDateTime(LocalDateTime time) {
		this.startDateTimePicker.setDateTime(time);
	}
	public void setEndDateTime(LocalDateTime time) {
		this.endDateTimePicker.setDateTime(time);
	}
	
	// Add change listener
	public void addChangeListener(ChangeListener listener) {
		startDateTimePicker.addChangeListener(listener);
		endDateTimePicker.addChangeListener(listener);
	}
}
