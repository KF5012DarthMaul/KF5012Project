package guicomponents.ome;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import guicomponents.utils.ObjectEditor;

/**
 * An editor for longer String variables.
 * 
 * Is a JTextArea.
 * 
 * @author William Taylor
 */
@SuppressWarnings("serial")
public class LongTextEditor extends JTextArea implements ObjectEditor<String> {
	private final Border base = this.getBorder();
	private final Border invalid = new LineBorder(Color.RED, 1);
	
	private Predicate<String> validator;

	public LongTextEditor(Predicate<String> validator) {
		this.setValidator(validator);
	}

	public LongTextEditor() {
		this((t) -> true);
	}
	
	public void setValidator(Predicate<String> validator) {
		this.validator = validator;
	}
	
	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(this);
		return arr;
	}

	@Override
	public void setObject(String obj) {
		this.setText(obj);
	}

	@Override
	public boolean validateFields() {
		boolean valid = validator.test(getText());
		if (valid) {
			setBorder(base);
		} else {
			setBorder(invalid);
		}
		return valid;
	}

	@Override
	public String getObject() {
		return this.getText();
	}
}
