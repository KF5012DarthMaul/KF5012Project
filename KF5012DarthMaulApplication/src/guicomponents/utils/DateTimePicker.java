package guicomponents.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

/**
 * A simple date and time picker that uses two separate input fields.
 * 
 * @author William Taylor
 */
public class DateTimePicker extends JPanel {
	public static final Properties datePanelProperties = new Properties();
	static {
		datePanelProperties.put("text.today", "Today");
		datePanelProperties.put("text.month", "Month");
		datePanelProperties.put("text.year", "Year");
	}
	
	private UtilDateModel dateModel;
	private JSpinner timeSpinner;
	
	public DateTimePicker(LocalDateTime initialTime, String label) {
		JLabel lblLabel = new JLabel(label);
		add(lblLabel);

		// See:
		// - https://stackoverflow.com/questions/26794698/how-do-i-implement-jdatepicker
		// - https://www.codejava.net/java-se/swing/how-to-use-jdatepicker-to-display-calendar-component
		dateModel = new UtilDateModel();
		JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, datePanelProperties);
		JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());
		add(datePicker);
		
		timeSpinner = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "h:mma");
		timeSpinner.setEditor(timeEditor);
		add(timeSpinner);
		
		setDateTime(initialTime);
	}
	
	public void addChangeListener(ChangeListener listener) {
		this.dateModel.addChangeListener(listener);
		this.timeSpinner.addChangeListener(listener);
	}

	public DateTimePicker(String label) {
		this(LocalDateTime.now(), label);
	}
	public DateTimePicker(LocalDateTime initialTime) {
		this(initialTime, "");
	}
	public DateTimePicker() {
		this(LocalDateTime.now(), "");
	}
	
	public LocalDateTime getDateTime() {
		Date date = (Date) dateModel.getValue();
		LocalDate localDate = LocalDate.ofInstant(
			date.toInstant(), ZoneId.systemDefault()
		);
		
		Date time = (Date) timeSpinner.getValue();
		LocalTime localTime = LocalTime.ofInstant(
			time.toInstant(), ZoneId.systemDefault()
		);
		
		return LocalDateTime.of(localDate, localTime);
	}

	public void setDateTime(LocalDateTime dateTime) {
		LocalDate date = LocalDate.from(dateTime);
		LocalTime time = LocalTime.from(dateTime);

		ZoneId systemZone = ZoneId.systemDefault();
		ZoneOffset offset = systemZone.getRules().getOffset(
			dateTime.atZone(systemZone).toInstant());
		
		long milliDate = date.toEpochSecond(LocalTime.ofSecondOfDay(0), offset) * 1000;
		dateModel.setValue(new Date(milliDate));
		
		long milliTime = time.toEpochSecond(date, offset) * 1000;
		timeSpinner.setValue(new Date(milliTime));
	}
}
