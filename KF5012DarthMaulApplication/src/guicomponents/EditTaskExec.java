package guicomponents;

import domain.TaskExecution;
import domain.TaskPriority;

import guicomponents.utils.ObjectEditor;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class EditTaskExec
		extends JScrollPane
		implements ObjectEditor<TaskExecution>
{
	private JTextField txtNotes;
	private JComboBox<Object> cmbPriority;

	/**
	 * Create the panel.
	 */
	public EditTaskExec() {
		JPanel formPanel = new JPanel();
		setViewportView(formPanel);
		GridBagLayout gbl_formPanel = new GridBagLayout();
		gbl_formPanel.columnWidths = new int[]{0, 0, 0};
		gbl_formPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_formPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_formPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		formPanel.setLayout(gbl_formPanel);
		
		JLabel lblNotes = new JLabel("Notes");
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.insets = new Insets(5, 5, 5, 5);
		gbc_lblNotes.anchor = GridBagConstraints.EAST;
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 0;
		formPanel.add(lblNotes, gbc_lblNotes);
		
		txtNotes = new JTextField();
		GridBagConstraints gbc_txtNotes = new GridBagConstraints();
		gbc_txtNotes.insets = new Insets(5, 5, 5, 0);
		gbc_txtNotes.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtNotes.gridx = 1;
		gbc_txtNotes.gridy = 0;
		formPanel.add(txtNotes, gbc_txtNotes);
		txtNotes.setColumns(10);
		
		JLabel lblPriority = new JLabel("Priority");
		lblPriority.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblPriority = new GridBagConstraints();
		gbc_lblPriority.anchor = GridBagConstraints.EAST;
		gbc_lblPriority.insets = new Insets(0, 5, 5, 5);
		gbc_lblPriority.gridx = 0;
		gbc_lblPriority.gridy = 1;
		formPanel.add(lblPriority, gbc_lblPriority);
		
		cmbPriority = new JComboBox<>(TaskPriority.values());
		GridBagConstraints gbc_cmbPriority = new GridBagConstraints();
		gbc_cmbPriority.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbPriority.insets = new Insets(0, 5, 5, 0);
		gbc_cmbPriority.gridx = 1;
		gbc_cmbPriority.gridy = 1;
		formPanel.add(cmbPriority, gbc_cmbPriority);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	/**
	 * Mark the given task execution to be the current task execution to edit.
	 * 
	 * @param taskExec The task execution to edit.
	 */
	@Override
	public void showObject(TaskExecution taskExec) {
		txtNotes.setText(taskExec.getNotes());
		cmbPriority.setSelectedItem(taskExec.getPriority());
	}

	/**
	 * Validate fields and visually mark invalid fields.
	 * 
	 * @return True if all fields are valid, false otherwise.
	 */
	@Override
	public boolean validateFields() {
		boolean valid = true;

		// Notes - no validation
		// Priority - combo box does validation
		
		return valid;
	}

	/**
	 * Update the given task execution with the values currently in the editor's
	 * inputs.
	 * 
	 * @param taskExec The task execution to update.
	 */
	@Override
	public void updateObject(TaskExecution taskExec) {
		taskExec.setNotes(txtNotes.getText());
		taskExec.setPriority((TaskPriority) cmbPriority.getSelectedObjects()[0]);
	}
}
