package guicomponents.ome;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import guicomponents.utils.BoundedTimelinePanel;
import guicomponents.utils.DateRangePicker;
import guicomponents.utils.ObjectEditor;
import guicomponents.utils.ObjectManager;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.LocalDateTime;

@SuppressWarnings("serial")
public class TaskEditor extends JScrollPane implements ObjectEditor<Task> {
	private Task active;
	
	// Basic fields
	private TextEditor txteName;
	private LongTextEditor txteNotes;
	private ListSelectionEditor<TaskPriority> lstePriority;
	private ListSelectionEditor<User> lsteAllocationConstraint;
	
	// Timeline
	private List<ChartableEvent> currentTimelineHistory;
	private GenerativeTemporalMap<ChartableEvent> currentTimelineMap;
	private TimelinePanel timelinePanel;
	private DateRangePicker dateRangePicker;
	
	// Schedule fields (nested)
	// not ipseSet because IntervaledPeriodSetEditor is not a JComponent
	private IntervaledPeriodSetEditor setEditor;
	private ObjectManager<IntervaledPeriodSet> cSetManager;

	// Verification editor
	private VerificationEditor edtVerification;
	private DomainObjectManager<Verification> omgVerification;
	
	// Loading of users for various components
	private boolean usersLoaded = false;

	/**
	 * Set up the Edit Task panel.
	 */
	public TaskEditor() {
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
		
		txteName = new TextEditor((s) -> !s.isEmpty()); // Must be non-empty
		GridBagConstraints gbc_txteName = new GridBagConstraints();
		gbc_txteName.anchor = GridBagConstraints.WEST;
		gbc_txteName.insets = new Insets(5, 5, 5, 0);
		gbc_txteName.gridwidth = 2;
		gbc_txteName.gridx = 1;
		gbc_txteName.gridy = 0;
		formPanel.add(txteName, gbc_txteName);
		txteName.setColumns(40);
		
		JLabel lblNotes = new JLabel("Notes");
		GridBagConstraints gbc_lblNotes = new GridBagConstraints();
		gbc_lblNotes.anchor = GridBagConstraints.EAST;
		gbc_lblNotes.insets = new Insets(0, 5, 5, 5);
		gbc_lblNotes.gridx = 0;
		gbc_lblNotes.gridy = 1;
		formPanel.add(lblNotes, gbc_lblNotes);
		
		txteNotes = new LongTextEditor();
		txteNotes.setLineWrap(true);
		txteNotes.setWrapStyleWord(true);
		GridBagConstraints gbc_txteNotes = new GridBagConstraints();
		gbc_txteNotes.gridwidth = 2;
		gbc_txteNotes.anchor = GridBagConstraints.WEST;
		gbc_txteNotes.insets = new Insets(0, 5, 5, 0);
		gbc_txteNotes.gridx = 1;
		gbc_txteNotes.gridy = 1;
		formPanel.add(txteNotes, gbc_txteNotes);
		txteNotes.setColumns(40);
		txteNotes.setRows(6);
		
		JLabel lblPriority = new JLabel("Priority");
		GridBagConstraints gbc_lblPriority = new GridBagConstraints();
		gbc_lblPriority.anchor = GridBagConstraints.EAST;
		gbc_lblPriority.insets = new Insets(0, 5, 5, 5);
		gbc_lblPriority.gridx = 0;
		gbc_lblPriority.gridy = 2;
		formPanel.add(lblPriority, gbc_lblPriority);
		
		lstePriority = new ListSelectionEditor<TaskPriority>(
			(taskPriority) -> taskPriority.toString()
		);
		lstePriority.populate(Arrays.asList(TaskPriority.values()));
		GridBagConstraints gbc_lstePriority = new GridBagConstraints();
		gbc_lstePriority.anchor = GridBagConstraints.WEST;
		gbc_lstePriority.insets = new Insets(0, 5, 5, 0);
		gbc_lstePriority.gridwidth = 2;
		gbc_lstePriority.gridx = 1;
		gbc_lstePriority.gridy = 2;
		formPanel.add(lstePriority, gbc_lstePriority);
		
		JLabel lblAllocationConstraint = new JLabel("Allocation Constraint");
		GridBagConstraints gbc_lblAllocationConstraint = new GridBagConstraints();
		gbc_lblAllocationConstraint.insets = new Insets(0, 5, 5, 5);
		gbc_lblAllocationConstraint.gridx = 0;
		gbc_lblAllocationConstraint.gridy = 3;
		formPanel.add(lblAllocationConstraint, gbc_lblAllocationConstraint);

		lsteAllocationConstraint = new ListSelectionEditor<>(
			(user) -> {
				if (user == null) {
					return "No Allocation Constraint";
				} else {
					return user.getUsername();
				}
			}
		);
		GridBagConstraints gbc_lsteAllocationConstraint = new GridBagConstraints();
		gbc_lsteAllocationConstraint.anchor = GridBagConstraints.WEST;
		gbc_lsteAllocationConstraint.insets = new Insets(0, 5, 5, 0);
		gbc_lsteAllocationConstraint.gridwidth = 2;
		gbc_lsteAllocationConstraint.gridx = 1;
		gbc_lsteAllocationConstraint.gridy = 3;
		formPanel.add(lsteAllocationConstraint, gbc_lsteAllocationConstraint);
		
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
			this.updateTimeline(txteName.getObject(), getScheduleConstraint());
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
		
		LocalDateTimeEditor ldteSetRefStart = new LocalDateTimeEditor();
		GridBagConstraints gbc_ldteSetRefStart = new GridBagConstraints();
		gbc_ldteSetRefStart.insets = new Insets(0, 5, 5, 0);
		gbc_ldteSetRefStart.anchor = GridBagConstraints.WEST;
		gbc_ldteSetRefStart.gridx = 2;
		gbc_ldteSetRefStart.gridy = 8;
		formPanel.add(ldteSetRefStart, gbc_ldteSetRefStart);
		
		// Set ref end
		
		JLabel lblSetRefEnd = new JLabel("Latest End Time");
		GridBagConstraints gbc_lblSetRefEnd = new GridBagConstraints();
		gbc_lblSetRefEnd.anchor = GridBagConstraints.EAST;
		gbc_lblSetRefEnd.insets = new Insets(0, 5, 5, 5);
		gbc_lblSetRefEnd.gridx = 0;
		gbc_lblSetRefEnd.gridy = 9;
		formPanel.add(lblSetRefEnd, gbc_lblSetRefEnd);
		
		JCheckBox chkSetRefEnd = new JCheckBox("");
		GridBagConstraints gbc_chkSetRefEnd = new GridBagConstraints();
		gbc_chkSetRefEnd.insets = new Insets(0, 0, 5, 5);
		gbc_chkSetRefEnd.gridx = 1;
		gbc_chkSetRefEnd.gridy = 9;
		formPanel.add(chkSetRefEnd, gbc_chkSetRefEnd);

		LocalDateTimeEditor ldteSetRefEnd = new LocalDateTimeEditor();
		GridBagConstraints gbc_ldteSetRefEnd = new GridBagConstraints();
		gbc_ldteSetRefEnd.insets = new Insets(0, 5, 5, 0);
		gbc_ldteSetRefEnd.anchor = GridBagConstraints.WEST;
		gbc_ldteSetRefEnd.gridx = 2;
		gbc_ldteSetRefEnd.gridy = 9;
		formPanel.add(ldteSetRefEnd, gbc_ldteSetRefEnd);
		
		// Set interval
		
		JLabel lblSetInterval = new JLabel("Regularity");
		GridBagConstraints gbc_lblSetInterval = new GridBagConstraints();
		gbc_lblSetInterval.anchor = GridBagConstraints.EAST;
		gbc_lblSetInterval.insets = new Insets(0, 5, 5, 5);
		gbc_lblSetInterval.gridx = 0;
		gbc_lblSetInterval.gridy = 10;
		formPanel.add(lblSetInterval, gbc_lblSetInterval);

		JCheckBox chkSetInterval = new JCheckBox("");
		GridBagConstraints gbc_chkSetInterval = new GridBagConstraints();
		gbc_chkSetInterval.insets = new Insets(0, 0, 5, 5);
		gbc_chkSetInterval.gridx = 1;
		gbc_chkSetInterval.gridy = 10;
		formPanel.add(chkSetInterval, gbc_chkSetInterval);

		DurationEditor dureSetInterval = new DurationEditor();
		GridBagConstraints gbc_spnSetInterval = new GridBagConstraints();
		gbc_spnSetInterval.anchor = GridBagConstraints.WEST;
		gbc_spnSetInterval.insets = new Insets(0, 5, 5, 0);
		gbc_spnSetInterval.gridx = 2;
		gbc_spnSetInterval.gridy = 10;
		formPanel.add(dureSetInterval, gbc_spnSetInterval);

		// Create managers for set
		
		ObjectManager<LocalDateTime> setRefEndManager = new ObjectManager<>(
			chkSetRefEnd, ldteSetRefEnd, () -> ldteSetRefEnd.getObject()
		);
		
		ObjectManager<Duration> setIntervalManager = new ObjectManager<>(
			chkSetInterval, dureSetInterval, () -> dureSetInterval.getObject()
		);
		
		setEditor = new IntervaledPeriodSetEditor(
			ldteSetRefStart, setRefEndManager, setIntervalManager,
			ldteSetRefEnd // Needed to set validator
		);
		
		// CSet ref start
		
		JLabel lblCSetRefStart = new JLabel("Constraint Start");
		GridBagConstraints gbc_lblCSetRefStart = new GridBagConstraints();
		gbc_lblCSetRefStart.anchor = GridBagConstraints.EAST;
		gbc_lblCSetRefStart.insets = new Insets(0, 5, 5, 5);
		gbc_lblCSetRefStart.gridx = 0;
		gbc_lblCSetRefStart.gridy = 11;
		formPanel.add(lblCSetRefStart, gbc_lblCSetRefStart);
		
		JCheckBox chkCSet = new JCheckBox("");
		GridBagConstraints gbc_chkCSet = new GridBagConstraints();
		gbc_chkCSet.insets = new Insets(0, 0, 5, 5);
		gbc_chkCSet.gridx = 1;
		gbc_chkCSet.gridy = 11;
		formPanel.add(chkCSet, gbc_chkCSet);
		
		LocalDateTimeEditor ldteCSetRefStart = new LocalDateTimeEditor((ldt) -> true);
		GridBagConstraints gbc_ldteCSetRefStart = new GridBagConstraints();
		gbc_ldteCSetRefStart.anchor = GridBagConstraints.WEST;
		gbc_ldteCSetRefStart.insets = new Insets(0, 5, 5, 0);
		gbc_ldteCSetRefStart.gridx = 2;
		gbc_ldteCSetRefStart.gridy = 11;
		formPanel.add(ldteCSetRefStart, gbc_ldteCSetRefStart);
		
		// CSet ref end
		
		JLabel lblCSetRefEnd = new JLabel("Constraint End");
		GridBagConstraints gbc_lblCSetRefEnd = new GridBagConstraints();
		gbc_lblCSetRefEnd.anchor = GridBagConstraints.EAST;
		gbc_lblCSetRefEnd.insets = new Insets(0, 5, 5, 5);
		gbc_lblCSetRefEnd.gridx = 0;
		gbc_lblCSetRefEnd.gridy = 12;
		formPanel.add(lblCSetRefEnd, gbc_lblCSetRefEnd);
		
		JCheckBox chkCSetRefEnd = new JCheckBox("");
		GridBagConstraints gbc_chkCSetRefEnd = new GridBagConstraints();
		gbc_chkCSetRefEnd.insets = new Insets(0, 0, 5, 5);
		gbc_chkCSetRefEnd.gridx = 1;
		gbc_chkCSetRefEnd.gridy = 12;
		formPanel.add(chkCSetRefEnd, gbc_chkCSetRefEnd);

		LocalDateTimeEditor ldteCSetRefEnd = new LocalDateTimeEditor();
		GridBagConstraints gbc_ldteCSetRefEnd = new GridBagConstraints();
		gbc_ldteCSetRefEnd.insets = new Insets(0, 5, 5, 0);
		gbc_ldteCSetRefEnd.anchor = GridBagConstraints.WEST;
		gbc_ldteCSetRefEnd.gridx = 2;
		gbc_ldteCSetRefEnd.gridy = 12;
		formPanel.add(ldteCSetRefEnd, gbc_ldteCSetRefEnd);
		
		// CSet interval
		
		JLabel lblCSetInterval = new JLabel("Constraint Interval");
		GridBagConstraints gbc_lblCSetInterval = new GridBagConstraints();
		gbc_lblCSetInterval.anchor = GridBagConstraints.EAST;
		gbc_lblCSetInterval.insets = new Insets(0, 5, 0, 5);
		gbc_lblCSetInterval.gridx = 0;
		gbc_lblCSetInterval.gridy = 13;
		formPanel.add(lblCSetInterval, gbc_lblCSetInterval);
		
		JCheckBox chkCSetInterval = new JCheckBox("");
		GridBagConstraints gbc_chkCSetInterval = new GridBagConstraints();
		gbc_chkCSetInterval.insets = new Insets(0, 0, 0, 5);
		gbc_chkCSetInterval.gridx = 1;
		gbc_chkCSetInterval.gridy = 13;
		formPanel.add(chkCSetInterval, gbc_chkCSetInterval);
		
		DurationEditor dureCSetInterval = new DurationEditor();
		GridBagConstraints gbc_dureCSetInterval = new GridBagConstraints();
		gbc_dureCSetInterval.insets = new Insets(0, 5, 0, 0);
		gbc_dureCSetInterval.anchor = GridBagConstraints.WEST;
		gbc_dureCSetInterval.gridx = 2;
		gbc_dureCSetInterval.gridy = 13;
		formPanel.add(dureCSetInterval, gbc_dureCSetInterval);

		// Create managers for cSet
		
		ObjectManager<LocalDateTime> cSetRefEndManager = new ObjectManager<>(
			chkCSetRefEnd, ldteCSetRefEnd, () -> ldteCSetRefEnd.getObject()
		);
		
		ObjectManager<Duration> cSetIntervalManager = new ObjectManager<>(
			chkCSetInterval, dureCSetInterval, () -> dureCSetInterval.getObject()
		);
		
		IntervaledPeriodSetEditor cSetEditor = new IntervaledPeriodSetEditor(
			ldteCSetRefStart, cSetRefEndManager, cSetIntervalManager,
			ldteCSetRefEnd // Needed to set 
		);
		
		cSetManager = new ObjectManager<IntervaledPeriodSet>(
			chkCSet, cSetEditor,
			
			// On re-activation, get the object from what the editors were
			// before deactivation (ie. don't reset their values to default).
			() -> new IntervaledPeriodSet(
				new Period(
					ldteCSetRefStart.getObject(),
					cSetRefEndManager.getObject()
				),
				cSetIntervalManager.getObject()
			)
		);
		
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

		edtVerification = new VerificationEditor();
		omgVerification = new DomainObjectManager<>(
			"Requires Verification", edtVerification, () -> new Verification()
		);
		GridBagConstraints gbc_edtVerification = new GridBagConstraints();
		gbc_edtVerification.insets = new Insets(0, 5, 0, 0);
		gbc_edtVerification.anchor = GridBagConstraints.WEST;
		gbc_edtVerification.gridwidth = 3;
		gbc_edtVerification.gridx = 0;
		gbc_edtVerification.gridy = 15;
		formPanel.add(omgVerification, gbc_edtVerification);
	}

	@Override
	public List<JComponent> getEditorComponents() {
		List<JComponent> arr = new ArrayList<>();
		arr.add(this);
		return arr;
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
			List<User> caretakersAndNull = nullable(
				allUsers.stream()
				.filter(u -> u.getAccountType() == PermissionManager.AccountType.CARETAKER)
				.collect(Collectors.toList())
			);
			
			// (Re)fill the lists
			lsteAllocationConstraint.populate(caretakersAndNull);
			edtVerification.setUsers(caretakersAndNull);
			
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

	/* Task get/validate/update cycle
	 * -------------------------------------------------- */
	
	/**
	 * Mark the given task to be the current task to edit.
	 * 
	 * @param task The task to edit.
	 */
	@Override
	public void setObject(Task task) {
		active = task;

		// For easier reading
		ConstrainedIntervaledPeriodSet cips = task.getScheduleConstraint();
		
		// Set fields
		txteName.setObject(task.getName());
		txteNotes.setObject(task.getNotes());
		lstePriority.setObject(task.getStandardPriority());
		lsteAllocationConstraint.setObject(task.getAllocationConstraint());
		setEditor.setObject(cips.periodSet());
		cSetManager.setObject(cips.periodSetConstraint());
		omgVerification.getObjectManager().setObject(task.getVerification());
		
		// Update the timeline to use the new name/cips
		this.updateTimeline(task.getName(), cips);
	}

	/**
	 * Validate fields and visually mark invalid fields.
	 * 
	 * @return True if all fields are valid, false otherwise.
	 */
	@Override
	public boolean validateFields() {
		boolean valid = true;
		
		if (!txteName.validateFields()) valid = false;
		if (!txteNotes.validateFields()) valid = false;
		if (!lstePriority.validateFields()) valid = false;
		if (!lsteAllocationConstraint.validateFields()) valid = false;
		if (!setEditor.validateFields()) valid = false;
		if (!cSetManager.validateFields()) valid = false;
		if (!omgVerification.getObjectManager().validateFields()) valid = false;
		
		return valid;
	}
	
	/**
	 * Update the given task with the values currently in the editor's inputs.
	 * 
	 * @param task The task to update.
	 */
	@Override
	public Task getObject() {
		active.setName(txteName.getObject());
		active.setNotes(txteNotes.getObject());
		active.setStandardPriority(lstePriority.getObject());
		active.setAllocationConstraint(lsteAllocationConstraint.getObject());
		active.setScheduleConstraint(this.getScheduleConstraint());
		active.setVerification(omgVerification.getObjectManager().getObject());
		
		return active;
	}

	/* Utilities used in multiple places
	 * -------------------------------------------------- */
	
	private ConstrainedIntervaledPeriodSet getScheduleConstraint() {
		return new ConstrainedIntervaledPeriodSet(
			setEditor.getObject(), cSetManager.getObject()
		);
	}

	private static <T> List<T> nullable(List<T> list) {
		List<T> fullList = list;
		fullList.add(null);
		return fullList;
	}
}
