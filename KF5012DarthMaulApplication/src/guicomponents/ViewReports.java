package guicomponents;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions;
import dbmgr.DBExceptions.FailedToConnectException;

import javax.swing.JButton;
import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;  

import domain.TaskExecution;
import kf5012darthmaulapplication.ExceptionDialog;

public class ViewReports extends JPanel {
	private JTable table;

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
		
		JScrollPane scrollPane_1 = new JScrollPane();
		report1.add(scrollPane_1);
					
		JPanel report2 = new JPanel();
		tabbedPane.addTab("Caretaker Performance", report2);
		
		JPanel report3 = new JPanel();
		tabbedPane.addTab("Task Performance", report3);
		
		DBAbstraction db;
		try {
			db = DBAbstraction.getInstance();
		}
		catch (DBExceptions.FailedToConnectException ex) {
			new ExceptionDialog("An error has occured with the database", ex);
			return;
		}
		
		ChangeListener changeTabs = new ChangeListener() {
			private final DateTimeFormatter formatter =
					DateTimeFormatter.ofPattern("h:mma d/M/yyyy");
	        public void stateChanged(ChangeEvent e) {
	        	int tabIndex = tabbedPane.getSelectedIndex();
	        	switch (tabIndex) {
	        	case 0:
	        		Object[] columns = {"Task Name", "Allocated Caretaker", "Due Date"};
	        		
	        		List<TaskExecution> tasks = db.getTaskStatus();
	        		Object[][] data = new Object[tasks.size()][columns.length];
	        		
	        		for (int i = 0; i<tasks.size(); i++) {
	        			data[i][0] = tasks.get(i).getName();
	        			data[i][1] = tasks.get(i).getAllocation().getUsername();
	        			data[i][2] = tasks.get(i).getPeriod().end().format(formatter);
	        		}
	        		
	        		table = new JTable(data, columns);
	        		scrollPane_1.setViewportView(table);	        		
	        		break;
	        		
	        	case 1:
	        		//Placeholder
	        		break;
	        		
	        	case 2:
	        		//Placeholder
	        		break;
	        	}  
	        }
		};
		
		changeTabs.stateChanged(null);
		tabbedPane.addChangeListener(changeTabs);
	}

}
