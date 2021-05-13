package guicomponents.ome;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import guicomponents.utils.ObjectEditor;

/**
 * An editor for String variables.
 * 
 * Is a JTextField.
 * 
 * @author William Taylor
 */
@SuppressWarnings("serial")
public class TextEditor extends JTextField implements ObjectEditor<String> {
	private final Border base = this.getBorder();
	private final Border invalid = new LineBorder(Color.RED, 1);
	
	private Predicate<String> validator;

	public TextEditor(Predicate<String> validator) {
		this.setValidator(validator);
	}

	public TextEditor() {
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
		boolean valid = validator.test(getObject());
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
