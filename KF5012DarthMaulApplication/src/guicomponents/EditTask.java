package guicomponents;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import domain.Task;
import domain.TaskPriority;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class EditTask extends JScrollPane {
	private JTextField txtName;
	private JTextField txtNotes;
	private JComboBox<Object> cmbPriority;
	private JComboBox<Object> cmbAllocationConstraint;
	private boolean usersLoaded = false;

	/**
	 * Create the panel.
	 */
	@SuppressWarnings("serial")
	public EditTask() {
		JPanel formPanel = new JPanel();
		setViewportView(formPanel);
		GridBagLayout gbl_formPanel = new GridBagLayout();
		gbl_formPanel.columnWidths = new int[]{0, 0, 0};
		gbl_formPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_formPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_formPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		formPanel.setLayout(gbl_formPanel);
		
		/* Basics
		 * -------------------- */
		
		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(5, 5, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		formPanel.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		lblName.setLabelFor(txtName);
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.insets = new Insets(5, 5, 5, 5);
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		formPanel.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		
		JLabel lblNotes = new JLabel("Notes");
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.anchor = GridBagConstraints.EAST;
		gbc_lblNotes.insets = new Insets(0, 5, 5, 5);
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 1;
		formPanel.add(lblNotes, gbc_lblNotes);
		
		txtNotes = new JTextField();
		GridBagConstraints gbc_txtNotes = new GridBagConstraints();
		gbc_txtNotes.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtNotes.insets = new Insets(0, 5, 5, 5);
		gbc_txtNotes.gridx = 1;
		gbc_txtNotes.gridy = 1;
		formPanel.add(txtNotes, gbc_txtNotes);
		txtNotes.setColumns(10);
		
		JLabel lblPriority = new JLabel("Priority");
		GridBagConstraints gbc_lblPriority = new GridBagConstraints();
		gbc_lblPriority.anchor = GridBagConstraints.EAST;
		gbc_lblPriority.insets = new Insets(0, 5, 5, 5);
		gbc_lblPriority.gridx = 0;
		gbc_lblPriority.gridy = 2;
		formPanel.add(lblPriority, gbc_lblPriority);
		
		cmbPriority = new JComboBox<>(TaskPriority.values());
		GridBagConstraints gbc_cmbPriority = new GridBagConstraints();
		gbc_cmbPriority.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbPriority.insets = new Insets(0, 5, 5, 5);
		gbc_cmbPriority.gridx = 1;
		gbc_cmbPriority.gridy = 2;
		formPanel.add(cmbPriority, gbc_cmbPriority);
		
		JLabel lblAllocationConstraint = new JLabel("Allocation Constraint");
		GridBagConstraints gbc_lblAllocationConstraint = new GridBagConstraints();
		gbc_lblAllocationConstraint.insets = new Insets(0, 5, 5, 5);
		gbc_lblAllocationConstraint.gridx = 0;
		gbc_lblAllocationConstraint.gridy = 3;
		formPanel.add(lblAllocationConstraint, gbc_lblAllocationConstraint);

		cmbAllocationConstraint = new JComboBox<>();
		cmbAllocationConstraint.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(
					JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus
			) {
				super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
				
				// If it is the string "", then ...
				if (value == "") {
					this.setText("No Allocation Constraint");
				} else {
					User user = (User) value;
					this.setText(user.getUsername());
				}
				
				return this;
			}
		});
		GridBagConstraints gbc_cmbAllocationConstraint = new GridBagConstraints();
		gbc_cmbAllocationConstraint.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbAllocationConstraint.insets = new Insets(0, 5, 5, 5);
		gbc_cmbAllocationConstraint.gridx = 1;
		gbc_cmbAllocationConstraint.gridy = 3;
		formPanel.add(cmbAllocationConstraint, gbc_cmbAllocationConstraint);

		JSeparator sep1 = new JSeparator();
		GridBagConstraints gbc_sep1 = new GridBagConstraints();
		gbc_sep1.gridwidth = 2;
		gbc_sep1.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep1.insets = new Insets(0, 5, 5, 5);
		gbc_sep1.gridx = 0;
		gbc_sep1.gridy = 4;
		formPanel.add(sep1, gbc_sep1);

		/* Schedule
		 * -------------------- */
		
		JLabel lblNewLabel_5 = new JLabel("New label");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 5, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 5;
		formPanel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		JLabel lblNewLabel_4 = new JLabel("New label");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 5, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 6;
		formPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		JLabel lblNewLabel_3 = new JLabel("New label");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 5, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 7;
		formPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		JLabel lblNewLabel_2 = new JLabel("New label");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 5, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 8;
		formPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JLabel lblNewLabel_1 = new JLabel("New label");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 5, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 9;
		formPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JLabel lblNewLabel = new JLabel("New label");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 5, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 10;
		formPanel.add(lblNewLabel, gbc_lblNewLabel);
	}
	
	/**
	 * Load users into the allocation constraint combo box.
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
			} catch (FailedToConnectException e) {
				new ExceptionDialog("Could not connect to database. Please try again now or soon.", e);
				return;
			}
	
			// Get the users
			List<User> allUsers = db.getAllUsers();
			List<User> caretakers = (List<User>) allUsers.stream()
				.filter(u -> u.getAccountType() == PermissionManager.AccountType.CARETAKER)
				.collect(Collectors.toList());
	
			// (Re)fill the list
			this.cmbAllocationConstraint.removeAllItems();
			for (User user : caretakers) {
				this.cmbAllocationConstraint.addItem(user);
			}
			this.cmbAllocationConstraint.addItem(""); // "" == null (null is special-cased)
			this.usersLoaded  = true;
		}
	}
	
	/**
	 * Load users into the allocation constraint combo box. If already loaded,
	 * do not reload.
	 */
	public void loadUsers() {
		this.loadUsers(false);
	}
	
	/**
	 * Mark the given task to be the current task to edit.
	 * 
	 * @param task The task to edit.
	 */
	public void showTask(Task task) {
		this.txtName.setText(task.getName());
		this.txtNotes.setText(task.getNotes());
		this.cmbPriority.setSelectedItem(task.getStandardPriority());
		
		User allocConst = task.getAllocationConstraint();
		if (allocConst == null) {
			this.cmbAllocationConstraint.setSelectedItem(""); // null -> ""
		} else {
			this.cmbAllocationConstraint.setSelectedItem(allocConst);
		}
	}

	/**
	 * Validate fields and visually mark invalid fields.
	 * 
	 * @return True if all fields are valid, false otherwise.
	 */
	public boolean validateFields() {
		boolean valid = true;
		
		// Name
		String name = this.txtName.getText();
		if (name.isEmpty()) valid = false;
		
		// Notes - no validation
		// Standard Priority - combo box does validation
		// Allocation Constraint - combo box does validation
		
		return valid;
	}
	
	/**
	 * Update the given task with the values currently in the editor's inputs.
	 * 
	 * @param task The task to update.
	 */
	public void updateTask(Task task) {
		task.setName(this.txtName.getText());
		task.setNotes(this.txtNotes.getText());
		task.setStandardPriority(
			(TaskPriority) this.cmbPriority.getSelectedObjects()[0]);
		
		Object obj = this.cmbAllocationConstraint.getSelectedObjects()[0];
		if (obj instanceof String && obj.equals("")) {
			task.setAllocationConstraint(null);
		} else {
			task.setAllocationConstraint((User) obj);
		}
	}
}
