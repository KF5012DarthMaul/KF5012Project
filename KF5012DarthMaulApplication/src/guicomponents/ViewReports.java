package guicomponents;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import dbmgr.DBAbstraction;
import dbmgr.DBExceptions;
import dbmgr.DBExceptions.FailedToConnectException;
import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;

import domain.Completion;
import domain.TaskExecution;
import domain.VerificationExecution;
import guicomponents.ome.ListSelectionEditor;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
//All imports of packages and other functions from outside of this file
public class ViewReports extends JPanel {
	private static final DateTimeFormatter formatter =
			DateTimeFormatter.ofPattern("h:mma d/M/yyyy");
	//Formatting incoming dates from a ISO format into a more user friendly and readable format
	private JTable table;
	private ListSelectionEditor<User> lsteCaretaker;
	//Used later to print a drop down menu of all caretakers
	private boolean usersLoaded;
	
	/**
	 * Create the panel.
	 */
	public ViewReports() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("View Reports");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		add(tabbedPane, gbc_tabbedPane);
		
		JPanel report1 = new JPanel();
		tabbedPane.addTab("Task Status", report1);
		report1.setLayout(new BorderLayout(0, 0));
		//New tab for showing tasks that are not done
		JScrollPane scrollPane_Task_Status = new JScrollPane();
		report1.add(scrollPane_Task_Status);
					
		JPanel report2 = new JPanel();
		tabbedPane.addTab("Caretaker Performance", report2);
		//New tab for showing a single caretakers task performance history
		lsteCaretaker = new ListSelectionEditor<>(
			(user) -> user.getDisplayName()
			//Drop down list to dynamically get the list of all caretakers on the system
		);
		report2.add(lsteCaretaker);
			//adds this to the new tab and is used to show a list of options to the user to select
		loadUsers (true);
		report2.setLayout(new BoxLayout(report2, BoxLayout.Y_AXIS));
		//Set box format to make the UI look nicer with the drop down menu
		JScrollPane scrollPane_Caretaker_Performance = new JScrollPane();
		report2.add(scrollPane_Caretaker_Performance);
		//New scroll pane to allow for longer list of queried results to be displayed
		
		JPanel report3 = new JPanel();
		tabbedPane.addTab("Task Performance", report3);
		report3.setLayout(new BorderLayout(0, 0));
		//Final new tab to show the history of all completed tasks
		
		JScrollPane scrollPane_Task_Performance = new JScrollPane();
		report3.add(scrollPane_Task_Performance);
		//Scroll pane as this could be a long list, need to be dynamically able to handle large lists 
		DBAbstraction db;
		try {
			db = DBAbstraction.getInstance();
		}
		catch (DBExceptions.FailedToConnectException ex) {
			new ExceptionDialog("An error has occured with the database", ex);
			return;
		}
		//connection to the database, or list that a database could not be connected to	
		lsteCaretaker.addItemListener((e) -> {
                    Object[] columns2 = {"Task Name", "Due Date", "Completion Time", "Overdue?", "Personal Review", "Verified Quality"};
                    //Columns to display data in a tidy format
                    List<TaskExecution> tasks2 = db.getTaskExecutionList().stream()
                                    .filter(task -> task.getCompletion() != null && 
                                            task.getCompletion().getStaff().equals(lsteCaretaker.getObject()))
                                    .collect(Collectors.toList());
                    //Query to get the list of caretakers and the tasks they have finished 
                    Object[][] data2 = new Object[tasks2.size()][columns2.length];
                    //Allows for the dynamic printing of the database content no matter the size
                    for (int i = 0; i<tasks2.size(); i++) {
                    	//For loop to loop through the queried results no matter the size
                            LocalDateTime dueDate = tasks2.get(i).getPeriod().end();
                            //Gets the expected due date for the task in a vairable 
                            LocalDateTime completionTime = tasks2.get(i).getCompletion().getCompletionTime();	
                            //Gets the actual time of completion and stores it in a variable 
                            data2[i][0] = tasks2.get(i).getName();
                            //Get the name of the task currently selected in the loop
                            if (dueDate == null) {
                                    data2[i][1] = "No Task Deadline Set";
                                    //Print this if there is no set deadline, so dueDate equals null
                            }
                            else {
                                    data2[i][1] = dueDate.format(formatter);
                                    //Otherwise print the related due date in the formatter set at the top of the file
                            }
                            data2[i][2] = completionTime.format(formatter);
                            //Format the completion time the same way
                            if (dueDate == null || !completionTime.isAfter(dueDate)) {
                            	//If the due date is null or if the completion time is NOT after the expected due date
                                    data2[i][3] = "Completed on-time";
                                    //Print saying the task was done on time
                            }
                            else {
                                    data2[i][3] = "Over deadline";	
                                    //Otherwise, it wasn't done on time so print that it wasn't
                            }
                            data2[i][4] = tasks2.get(i).getCompletion().getWorkQuality().toString();
                            VerificationExecution verification = tasks2.get(i).getVerification();
                            //Get the verification if a task has been completed or not
                            if (verification != null) {
                            	//If there is a verification for the task
                            	Completion completion = verification.getCompletion();
                            	//Print it out
                            	if (completion != null) {
                            		//If there is a managers verification for the task
                            		data2[i][5] = completion.getWorkQuality().toString();
                            		//Print it out
                            	}
                            	else {
                            		//If there is no verification, print out so
                            		data2[i][5] = "No Verification";
                            	}
                            }
                            else {
                            	//If there is no verification, print out so
                            	data2[i][5] = "No Verification";
                            }
                    }
                    table = new JTable(data2, columns2){
                		public boolean editCellAt(int row, int column, java.util.EventObject e) {
                			return false;
                			//Prevent edits being made 
                		}
                     };
                    scrollPane_Caretaker_Performance.setViewportView(table);
		});	
		ChangeListener changeTabs = new ChangeListener() {
					//Loads different cases, or tabs, depending on which is selected
                    public void stateChanged(ChangeEvent e) {
                        int tabIndex = tabbedPane.getSelectedIndex();
                        List<TaskExecution> tasks = db.getTaskExecutionList();
                        switch (tabIndex) {
                        case 0:
                        	//Case 0 is to find tasks that are not done along with their related information
                            Object[] columns = {"Task Name", "Allocated Caretaker", "Due Date"};
                            //Easy to read and nicely formatted columns for a easy to read UI
                            List<TaskExecution> incompleteTaskExecsList = tasks.stream()
                                    .filter(task -> task.getCompletion() == null 
                                            && task.getAllocation() != null)
                                    .collect(Collectors.toList());
                            //Filter tasks from the database where completion is not true (or null)
                            Object[][] data = new Object[incompleteTaskExecsList.size()][columns.length];
                            //Get the size of this query
                            for (int i = 0; i<incompleteTaskExecsList.size(); i++) {
                            	//A for loop lasting the amount of time based upon the size of the query
                                data[i][0] = incompleteTaskExecsList.get(i).getName();
                                //Get the name of the task currently selected in the loop
                                data[i][1] = incompleteTaskExecsList.get(i).getAllocation().getDisplayName();
                                //Get the related user name for this task
                                LocalDateTime taskDeadline = incompleteTaskExecsList.get(i).getPeriod().end();
                                //Get the related due date 
                                if (taskDeadline == null) {
                                        data[i][2] = "No Task Deadline Set";
                                        //If there is no due date set, state so
                                } 
                                else {
                                        data[i][2] = taskDeadline.format(formatter);
                                        //Otherwise, print it and format it in the format set at the top of the file
                                }
                            }

                            table = new JTable(data, columns){
                        		public boolean editCellAt(int row, int column, java.util.EventObject e) {
                        			return false;
                        			//Prevent edits being made 
                        		}
                             };
                            scrollPane_Task_Status.setViewportView(table);	        		
                            break;

                        case 1:
                        	//Case one shows a selected users history of completed tasks and performance
                            Object[] columns2 = {"Task Name", "Due Date", "Completion Time", "Overdue?", "Personal Review", "Verified Quality"};
                            //Columns showing related information for this task, in an easy to read format
                            List<TaskExecution> completedByUserTaskExecsList = tasks.stream()
                                    .filter(task -> task.getCompletion() != null 
                                            && task.getCompletion().getStaff().equals(lsteCaretaker.getObject()))
                                    .collect(Collectors.toList());
                            //Query database based upon the selected caretaker from the drop down menu
                            Object[][] data2 = new Object[completedByUserTaskExecsList.size()][columns2.length];
                            //Get all related tasks and find out the size
                            for (int i = 0; i<completedByUserTaskExecsList.size(); i++) {
                            	//Form a loop based upon the size of the query
                                    LocalDateTime dueDate = completedByUserTaskExecsList.get(i).getPeriod().end();
                                    //Get the expected due date for the task currently selected
                                    LocalDateTime completionTime = completedByUserTaskExecsList.get(i).getCompletion().getCompletionTime();	    
                                    //Get the time the task was actually completed by
                                    data2[i][0] = completedByUserTaskExecsList.get(i).getName();
                                    //Print out the name of the task currently selected
                                    if (dueDate == null) {
                                    	//If there is no deadline set for the currently selected task
                                            data2[i][1] = "No Task Deadline Set";
                                            //print saying there is no deadline
                                    }
                                    else {
                                            data2[i][1] = dueDate.format(formatter);
                                            //Otherwise print the due date in the format set at the top of the file
                                    }
                                    data2[i][2] = completionTime.format(formatter);
                                    //Get the completion time, print it and format it in the style set at the top of the file
                                    if (dueDate == null || !completionTime.isAfter(dueDate)) {
                                    	//If the due date is null OR if the completion time is NOT after the due date
                                            data2[i][3] = "Completed on-time";
                                            //Task was done on time, print that it was done on time
                                    }
                                    else {
                                    	//Otherwise the task was not done on time
                                            data2[i][3] = "Over deadline";
                                            //Print that it was not done on time
                                    }
                                    data2[i][4] = completedByUserTaskExecsList.get(i).getCompletion().getWorkQuality().toString();
                                    VerificationExecution verification = completedByUserTaskExecsList.get(i).getVerification();
                                    //Get the verification if a task has been completed or not
                                    if (verification != null) {
                                    	//If this task has a verification
                                    	Completion completion = verification.getCompletion();
                                    	//Print out the caretakers rating of their work quality
                                    	if (completion != null) {
                                    		//If there is a managers verfication
                                    		data2[i][5] = completion.getWorkQuality().toString();
                                    		//print out the managers review of the work
                                    	}
                                    	else {
                                    		//if there is no verification print out so
                                    		data2[i][5] = "No Verification";
                                    	}
                                    }
                                    else {
                                    	//if there is no verification print out so
                                    	data2[i][5] = "No Verification";
                                    }
                            }

                            table = new JTable(data2, columns2){
                        		public boolean editCellAt(int row, int column, java.util.EventObject e) {
                        			return false;
                        			//Prevent edits being made 
                        		}
                             };
                            scrollPane_Caretaker_Performance.setViewportView(table);
                            break;

                        case 2:
                        	//Final case, shows the entire history of all completed tasks
                            Object[] columns3 = {"Caretaker", "Task Name", "Due Date", "Completion Time", "Overdue?", "Personal Review", "Verified Quality"};
                            //Columns related to this task, makes reading the UI a lot nicer and simpler
                            List<TaskExecution> allCompletedTaskExecsList = tasks.stream()
                                                    .filter(task -> task.getCompletion() != null)
                                                    .collect(Collectors.toList());
                            //Get a list of all complete tasks, or where completion is NOT null
                            Object[][] data3 = new Object[allCompletedTaskExecsList.size()][columns3.length];
                            //Find out the size of this query and store it
                            for (int i = 0; i<allCompletedTaskExecsList.size(); i++) {
                            	//Form a loop based upon the size of this query
                                    LocalDateTime dueDate = allCompletedTaskExecsList.get(i).getPeriod().end();
                                    //Store the time in which the task was expected to be completed
                                    LocalDateTime completionTime = allCompletedTaskExecsList.get(i).getCompletion().getCompletionTime();
                                    //Store the time the task was actually completed
                                    data3[i][0] = allCompletedTaskExecsList.get(i).getCompletion().getStaff().getDisplayName();
                                    //Get the related staffs username for the query
                                    data3[i][1] = allCompletedTaskExecsList.get(i).getName();
                                    //Get the name of the task this query is selecting
                                    if (dueDate == null) {
                                    	//If this task has no set due date
                                            data3[i][2] = "No Task Deadline Set";
                                            //Print that there is no due date
                                    }
                                    else {
                                    	//Otherwise, it has a deadline
                                            data3[i][2] = dueDate.format(formatter);
                                            //Print the deadline and format it as seen at the top of this file
                                    }
                                    data3[i][3] = completionTime.format(formatter);
                                    //Print out the time at which the task was completed, and style it the same
                                    if (dueDate == null || !completionTime.isAfter(dueDate)) {
                                    	//If there is no due date, OR the completion time was NOT after the due date
                                            data3[i][4] = "Completed on-time";
                                            //Task was completed on time, print it
                                    }
                                    else {
                                    	//Otherwise it was not completed on time
                                            data3[i][4] = "Over deadline";	
                                            //Print that it was over due
                                    }
                                    data3[i][5] = allCompletedTaskExecsList.get(i).getCompletion().getWorkQuality().toString();
                                    VerificationExecution verification = allCompletedTaskExecsList.get(i).getVerification();
                                    //Get the verification if a task has been completed or not
                                    if (verification != null) {
                                    	//If this task has a verification
                                    	Completion completion = verification.getCompletion();
                                    	//Print out the caretakers rating of their work quality
                                    	if (completion != null) {
                                    		//If there is a managers verfication
                                    		data3[i][6] = completion.getWorkQuality().toString();
                                    		//print out the managers review of the work
                                    	}
                                    	else {
                                    		//if there is no verification print out so
                                    		data3[i][6] = "No Verification";
                                    	}
                                    }
                                    else {
                                    	//if there is no verification print out so
                                    	data3[i][6] = "No Verification";
                                    }

                                    
                            }
                            table = new JTable(data3, columns3){
                        		public boolean editCellAt(int row, int column, java.util.EventObject e) {
                        			return false;
                        			//Prevent edits being made 
                        		}
                             };
                         
                            scrollPane_Task_Performance.setViewportView(table);
                            break;
                        }  
                    }
		};		
		changeTabs.stateChanged(null);
		tabbedPane.addChangeListener(changeTabs);
		//Functions to switch between tabs
	}   
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
			List<User> caretakersAndNull = 
				allUsers.stream()
				.filter(u -> u.getAccountType() == PermissionManager.AccountType.CARETAKER)
				.collect(Collectors.toList());
			// (Re)fill the lists
			lsteCaretaker.populate(caretakersAndNull);
			usersLoaded = true;
		}
	}
	
}