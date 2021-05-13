package guicomponents;

import domain.Task;
import domain.TaskExecution;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;

import exceptions.TaskManagerExceptions;
import kf5012darthmaulapplication.ExceptionDialog;

import java.awt.CardLayout;
import javax.swing.BoxLayout;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ManageTasks extends JPanel {
	private Map<String, Action> views = new HashMap<>();

	private JPanel buttonPanel;
	private JPanel mainPanel;
	
	// Main Panel
	private ViewTasks viewTasksComponent;
	private EditTask edtTask;
	private EditTaskExec edtTaskExecution;
	
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
		
		edtTask = new EditTask();
		mainPanel.add(edtTask, "editTask");

		edtTaskExecution = new EditTaskExec();
		mainPanel.add(edtTaskExecution, "editTaskExec");

		/* Initialise the data model
		 * -------------------------------------------------- */

		reload();
	}
	
	private void reload() {
		viewTasksComponent.reload();
	}
	
	private void removeTask() {
		Object obj = viewTasksComponent.getSelectedObject();
		if (obj == null) {
			new ExceptionDialog("You must select a task or instance to remove.");
			
		} else if (obj instanceof Task) {
			// TODO: remove how?
			
		} else if (obj instanceof TaskExecution) {
			// TODO: remove how?
			
		} else {
			throw new TaskManagerExceptions.InvalidTaskTypeException();
		}
	}

	private void addTask() {
		Task newTask = new Task(); // Make a new task (null ID / not in DB)
		active = newTask; // Keep a reference to it (the task being edited)
		views.get("editTask").run(); // Show the edit view
		edtTask.showObject(newTask); // Set up the edit view to edit that task
	}

	private void editTask() {
		Object obj = viewTasksComponent.getSelectedObject();
		if (obj == null) {
			new ExceptionDialog("You must select a task or instance to edit.");
			
		} else if (obj instanceof Task) {
			active = obj; // Keep a reference to it (the task being edited)
			views.get("editTask").run(); // Show the edit view
			edtTask.showObject((Task) obj); // Set up the edit view to edit that task
			
		} else if (obj instanceof TaskExecution) {
			active = obj; // Keep a reference to it (the task execution being edited)
			views.get("editTaskInstance").run(); // Show the edit view
			edtTaskExecution.showObject((TaskExecution) obj);
			
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
			
			Task task = (Task) active;
			edtTask.updateObject(task);
			
			//db.submitTask(task); // TODO
			
		} else if (active instanceof TaskExecution) {
			boolean valid = edtTaskExecution.validateFields();
			if (!valid) {
				new ExceptionDialog("Invalid inputs found. Please correct the marked values.");
				return;
			}
			
			TaskExecution taskExec = (TaskExecution) active;
			edtTaskExecution.updateObject(taskExec);
			
			//db.submitTaskExecution(taskExec); // TODO
			
		} else {
			throw new TaskManagerExceptions.InvalidTaskTypeException();
		}

		views.get("viewTasks").run();
	}
}
