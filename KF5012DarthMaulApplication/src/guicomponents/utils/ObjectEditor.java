package guicomponents.utils;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public interface ObjectEditor<T> {
	// Note: getComponent() is a work-around to avoid the diamond pattern:
	//           ObjectEditor<T>  ---extends--->  JComponent
	//             ^                                ^
	//             | extends <T = Something>        | extends
	//             |                                |
	//           SomethingEditor  ---extends--->  JPanel
	
	/**
	 * Return the component for this editor.
	 * 
	 * @return the component for this editor.
	 */
	public JComponent getComponent();
	
	/**
	 * Show the given object in the editor.
	 * 
	 * @param obj The object to show, or null to hide the editor.
	 */
	public void showObject(T obj);
	
	/**
	 * Validate the fields for this editor's object type.
	 */
	public boolean validateFields();
	
	/**
	 * Update the given object's attributes with the values in this editor's
	 * fields.
	 * 
	 * @param obj
	 */
	public void updateObject(T obj);
}
