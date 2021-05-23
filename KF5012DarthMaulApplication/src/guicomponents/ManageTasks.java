package guicomponents;

import domain.Task;
import domain.TaskExecution;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;

import exceptions.TaskManagerExceptions;
import guicomponents.ome.TaskEditor;
import guicomponents.ome.TaskExecutionEditor;
import kf5012darthmaulapplication.ExceptionDialog;

import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.BoxLayout;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kf5012darthmaulapplication.PermissionManager;

@SuppressWarnings("serial")
public class ManageTasks extends JPanel {
	private Map<String, Action> views = new HashMap<>();

	private DBAbstraction db;
	
	private JPanel buttonPanel;
	private JPanel mainPanel;
	
	// Main Panel
	private List<Task> allTasks;
	private List<TaskExecution> allTaskExecs;
	private ViewTasks viewTasksComponent;
	private TaskEditor edtTask;
	private TaskExecutionEditor edtTaskExecution;
	
	private Object active; // Nullable
	
	/**
	 * Create the panel.
	 */
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

				reload();
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

				edtTask.loadUsers(); // In case they weren't already
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
                                
                                edtTaskExecution.loadUsers();
				layout = (CardLayout) mainPanel.getLayout();
				layout.show(mainPanel, "editTaskExec");
			}
		});
		
		/* Button Panel
		 * -------------------------------------------------- */

		buttonPanel = new JPanel();
		add(buttonPanel);
		buttonPanel.setLayout(new CardLayout(0, 0));

		// View Task buttons panel
		JPanel viewTasksButtonsPanel = new JPanel();
		buttonPanel.add(viewTasksButtonsPanel, "viewTasksButtons");
		
		JButton btnRefresh = new JButton("Reload");
		btnRefresh.addActionListener((e) -> reload());
		viewTasksButtonsPanel.add(btnRefresh);
		
		JButton btnAddTask = new JButton("Add Task");
		btnAddTask.addActionListener((e) -> addTask());
		viewTasksButtonsPanel.add(btnAddTask);
		
		JButton btnEditTask = new JButton("Edit Task");
		btnEditTask.addActionListener((e) -> editTask());
		viewTasksButtonsPanel.add(btnEditTask);
		
		JButton btnRemoveTask = new JButton("Remove Task");
		btnRemoveTask.addActionListener((e) -> removeTask());
                // Only enable the button if user has permission to remove tasks
                btnRemoveTask.setEnabled(
                        PermissionManager.hasPermission(
                                MainWindow.getCurrentUser().getAccountType(), 
                                PermissionManager.Permission.REMOVE_TASKS
                        )
                );
		viewTasksButtonsPanel.add(btnRemoveTask);
		
		// Edit Task / Edit Task Execution buttons panel
		JPanel editTaskOrExecButtonsPanel = new JPanel();
		buttonPanel.add(editTaskOrExecButtonsPanel, "editTaskOrExecButtons");

		JButton btnCancelEdit = new JButton("Cancel");
		btnCancelEdit.addActionListener((e) -> cancelEdit());
		editTaskOrExecButtonsPanel.add(btnCancelEdit);
		
		JButton btnConfirmEdit = new JButton("Done");
		btnConfirmEdit.addActionListener((e) -> confirmEdit());
		editTaskOrExecButtonsPanel.add(btnConfirmEdit);
		
		// Separator
		JSeparator sep1 = new JSeparator();
		add(sep1);
		
		/* Main Panel
		 * -------------------------------------------------- */

		mainPanel = new JPanel();
		add(mainPanel);
		mainPanel.setLayout(new CardLayout(0, 0));

		viewTasksComponent = new ViewTasks();
		mainPanel.add(viewTasksComponent, "viewTasks");
		
		edtTask = new TaskEditor();
		mainPanel.add(edtTask, "editTask");

		edtTaskExecution = new TaskExecutionEditor();
		mainPanel.add(edtTaskExecution, "editTaskExec");

		/* Initialise the data model
		 * -------------------------------------------------- */

		views.get("viewTasks").run();
	}

	public void reload() {
		// Try to connect to the DB each time you refresh - if one fails, you
		// can try again.
		try {
			db = DBAbstraction.getInstance();
		} catch (FailedToConnectException e) {
			new ExceptionDialog(
				"Could not connect to database. Click 'Refresh' to retry loading tasks.", e);
			return;
		}

		allTasks = db.getTaskList();
		allTaskExecs = db.getTaskExecutionList();
		viewTasksComponent.refresh(allTasks, allTaskExecs);
	}
	
	private void removeTask() {
		if (db == null) return;
		ArrayList<Object> objs = viewTasksComponent.getSelectedObjects();
                if (objs == null)
                {
                    new ExceptionDialog("You must select a task or instance to remove.");
                    return;
                }
                Map<Task, List<TaskExecution>> linkedTaskExecutionMap = new HashMap<>();
                for (Task task : allTasks) {
			linkedTaskExecutionMap.put(task, new ArrayList<>());
		}
                for (TaskExecution exe : allTaskExecs) 
                {
                    if(linkedTaskExecutionMap.containsKey(exe.getTask()))
                        linkedTaskExecutionMap.get(exe.getTask()).add(exe);
                }
                List<Task> tasksToDelete = new ArrayList<>();
                Set<TaskExecution> exesToDelete = new HashSet<>();
                for(Object obj: objs)
                {
                    if (obj instanceof Task) 
                    {
                        Task t = (Task) obj;
                        tasksToDelete.add(t);
                        List<TaskExecution> execList = linkedTaskExecutionMap.get(t);
                        if(execList != null)
                            for(TaskExecution exe: execList)
                            {
                                if(exe.getAllocation() == null && exe.getCompletion() == null)
                                    exesToDelete.add(exe);
                            }
                    }
                    else if (obj instanceof TaskExecution) 
                    {
                        TaskExecution exe = (TaskExecution) obj;
                        exesToDelete.add(exe);
                    } 
                    else 
                    {
                        throw new TaskManagerExceptions.InvalidTaskTypeException();
                    }
                }
                db.deleteTasks(tasksToDelete);
                db.deleteTaskExecutions(new ArrayList<>(exesToDelete));
                reload();
	}

	private void addTask() {
		Task newTask = new Task(); // Make a new task (null ID / not in DB)
		active = newTask; // Keep a reference to it (the real task being edited)
		views.get("editTask").run(); // Show the edit view
		edtTask.setObject(newTask); // Set up the edit view to edit that task
	}

	private void editTask() {
		Object obj = viewTasksComponent.getSelectedObject();
		if (obj == null) {
			new ExceptionDialog("You must select a task or instance to edit.");
			
		} else if (obj instanceof Task) {
                        if(!allTasks.contains(obj))
                        {
                            new ExceptionDialog("You cannot edit a deleted task");
                            return;
                        }
			active = obj; // Keep a reference to it (the task being edited)
			views.get("editTask").run(); // Show the edit view

			// Copy task and edit the copy
			Task taskCopy = new Task((Task) obj);
			edtTask.setObject(taskCopy); // Set up the edit view to edit that task
			
		} else if (obj instanceof TaskExecution) {
			active = obj; // Keep a reference to it (the task execution being edited)
			views.get("editTaskInstance").run(); // Show the edit view
			
			TaskExecution taskExecCopy = new TaskExecution((TaskExecution) obj);
			edtTaskExecution.setObject(taskExecCopy);
			
		} else {
			throw new TaskManagerExceptions.InvalidTaskTypeException();
		}
	}
	
	private void cancelEdit() {
		views.get("viewTasks").run();
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
		
		if (active instanceof Task) {
			boolean valid = edtTask.validateFields();
			if (!valid) {
				new ExceptionDialog("Invalid inputs found. Please correct the marked values.");
				return;
			}
			
			// Get the new copy, overwrite the 'real' one, then flush to DB
			Task task = (Task) active;
			task.copyFrom(edtTask.getObject());
			db.submitTask(task);
			
		} else if (active instanceof TaskExecution) {
			boolean valid = edtTaskExecution.validateFields();
			if (!valid) {
				new ExceptionDialog("Invalid inputs found. Please correct the marked values.");
				return;
			}

			TaskExecution taskExec = (TaskExecution) active;
			taskExec.copyFrom(edtTaskExecution.getObject());
			db.submitTaskExecution(taskExec);
			
		} else {
			throw new TaskManagerExceptions.InvalidTaskTypeException();
		}

		views.get("viewTasks").run();
	}
}
