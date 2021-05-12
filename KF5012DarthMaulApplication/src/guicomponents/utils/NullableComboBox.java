package guicomponents.utils;

import java.awt.Component;
import java.util.List;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

@SuppressWarnings({"serial", "unchecked"})
public class NullableComboBox<T> extends JComboBox<Object> {
	/**
	 * Initialise the combo box.
	 */
	public NullableComboBox(Function<T, String> strval) {
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
	}
	
	public void populate(List<T> elems) {
		this.removeAllItems();
		for (T elem : elems) {
			this.addItem(elem);
		}
		this.addItem(""); // "" (not a User) -> null
	}
	
	public void setSelection(T elem) {
		if (elem == null) {
			setSelectedItem(""); // null -> "" (not a User)
		} else {
			setSelectedItem(elem);
		}
	}
	
	public T getSelection() {
		Object elem = getSelectedItem();
		if (elem instanceof String && elem.equals("")) {
			return null;
		} else {
			return (T) elem;
		}
	}
}
