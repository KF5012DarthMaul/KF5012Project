package guicomponents;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import domain.Task;
import domain.TaskExecution;
import domain.TaskPriority;
import domain.Verification;
import domain.VerificationExecution;
import exceptions.TaskManagerExceptions;
import guicomponents.formatters.TaskExecutionFormatter;
import guicomponents.formatters.TaskFormatter;
import guicomponents.utils.DateRangePicker;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.Event;
import temporal.IntervaledPeriodSet;
import temporal.Period;
import temporal.TemporalList;

@SuppressWarnings("serial")
public class ViewTasks extends JPanel {
	private static final DateTimeFormatter dateTimeFormatter =
		DateTimeFormatter.ofPattern("h:mma d/M/yyyy");

	private static final TaskFormatter TASK_FORMATTER = new TaskFormatter();
	private static final TaskExecutionFormatter TASK_EXEC_FORMATTER = new TaskExecutionFormatter();

	private DateRangePicker dateRangePicker;

	private List<TaskExecution> allTaskExecs;
	private Map<Task, List<TaskExecution>> taskTree;
	private DefaultMutableTreeNode taskJTreeRoot;
	private DefaultTreeModel taskJTreeModel;
	private JTree taskJTree;

	/**
	 * Create the panel.
	 */
	public ViewTasks() {
		GridBagLayout gbl_viewTasksPanel = new GridBagLayout();
		gbl_viewTasksPanel.columnWidths = new int[]{0, 0};
		gbl_viewTasksPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_viewTasksPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_viewTasksPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		this.setLayout(gbl_viewTasksPanel);

		// Date Range
		dateRangePicker = new DateRangePicker();
		dateRangePicker.addChangeListener((e) -> this.refresh());
		GridBagConstraints gbc_dateRangePicker = new GridBagConstraints();
		gbc_dateRangePicker.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateRangePicker.insets = new Insets(0, 0, 5, 0);
		gbc_dateRangePicker.gridx = 0;
		gbc_dateRangePicker.gridy = 0;
		this.add(dateRangePicker, gbc_dateRangePicker);
		
		JPanel instructionsPanel = new JPanel();
		GridBagConstraints gbc_instructionsPanel = new GridBagConstraints();
		gbc_instructionsPanel.fill = GridBagConstraints.BOTH;
		gbc_instructionsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_instructionsPanel.gridx = 0;
		gbc_instructionsPanel.gridy = 1;
		this.add(instructionsPanel, gbc_instructionsPanel);
		instructionsPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("<html>Double-click on a task to view its instances in the selected range. Select a task or instance and click 'Edit Task' to view/edit details.<html>");
		instructionsPanel.add(lblNewLabel);
		
		// Tree
		JScrollPane taskTreeScrollPane = new JScrollPane();
		GridBagConstraints gbc_taskTreeScrollPane = new GridBagConstraints();
		gbc_taskTreeScrollPane.fill = GridBagConstraints.BOTH;
		gbc_taskTreeScrollPane.gridx = 0;
		gbc_taskTreeScrollPane.gridy = 2;
		this.add(taskTreeScrollPane, gbc_taskTreeScrollPane);

		this.taskJTreeRoot = new DefaultMutableTreeNode();
		this.taskJTreeModel = new DefaultTreeModel(this.taskJTreeRoot);
		this.taskJTree = new JTree(this.taskJTreeModel) {
			@Override
			public String convertValueToText(
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus
			) {
				Object item = ((DefaultMutableTreeNode) value).getUserObject();
				if (item == null) {
					return "";
					
				} else if (item instanceof Task) {
					return TASK_FORMATTER.apply((Task) item);
					
				} else if (item instanceof TaskExecution) {
					return TASK_EXEC_FORMATTER.apply((TaskExecution) item);
					
				} else {
					throw new TaskManagerExceptions.InvalidTaskTypeException();
				}
			}
		};
		this.taskJTree.setRootVisible(false);
		taskTreeScrollPane.setViewportView(this.taskJTree);
	}
	
	public void reload() {
		// Try to connect to the DB each time you refresh - if one fails, you
		// can try again.
		DBAbstraction db;
		try {
			db = DBAbstraction.getInstance();
		} catch (FailedToConnectException e) {
			new ExceptionDialog(
				"Could not connect to database. Click 'Refresh' to retry loading tasks.", e);
			return;
		}

//		db.getTaskList()
//		
//		db.getUnallocatedDailyPriorityTaskList()
//		db.getUnallocatedOverflowTaskList()
//		db.getUnallocatedTaskList(Period period)
//		db.getUserDailyTaskList(String username)
//
//		db.submitTask(Task task)
//		db.submitTaskExecution(TaskExecution task)
//		db.submitTaskExecutions(List<TaskExecution> tasks)
//		
//		db.submitVerificationExecution(VerificationExecution verification)
//		db.submitVerificationExecutions(List<VerificationExecution> vers)

		// Get tasks and task executions
		//this.allTasks = db.getTaskList();
		//this.allTaskExecs = db.getTaskExecutionList();

		//////////////////////////////////////////////////
		this.allTaskExecs = new ArrayList<>();
		
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
		//////////////////////////////////////////////////
		
		this.refresh();
	}

	public void refresh() {
		// Filter list to relevant task executions
		LocalDateTime start = this.dateRangePicker.getStartDateTime();
		LocalDateTime end = this.dateRangePicker.getEndDateTime();
		
		TemporalList<TaskExecution> taskExecTemporal = new TemporalList<>(this.allTaskExecs);
		List<TaskExecution> execsInRange = taskExecTemporal.getBetween(
			start, end, Event.byPeriodDefaultInf, true, true
		);
		
		// Map back to tasks
		this.taskTree = new HashMap<>();
		for (TaskExecution task : execsInRange) {
			Task owningTask = task.getTask();
			
			if (!taskTree.containsKey(owningTask)) {
				taskTree.put(owningTask, new ArrayList<TaskExecution>());
			}
			
			taskTree.get(owningTask).add(task);
		}
		
		// Construct/Reconstruct the tree
		this.taskJTreeRoot.removeAllChildren();
		for (Map.Entry<Task, List<TaskExecution>> entry : this.taskTree.entrySet()) {
			DefaultMutableTreeNode taskNode = new DefaultMutableTreeNode(entry.getKey());
			for (TaskExecution taskExec : entry.getValue()) {
				taskNode.add(new DefaultMutableTreeNode(taskExec));
			}
			this.taskJTreeRoot.add(taskNode);
		}
		this.taskJTreeModel.nodeStructureChanged(this.taskJTreeRoot);
	}

	public Object getSelectedObject() {
		TreePath path = this.taskJTree.getSelectionPath();
		if (path == null) {
			return null;
		} else {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			return node.getUserObject();
		}
	}

	/* Utils
	 * -------------------------------------------------- */

	private LocalDateTime dt(String dateTimeString) {
		return LocalDateTime.parse(dateTimeString, dateTimeFormatter);
	}
}
