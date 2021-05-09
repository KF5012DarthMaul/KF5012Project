package guicomponents;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;

import domain.Completion;
import domain.Task;
import domain.TaskExecution;
import domain.TaskPriority;
import domain.VerificationExecution;

import temporal.Event;
import temporal.Period;
import temporal.TemporalList;

import exceptions.TaskManagerExceptions;
import guicomponents.utils.DateTimePicker;
import kf5012darthmaulapplication.ExceptionDialog;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import java.awt.BorderLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.awt.CardLayout;
import javax.swing.JSeparator;
import javax.swing.BoxLayout;

public class ManageTasks extends JPanel {
	private static final DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofPattern("h:mma d/M/yyyy");

	private JPanel buttonPanel;
	private JPanel mainPanel;
	
	private Map<String, Action> views = new HashMap<>();
	
	// Main Pane: View Tasks
	private DateTimePicker startDateTimePicker;
	private DateTimePicker endDateTimePicker;

	private List<TaskExecution> allTaskExecs;
	private Map<Task, List<TaskExecution>> taskTree;
	private DefaultMutableTreeNode taskJTreeRoot;
	private DefaultTreeModel taskJTreeModel;
	private JTree taskJTree;
	
	// Main Pane: Edit Task / Edit Task Execution
	private EditTask editTaskComponent;
	private EditTaskExec editTaskExecComponent;
	private Object active; // Nullable
	
	/**
	 * Create the panel.
	 */
	@SuppressWarnings("serial")
	public ManageTasks() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		/* Setup Views (Screens)
		 * -------------------------------------------------- */
		
		// These hold the definitive names of the panels
		
		views.put("viewTasks", new Action() {
			@Override
			public void run() {
				CardLayout layout;
				
				layout = (CardLayout) buttonPanel.getLayout();
				layout.show(buttonPanel, "viewTasksButtons");

				layout = (CardLayout) mainPanel.getLayout();
				layout.show(mainPanel, "viewTasks");
			}
		});
		
		views.put("editTask", new Action() {
			@Override
			public void run() {
				CardLayout layout;
				
				layout = (CardLayout) buttonPanel.getLayout();
				layout.show(buttonPanel, "editTaskOrExecButtons");

				editTaskComponent.loadUsers(); // In case they weren't already
				layout = (CardLayout) mainPanel.getLayout();
				layout.show(mainPanel, "editTask");
			}
		});
		
		views.put("editTaskInstance", new Action() {
			@Override
			public void run() {
				CardLayout layout;
				
				layout = (CardLayout) buttonPanel.getLayout();
				layout.show(buttonPanel, "editTaskOrExecButtons");

				layout = (CardLayout) mainPanel.getLayout();
				layout.show(mainPanel, "editTaskExec");
			}
		});
		
		/* Button Panel
		 * -------------------------------------------------- */
		
		this.buttonPanel = new JPanel();
		add(this.buttonPanel);
		this.buttonPanel.setLayout(new CardLayout(0, 0));

		// View Task buttons panel
		JPanel viewTasksButtonsPanel = new JPanel();
		this.buttonPanel.add(viewTasksButtonsPanel, "viewTasksButtons");
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener((e) -> this.refresh());
		viewTasksButtonsPanel.add(btnRefresh);
		
		JButton btnAddTask = new JButton("Add Task");
		btnAddTask.addActionListener((e) -> this.addTask());
		viewTasksButtonsPanel.add(btnAddTask);
		
		JButton btnEditTask = new JButton("Edit Task");
		btnEditTask.addActionListener((e) -> this.editTask());
		viewTasksButtonsPanel.add(btnEditTask);
		
		JButton btnRemoveTask = new JButton("Remove Task");
		btnRemoveTask.addActionListener((e) -> this.removeTask());
		viewTasksButtonsPanel.add(btnRemoveTask);
		
		// Edit Task / Edit Task Execution buttons panel
		JPanel editTaskOrExecButtonsPanel = new JPanel();
		this.buttonPanel.add(editTaskOrExecButtonsPanel, "editTaskOrExecButtons");

		JButton btnCancelEdit = new JButton("Cancel");
		btnCancelEdit.addActionListener((e) -> this.cancelEdit());
		editTaskOrExecButtonsPanel.add(btnCancelEdit);
		
		JButton btnConfirmEdit = new JButton("Done");
		btnConfirmEdit.addActionListener((e) -> this.confirmEdit());
		editTaskOrExecButtonsPanel.add(btnConfirmEdit);
		
		// Separator
		JSeparator sep1 = new JSeparator();
		add(sep1);
		
		/* Main Panel
		 * -------------------------------------------------- */

		this.mainPanel = new JPanel();
		add(this.mainPanel);
		this.mainPanel.setLayout(new CardLayout(0, 0));

		/* View Tasks
		 * -------------------- */
		
		JPanel viewTasksPanel = new JPanel();
		this.mainPanel.add(viewTasksPanel, "viewTasks");
		GridBagLayout gbl_viewTasksPanel = new GridBagLayout();
		gbl_viewTasksPanel.columnWidths = new int[]{0, 0};
		gbl_viewTasksPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_viewTasksPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		// The 0.15 weight is a hack - there's a bug/miscommunication somewhere
		// between GridBagLayout and FlowLayout/WrapLayout, but I don't have
		// time to properly diagnose it.
		gbl_viewTasksPanel.rowWeights = new double[]{0.15, 0.0, 1.0, Double.MIN_VALUE};
		viewTasksPanel.setLayout(gbl_viewTasksPanel);

		// Date Range
		JPanel rangePanel = new JPanel();
		GridBagConstraints gbc_rangePanel = new GridBagConstraints();
		gbc_rangePanel.fill = GridBagConstraints.BOTH;
		gbc_rangePanel.insets = new Insets(0, 0, 5, 0);
		gbc_rangePanel.gridx = 0;
		gbc_rangePanel.gridy = 0;
		viewTasksPanel.add(rangePanel, gbc_rangePanel);
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
		viewTasksPanel.add(instructionsPanel, gbc_instructionsPanel);
		instructionsPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("<html>Double-click on a task to view its instances in the selected range. Select a task or instance and click 'Edit Task' to view/edit details.<html>");
		instructionsPanel.add(lblNewLabel);
		
		// Tree
		JScrollPane taskTreeScrollPane = new JScrollPane();
		GridBagConstraints gbc_taskTreeScrollPane = new GridBagConstraints();
		gbc_taskTreeScrollPane.fill = GridBagConstraints.BOTH;
		gbc_taskTreeScrollPane.gridx = 0;
		gbc_taskTreeScrollPane.gridy = 2;
		viewTasksPanel.add(taskTreeScrollPane, gbc_taskTreeScrollPane);

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
		
		/* Edit Task
		 * -------------------- */
		
		this.editTaskComponent = new EditTask();
		this.mainPanel.add(this.editTaskComponent, "editTask");

		/* Initialise the data model
		 * -------------------------------------------------- */
		
		this.refresh();
	}

	private void refresh() {
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

	private void removeTask() {
		TreePath path = this.taskJTree.getSelectionPath();
		if (path == null) {
			new ExceptionDialog("You must select a task or instance to remove.");
			return;
		}
		
		Object obj = path.getLastPathComponent();
		
		// TODO: remove how?
	}

	private void addTask() {
		Task newTask = new Task(); // Make a new task (null ID / not in DB)
		this.active = newTask; // Keep a reference to it (the task being edited)
		this.views.get("editTask").run(); // Show the edit view
		this.editTaskComponent.showTask(newTask); // Set up the edit view to edit that task
	}

	private void editTask() {
		TreePath path = this.taskJTree.getSelectionPath();
		if (path == null) {
			new ExceptionDialog("You must select a task or instance to edit.");
			return;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object obj = node.getUserObject();
		if (obj instanceof Task) {
			this.active = obj; // Keep a reference to it (the task being edited)
			this.views.get("editTask").run(); // Show the edit view
			this.editTaskComponent.showTask((Task) obj); // Set up the edit view to edit that task
			
		} else if (obj instanceof TaskExecution) {
			this.active = obj; // Keep a reference to it (the task execution being edited)
			this.views.get("editTaskInstance").run(); // Show the edit view
			this.editTaskExecComponent.showTaskExec((TaskExecution) obj);
			
		} else {
			throw new TaskManagerExceptions.InvalidTaskTypeException();
		}
	}
	
	private void cancelEdit() {
		this.views.get("viewTasks").run();
	}
	
	private void confirmEdit() {
		// Try to connect to the DB each time you confirm - if one fails, you
		// can try again.
		DBAbstraction db;
		try {
			db = DBAbstraction.getInstance();
		} catch (FailedToConnectException e) {
			new ExceptionDialog("Could not connect to database. Please try saving again now or soon, or cancel the update (which will loose your changes).", e);
			return;
		}
		
		if (this.active instanceof Task) {
			boolean valid = this.editTaskComponent.validateFields();
			if (!valid) {
				new ExceptionDialog("Invalid inputs found. Please correct the marked values.");
				return;
			}
			
			this.editTaskComponent.updateTask((Task) this.active);
			//db.submitTask(this.active); // TODO
		}

		this.views.get("viewTasks").run();
	}
	
	/* Utils
	 * -------------------------------------------------- */

	private LocalDateTime dt(String dateTimeString) {
		return LocalDateTime.parse(dateTimeString, dateTimeFormatter);
	}
}
