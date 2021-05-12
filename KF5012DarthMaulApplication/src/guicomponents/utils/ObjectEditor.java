package guicomponents.utils;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class ObjectEditor<T> extends JPanel {
	/**
	 * Show the given object in the editor.
	 * 
	 * @param obj The object to show, or null to hide the editor.
	 */
	public abstract void showObject(T obj);
	
	/**
	 * Validate the fields for this editor's object type.
	 */
	public abstract boolean validateFields();
	
	/**
	 * Update the given object's attributes with the values in this editor's
	 * fields.
	 * 
	 * @param obj
	 */
	public abstract void updateObject(T obj);
}
