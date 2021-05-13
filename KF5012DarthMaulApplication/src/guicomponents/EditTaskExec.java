package guicomponents;

import domain.TaskExecution;
import domain.TaskPriority;
import guicomponents.ome.ListSelectionEditor;
import guicomponents.ome.LongTextEditor;
import guicomponents.utils.ObjectEditor;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class EditTaskExec
		extends JScrollPane
		implements ObjectEditor<TaskExecution>
{
	private TaskExecution active;
	
	// Basic Fields
	private LongTextEditor txteNotes;
	private ListSelectionEditor<TaskPriority> lstePriority;

	/**
	 * Create the panel.
	 */
	public EditTaskExec() {
		JPanel formPanel = new JPanel();
		setViewportView(formPanel);
		GridBagLayout gbl_formPanel = new GridBagLayout();
		gbl_formPanel.columnWidths = new int[]{0, 0, 0};
		gbl_formPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_formPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_formPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		formPanel.setLayout(gbl_formPanel);
		
		JLabel lblNotes = new JLabel("Notes");
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.insets = new Insets(5, 5, 5, 5);
		gbc_lblNotes.anchor = GridBagConstraints.EAST;
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 0;
		formPanel.add(lblNotes, gbc_lblNotes);
		
		txteNotes = new LongTextEditor();
		GridBagConstraints gbc_txteNotes = new GridBagConstraints();
		gbc_txteNotes.insets = new Insets(5, 5, 5, 5);
		gbc_txteNotes.fill = GridBagConstraints.HORIZONTAL;
		gbc_txteNotes.gridx = 1;
		gbc_txteNotes.gridy = 0;
		formPanel.add(txteNotes, gbc_txteNotes);
		txteNotes.setColumns(40);
		txteNotes.setRows(6);
		
		JLabel lblPriority = new JLabel("Priority");
		lblPriority.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblPriority = new GridBagConstraints();
		gbc_lblPriority.anchor = GridBagConstraints.EAST;
		gbc_lblPriority.insets = new Insets(0, 5, 5, 5);
		gbc_lblPriority.gridx = 0;
		gbc_lblPriority.gridy = 1;
		formPanel.add(lblPriority, gbc_lblPriority);

		lstePriority = new ListSelectionEditor<>(
			(taskPriority) -> taskPriority.toString()
		);
		lstePriority.populate(Arrays.asList(TaskPriority.values()));
		GridBagConstraints gbc_cmbPriority = new GridBagConstraints();
		gbc_cmbPriority.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbPriority.insets = new Insets(5, 5, 5, 5);
		gbc_cmbPriority.gridx = 1;
		gbc_cmbPriority.gridy = 1;
		formPanel.add(lstePriority, gbc_cmbPriority);
		
		JLabel lblNewLabel = new JLabel("<html><strong>Note:</strong> You can edit this task execution's allocation information (time and caretaker) in the \"Allocate Tasks\" tab on the left.</html>");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(10, 5, 10, 5);
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 2;
		formPanel.add(lblNewLabel, gbc_lblNewLabel);
	}

	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(this);
		return arr;
	}

	/**
	 * Mark the given task execution to be the current task execution to edit.
	 * 
	 * @param taskExec The task execution to edit.
	 */
	@Override
	public void setObject(TaskExecution taskExec) {
		active = taskExec;
		
		txteNotes.setObject(taskExec.getNotes());
		lstePriority.setObject(taskExec.getPriority());
	}

	/**
	 * Validate fields and visually mark invalid fields.
	 * 
	 * @return True if all fields are valid, false otherwise.
	 */
	@Override
	public boolean validateFields() {
		boolean valid = true;

		if (!txteNotes.validateFields()) valid = false;
		if (!lstePriority.validateFields()) valid = false;
		
		return valid;
	}

	/**
	 * Update the given task execution with the values currently in the editor's
	 * inputs.
	 * 
	 * @param taskExec The task execution to update.
	 */
	@Override
	public TaskExecution getObject() {
		active.setNotes(txteNotes.getObject());
		active.setPriority(lstePriority.getObject());
		
		return active;
	}
}
