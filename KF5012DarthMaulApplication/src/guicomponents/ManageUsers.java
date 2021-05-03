package guicomponents;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import dbmgr.DBAbstraction;

import dbmgr.DBExceptions.FailedToConnectException;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.User;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;


@SuppressWarnings("serial")
public class ManageUsers extends JPanel {

	/**
	 * Create the panel.
	 */
	DBAbstraction db = null;
	JTable tbl_viewUsersTable = new JTable() {
		public boolean editCellAt(int row, int column, java.util.EventObject e) {
			return false;
		}
     };
	Object[] tblUsers_columnNames = {"Username", "Roles"};
	ArrayList<User> allUsersList;
	private JTextField txt_searchField;
	
	public ManageUsers() {
		try {
			db = DBAbstraction.getInstance();
			allUsersList = db.getAllUsers();			
		} catch (FailedToConnectException e) {
			e.printStackTrace();
		}
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);
		
		JPanel panel_viewUsers = new JPanel();
		tabbedPane.addTab("View Users", null, panel_viewUsers, null);
		panel_viewUsers.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_middleViewUsers = new JPanel();
		panel_viewUsers.add(panel_middleViewUsers, BorderLayout.CENTER);
		
		JScrollPane scrollPane_tableParent = new JScrollPane();
		panel_middleViewUsers.add(scrollPane_tableParent);
		
		JPanel panel_newUsers = new JPanel();
		tabbedPane.addTab("Add Users", null, panel_newUsers, null);
		
		JPanel panel_removeUsers = new JPanel();
		tabbedPane.addTab("Remove Users", null, panel_removeUsers, null);
						
		
		JPanel panel_topViewUsers = new JPanel();
		panel_viewUsers.add(panel_topViewUsers, BorderLayout.NORTH);
		
		JLabel lbl_filter = new JLabel("Filter:");
		panel_topViewUsers.add(lbl_filter);
		
		ArrayList<String> rolesAvailable = new ArrayList<String>();
		rolesAvailable.add("All");
		for(PermissionManager.AccountType at : PermissionManager.AccountType.values()) {
			rolesAvailable.add(PermissionManager.AccountTypeToString(at));
		}
		JComboBox<?> comboBox_roleTypes = new JComboBox<Object>(rolesAvailable.toArray());
		comboBox_roleTypes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTable(filterListAccountType(allUsersList, comboBoxParser(comboBox_roleTypes)));
			}
		});
		panel_topViewUsers.add(comboBox_roleTypes);
		
		JLabel lbl_search = new JLabel("Search:");
		panel_topViewUsers.add(lbl_search);
		
		txt_searchField = new JTextField();
		txt_searchField.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateTable(filterListUsername(allUsersList, txt_searchField.getText().toString()));
			}
		});
		txt_searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTable(filterListUsername(allUsersList, txt_searchField.getText().toString()));
			}
		});
		panel_topViewUsers.add(txt_searchField);
		txt_searchField.setColumns(10);
		
		tbl_viewUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl_viewUsersTable.setModel(new DefaultTableModel(
				new Object[][] {}, tblUsers_columnNames
				));
		updateTable(allUsersList);
		for(User x : allUsersList) {
			System.out.println(x.getUsername());
		}
		scrollPane_tableParent.setViewportView(tbl_viewUsersTable);
	}
	
	private PermissionManager.AccountType comboBoxParser(JComboBox<?> combo) {
		String selected = combo.getItemAt(combo.getSelectedIndex()).toString().toLowerCase();
		if(selected == "all") return null;
		else {
			switch (selected) {
			case "human resources":
				return PermissionManager.AccountType.HR_PERSONNEL;
			case "manager":
				return PermissionManager.AccountType.MANAGER;
			case "caretaker":
				return PermissionManager.AccountType.CARETAKER;
			case "estate":
				return PermissionManager.AccountType.ESTATE;
			}
		}
		return null;
	}
	private ArrayList<User> filterListAccountType (ArrayList<User> fullList, PermissionManager.AccountType filter){
		if(filter == null) return fullList;
		return (ArrayList<User>) fullList.stream().filter(u -> u.getAccountType() == filter).collect(Collectors.toList());
	}
	private ArrayList<User> filterListUsername(ArrayList<User> fullList, String filter){
		if(filter == "" || filter == null) return fullList;
		return (ArrayList<User>) fullList.stream().filter(u -> u.getUsername().toLowerCase().contains(filter)).collect(Collectors.toList());
	};
	
	private void updateTable(ArrayList<User> userList) {
		DefaultTableModel tableModel = (DefaultTableModel) tbl_viewUsersTable.getModel();
		tableModel.setRowCount(0);
		for(User u : userList) {
			tableModel.addRow(new Object[] {u.getUsername(), PermissionManager.AccountTypeToString(u.getAccountType())});
		}
	}
}
