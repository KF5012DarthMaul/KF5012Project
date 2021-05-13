package guicomponents.utils;

import java.util.List;

import javax.swing.JComponent;

public interface ObjectEditor<T> {
	// Note: getComponent() is a work-around to avoid the diamond pattern:
	//           ObjectEditor<T>  ---extends--->  JComponent
	//             ^                                ^
	//             | extends <T = Something>        | extends
	//             |                                |
	//           SomethingEditor  ---extends--->  JPanel
	
	/**
	 * Return the components for this editor.
	 * 
	 * @return The components for this editor.
	 */
	public List<JComponent> getEditorComponents();
	
	/**
	 * Show the given object in the editor.
	 * 
	 * @param obj The object to show. Must not be null.
	 */
	public void setObject(T obj);
	
	/**
	 * Validate the fields for this editor's object type.
	 */
	public boolean validateFields();
	
	/**
	 * Get the object represented by the current values of this editor's fields.
	 * 
	 * For mutable types, this method may update the object given in setObject()
	 * with the values in this editor's fields and return that. For immutable
	 * objects, the object given in setObject() may be returned, or a different
	 * object may be returned.
	 * 
	 * @return obj The object represented by the current values of this editor's
	 * fields. Will never be null.
	 */
	public T getObject();
}
