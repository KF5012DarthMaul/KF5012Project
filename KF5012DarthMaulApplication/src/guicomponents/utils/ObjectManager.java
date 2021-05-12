package guicomponents.utils;

import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import java.util.function.Supplier;

@SuppressWarnings("serial")
public class ObjectManager<T> extends JPanel {
	private JCheckBox chkExists;
	private ObjectEditor<T> editorPanel;
	
	private T obj;
	
	/**
	 * Create the panel.
	 */
	public ObjectManager(
			String createDeleteLabel,
			Supplier<T> activationFactory,
			ObjectEditor<T> editorPanel
	) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		chkExists = new JCheckBox(createDeleteLabel);
		chkExists.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setObject(activationFactory.get(), false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				setObject(null, false);
			}
		});
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.anchor = GridBagConstraints.WEST;
		gbc_buttonPanel.insets = new Insets(0, 0, 5, 0);
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 0;
		add(chkExists, gbc_buttonPanel);
		
		this.editorPanel = editorPanel;
		GridBagConstraints gbc_editorPanel = new GridBagConstraints();
		gbc_buttonPanel.fill = GridBagConstraints.BOTH;
		gbc_editorPanel.gridx = 0;
		gbc_editorPanel.gridy = 1;
		add(editorPanel, gbc_editorPanel);
	}

	private void setObject(T obj, boolean force) {
		// Keep a reference for getObject()
		this.obj = obj;
		
		// Do the display work
		boolean exists = obj != null;
		if (force) {
			chkExists.setSelected(exists);
		}
		editorPanel.setVisible(exists);
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
