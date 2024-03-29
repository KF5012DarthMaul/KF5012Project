package guicomponents;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
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
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import domain.Task;
import domain.TaskExecution;
import exceptions.TaskManagerExceptions;
import guicomponents.formatters.DeletionFormatter;
import guicomponents.formatters.Formatter;
import guicomponents.formatters.HTMLFormatter;
import guicomponents.formatters.TaskExecutionFormatter;
import guicomponents.formatters.TaskFormatter;
import guicomponents.utils.DateRangePicker;
import java.util.Comparator;
import java.util.stream.Collectors;
import temporal.Event;
import temporal.TemporalList;
import javax.swing.BoxLayout;

@SuppressWarnings("serial")
public class ViewTasks extends JPanel {
	private static final Font LIST_FONT = new Font("Arial", Font.PLAIN, 12);
	
	private static final Formatter<TaskExecution> TASK_EXEC_FORMATTER =
		new HTMLFormatter<>(new TaskExecutionFormatter());

	private DateRangePicker dateRangePicker;
	private JCheckBox chkDisplayUserTasks;
	private JCheckBox chkDisplayAllTasks;

	// Tree state management and display
	private List<Task> allTasks;
	private List<TaskExecution> allTaskExecs;
	
	private Map<Task, List<TaskExecution>> taskTree;
	private Map<Task, List<TaskExecution>> deletedTaskTree;
	
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
		
		JPanel infoAndOtherFiltersPanel = new JPanel();
		GridBagConstraints gbc_infoAndOtherFiltersPanel = new GridBagConstraints();
		gbc_infoAndOtherFiltersPanel.fill = GridBagConstraints.BOTH;
		gbc_infoAndOtherFiltersPanel.insets = new Insets(0, 0, 5, 0);
		gbc_infoAndOtherFiltersPanel.gridx = 0;
		gbc_infoAndOtherFiltersPanel.gridy = 1;
		this.add(infoAndOtherFiltersPanel, gbc_infoAndOtherFiltersPanel);
		infoAndOtherFiltersPanel.setLayout(new BoxLayout(infoAndOtherFiltersPanel, BoxLayout.X_AXIS));
		
		JLabel lblInstructions = new JLabel("<html>Double-click on a task to view its instances in the selected range. Select a task or instance and click 'Edit Task' to view/edit details.<html>");
		infoAndOtherFiltersPanel.add(lblInstructions);
		
		chkDisplayUserTasks = new JCheckBox("Display Only My Tasks");
		chkDisplayUserTasks.addItemListener((e) -> refresh(allTasks, allTaskExecs));
		infoAndOtherFiltersPanel.add(chkDisplayUserTasks);

		chkDisplayAllTasks = new JCheckBox("Display All Tasks");
		chkDisplayAllTasks.addItemListener((e) -> refresh(allTasks, allTaskExecs));
		infoAndOtherFiltersPanel.add(chkDisplayAllTasks);

		// Tree
		JScrollPane taskTreeScrollPane = new JScrollPane();
		GridBagConstraints gbc_taskTreeScrollPane = new GridBagConstraints();
		gbc_taskTreeScrollPane.fill = GridBagConstraints.BOTH;
		gbc_taskTreeScrollPane.gridx = 0;
		gbc_taskTreeScrollPane.gridy = 2;
		this.add(taskTreeScrollPane, gbc_taskTreeScrollPane);

		this.taskJTreeRoot = new DefaultMutableTreeNode();
		this.taskJTreeModel = new DefaultTreeModel(this.taskJTreeRoot);
		this.taskJTree = new JTree(this.taskJTreeModel);
		this.taskJTree.setCellRenderer(new DefaultTreeCellRenderer() {
			@SuppressWarnings("unchecked")
			@Override
			public Component getTreeCellRendererComponent(
					JTree tree, Object value, boolean sel, boolean expanded,
                    boolean leaf, int row, boolean hasFocus
			) {
				// Set stuff related to isSelected and cellHasFocus
				super.getTreeCellRendererComponent(
					tree, value, sel, expanded, leaf, row, hasFocus);

				this.setFont(LIST_FONT);
				
				Object nodeObj = ((DefaultMutableTreeNode) value).getUserObject();
				if (nodeObj == null) {
					setText(""); // Root node
					return this;
				}

				Map<String, Object> map = (Map<String, Object>) nodeObj;

				Object item = map.get("obj");
				if (item instanceof Task) {
					Formatter<Task> formatter = (Formatter<Task>) map.get("formatter");
					setText(formatter.apply((Task) item));

				} else if (item instanceof TaskExecution) {
					Formatter<TaskExecution> formatter = (Formatter<TaskExecution>) map.get("formatter");
					setText(formatter.apply((TaskExecution) item));

				} else {
					throw new TaskManagerExceptions.InvalidTaskTypeException();
				}
				return this;
			}
		});
		this.taskJTree.setRootVisible(false);
		taskTreeScrollPane.setViewportView(this.taskJTree);
	}
	
	public void refresh(List<Task> allTasks, List<TaskExecution> allTaskExecs) {
		this.allTasks = allTasks;
		this.allTaskExecs = allTaskExecs;
		/* Filter list to relevant task executions
		 * -------------------- */
		
		LocalDateTime start = this.dateRangePicker.getStartDateTime();
		LocalDateTime end = this.dateRangePicker.getEndDateTime();
		
		List<TaskExecution> filteredTaskExecs = allTaskExecs;
		if(chkDisplayUserTasks.isSelected()) {
		    filteredTaskExecs = filteredTaskExecs.stream()
				.filter(exec -> (
					exec.getAllocation() != null &&
					exec.getAllocation().equals(MainWindow.getCurrentUser())
				))
				.collect(Collectors.toList());
		}
		
		TemporalList<TaskExecution> taskExecTemporal = new TemporalList<>(filteredTaskExecs);
		List<TaskExecution> execsInRange = taskExecTemporal.getBetween(
			start, end, Event.byPeriodDefaultInf, true, true
		);
		

		/* Map back to tasks
		 * -------------------- */
		
		taskTree = new HashMap<>();
		deletedTaskTree = new HashMap<>();
		
		// Create a map of the set of tasks
		Map<Task, List<TaskExecution>> allTasksSet = new HashMap<>();
		for (Task task : allTasks) {
			allTasksSet.put(task, new ArrayList<>());
		}
		
		// If we're displaying all tasks, add all tasks regardless of whether
		// there are any executions for them.
		if (chkDisplayAllTasks.isSelected()) {
			taskTree.putAll(allTasksSet);
		}
		
		// For each task execution ...
		for (TaskExecution task : execsInRange) {
			Task owningTask = task.getTask();

			// If it is not deleted, put it in taskTree
			if (allTasksSet.containsKey(owningTask)) {
				if (!taskTree.containsKey(owningTask)) {
					taskTree.put(owningTask, allTasksSet.get(owningTask));
				}
				taskTree.get(owningTask).add(task);
				
			// If it is deleted, put it in deletedTaskTree (which is displayed
			// differently).
			} else {
				if (!deletedTaskTree.containsKey(owningTask)) {
					deletedTaskTree.put(owningTask, new ArrayList<>());
				}
				deletedTaskTree.get(owningTask).add(task);
			}
		}
		
		/* Construct/Reconstruct the tree
		 * -------------------- */
		
		this.taskJTreeRoot.removeAllChildren();
		
		Formatter<Task> nonDeletedFormatter = new HTMLFormatter<>(new TaskFormatter());
		Formatter<Task> deletedFormatter = new HTMLFormatter<>(new DeletionFormatter<>(new TaskFormatter()));
		
		Comparator<Task> taskComp = (c, t) -> c.getName().compareTo(t.getName());
                
                // Insert all visible non-deleted tasks
                List<Task> insertTasks = new ArrayList<>(this.taskTree.keySet());
                insertTasks.sort(taskComp);
                for (Task task : insertTasks) {
                    DefaultMutableTreeNode taskNode = makeTreeNode(task, nonDeletedFormatter);
                    for (TaskExecution taskExec : this.taskTree.get(task)) {
                        taskNode.add(makeTreeNode(taskExec, TASK_EXEC_FORMATTER));
                    }
                    this.taskJTreeRoot.add(taskNode);
                }
                // Insert all deleted tasks
                List<Task> insertDelTasks = new ArrayList<>(this.deletedTaskTree.keySet());
                insertDelTasks.sort(taskComp);
                for (Task task : insertDelTasks) {
                    DefaultMutableTreeNode taskNode = makeTreeNode(task, deletedFormatter);
                    for (TaskExecution taskExec : this.deletedTaskTree.get(task)) {
                        taskNode.add(makeTreeNode(taskExec, TASK_EXEC_FORMATTER));
                    }
                    this.taskJTreeRoot.add(taskNode);
                }

		// Update the GUI
		this.taskJTreeModel.nodeStructureChanged(this.taskJTreeRoot);
	}
	
	private static <T> DefaultMutableTreeNode makeTreeNode(T obj, Formatter<T> formatter) {
		Map<String, Object> content = new HashMap<>();
		content.put("obj", obj);
		content.put("formatter", formatter);
		
		return new DefaultMutableTreeNode(content);
	}

	@SuppressWarnings("unchecked")
	public Object getSelectedObject() {
		TreePath path = this.taskJTree.getSelectionPath();
		if (path == null) {
			return null;
		} else {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Object nodeObj = node.getUserObject();
			if (nodeObj == null) {
				return null; // Root node selected, I suppose? Not sure how ...
			}
			
			Map<String, Object> map = (Map<String, Object>) nodeObj;
			return map.get("obj");
		}
	}
        
        @SuppressWarnings("unchecked")
	public ArrayList<Object> getSelectedObjects() 
        {
            TreePath[] paths = this.taskJTree.getSelectionPaths();
            if(paths == null)
                return null;
            ArrayList<Object>  objects = new ArrayList(paths.length);
            for(TreePath path: paths)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object nodeObj = node.getUserObject();
                if (nodeObj == null) {
                        return null; // Root node selected, I suppose? Not sure how ...
                }
                Map<String, Object> map = (Map<String, Object>) nodeObj;
                objects.add(map.get("obj"));
            }
            return objects;
	}
}
