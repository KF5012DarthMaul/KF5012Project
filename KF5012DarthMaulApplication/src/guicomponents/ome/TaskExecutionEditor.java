package guicomponents.ome;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions;
import domain.Completion;
import domain.TaskCompletionQuality;
import domain.TaskPriority;
import domain.TaskExecution;
import domain.VerificationExecution;
import guicomponents.utils.ObjectEditor;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;

@SuppressWarnings("serial")
public class TaskExecutionEditor
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
	
	//Completion editor
	private CompletionEditor edtCompletion;
	private DomainObjectManager<Completion> omgCompletion;
	/**
	 * Create the panel.
	 */
	public TaskExecutionEditor() {
		JPanel formPanel = new JPanel();
		setViewportView(formPanel);
		GridBagLayout gbl_formPanel = new GridBagLayout();
		gbl_formPanel.columnWidths = new int[]{0, 0, 0};
		gbl_formPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_formPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_formPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ,Double.MIN_VALUE};
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
	
		/* Completion Editor
		 * -------------------- */
		
		JSeparator sep2 = new JSeparator();
		GridBagConstraints gbc_sep2 = new GridBagConstraints();
		gbc_sep2.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep2.insets = new Insets(0, 5, 5, 5);
		gbc_sep2.gridwidth = 2;
		gbc_sep2.gridx = 0;
		gbc_sep2.gridy = 3;
		formPanel.add(sep2, gbc_sep2);

		edtCompletion = new CompletionEditor();

		omgCompletion = new DomainObjectManager<>(
			"Is complete", edtCompletion,

			// Create a new one each time the checkbox is re-ticked
			() -> new Completion(
				null, // No ID
				active.getAllocation(),
				active.getPeriod().start(),
				LocalDateTime.now(),
				TaskCompletionQuality.GOOD,
				"" // No notes
			)
		);

		GridBagConstraints gbc_compPanel = new GridBagConstraints();
		gbc_compPanel.insets = new Insets(5, 5, 5, 5);
		gbc_compPanel.anchor = GridBagConstraints.WEST;
		gbc_compPanel.gridwidth = 2;
		gbc_compPanel.gridx = 0;
		gbc_compPanel.gridy = 4;
		formPanel.add(omgCompletion, gbc_compPanel);

		/* Verification Execution
		 * -------------------- */

		JSeparator sep1 = new JSeparator();
		GridBagConstraints gbc_sep1 = new GridBagConstraints();
		gbc_sep1.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep1.insets = new Insets(0, 5, 5, 5);
		gbc_sep1.gridwidth = 2;
		gbc_sep1.gridx = 0;
		gbc_sep1.gridy = 5;
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
		gbc_panel.gridy = 6;
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
		omgCompletion.getObjectManager().setObject(taskExec.getCompletion());
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
		if (!omgVerificationExec.getObjectManager().validateFields()) valid = false;
		if (!omgCompletion.getObjectManager().validateFields()) valid = false;

		VerificationExecution verifExec = omgVerificationExec.getObjectManager().getObject();
		Completion compl = omgCompletion.getObjectManager().getObject();
		Completion verifCompl = (verifExec == null ? null : verifExec.getCompletion());

		// You cannot complete the verification of a task before completing the
		// task.
		if (verifCompl != null && compl == null) {
			new ExceptionDialog("Cannot complete a verification without completing the task.");
			valid = false;
			
		} else if (verifCompl != null && compl != null) {
			// The same user cannot complete both a task execution and its
			// verification execution.
			if (compl.getStaff() == verifCompl.getStaff()) {
				new ExceptionDialog("Task and verification cannot be completed by the same person.");
				valid = false;
			}
			
			// You cannot start a verification before completing the task.
			if (verifCompl.getStartTime().isBefore(compl.getCompletionTime())) {
				new ExceptionDialog("You cannot have started a verification before completing the task.");
				valid = false;
			}
		}
		
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
		active.setCompletion(omgCompletion.getObjectManager().getObject());
		
		return active;
	}
	
	
	/* Allocation combo box management
	 * -------------------------------------------------- */
	// Loading of users for various components
	private boolean usersLoaded = false;
	/**
	 * Load users into the allocation constraint combo box and all other
	 * components that require them.
	 * 
	 * @param reload If users are cached, whether to re-fetch users regardless.
	 */
	public void loadUsers(boolean reload) {
		if (!usersLoaded || reload) {
			// Try to connect to the DB to get users - if that fails, you won't be
			// able to edit the user, but can try again.
			DBAbstraction db;
			try {
				db = DBAbstraction.getInstance();
			} catch (DBExceptions.FailedToConnectException e) {
				new ExceptionDialog("Could not connect to database. Please try again now or soon.", e);
				return;
			}

			// Get the users
			List<User> allUsers = db.getAllUsers();
			List<User> caretakersAndNull = 
				allUsers.stream()
				.filter(u -> u.getAccountType() == PermissionManager.AccountType.CARETAKER)
				.collect(Collectors.toList());
			
			// (Re)fill the list
			edtCompletion.setUsers(caretakersAndNull);
			edtVerificationExec.loadUsers(caretakersAndNull);
			usersLoaded = true;
		}
	}
	
	/**
	 * Load users into the allocation constraint combo box. If already loaded,
	 * do not reload.
	 */
	public void loadUsers() {
		loadUsers(false);
	}
	
	private static <T> List<T> nullable(List<T> list) {
		List<T> fullList = list;
		fullList.add(null);
		return fullList;
	}
}
