package guicomponents;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import domain.Completion;
import domain.Task;
import domain.TaskExecution;
import domain.TaskPriority;
import domain.VerificationExecution;
import exceptions.TaskManagerExceptions;
import guicomponents.utils.DateTimePicker;
import kf5012darthmaulapplication.ExceptionDialog;
import temporal.Event;
import temporal.Period;
import temporal.TemporalList;

@SuppressWarnings("serial")
public class ViewTasks extends JPanel {
	private static final DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofPattern("h:mma d/M/yyyy");

	private DateTimePicker startDateTimePicker;
	private DateTimePicker endDateTimePicker;

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
		// The 0.15 weight is a hack - there's a bug/miscommunication somewhere
		// between GridBagLayout and FlowLayout/WrapLayout, but I don't have
		// time to properly diagnose it.
		gbl_viewTasksPanel.rowWeights = new double[]{0.15, 0.0, 1.0, Double.MIN_VALUE};
		this.setLayout(gbl_viewTasksPanel);

		// Date Range
		JPanel rangePanel = new JPanel();
		GridBagConstraints gbc_rangePanel = new GridBagConstraints();
		gbc_rangePanel.fill = GridBagConstraints.BOTH;
		gbc_rangePanel.insets = new Insets(0, 0, 5, 0);
		gbc_rangePanel.gridx = 0;
		gbc_rangePanel.gridy = 0;
		this.add(rangePanel, gbc_rangePanel);
		rangePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		this.startDateTimePicker = new DateTimePicker(
			LocalDateTime.now().truncatedTo(ChronoUnit.DAYS), // Start of today
			"Start Date/Time"
		);
		rangePanel.add(startDateTimePicker);
		
		this.endDateTimePicker = new DateTimePicker(
			LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1), // End of today
			"End Date/Time"
		);
		rangePanel.add(endDateTimePicker);
		
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
			// Based on https://www.logicbig.com/tutorials/java-swing/jtree-renderer.html
			private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";
			
			@Override
			public String convertValueToText(
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus
			) {
				Object item = ((DefaultMutableTreeNode) value).getUserObject();
				if (item == null) {
					return "";
				}
				
				String text;
				if (item instanceof Task) {
					Task task = (Task) item;
					
					String name = task.getName();
					String priority = task.getStandardPriority().toString();
					
					text = name+" (standard priority: "+priority+")";
					
				} else if (item instanceof TaskExecution) {
					// Get Objects
					TaskExecution taskExec = (TaskExecution) item;
					Period period = taskExec.getPeriod();
					Duration duration = period.duration();
					Completion completion = taskExec.getCompletion();
					VerificationExecution verification = taskExec.getVerification();

					// Times
					String start = period.start().format(dateTimeFormatter);
					String durationStr = "continuous";
					if (duration != null) {
						durationStr = formatDuration(duration);
					}
					
					// Priority
					String priority = taskExec.getPriority().toString();
					
					// Completion / Overdue
					String completionStatus = formatCompletionStatus(
						period, completion);

					// Verification
					String verificationStr = "";
					if (verification != null) {
						Period verPeriod = taskExec.getPeriod();
						Duration verDuration = verPeriod.duration();
						Completion verCompletion = taskExec.getCompletion();

						// Times
						String verDurationStr = formatDuration(verDuration);
						
						// Completion / Overdue
						String verCompletionStatus = formatCompletionStatus(
							verPeriod, verCompletion);
						
						// Glue together
						verificationStr = String.format(
							" [verification (%s) - %s]",
							verDurationStr, verCompletionStatus
						);
					}
					
					// Glue together
					text = String.format(
						"<html>%s (%s | priority: %s) - %s%s</html>",
						start, durationStr, priority, completionStatus,
						verificationStr
					);
					
				} else {
					throw new TaskManagerExceptions.InvalidTaskTypeException();
				}
				
				return text;
			}

			private String formatCompletionStatus(
					Period period, Completion completion
			) {
				LocalDateTime now = LocalDateTime.now();
				
				if (completion == null) {
					if (now.isAfter(period.end())) {
						return String.format(SPAN_FORMAT, "red", "overdue!");
					} else if (now.isAfter(period.start())) {
						return String.format(SPAN_FORMAT, "blue", "in progress");
					} else {
						return "upcoming";
					}
				} else {
					return String.format(SPAN_FORMAT, "green", "done ✔️");
				}
			}
			
			// Based on https://stackoverflow.com/a/266970
			private String formatDuration(Duration duration) {
				long totalM = duration.toMinutes();
				long h = (totalM / 60);
				long m = (totalM % 60);
				
				if (h == 0) {
					return String.format("%02dm", m);
				} else {
					return String.format("%dh, %02dm", h, m);
				}
			}
		};
		this.taskJTree.setRootVisible(false);
		taskTreeScrollPane.setViewportView(this.taskJTree);
	}

	public void refresh() {
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
		
		Task t1 = new Task();
		Task t2 = new Task();
		Task t3 = new Task();
		this.allTaskExecs = new ArrayList<>();
		this.allTaskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("8:30am 9/5/2021"), dt("11:00am 9/5/2021")),
			null, null, null
		));
		this.allTaskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("11:30am 9/5/2021"), dt("12:00pm 9/5/2021")),
			null, null, null
		));
		this.allTaskExecs.add(new TaskExecution(
			null, t1, "", TaskPriority.NORMAL,
			new Period(dt("12:30pm 9/5/2021"), dt("1:00pm 9/5/2021")),
			null, null, null
		));
		this.allTaskExecs.add(new TaskExecution(
			null, t2, "", TaskPriority.LOW,
			new Period(dt("1:00pm 9/5/2021"), dt("3:00pm 9/5/2021")),
			null, null, null
		));
		this.allTaskExecs.add(new TaskExecution(
			null, t3, "Important for some reason", TaskPriority.HIGH,
			new Period(dt("3:30pm 9/5/2021"), dt("4:15pm 9/5/2021")),
			null, null, null
		));
		
		// Filter list to relevant task executions
		LocalDateTime start = this.startDateTimePicker.getDateTime();
		LocalDateTime end = this.endDateTimePicker.getDateTime();
		
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
