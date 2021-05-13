package guicomponents.ome;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Supplier;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import guicomponents.utils.ObjectEditor;
import guicomponents.utils.ObjectManager;

/**
 * This is a wrapper for the standard layout of domain object managers, ie. a
 * checkbox above a JPanel that is the editor.
 * 
 * @author William Taylor
 *
 * @param <T> The type of object under reference management.
 */
@SuppressWarnings("serial")
public class DomainObjectManager<T> extends JPanel {
	private ObjectManager<T> objManager;

	public DomainObjectManager(
			String createDeleteLabel,
			ObjectEditor<T> editor,
			Supplier<T> activationFactory
	) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JCheckBox chkExists = new JCheckBox(createDeleteLabel);
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.anchor = GridBagConstraints.WEST;
		gbc_buttonPanel.insets = new Insets(0, 0, 5, 0);
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 0;
		add(chkExists, gbc_buttonPanel);
		
		GridBagConstraints gbc_editorPanel = new GridBagConstraints();
		gbc_buttonPanel.fill = GridBagConstraints.BOTH;
		gbc_editorPanel.gridx = 0;
		gbc_editorPanel.gridy = 1;
		add(editor.getComponent(), gbc_editorPanel);

		this.objManager = new ObjectManager<>(chkExists, editor, activationFactory);
	}
	
	public ObjectManager<T> getObjectManager() {
		return objManager;
	}
}
