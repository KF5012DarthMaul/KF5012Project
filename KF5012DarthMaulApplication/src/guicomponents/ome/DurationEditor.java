package guicomponents.ome;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import guicomponents.utils.ObjectEditor;
import lib.DurationField;

/**
 * An editor for LocalDateTime variables.
 * 
 * Is a DateTimePicker.
 * 
 * @author William Taylor
 */
@SuppressWarnings("serial")
public class DurationEditor
		extends DurationField
		implements ObjectEditor<Duration>
{
	private final Border base = this.getBorder();
	private final Border invalid = new LineBorder(Color.RED, 1);
	
	private Predicate<Duration> validator;
	
	public DurationEditor(Predicate<Duration> validator) {
		this.setValidator(validator);
	}
	
	public DurationEditor() {
		this((d) -> true);
	}

	public void setValidator(Predicate<Duration> validator) {
		this.validator = validator;
	}
	
	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(this);
		return arr;
	}

	@Override
	public void setObject(Duration obj) {
		this.setHour((int) obj.getSeconds() / 3600);
		this.setMinute((int) obj.getSeconds() % 3600 / 60);
	}

	@Override
	public boolean validateFields() {
		boolean valid = validator.test(getObject());
		if (valid) {
			setBorder(base);
		} else {
			setBorder(invalid);
		}
		return valid;
	}

	@Override
	public Duration getObject() {
		return Duration.ofSeconds(this.getDuration());
	}
}
