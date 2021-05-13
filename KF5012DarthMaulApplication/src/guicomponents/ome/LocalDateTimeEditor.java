package guicomponents.ome;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import guicomponents.utils.DateTimePicker;
import guicomponents.utils.ObjectEditor;

/**
 * An editor for LocalDateTime variables.
 * 
 * Is a DateTimePicker.
 * 
 * @author William Taylor
 */
@SuppressWarnings("serial")
public class LocalDateTimeEditor
		extends DateTimePicker
		implements ObjectEditor<LocalDateTime>
{
	private final Border base = this.getBorder();
	private final Border invalid = new LineBorder(Color.RED, 1);
	
	private Predicate<LocalDateTime> validator;

	public LocalDateTimeEditor(Predicate<LocalDateTime> validator) {
		this.setValidator(validator);
	}

	public LocalDateTimeEditor() {
		this((ldt) -> true);
	}
	
	public void setValidator(Predicate<LocalDateTime> validator) {
		this.validator = validator;
	}
	
	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(this);
		return arr;
	}

	@Override
	public void setObject(LocalDateTime obj) {
		this.setDateTime(obj);
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
	public LocalDateTime getObject() {
		return this.getDateTime();
	}
}
