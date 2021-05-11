package guicomponents;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;

import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import lib.DurationField;
import guicomponents.utils.BoundedTimelinePanel;
import guicomponents.utils.DateTimePicker;

import domain.Task;
import domain.TaskPriority;
import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.IntervaledPeriodSet;

import java.util.List;
import java.util.stream.Collectors;
import java.time.Duration;
import javax.swing.JCheckBox;
import java.awt.event.ItemEvent;

@SuppressWarnings("serial")
public class EditTask extends JScrollPane {
	private JTextField txtName;
	private JTextField txtNotes;
	private JComboBox<Object> cmbPriority;
	private JComboBox<Object> cmbAllocationConstraint;
	
	// dtpSetRefStart is always enabled
	private JCheckBox chkSetRefDur;
	private JCheckBox chkSetInterval;
	private JCheckBox chkCSet;
	private JCheckBox chkCSetRefDur;
	private JCheckBox chkCSetInterval;
	
	private DateTimePicker dtpSetRefStart;
	private DurationField durSetRefDur;
	private DurationField durSetInterval;
	private DateTimePicker dtpCSetRefStart;
	private DurationField durCSetRefDur;
	private DurationField durCSetInterval;
	
	private boolean usersLoaded = false;

	/**
	 * Create the panel.
	 */
	public EditTask() {
		JPanel formPanel = new JPanel();
		setViewportView(formPanel);
		GridBagLayout gbl_formPanel = new GridBagLayout();
		gbl_formPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_formPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_formPanel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_formPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
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
		gbc_txtName.gridwidth = 2;
		gbc_txtName.anchor = GridBagConstraints.WEST;
		gbc_txtName.insets = new Insets(5, 5, 5, 0);
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
		gbc_txtNotes.gridwidth = 2;
		gbc_txtNotes.anchor = GridBagConstraints.WEST;
		gbc_txtNotes.insets = new Insets(0, 5, 5, 0);
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
		gbc_cmbPriority.gridwidth = 2;
		gbc_cmbPriority.anchor = GridBagConstraints.WEST;
		gbc_cmbPriority.insets = new Insets(0, 5, 5, 0);
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
					setText("No Allocation Constraint");
				} else {
					User user = (User) value;
					setText(user.getUsername());
				}
				
				return this;
			}
		});
		GridBagConstraints gbc_cmbAllocationConstraint = new GridBagConstraints();
		gbc_cmbAllocationConstraint.gridwidth = 2;
		gbc_cmbAllocationConstraint.anchor = GridBagConstraints.WEST;
		gbc_cmbAllocationConstraint.insets = new Insets(0, 5, 5, 0);
		gbc_cmbAllocationConstraint.gridx = 1;
		gbc_cmbAllocationConstraint.gridy = 3;
		formPanel.add(cmbAllocationConstraint, gbc_cmbAllocationConstraint);

		JSeparator sep1 = new JSeparator();
		GridBagConstraints gbc_sep1 = new GridBagConstraints();
		gbc_sep1.gridwidth = 3;
		gbc_sep1.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep1.insets = new Insets(0, 5, 5, 0);
		gbc_sep1.gridx = 0;
		gbc_sep1.gridy = 4;
		formPanel.add(sep1, gbc_sep1);

		/* Schedule - Graphical Overview
		 * -------------------- */
		
		BoundedTimelinePanel timelinePanel = new BoundedTimelinePanel();
		GridBagConstraints gbc_timelinePanel = new GridBagConstraints();
		gbc_timelinePanel.anchor = GridBagConstraints.WEST;
		gbc_timelinePanel.insets = new Insets(0, 5, 5, 0);
		gbc_timelinePanel.gridwidth = 3;
		gbc_timelinePanel.gridx = 0;
		gbc_timelinePanel.gridy = 5;
		formPanel.add(timelinePanel, gbc_timelinePanel);

		JSeparator sep2 = new JSeparator();
		GridBagConstraints gbc_sep2 = new GridBagConstraints();
		gbc_sep2.gridwidth = 3;
		gbc_sep2.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep2.insets = new Insets(0, 5, 5, 0);
		gbc_sep2.gridx = 0;
		gbc_sep2.gridy = 6;
		formPanel.add(sep2, gbc_sep2);

		/* Schedule - Fields
		 * -------------------- */
		
		JLabel lblSetRefStart = new JLabel("Earliest Start Time");
		GridBagConstraints gbc_lblSetRefStart = new GridBagConstraints();
		gbc_lblSetRefStart.anchor = GridBagConstraints.EAST;
		gbc_lblSetRefStart.insets = new Insets(0, 5, 5, 5);
		gbc_lblSetRefStart.gridx = 0;
		gbc_lblSetRefStart.gridy = 7;
		formPanel.add(lblSetRefStart, gbc_lblSetRefStart);
		
		dtpSetRefStart = new DateTimePicker();
		GridBagConstraints gbc_dtpSetRefStart = new GridBagConstraints();
		gbc_dtpSetRefStart.insets = new Insets(0, 5, 5, 0);
		gbc_dtpSetRefStart.anchor = GridBagConstraints.WEST;
		gbc_dtpSetRefStart.gridx = 2;
		gbc_dtpSetRefStart.gridy = 7;
		formPanel.add(dtpSetRefStart, gbc_dtpSetRefStart);
		
		JLabel lblSetRefDur = new JLabel("Maximum Duration");
		GridBagConstraints gbc_lblSetRefDur = new GridBagConstraints();
		gbc_lblSetRefDur.anchor = GridBagConstraints.EAST;
		gbc_lblSetRefDur.insets = new Insets(0, 5, 5, 5);
		gbc_lblSetRefDur.gridx = 0;
		gbc_lblSetRefDur.gridy = 8;
		formPanel.add(lblSetRefDur, gbc_lblSetRefDur);
		
		chkSetRefDur = new JCheckBox("");
		chkSetRefDur.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setSetRefDurEnabled(true, false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				setSetRefDurEnabled(false, false);
			}
		});
		GridBagConstraints gbc_chkSetRefDur = new GridBagConstraints();
		gbc_chkSetRefDur.insets = new Insets(0, 0, 5, 5);
		gbc_chkSetRefDur.gridx = 1;
		gbc_chkSetRefDur.gridy = 8;
		formPanel.add(chkSetRefDur, gbc_chkSetRefDur);

		durSetRefDur = new DurationField();
		GridBagConstraints gbc_spnSetRefDur = new GridBagConstraints();
		gbc_spnSetRefDur.anchor = GridBagConstraints.WEST;
		gbc_spnSetRefDur.insets = new Insets(0, 5, 5, 0);
		gbc_spnSetRefDur.gridx = 2;
		gbc_spnSetRefDur.gridy = 8;
		formPanel.add(durSetRefDur, gbc_spnSetRefDur);
		
		JLabel lblSetInterval = new JLabel("Regularity");
		GridBagConstraints gbc_lblSetInterval = new GridBagConstraints();
		gbc_lblSetInterval.anchor = GridBagConstraints.EAST;
		gbc_lblSetInterval.insets = new Insets(0, 5, 5, 5);
		gbc_lblSetInterval.gridx = 0;
		gbc_lblSetInterval.gridy = 9;
		formPanel.add(lblSetInterval, gbc_lblSetInterval);
		
		chkSetInterval = new JCheckBox("");
		chkSetInterval.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setSetIntervalEnabled(true, false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				setSetIntervalEnabled(false, false);
			}
		});
		GridBagConstraints gbc_chkSetInterval = new GridBagConstraints();
		gbc_chkSetInterval.insets = new Insets(0, 0, 5, 5);
		gbc_chkSetInterval.gridx = 1;
		gbc_chkSetInterval.gridy = 9;
		formPanel.add(chkSetInterval, gbc_chkSetInterval);

		durSetInterval = new DurationField();
		GridBagConstraints gbc_spnSetInterval = new GridBagConstraints();
		gbc_spnSetInterval.anchor = GridBagConstraints.WEST;
		gbc_spnSetInterval.insets = new Insets(0, 5, 5, 0);
		gbc_spnSetInterval.gridx = 2;
		gbc_spnSetInterval.gridy = 9;
		formPanel.add(durSetInterval, gbc_spnSetInterval);
		
		JLabel lblCSetRefBase = new JLabel("Constraint Base Time");
		GridBagConstraints gbc_lblCSetRefBase = new GridBagConstraints();
		gbc_lblCSetRefBase.anchor = GridBagConstraints.EAST;
		gbc_lblCSetRefBase.insets = new Insets(0, 5, 5, 5);
		gbc_lblCSetRefBase.gridx = 0;
		gbc_lblCSetRefBase.gridy = 10;
		formPanel.add(lblCSetRefBase, gbc_lblCSetRefBase);
		
		chkCSet = new JCheckBox("");
		chkCSet.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setCSetEnabled(true, false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				setCSetEnabled(false, false);
			}
		});
		GridBagConstraints gbc_chkCSet = new GridBagConstraints();
		gbc_chkCSet.insets = new Insets(0, 0, 5, 5);
		gbc_chkCSet.gridx = 1;
		gbc_chkCSet.gridy = 10;
		formPanel.add(chkCSet, gbc_chkCSet);
		
		dtpCSetRefStart = new DateTimePicker();
		GridBagConstraints gbc_dtpCSetRefStart = new GridBagConstraints();
		gbc_dtpCSetRefStart.anchor = GridBagConstraints.WEST;
		gbc_dtpCSetRefStart.insets = new Insets(0, 5, 5, 0);
		gbc_dtpCSetRefStart.gridx = 2;
		gbc_dtpCSetRefStart.gridy = 10;
		formPanel.add(dtpCSetRefStart, gbc_dtpCSetRefStart);
		
		JLabel lblCSetRefDur = new JLabel("Constraint Duration");
		GridBagConstraints gbc_lblCSetRefDur = new GridBagConstraints();
		gbc_lblCSetRefDur.anchor = GridBagConstraints.EAST;
		gbc_lblCSetRefDur.insets = new Insets(0, 5, 5, 5);
		gbc_lblCSetRefDur.gridx = 0;
		gbc_lblCSetRefDur.gridy = 11;
		formPanel.add(lblCSetRefDur, gbc_lblCSetRefDur);
		
		chkCSetRefDur = new JCheckBox("");
		chkCSetRefDur.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setCSetRefDurEnabled(true, false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				setCSetRefDurEnabled(false, false);
			}
		});
		GridBagConstraints gbc_chkCSetRefDur = new GridBagConstraints();
		gbc_chkCSetRefDur.insets = new Insets(0, 0, 5, 5);
		gbc_chkCSetRefDur.gridx = 1;
		gbc_chkCSetRefDur.gridy = 11;
		formPanel.add(chkCSetRefDur, gbc_chkCSetRefDur);
		
		durCSetRefDur = new DurationField();
		GridBagConstraints gbc_durCSetRefDur = new GridBagConstraints();
		gbc_durCSetRefDur.insets = new Insets(0, 5, 5, 0);
		gbc_durCSetRefDur.anchor = GridBagConstraints.WEST;
		gbc_durCSetRefDur.gridx = 2;
		gbc_durCSetRefDur.gridy = 11;
		formPanel.add(durCSetRefDur, gbc_durCSetRefDur);
		
		JLabel lblCSetInterval = new JLabel("Constraint Interval");
		GridBagConstraints gbc_lblCSetInterval = new GridBagConstraints();
		gbc_lblCSetInterval.anchor = GridBagConstraints.EAST;
		gbc_lblCSetInterval.insets = new Insets(0, 5, 0, 5);
		gbc_lblCSetInterval.gridx = 0;
		gbc_lblCSetInterval.gridy = 12;
		formPanel.add(lblCSetInterval, gbc_lblCSetInterval);
		
		chkCSetInterval = new JCheckBox("");
		chkCSetInterval.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setCSetIntervalEnabled(true, false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				setCSetIntervalEnabled(false, false);
			}
		});
		GridBagConstraints gbc_chkCSetInterval = new GridBagConstraints();
		gbc_chkCSetInterval.insets = new Insets(0, 0, 0, 5);
		gbc_chkCSetInterval.gridx = 1;
		gbc_chkCSetInterval.gridy = 12;
		formPanel.add(chkCSetInterval, gbc_chkCSetInterval);
		
		durCSetInterval = new DurationField();
		GridBagConstraints gbc_durCSetInterval = new GridBagConstraints();
		gbc_durCSetInterval.insets = new Insets(0, 5, 0, 0);
		gbc_durCSetInterval.anchor = GridBagConstraints.WEST;
		gbc_durCSetInterval.gridx = 2;
		gbc_durCSetInterval.gridy = 12;
		formPanel.add(durCSetInterval, gbc_durCSetInterval);
	}

	private void setSetRefDurEnabled(boolean enabled, boolean force) {
		if (force) {
			chkSetRefDur.setSelected(enabled);
		}
		durSetRefDur.setVisible(enabled);
	}
	
	private void setSetIntervalEnabled(boolean enabled, boolean force) {
		if (force) {
			chkSetInterval.setSelected(enabled);
		}
		durSetInterval.setVisible(enabled);
	}

	private void setCSetEnabled(boolean enabled, boolean force) {
		if (force) {
			chkCSet.setSelected(enabled);
		}

		// Show/Hide corresponding items, but keep checkbox values, regardless
		// of force (they're not controlled by this checkbox).
		setCSetRefDurEnabled(chkCSetRefDur.isSelected(), false);
		setCSetIntervalEnabled(chkCSetInterval.isSelected(), false);
		
		// Enable/Disable (but don't show/hide) the checkboxes
		chkCSetRefDur.setEnabled(enabled);
		chkCSetInterval.setEnabled(enabled);
		
		// Show/Hide cSetStart
		dtpCSetRefStart.setVisible(enabled);
	}

	private void setCSetRefDurEnabled(boolean enabled, boolean force) {
		if (force) {
			chkCSetRefDur.setSelected(enabled);
		}
		durCSetRefDur.setVisible(enabled);
	}
	
	private void setCSetIntervalEnabled(boolean enabled, boolean force) {
		if (force) {
			chkCSetInterval.setSelected(enabled);
		}
		durCSetInterval.setVisible(enabled);
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
			cmbAllocationConstraint.removeAllItems();
			for (User user : caretakers) {
				cmbAllocationConstraint.addItem(user);
			}
			cmbAllocationConstraint.addItem(""); // "" == null (null is special-cased)
			usersLoaded  = true;
		}
	}
	
	/**
	 * Load users into the allocation constraint combo box. If already loaded,
	 * do not reload.
	 */
	public void loadUsers() {
		loadUsers(false);
	}
	
	/**
	 * Mark the given task to be the current task to edit.
	 * 
	 * @param task The task to edit.
	 */
	public void showTask(Task task) {
		txtName.setText(task.getName());
		txtNotes.setText(task.getNotes());
		cmbPriority.setSelectedItem(task.getStandardPriority());
		
		User allocConst = task.getAllocationConstraint();
		if (allocConst == null) {
			cmbAllocationConstraint.setSelectedItem(""); // null -> ""
		} else {
			cmbAllocationConstraint.setSelectedItem(allocConst);
		}
		
		ConstrainedIntervaledPeriodSet cips = task.getScheduleConstraint();
		
		IntervaledPeriodSet set = cips.periodSet();
		dtpSetRefStart.setDateTime(set.referencePeriod().start());
		
		Duration setRefDur = set.referencePeriod().duration();
		if (setRefDur == null) {
			setSetRefDurEnabled(false, true);
		} else {
			setSetRefDurEnabled(true, true);
			// If these overflow ... it is the user's fault >:(
			durSetRefDur.setHour((int) setRefDur.getSeconds() / 3600);
			durSetRefDur.setMinute((int) setRefDur.getSeconds() / 60);
		}
		
		Duration setInterval = set.interval();
		if (setInterval == null) {
			setSetIntervalEnabled(false, true);
		} else {
			setSetIntervalEnabled(true, true);
			durSetInterval.setHour((int) setInterval.getSeconds() / 3600);
			durSetInterval.setMinute((int) setInterval.getSeconds() / 60);
		}

		IntervaledPeriodSet cSet = cips.periodSetConstraint();
		if (cSet == null) {
			setCSetEnabled(false, true);
		} else {
			setCSetEnabled(true, true);
			dtpCSetRefStart.setDateTime(cSet.referencePeriod().start());
			
			Duration cSetRefDur = cSet.referencePeriod().duration();
			if (cSetRefDur == null) {
				setCSetRefDurEnabled(false, true);
			} else {
				setCSetRefDurEnabled(true, true);
				durCSetRefDur.setHour((int) cSetRefDur.getSeconds() / 3600);
				durCSetRefDur.setMinute((int) cSetRefDur.getSeconds() / 60);
			}
			
			Duration cSetInterval = cSet.interval();
			if (cSetInterval == null) {
				setCSetIntervalEnabled(false, true);
			} else {
				setCSetIntervalEnabled(true, true);
				durCSetInterval.setHour((int) cSetInterval.getSeconds() / 3600);
				durCSetInterval.setMinute((int) cSetInterval.getSeconds() / 60);
			}
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
		String name = txtName.getText();
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
		task.setName(txtName.getText());
		task.setNotes(txtNotes.getText());
		task.setStandardPriority(
			(TaskPriority) cmbPriority.getSelectedObjects()[0]);
		
		Object obj = cmbAllocationConstraint.getSelectedObjects()[0];
		if (obj instanceof String && obj.equals("")) {
			task.setAllocationConstraint(null);
		} else {
			task.setAllocationConstraint((User) obj);
		}
	}
}
