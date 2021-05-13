package guicomponents.utils;

import javax.swing.JCheckBox;
import java.awt.event.ItemEvent;
import java.util.function.Supplier;

/**
 * A class that manages the lifecycle of a variable.
 * 
 * For example:
 * <pre><code>
 * JCheckBox chkBox = new JCheckBox("Requires Something");
 * JTextField txtField = new JTextField();
 * 
 * // Wherever you need to add them to your layout
 * add(chkBox);
 * add(txtField);
 * 
 * // Create the ObjectEditor
 * // You should create your own class for this and inject components through
 * // the constructor, rather than using a closure-anonymous-class - this is
 * // just just a demo.
 * ObjectEditor&lt;Something&gt; somethingEditor = new ObjectEditor<>() {
 *   &commat;Override
 *   public JComponent getComponent() {
 *     return txtField;
 *   }
 *   &commat;Override
 *   public void showObject(Something obj) {
 *     txtField.setText(obj.getStringAttr());
 *   }
 *   &commat;Override
 *   public boolean validateFields() {
 *     // could be `return true;` if blank is fine
 *     return !txtField.getText().isEmpty();
 *   }
 *   &commat;Override
 *   public void updateObject(Something obj) {
 *     obj.setStringAttr(txtField.getText());
 *   }
 * };
 * 
 * // Create the ObjectManager
 * new ObjectManager&lt;Something&gt; somethingManager = new ObjectManager<>(
 *   chkBox, somethingEditor,
 *   
 *   // Could be `() -> this.something` if you want this to just be visibility
 *   // toggle.
 *   () -> new Something()
 * );
 * 
 * // Initialise the ObjectManager
 * // `this.something` is a Something created whenever (it may be null)
 * somethingManager.setObject(this.something);
 * 
 * // Retrieve from the ObjectManager
 * // getObject() does all of the work of updating the object it is currently
 * // storing (which may be the one passed, or may be a new one
 * this.something = somethingManager.getObject();
 * </code></pre>
 * 
 * @author William Taylor
 *
 * @param <T> The type of object under reference management.
 */
public class ObjectManager<T> {
	private JCheckBox chkExists;
	private ObjectEditor<T> editorPanel;
	
	private T obj;
	
	public ObjectManager(
			JCheckBox chkExists,
			ObjectEditor<T> editorPanel,
			Supplier<T> activationFactory
	) {
		this.chkExists = chkExists;
		chkExists.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setObject(activationFactory.get(), false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				setObject(null, false);
			}
		});
		
		this.editorPanel = editorPanel;
	}

	private void setObject(T obj, boolean force) {
		// Keep a reference for getObject()
		this.obj = obj;
		
		// Do the display work
		boolean exists = obj != null;
		if (force) {
			chkExists.setSelected(exists);
		}
		editorPanel.getComponent().setVisible(exists);
		if (exists) {
			editorPanel.showObject(obj);
		}
	}

	public void setObject(T obj) {
		setObject(obj, true);
	}
	
	public T getObject() {
		if (obj != null) {
			editorPanel.updateObject(obj);
		}
		return obj;
	}
}
