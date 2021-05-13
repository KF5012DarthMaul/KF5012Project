package guicomponents;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.event.ItemEvent;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import lib.DurationField;
import guicomponents.subcomponents.DomainObjectManager;
import guicomponents.subcomponents.VerificationEditor;
import guicomponents.utils.BoundedTimelinePanel;
import guicomponents.utils.DateRangePicker;
import guicomponents.utils.DateTimePicker;
import guicomponents.utils.NullableComboBox;
import guicomponents.utils.ObjectEditor;
import guicomponents.utils.TimelinePanel;
import kf5012darthmaulapplication.ExceptionDialog;

import domain.Task;
import domain.TaskPriority;
import domain.Verification;
import kf5012darthmaulapplication.User;
import kf5012darthmaulapplication.PermissionManager;
import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;

import temporal.BasicChartableEvent;
import temporal.ChartableEvent;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.GenerativeTemporalMap;
import temporal.IntervaledPeriodSet;
import temporal.Period;
import temporal.TemporalMap;
import temporal.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.LocalDateTime;

@SuppressWarnings("serial")
public class EditTask extends JPanel implements ObjectEditor<Task> {
	// Basic fields
	private JTextField txtName;
	private JTextArea txtNotes;
	private JComboBox<Object> cmbPriority;
	private NullableComboBox<User> ncmbAllocationConstraint;
	
	// Timeline
	private List<ChartableEvent> currentTimelineHistory;
	private GenerativeTemporalMap<ChartableEvent> currentTimelineMap;
	private TimelinePanel timelinePanel;
	private DateRangePicker dateRangePicker;
	
	// Schedule fields
	// dtpSetRefStart is always enabled
	private JCheckBox chkSetRefEnd;
	private JCheckBox chkSetInterval;
	private JCheckBox chkCSet;
	private JCheckBox chkCSetRefEnd;
	private JCheckBox chkCSetInterval;
	
	private DateTimePicker dtpSetRefStart;
	private DateTimePicker dtpSetRefEnd;
	private DurationField durSetInterval;
	private DateTimePicker dtpCSetRefStart;
	private DateTimePicker dtpCSetRefEnd;
	private DurationField durCSetInterval;
	
	// Verification editor
	private VerificationEditor verificationEditor;
	private DomainObjectManager<Verification> omgVerification;
	
	// Loading of users for various components
	private boolean usersLoaded = false;

	/**
	 * Set up the Edit Task panel.
	 */
	public EditTask() {
		setLayout(new BorderLayout(0,0));
		
		JScrollPane sclWrapper = new JScrollPane();
		add(sclWrapper, BorderLayout.CENTER);
		
		JPanel formPanel = new JPanel();
		sclWrapper.setViewportView(formPanel);
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
		gbc_txtName.anchor = GridBagConstraints.WEST;
		gbc_txtName.insets = new Insets(5, 5, 5, 0);
		gbc_txtName.gridwidth = 2;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		formPanel.add(txtName, gbc_txtName);
		txtName.setColumns(40);
		
		JLabel lblNotes = new JLabel("Notes");
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.anchor = GridBagConstraints.EAST;
		gbc_lblNotes.insets = new Insets(0, 5, 5, 5);
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 1;
		formPanel.add(lblNotes, gbc_lblNotes);
		
		txtNotes = new JTextArea();
		txtNotes.setLineWrap(true);
		txtNotes.setWrapStyleWord(true);
		GridBagConstraints gbc_txtNotes = new GridBagConstraints();
		gbc_txtNotes.gridwidth = 2;
		gbc_txtNotes.anchor = GridBagConstraints.WEST;
		gbc_txtNotes.insets = new Insets(0, 5, 5, 0);
		gbc_txtNotes.gridx = 1;
		gbc_txtNotes.gridy = 1;
		formPanel.add(txtNotes, gbc_txtNotes);
		txtNotes.setColumns(40);
		txtNotes.setRows(6);
		
		JLabel lblPriority = new JLabel("Priority");
		GridBagConstraints gbc_lblPriority = new GridBagConstraints();
		gbc_lblPriority.anchor = GridBagConstraints.EAST;
		gbc_lblPriority.insets = new Insets(0, 5, 5, 5);
		gbc_lblPriority.gridx = 0;
		gbc_lblPriority.gridy = 2;
		formPanel.add(lblPriority, gbc_lblPriority);
		
		cmbPriority = new JComboBox<>(TaskPriority.values());
		GridBagConstraints gbc_cmbPriority = new GridBagConstraints();
		gbc_cmbPriority.anchor = GridBagConstraints.WEST;
		gbc_cmbPriority.insets = new Insets(0, 5, 5, 0);
		gbc_cmbPriority.gridwidth = 2;
		gbc_cmbPriority.gridx = 1;
		gbc_cmbPriority.gridy = 2;
		formPanel.add(cmbPriority, gbc_cmbPriority);
		
		JLabel lblAllocationConstraint = new JLabel("Allocation Constraint");
		GridBagConstraints gbc_lblAllocationConstraint = new GridBagConstraints();
		gbc_lblAllocationConstraint.insets = new Insets(0, 5, 5, 5);
		gbc_lblAllocationConstraint.gridx = 0;
		gbc_lblAllocationConstraint.gridy = 3;
		formPanel.add(lblAllocationConstraint, gbc_lblAllocationConstraint);

		ncmbAllocationConstraint = new NullableComboBox<>((user) -> {
			return user == null ? "No Allocation Constraint" : user.getUsername();
		});
		GridBagConstraints gbc_cmbAllocationConstraint = new GridBagConstraints();
		gbc_cmbAllocationConstraint.anchor = GridBagConstraints.WEST;
		gbc_cmbAllocationConstraint.insets = new Insets(0, 5, 5, 0);
		gbc_cmbAllocationConstraint.gridwidth = 2;
		gbc_cmbAllocationConstraint.gridx = 1;
		gbc_cmbAllocationConstraint.gridy = 3;
		formPanel.add(ncmbAllocationConstraint, gbc_cmbAllocationConstraint);
		
		/* Schedule - Graphical Overview
		 * -------------------- */
		
		JSeparator sep1 = new JSeparator();
		GridBagConstraints gbc_sep1 = new GridBagConstraints();
		gbc_sep1.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep1.insets = new Insets(0, 5, 5, 0);
		gbc_sep1.gridwidth = 3;
		gbc_sep1.gridx = 0;
		gbc_sep1.gridy = 4;
		formPanel.add(sep1, gbc_sep1);

		// Don't give it a Timeline yet - that's can only be done when a task is
		// selected to be edited.
		timelinePanel = new TimelinePanel();
		timelinePanel.setPreferredSize(
			new Dimension(this.getPreferredSize().width, 100)
		);
		
		dateRangePicker = new DateRangePicker("From", "To");

		// If the date/time range of the bounded timeline panel changes, then
		// regenerate the data to be displayed in the timeline panel completely.
		// If you don't do this, the generator won't generate events before the
		// end of the latest event generated.
		// Note 2: This change listener must be added before the dateRangePicker
		//         is passed to the BoundedTimelinePanel, or the data won't be
		//         wiped before the bounded timeline panel tries to re-fetch the
		//         data to re-draw the timeline panel, which may lead to missing
		//         events.
		dateRangePicker.addChangeListener((e) -> this.resetTimeline());
		
		// Link together the timeline panel and the date range picker so that
		// the panel responds to changes in the date range (or if its own
		// timeline changes).
		BoundedTimelinePanel boundedTimelinePanel = new BoundedTimelinePanel(
			timelinePanel, dateRangePicker, true
		);
		
		// Then add as usual
		GridBagConstraints gbc_timelinePanel = new GridBagConstraints();
		gbc_timelinePanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_timelinePanel.insets = new Insets(0, 5, 5, 0);
		gbc_timelinePanel.gridwidth = 3;
		gbc_timelinePanel.gridx = 0;
		gbc_timelinePanel.gridy = 5;
		formPanel.add(boundedTimelinePanel, gbc_timelinePanel);

		JButton btnUpdateTimeline = new JButton("Update Timeline");
		btnUpdateTimeline.addActionListener((e) -> {
			this.updateTimeline(txtName.getText(), getScheduleConstraint());
		});
		GridBagConstraints gbc_btnUpdateTimeline = new GridBagConstraints();
		gbc_btnUpdateTimeline.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUpdateTimeline.insets = new Insets(0, 5, 5, 0);
		gbc_btnUpdateTimeline.gridx = 0;
		gbc_btnUpdateTimeline.gridy = 6;
		formPanel.add(btnUpdateTimeline, gbc_btnUpdateTimeline);

		/* Schedule - Fields
		 * -------------------- */

		JSeparator sep2 = new JSeparator();
		GridBagConstraints gbc_sep2 = new GridBagConstraints();
		gbc_sep2.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep2.insets = new Insets(0, 5, 5, 0);
		gbc_sep2.gridwidth = 3;
		gbc_sep2.gridx = 0;
		gbc_sep2.gridy = 7;
		formPanel.add(sep2, gbc_sep2);

		// Set ref start
		
		JLabel lblSetRefStart = new JLabel("Earliest Start Time");
		GridBagConstraints gbc_lblSetRefStart = new GridBagConstraints();
		gbc_lblSetRefStart.anchor = GridBagConstraints.EAST;
		gbc_lblSetRefStart.insets = new Insets(0, 5, 5, 5);
		gbc_lblSetRefStart.gridx = 0;
		gbc_lblSetRefStart.gridy = 8;
		formPanel.add(lblSetRefStart, gbc_lblSetRefStart);
		
		dtpSetRefStart = new DateTimePicker();
		GridBagConstraints gbc_dtpSetRefStart = new GridBagConstraints();
		gbc_dtpSetRefStart.insets = new Insets(0, 5, 5, 0);
		gbc_dtpSetRefStart.anchor = GridBagConstraints.WEST;
		gbc_dtpSetRefStart.gridx = 2;
		gbc_dtpSetRefStart.gridy = 8;
		formPanel.add(dtpSetRefStart, gbc_dtpSetRefStart);
		
		// Set ref end
		
		JLabel lblSetRefEnd = new JLabel("Latest End Time");
		GridBagConstraints gbc_lblSetRefEnd = new GridBagConstraints();
		gbc_lblSetRefEnd.anchor = GridBagConstraints.EAST;
		gbc_lblSetRefEnd.insets = new Insets(0, 5, 5, 5);
		gbc_lblSetRefEnd.gridx = 0;
		gbc_lblSetRefEnd.gridy = 9;
		formPanel.add(lblSetRefEnd, gbc_lblSetRefEnd);
		
		chkSetRefEnd = new JCheckBox("");
		chkSetRefEnd.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setSetRefEndEnabled(true, false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				setSetRefEndEnabled(false, false);
			}
		});
		GridBagConstraints gbc_chkSetRefEnd = new GridBagConstraints();
		gbc_chkSetRefEnd.insets = new Insets(0, 0, 5, 5);
		gbc_chkSetRefEnd.gridx = 1;
		gbc_chkSetRefEnd.gridy = 9;
		formPanel.add(chkSetRefEnd, gbc_chkSetRefEnd);

		dtpSetRefEnd = new DateTimePicker();
		GridBagConstraints gbc_dtpSetRefEnd = new GridBagConstraints();
		gbc_dtpSetRefEnd.insets = new Insets(0, 5, 5, 0);
		gbc_dtpSetRefEnd.anchor = GridBagConstraints.WEST;
		gbc_dtpSetRefEnd.gridx = 2;
		gbc_dtpSetRefEnd.gridy = 9;
		formPanel.add(dtpSetRefEnd, gbc_dtpSetRefEnd);
		
		// Set interval
		
		JLabel lblSetInterval = new JLabel("Regularity");
		GridBagConstraints gbc_lblSetInterval = new GridBagConstraints();
		gbc_lblSetInterval.anchor = GridBagConstraints.EAST;
		gbc_lblSetInterval.insets = new Insets(0, 5, 5, 5);
		gbc_lblSetInterval.gridx = 0;
		gbc_lblSetInterval.gridy = 10;
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
		gbc_chkSetInterval.gridy = 10;
		formPanel.add(chkSetInterval, gbc_chkSetInterval);

		durSetInterval = new DurationField();
		GridBagConstraints gbc_spnSetInterval = new GridBagConstraints();
		gbc_spnSetInterval.anchor = GridBagConstraints.WEST;
		gbc_spnSetInterval.insets = new Insets(0, 5, 5, 0);
		gbc_spnSetInterval.gridx = 2;
		gbc_spnSetInterval.gridy = 10;
		formPanel.add(durSetInterval, gbc_spnSetInterval);
		
		// CSet ref start
		
		JLabel lblCSetRefStart = new JLabel("Constraint Start");
		GridBagConstraints gbc_lblCSetRefStart = new GridBagConstraints();
		gbc_lblCSetRefStart.anchor = GridBagConstraints.EAST;
		gbc_lblCSetRefStart.insets = new Insets(0, 5, 5, 5);
		gbc_lblCSetRefStart.gridx = 0;
		gbc_lblCSetRefStart.gridy = 11;
		formPanel.add(lblCSetRefStart, gbc_lblCSetRefStart);
		
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
		gbc_chkCSet.gridy = 11;
		formPanel.add(chkCSet, gbc_chkCSet);
		
		dtpCSetRefStart = new DateTimePicker();
		GridBagConstraints gbc_dtpCSetRefStart = new GridBagConstraints();
		gbc_dtpCSetRefStart.anchor = GridBagConstraints.WEST;
		gbc_dtpCSetRefStart.insets = new Insets(0, 5, 5, 0);
		gbc_dtpCSetRefStart.gridx = 2;
		gbc_dtpCSetRefStart.gridy = 11;
		formPanel.add(dtpCSetRefStart, gbc_dtpCSetRefStart);
		
		// CSet ref end
		
		JLabel lblCSetRefEnd = new JLabel("Constraint End");
		GridBagConstraints gbc_lblCSetRefEnd = new GridBagConstraints();
		gbc_lblCSetRefEnd.anchor = GridBagConstraints.EAST;
		gbc_lblCSetRefEnd.insets = new Insets(0, 5, 5, 5);
		gbc_lblCSetRefEnd.gridx = 0;
		gbc_lblCSetRefEnd.gridy = 12;
		formPanel.add(lblCSetRefEnd, gbc_lblCSetRefEnd);
		
		chkCSetRefEnd = new JCheckBox("");
		chkCSetRefEnd.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setCSetRefEndEnabled(true, false);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				setCSetRefEndEnabled(false, false);
			}
		});
		GridBagConstraints gbc_chkCSetRefEnd = new GridBagConstraints();
		gbc_chkCSetRefEnd.insets = new Insets(0, 0, 5, 5);
		gbc_chkCSetRefEnd.gridx = 1;
		gbc_chkCSetRefEnd.gridy = 12;
		formPanel.add(chkCSetRefEnd, gbc_chkCSetRefEnd);

		dtpCSetRefEnd = new DateTimePicker();
		GridBagConstraints gbc_dtpCSetRefEnd = new GridBagConstraints();
		gbc_dtpCSetRefEnd.insets = new Insets(0, 5, 5, 0);
		gbc_dtpCSetRefEnd.anchor = GridBagConstraints.WEST;
		gbc_dtpCSetRefEnd.gridx = 2;
		gbc_dtpCSetRefEnd.gridy = 12;
		formPanel.add(dtpCSetRefEnd, gbc_dtpCSetRefEnd);
		
		// CSet interval
		
		JLabel lblCSetInterval = new JLabel("Constraint Interval");
		GridBagConstraints gbc_lblCSetInterval = new GridBagConstraints();
		gbc_lblCSetInterval.anchor = GridBagConstraints.EAST;
		gbc_lblCSetInterval.insets = new Insets(0, 5, 0, 5);
		gbc_lblCSetInterval.gridx = 0;
		gbc_lblCSetInterval.gridy = 13;
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
		gbc_chkCSetInterval.gridy = 13;
		formPanel.add(chkCSetInterval, gbc_chkCSetInterval);
		
		durCSetInterval = new DurationField();
		GridBagConstraints gbc_durCSetInterval = new GridBagConstraints();
		gbc_durCSetInterval.insets = new Insets(0, 5, 0, 0);
		gbc_durCSetInterval.anchor = GridBagConstraints.WEST;
		gbc_durCSetInterval.gridx = 2;
		gbc_durCSetInterval.gridy = 13;
		formPanel.add(durCSetInterval, gbc_durCSetInterval);

		/* Verification
		 * -------------------- */

		JSeparator sep3 = new JSeparator();
		GridBagConstraints gbc_sep3 = new GridBagConstraints();
		gbc_sep3.fill = GridBagConstraints.HORIZONTAL;
		gbc_sep3.insets = new Insets(0, 5, 5, 0);
		gbc_sep3.gridwidth = 3;
		gbc_sep3.gridx = 0;
		gbc_sep3.gridy = 14;
		formPanel.add(sep3, gbc_sep3);

		verificationEditor = new VerificationEditor();
		omgVerification = new DomainObjectManager<>(
			"Requires Verification", verificationEditor, () -> new Verification()
		);
		GridBagConstraints gbc_edtVerificationEditor = new GridBagConstraints();
		gbc_edtVerificationEditor.insets = new Insets(0, 5, 0, 0);
		gbc_edtVerificationEditor.anchor = GridBagConstraints.WEST;
		gbc_edtVerificationEditor.gridwidth = 3;
		gbc_edtVerificationEditor.gridx = 0;
		gbc_edtVerificationEditor.gridy = 15;
		formPanel.add(omgVerification, gbc_edtVerificationEditor);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	/* Allocation combo box management
	 * -------------------------------------------------- */
	
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
			} catch (FailedToConnectException e) {
				new ExceptionDialog("Could not connect to database. Please try again now or soon.", e);
				return;
			}

			// Get the users
			List<User> allUsers = db.getAllUsers();
			List<User> caretakers = (List<User>) allUsers.stream()
				.filter(u -> u.getAccountType() == PermissionManager.AccountType.CARETAKER)
				.collect(Collectors.toList());
			
			// (Re)fill the lists
			ncmbAllocationConstraint.populate(caretakers);
			verificationEditor.setUsers(caretakers);
			
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
	
	/* Timeline Management
	 * -------------------------------------------------- */
	
	/**
	 * (Re)Initialise the generative temporal map of the timeline panel and set
	 * the timeline panel to track the new map.
	 * 
	 * @param name The name to give events in the timeline.
	 * @param cips The task schedule constraint to use for the timeline.
	 */
	private void updateTimeline(String name, ConstrainedIntervaledPeriodSet cips) {
		currentTimelineHistory = new ArrayList<>();
		currentTimelineMap = new GenerativeTemporalMap<>(
			currentTimelineHistory, cips,
			(p) -> new BasicChartableEvent(p, name)
		);
		List<TemporalMap<Integer, ChartableEvent>> maps = new ArrayList<>();
		maps.add(currentTimelineMap);

		// setTimeline() fires a change event, which the BoundedTimelinePanel
		// picks up and re-displays the current date range with the new timeline.
		timelinePanel.setTimeline(new Timeline<>(maps));
	}
	
	/**
	 * Clear the events in the generative temporal map of the timeline panel,
	 * and regenerate the events within its current date range, without changing
	 * the map being used.
	 */
	private void resetTimeline() {
		// Note: currentTimelineHistory and currentTimelineMap are
		//       (re)initialised every time the task being edited is changed.
		currentTimelineHistory.clear();
		currentTimelineMap.generateBetween(
			dateRangePicker.getStartDateTime(),
			dateRangePicker.getEndDateTime()
		);
	}

	/* Schedule value/display management
	 * -------------------------------------------------- */
	
	private void setSetRefEndEnabled(boolean enabled, boolean force) {
		if (force) {
			chkSetRefEnd.setSelected(enabled);
		}
		dtpSetRefEnd.setVisible(enabled);
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
		setCSetRefEndEnabled(enabled ? chkCSetRefEnd.isSelected() : enabled, false);
		setCSetIntervalEnabled(enabled ? chkCSetInterval.isSelected() : enabled, false);
		
		// Enable/Disable (but don't show/hide) the checkboxes
		chkCSetRefEnd.setEnabled(enabled);
		chkCSetInterval.setEnabled(enabled);
		
		// Show/Hide cSetStart
		dtpCSetRefStart.setVisible(enabled);
	}

	private void setCSetRefEndEnabled(boolean enabled, boolean force) {
		if (force) {
			chkCSetRefEnd.setSelected(enabled);
		}
		dtpCSetRefEnd.setVisible(enabled);
	}
	
	private void setCSetIntervalEnabled(boolean enabled, boolean force) {
		if (force) {
			chkCSetInterval.setSelected(enabled);
		}
		durCSetInterval.setVisible(enabled);
	}

	/* Verification value/display management
	 * -------------------------------------------------- */
	
	private void setVerification(Verification verification) {
		omgVerification.getObjectManager().setObject(verification);
	}
	
	/* Task get/validate/update cycle
	 * -------------------------------------------------- */
	
	/**
	 * Mark the given task to be the current task to edit.
	 * 
	 * @param task The task to edit.
	 */
	@Override
	public void showObject(Task task) {
		/* Basic fields
		 * -------------------- */
		
		txtName.setText(task.getName());
		txtNotes.setText(task.getNotes());
		cmbPriority.setSelectedItem(task.getStandardPriority());
		ncmbAllocationConstraint.setSelection(task.getAllocationConstraint());

		/* Schedule
		 * -------------------- */

		ConstrainedIntervaledPeriodSet cips = task.getScheduleConstraint();
				
		// Visualising the schedule
		this.updateTimeline(task.getName(), cips);
		
		// Editing the schedule
		this.setScheduleConstraint(cips);

		/* Verification
		 * -------------------- */
		
		this.setVerification(task.getVerification());
	}

	/**
	 * Validate fields and visually mark invalid fields.
	 * 
	 * @return True if all fields are valid, false otherwise.
	 */
	@Override
	public boolean validateFields() {
		boolean valid = true;
		
		// Name
		String name = txtName.getText();
		if (name.isEmpty()) valid = false;
		
		// Notes - no validation
		// Standard Priority - combo box does validation
		// Allocation Constraint - combo box does validation
		
		// setRefStart - date/time picker does validation
		// setRefEnd - date/time picker does validation
		// setInterval - duration field does validation
		// setCRefStart - date/time picker does validation
		// setCRefEnd - date/time picker does validation
		// setCInterval - duration field does validation

		if (!verificationEditor.validateFields()) valid = false;
		
		return valid;
	}
	
	/**
	 * Update the given task with the values currently in the editor's inputs.
	 * 
	 * @param task The task to update.
	 */
	@Override
	public void updateObject(Task task) {
		// Basic fields
		task.setName(txtName.getText());
		task.setNotes(txtNotes.getText());
		task.setStandardPriority((TaskPriority) cmbPriority.getSelectedItem());
		task.setAllocationConstraint(ncmbAllocationConstraint.getSelection());
		
		// Schedule constraint fields
		// Constructing a new ConstrainedIntervaledPeriodSet isn't that
		// problematic memory-wise, and is less complicated than checking to see
		// if it's changed.
		task.setScheduleConstraint(this.getScheduleConstraint());
		
		// Verification
		task.setVerification(omgVerification.getObjectManager().getObject());
	}

	/* Utilities used in multiple places
	 * -------------------------------------------------- */
	
	private void setScheduleConstraint(ConstrainedIntervaledPeriodSet cips) {
		IntervaledPeriodSet set = cips.periodSet();
		dtpSetRefStart.setDateTime(set.referencePeriod().start());
		
		LocalDateTime setRefEnd = set.referencePeriod().end();
		if (setRefEnd == null) {
			setSetRefEndEnabled(false, true);
		} else {
			setSetRefEndEnabled(true, true);
			dtpSetRefEnd.setDateTime(setRefEnd);
		}
		
		Duration setInterval = set.interval();
		if (setInterval == null) {
			setSetIntervalEnabled(false, true);
		} else {
			setSetIntervalEnabled(true, true);
			durSetInterval.setHour((int) setInterval.getSeconds() / 3600);
			durSetInterval.setMinute((int) setInterval.getSeconds() % 3600 / 60);
		}

		IntervaledPeriodSet cSet = cips.periodSetConstraint();
		if (cSet == null) {
			setCSetEnabled(false, true);
		} else {
			setCSetEnabled(true, true);
			dtpCSetRefStart.setDateTime(cSet.referencePeriod().start());
			
			LocalDateTime cSetRefEnd = cSet.referencePeriod().end();
			if (cSetRefEnd == null) {
				setCSetRefEndEnabled(false, true);
			} else {
				setCSetRefEndEnabled(true, true);
				dtpCSetRefEnd.setDateTime(cSetRefEnd);
			}
			
			Duration cSetInterval = cSet.interval();
			if (cSetInterval == null) {
				setCSetIntervalEnabled(false, true);
			} else {
				setCSetIntervalEnabled(true, true);
				durCSetInterval.setHour((int) cSetInterval.getSeconds() / 3600);
				durCSetInterval.setMinute((int) cSetInterval.getSeconds() % 3600 / 60);
			}
		}
	}
	
	private ConstrainedIntervaledPeriodSet getScheduleConstraint() {
		// Period set
		LocalDateTime setRefStart = dtpSetRefStart.getDateTime();
		LocalDateTime setRefEnd = null;
		if (chkSetRefEnd.isSelected()) {
			setRefEnd = dtpSetRefEnd.getDateTime();
		}
		Duration setInterval = null;
		if (chkSetInterval.isSelected()) {
			setInterval = Duration.ofSeconds(durSetInterval.getDuration());
		}

		// Create the set
		IntervaledPeriodSet set = new IntervaledPeriodSet(
			new Period(setRefStart, setRefEnd), setInterval
		);

		// Constraint period set
		IntervaledPeriodSet cSet = null;
		if (chkCSet.isSelected()) {
			LocalDateTime cSetRefStart = dtpCSetRefStart.getDateTime();
			LocalDateTime cSetRefEnd = null;
			if (chkCSetRefEnd.isSelected()) {
				cSetRefEnd = dtpCSetRefEnd.getDateTime();
			}
			Duration cSetInterval = null;
			if (chkCSetInterval.isSelected()) {
				cSetInterval = Duration.ofSeconds(durCSetInterval.getDuration());
			}

			// Create the constraint set (if needed)
			cSet = new IntervaledPeriodSet(
				new Period(cSetRefStart, cSetRefEnd), cSetInterval
			);
		}
		
		return new ConstrainedIntervaledPeriodSet(set, cSet);
	}
}
