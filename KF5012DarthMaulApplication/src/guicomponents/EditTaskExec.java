package guicomponents;

import domain.TaskPriority;
import domain.TaskExecution;
import domain.VerificationExecution;
import guicomponents.ome.DomainObjectManager;
import guicomponents.ome.ListSelectionEditor;
import guicomponents.ome.LongTextEditor;
import guicomponents.ome.VerificationExecutionEditor;
import guicomponents.utils.ObjectEditor;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
	
	// Verification Execution
	private VerificationExecutionEditor edtVerificationExec;
	private DomainObjectManager<VerificationExecution> omgVerificationExec;

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
		gbl_formPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		formPanel.setLayout(gbl_formPanel);

		/* Basics
		 * -------------------- */
		
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
		gbc_txteNotes.anchor = GridBagConstraints.WEST;
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
		gbc_cmbPriority.anchor = GridBagConstraints.WEST;
		gbc_cmbPriority.insets = new Insets(5, 5, 5, 5);
		gbc_cmbPriority.gridx = 1;
		gbc_cmbPriority.gridy = 1;
		formPanel.add(lstePriority, gbc_cmbPriority);

		JLabel lblNewLabel = new JLabel("<html><strong>Note:</strong> You can edit this task execution's allocation information (time and caretaker) in the \"Allocations\" tab on the left.</html>");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(10, 5, 10, 5);
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 2;
		formPanel.add(lblNewLabel, gbc_lblNewLabel);

		/* Verification Execution
		 * -------------------- */

		JSeparator sep1 = new JSeparator();
		GridBagConstraints gbc_sep1 = new GridBagConstraints();
		gbc_sep1.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep1.insets = new Insets(0, 5, 5, 5);
		gbc_sep1.gridwidth = 2;
		gbc_sep1.gridx = 0;
		gbc_sep1.gridy = 3;
		formPanel.add(sep1, gbc_sep1);

		edtVerificationExec = new VerificationExecutionEditor();
		omgVerificationExec = new DomainObjectManager<>(
			"Requires Verification", edtVerificationExec,
			
			// Create a new one each time the checkbox is re-ticked
			() -> new VerificationExecution(
				null, // No ID
				active.getTask().getVerification(), // May be null
				active,
				"", // No notes
				Duration.ofMinutes(0), // Zero deadline (user should set it)
				null, // No allocation
				null // Not completed
			)
		);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.anchor = GridBagConstraints.WEST;
		gbc_panel.gridwidth = 2;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		formPanel.add(omgVerificationExec, gbc_panel);
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
		omgVerificationExec.getObjectManager().setObject(taskExec.getVerification());
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
		
		if (
			!omgVerificationExec.getObjectManager().isObjectNull() &&
			!edtVerificationExec.validateFields()
		) valid = false;
		
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
		active.setVerification(omgVerificationExec.getObjectManager().getObject());
		
		return active;
	}
}
