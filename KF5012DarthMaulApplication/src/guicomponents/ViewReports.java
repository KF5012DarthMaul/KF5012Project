package guicomponents;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions;
import dbmgr.DBExceptions.EmptyResultSetException;
import dbmgr.DBExceptions.FailedToConnectException;

import javax.swing.JButton;
import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;  

import domain.TaskExecution;
import guicomponents.ome.ListSelectionEditor;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;

public class ViewReports extends JPanel {
	private static final DateTimeFormatter formatter =
			DateTimeFormatter.ofPattern("h:mma d/M/yyyy");
	private JTable table;
	private ListSelectionEditor<User> lsteCaretaker;
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
		
		JScrollPane scrollPane_Task_Status = new JScrollPane();
		report1.add(scrollPane_Task_Status);
					
		JPanel report2 = new JPanel();
		tabbedPane.addTab("Caretaker Performance", report2);
		
		lsteCaretaker = new ListSelectionEditor<>(
			(user) -> user.getUsername() 
		);
		report2.add(lsteCaretaker);
			
		loadUsers (true);
		
		JScrollPane scrollPane_Caretaker_Performance = new JScrollPane();
		report2.add(scrollPane_Caretaker_Performance);
		
		
		JPanel report3 = new JPanel();
		tabbedPane.addTab("Task Performance", report3);
		
		JScrollPane scrollPane_Task_Performance = new JScrollPane();
		report3.add(scrollPane_Task_Performance);
		
		DBAbstraction db;
		try {
			db = DBAbstraction.getInstance();
		}
		catch (DBExceptions.FailedToConnectException ex) {
			new ExceptionDialog("An error has occured with the database", ex);
			return;
		}
		
		lsteCaretaker.addItemListener((e) -> {
			Object[] columns2 = {"Task Name", "Due Date", "Completion Time", "Overdue?"};
    		List<TaskExecution> tasks2;

			db.getTaskExecutionList().forEach(t->System.out.println(t.getID()));
			tasks2 = db.getTaskExecutionList().stream()
				.filter(task -> task.getCompletion() != null && 
					task.getCompletion().getStaff().equals(lsteCaretaker.getObject()))
				.collect(Collectors.toList());

    		Object[][] data2 = new Object[tasks2.size()][columns2.length];
    		
    		for (int i = 0; i<tasks2.size(); i++) {
    			LocalDateTime dueDate = tasks2.get(i).getPeriod().end();
    			LocalDateTime completionTime = tasks2.get(i).getCompletion().getCompletionTime();	        			
    			data2[i][0] = tasks2.get(i).getName();
    			if (dueDate == null) {
    				data2[i][1] = "No Task Deadline Set";
    			}
    			else {
    				data2[i][1] = dueDate.format(formatter);
    			}
    			data2[i][2] = completionTime.format(formatter);
    			if (dueDate == null || !completionTime.isAfter(dueDate)) {
    				data2[i][3] = "Completed on-time";
    			}
    			else {
    				data2[i][3] = "Over deadline";	
    			}
    		}
    		table = new JTable(data2, columns2);
    		scrollPane_Caretaker_Performance.setViewportView(table);
		});
		
		ChangeListener changeTabs = new ChangeListener() {

	        public void stateChanged(ChangeEvent e) {
	        	int tabIndex = tabbedPane.getSelectedIndex();
	        	switch (tabIndex) {
	        	case 0:
	        		Object[] columns = {"Task Name", "Allocated Caretaker", "Due Date"};
	        		
	        		List<TaskExecution> tasks;

					tasks = db.getTaskExecutionList().stream()
							.filter(task -> task.getCompletion() == null)
							.collect(Collectors.toList());

	        		Object[][] data = new Object[tasks.size()][columns.length];
	        		for (int i = 0; i<tasks.size(); i++) {
	        			data[i][0] = tasks.get(i).getName();
	        			data[i][1] = tasks.get(i).getAllocation().getUsername();
	        			LocalDateTime taskDeadline = tasks.get(i).getPeriod().end();
	        			if (taskDeadline == null) {
	        				data[i][2] = "No Task Deadline Set";
	        			} 
	        			else {
	        				data[i][2] = taskDeadline.format(formatter);
	        			}
	        		}
	        		
	        		table = new JTable(data, columns);
	        		scrollPane_Task_Status.setViewportView(table);	        		
	        		break;
	        		
	        	case 1:
	        		Object[] columns2 = {"Task Name", "Due Date", "Completion Time", "Overdue?"};
	        		List<TaskExecution> tasks2;
	        		
	    			db.getTaskExecutionList().forEach(t->System.out.println(t.getID()));
	    			tasks2 = db.getTaskExecutionList().stream()
	    				.filter(task -> task.getCompletion() != null && 
	    					task.getCompletion().getStaff().equals(lsteCaretaker.getObject()))
	    				.collect(Collectors.toList());

	        		Object[][] data2 = new Object[tasks2.size()][columns2.length];
	        		
	        		for (int i = 0; i<tasks2.size(); i++) {
	        			LocalDateTime dueDate = tasks2.get(i).getPeriod().end();
	        			LocalDateTime completionTime = tasks2.get(i).getCompletion().getCompletionTime();	        			
	        			data2[i][0] = tasks2.get(i).getName();
	        			if (dueDate == null) {
	        				data2[i][1] = "No Task Deadline Set";
	        			}
	        			else {
	        				data2[i][1] = dueDate.format(formatter);
	        			}
	        			data2[i][2] = completionTime.format(formatter);
	        			if (dueDate == null || !completionTime.isAfter(dueDate)) {
	        				data2[i][3] = "Completed on-time";
	        			}
	        			else {
	        				data2[i][3] = "Over deadline";	
	        			}
	        		}
	        		
	        		table = new JTable(data2, columns2);
	        		scrollPane_Caretaker_Performance.setViewportView(table);
	        		break;
	        		
	        	case 2:
	        		Object[] columns3 = {"Caretaker", "Task Name", "Due Date", "Completion Time", "Overdue?"};
	        		List<TaskExecution> tasks3;

						tasks3 = db.getTaskExecutionList().stream()
							.filter(task -> task.getCompletion() != null)
							.collect(Collectors.toList());

	        		Object[][] data3 = new Object[tasks3.size()][columns3.length];
	        		
	        		for (int i = 0; i<tasks3.size(); i++) {
	        			LocalDateTime dueDate = tasks3.get(i).getPeriod().end();
	        			LocalDateTime completionTime = tasks3.get(i).getCompletion().getCompletionTime();
	        			
	        			data3[i][0] = tasks3.get(i).getCompletion().getStaff();
	        			data3[i][1] = tasks3.get(i).getName();
	        			if (dueDate == null) {
	        				data3[i][2] = "No Task Deadline Set";
	        			}
	        			else {
	        				data3[i][2] = dueDate.format(formatter);
	        			}
	        			data3[i][3] = completionTime.format(formatter);
	        			if (dueDate == null || !completionTime.isAfter(dueDate)) {
	        				data3[i][4] = "Completed on-time";
	        			}
	        			else {
	        				data3[i][4] = "Over deadline";	
	        			}
	        		}
	        		table = new JTable(data3, columns3);
	        		scrollPane_Task_Performance.setViewportView(table);
	        		break;
	        	}  
	        }
		};		
		changeTabs.stateChanged(null);
		tabbedPane.addChangeListener(changeTabs);
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
