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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import domain.Task;
import domain.TaskExecution;
import exceptions.TaskManagerExceptions;
import guicomponents.formatters.TaskExecutionFormatter;
import guicomponents.formatters.TaskFormatter;
import guicomponents.utils.DateRangePicker;
import temporal.Event;
import temporal.TemporalList;
import javax.swing.BoxLayout;

@SuppressWarnings("serial")
public class ViewTasks extends JPanel {
	private static final TaskFormatter TASK_FORMATTER = new TaskFormatter();
	private static final TaskExecutionFormatter TASK_EXEC_FORMATTER = new TaskExecutionFormatter();

	private DateRangePicker dateRangePicker;
	private JCheckBox chkDisplayAllTasks;

	private List<Task> allTasks;
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
		dateRangePicker.addChangeListener((e) -> refresh(allTasks, allTaskExecs));
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
		instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.X_AXIS));
		
		JLabel lblInstructions = new JLabel("<html>Double-click on a task to view its instances in the selected range. Select a task or instance and click 'Edit Task' to view/edit details.<html>");
		instructionsPanel.add(lblInstructions);
		
		chkDisplayAllTasks = new JCheckBox("Display All Tasks");
		chkDisplayAllTasks.addItemListener((e) -> refresh(allTasks, allTaskExecs));
		instructionsPanel.add(chkDisplayAllTasks);
		
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
	
	public void refresh(List<Task> allTasks, List<TaskExecution> allTaskExecs) {
		this.allTasks = allTasks;
		this.allTaskExecs = allTaskExecs;
		
		// Filter list to relevant task executions
		LocalDateTime start = this.dateRangePicker.getStartDateTime();
		LocalDateTime end = this.dateRangePicker.getEndDateTime();
		
		TemporalList<TaskExecution> taskExecTemporal = new TemporalList<>(this.allTaskExecs);
		List<TaskExecution> execsInRange = taskExecTemporal.getBetween(
			start, end, Event.byPeriodDefaultInf, true, true
		);
		
		// Map back to tasks
		taskTree = new HashMap<>();
		if (chkDisplayAllTasks.isSelected()) {
			for (Task task : allTasks) {
				taskTree.put(task, new ArrayList<>());
			}
		}
		for (TaskExecution task : execsInRange) {
			Task owningTask = task.getTask();

			if (!chkDisplayAllTasks.isSelected() && !taskTree.containsKey(owningTask)) {
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
}
