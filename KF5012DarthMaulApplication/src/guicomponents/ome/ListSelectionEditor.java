package guicomponents.ome;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import guicomponents.utils.ObjectEditor;

/**
 * An editor that allows selection of a single item from a list of items.
 * 
 * Notes:
 * - Is a combo box, so also has all methods of JComboBox.
 * - Sets a custom renderer - use the <code>strval</code> parameter of the
 *   constructor to tune how to convert this lists's object type (T) to a
 *   string.
 * 
 * @author William Taylor
 *
 * @param <T> The type of objects that this is a list of (that can be selected).
 */
@SuppressWarnings({"serial", "unchecked"})
public class ListSelectionEditor<T> extends JComboBox<Object> implements ObjectEditor<T> {
	private final Border base = this.getBorder();
	private final Border invalid = new LineBorder(Color.RED, 1);
	
	private Predicate<T> validator;

	/**
	 * Initialise the combo box.
	 */
	public ListSelectionEditor(Function<T, String> strval, Predicate<T> validator) {
		this.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(
					JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus
			) {
				super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);

				T realValue = null;
				if (!(value instanceof String && value.equals(""))) {
					realValue = (T) value;
				}
				setText(strval.apply(realValue));
				
				return this;
			}
		});

		this.validator = validator;
	}

	public ListSelectionEditor(Function<T, String> strval) {
		this(strval, (t) -> true);
	}

	public void setValidator(Predicate<T> validator) {
		this.validator = validator;
	}
	
	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(this);
		return arr;
	}

	public void populate(List<T> elems) {
		this.removeAllItems();
		for (T elem : elems) {
			if (elem == null) {
				this.addItem(""); // "" ((probably) not a T) -> null
			} else {
				this.addItem(elem);
			}
		}
	}
	
	@Override
	public void setObject(T obj) {
		if (obj == null) {
			setSelectedItem(""); // null -> "" ((probably) not a T)
		} else {
			setSelectedItem(obj);
		}
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
	public T getObject() {
		Object elem = getSelectedItem();
		if (elem instanceof String && elem.equals("")) {
			return null;
		} else {
			return (T) elem;
		}
	}
}
