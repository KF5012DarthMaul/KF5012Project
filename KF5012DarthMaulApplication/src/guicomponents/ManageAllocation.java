package guicomponents;

import domain.TaskPriority;
import domain.Task;
import domain.TaskExecution;
import domain.Verification;
import domain.VerificationExecution;

import temporal.ConstrainedIntervaledPeriodSet;
import temporal.Event;
import temporal.GenerativeTemporalMap;
import temporal.IntervaledPeriodSet;
import temporal.Period;
import temporal.TemporalMap;
import temporal.Timeline;

import guicomponents.formatters.TaskExecutionFormatter;
import guicomponents.ome.LocalDateTimeEditor;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("serial")
public class ManageAllocation extends JPanel {
	private static final DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofPattern("h:mma d/M/yyyy");

	private static final TaskExecutionFormatter TASK_EXEC_FORMATTER = new TaskExecutionFormatter();

	private LocalDateTimeEditor ldteEndTime;
	private JButton btnConfirm;
	private JList<Object> previewList;
	
	/**
	 * Create the panel.
	 */
	public ManageAllocation() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		JPanel generationPanel = new JPanel();
		tabbedPane.addTab("Task Generation", null, generationPanel, null);
		GridBagLayout gbl_generationPanel = new GridBagLayout();
		gbl_generationPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_generationPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_generationPanel.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_generationPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		generationPanel.setLayout(gbl_generationPanel);
		
		JLabel lblNewLabel = new JLabel("Generate from now until:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(5, 5, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		generationPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		ldteEndTime = new LocalDateTimeEditor();
		GridBagConstraints gbc_ldteEndTime = new GridBagConstraints();
		gbc_ldteEndTime.anchor = GridBagConstraints.WEST;
		gbc_ldteEndTime.insets = new Insets(5, 5, 5, 5);
		gbc_ldteEndTime.gridx = 1;
		gbc_ldteEndTime.gridy = 0;
		generationPanel.add(ldteEndTime, gbc_ldteEndTime);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 2;
		gbc_panel.gridy = 0;
		generationPanel.add(panel, gbc_panel);
		
		JButton btnGenerate = new JButton("Preview");
		btnGenerate.addActionListener((e) -> this.preview());
		panel.add(btnGenerate);
		
		btnConfirm = new JButton("Confirm and Add");
		btnConfirm.addActionListener((e) -> this.confirm());
		btnConfirm.setEnabled(false);
		panel.add(btnConfirm);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(5, 5, 5, 0);
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.gridwidth = 3;
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 1;
		generationPanel.add(separator, gbc_separator);
		
		previewList = new JList<>(new DefaultListModel<>());
		previewList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(
					JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus
			) {
				// Set stuff related to isSelected and cellHasFocus
				super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
				
				setText(TASK_EXEC_FORMATTER.apply((TaskExecution) value));
				return this;
			}
		});
		GridBagConstraints gbc_previewList = new GridBagConstraints();
		gbc_previewList.gridwidth = 3;
		gbc_previewList.insets = new Insets(5, 5, 5, 0);
		gbc_previewList.fill = GridBagConstraints.BOTH;
		gbc_previewList.gridx = 0;
		gbc_previewList.gridy = 2;
		generationPanel.add(previewList, gbc_previewList);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 3;
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		generationPanel.add(panel_1, gbc_panel_1);
		
		JButton btnRemove = new JButton("Remove Selected Task Instances");
		btnRemove.addActionListener((e) -> this.removeTaskExec());
		panel_1.add(btnRemove);
		
		JPanel allocationPanel = new JPanel();
		tabbedPane.addTab("Task Allocation", null, allocationPanel, null);
	}

	private void preview() {
		// Get all tasks (maybe filtered on the DB end?)
		List<Task> allTasks = testTasks;
		
		// create generative temporal maps for all tasks and verifications
		List<TemporalMap<Integer, TaskExecution>> taskMaps = new ArrayList<>();
		
		for (Task task : allTasks) {
			ConstrainedIntervaledPeriodSet cips = task.getScheduleConstraint();
			
			taskMaps.add(new GenerativeTemporalMap<>(
				new ArrayList<>(), // <all task executions for that task>,
				cips,
				(p) -> {
					TaskExecution taskExec = new TaskExecution(
						null, task, "", TaskPriority.NORMAL, p, null, null, null
					);

					Verification ver = task.getVerification();
					if (ver != null) {
						// Don't auto-allocate to the allocation constraint
						// because there are other constraints on allocation
						// (eg. time).
						VerificationExecution verExec = new VerificationExecution(
							null, ver, taskExec, "", ver.getStandardDeadline(), null, null
						);
						taskExec.setVerification(verExec);
					}
					
					return taskExec;
				}
			));
		}
		
		// Generate the task/verification executions
		Timeline<Integer, TaskExecution> tasksTimeline = new Timeline<>(taskMaps);
		List<TaskExecution> genTaskExecs = tasksTimeline.getBetween(
			LocalDateTime.now(), ldteEndTime.getObject(), Event.byStartTime, true, true
		);
		
		// Update the model
		DefaultListModel<Object> model = (DefaultListModel<Object>) (previewList.getModel());
		model.removeAllElements();
		model.addAll(genTaskExecs);
		
		// Finally, enable the 'confirm' button
		btnConfirm.setEnabled(true);
	}

	private void removeTaskExec() {
		DefaultListModel<Object> model = (DefaultListModel<Object>) previewList.getModel();
		int[] selectedIndexes = previewList.getSelectedIndices();
		for (int i = selectedIndexes.length - 1; i >= 0; i--) {
			model.remove(selectedIndexes[i]);
		}
	}
	
	private void confirm() {
		DefaultListModel<Object> model = (DefaultListModel<Object>) previewList.getModel();
		
		// Get List from JList
		List<TaskExecution> genTaskExecs = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			genTaskExecs.add((TaskExecution) model.get(i));
		}
		
		// Add list to DB
//		try {
//			//
//		} catch (WhateverException e) {
//			new ExceptionDialog("Failed to add generated tasks", e);
//			return;
//		}
		
		// Remove graphically
		model.removeAllElements();
	}

	/* TEST
	 * ---------- */
	
	private static final List<Task> testTasks;
	static {
		//////////////////////////////////////////////////
		testTasks = new ArrayList<>();
		List<TaskExecution> allTaskExecs = new ArrayList<>();
		
		Task t1 = new Task(
			null,
			"Check toilets", "",
			null, null, null,
			TaskPriority.NORMAL,
			new ConstrainedIntervaledPeriodSet(
				new IntervaledPeriodSet(
					new Period(dt("9:45am 9/5/2021"), dt("10:00am 9/5/2021")),
					Duration.ofHours(2)
				),
				new IntervaledPeriodSet(
					new Period(dt("9:00am 9/5/2021"), dt("5:00pm 9/5/2021")),
					Duration.ofDays(1)
				)
			),
			null, null
		);
		testTasks.add(t1);
		
		// Some on the 9th
		allTaskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("9:45am 9/5/2021"), dt("10:00am 9/5/2021")),
			null, null, null
		));
		allTaskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("11:45am 9/5/2021"), dt("12:00pm 9/5/2021")),
			null, null, null
		));
		allTaskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("1:45pm 9/5/2021"), dt("2:00pm 9/5/2021")),
			null, null, null
		));

		// Some on the 10th
		allTaskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("9:45am 10/5/2021"), dt("10:00am 10/5/2021")),
			null, null, null
		));
		allTaskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("11:45am 10/5/2021"), dt("12:00pm 10/5/2021")),
			null, null, null
		));
		allTaskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("1:45pm 10/5/2021"), dt("2:00pm 10/5/2021")),
			null, null, null
		));

		// A low-priority one-off task without a deadline
		Task t2 = new Task(
			null,
			"Fix Window on bike shed", "",
			null, null, null,
			TaskPriority.LOW,
			new ConstrainedIntervaledPeriodSet(
				new IntervaledPeriodSet(
					new Period(dt("9:45am 9/5/2021"), (Duration) null), null
				),
				null
			),
			null, null
		);
		testTasks.add(t2);
		
		allTaskExecs.add(new TaskExecution(
			null, t2, "", TaskPriority.LOW,
			new Period(dt("1:00pm 9/5/2021"), dt("3:00pm 9/5/2021")),
			null, null, null
		));

		// A high-priority one-off task with deadline and verification.
		User myUser = new User("myuser", PermissionManager.AccountType.CARETAKER);
		
		Verification verification = new Verification(null, "", TaskPriority.HIGH, Duration.ofHours(3), null);
		Task t3 = new Task(
			null,
			"Fix Broken Pipe",
			"The waste pipe outside of the toilets on the 3rd floor of Big Building is broken and leaking. Health hazard - fix ASAP.",
			null, null, null,
			TaskPriority.HIGH,
			new ConstrainedIntervaledPeriodSet(
				new IntervaledPeriodSet(
					new Period(dt("1:32pm 10/5/2021"), dt("5:00pm 10/5/2021")), null
				),
				null
			),
			null,
			verification
		);
		testTasks.add(t3);
		
		// The task execution has been allocated to myUser
		TaskExecution t3Exec = new TaskExecution(
			null, t3, "", TaskPriority.HIGH,
			new Period(dt("3:30pm 9/5/2021"), dt("4:15pm 9/5/2021")),
			myUser,
			null, null
		);
		
		// The verification execution
		VerificationExecution t3VerExec = new VerificationExecution(
			null, verification, t3Exec, "", Duration.ofHours(3), null, null
		);
		t3Exec.setVerification(t3VerExec);

		// Add both
		allTaskExecs.add(t3Exec);
	}

	private static LocalDateTime dt(String dateTimeString) {
		return LocalDateTime.parse(dateTimeString, dateTimeFormatter);
	}
}
