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
import kf5012darthmaulapplication.PermissionManager.AccountType;
import kf5012darthmaulapplication.User;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import java.awt.FlowLayout;
import javax.swing.JButton;
import net.miginfocom.swing.MigLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

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
    JTable tbl_addUsersTable = new JTable() {
		public boolean editCellAt(int row, int column, java.util.EventObject e) {
			return false;
		}
    };
    JTable tbl_removeUsersTable = new JTable() {
		public boolean editCellAt(int row, int column, java.util.EventObject e) {
			return false;
		}
    };
	Object[] tblUsers_columnNames = {"Username", "Roles"};
    DefaultTableModel userTableModel = new DefaultTableModel(
			new Object[][] {}, tblUsers_columnNames
			);
	ArrayList<User> allUsersList;
	private JTextField txt_searchField;
	private JTextField txt_newUserUsername;
		
	ArrayList<User> usersToAddTemp = new ArrayList<User>();
	ArrayList<User> usersToRemoveTemp = new ArrayList<User>();
	
	ArrayList<Action> undoQueue = new ArrayList<>();
	public ManageUsers() {
		try {
			db = DBAbstraction.getInstance();		
		} catch (FailedToConnectException e) {
			e.printStackTrace();
		}
		allUsersList = db.getAllUsers();
		
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
		panel_newUsers.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel_newUsers.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btn_newUserRemoveSelected = new JButton("Remove Selected");
		btn_newUserRemoveSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = tbl_addUsersTable.getSelectedRow();
				if(selectedIndex != -1) {
					undoQueue.add(new undoRemove(usersToAddTemp.get(selectedIndex), usersToAddTemp));
					usersToAddTemp.remove(selectedIndex);
					updateTable(usersToAddTemp, tbl_addUsersTable);
				}
			}
		});
		panel.add(btn_newUserRemoveSelected);
		
		JButton btn_newUsersUndo = new JButton("Undo");
		btn_newUsersUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				if(usersToAddTemp.size() != 0 ) {
//					usersToAddTemp.remove(usersToAddTemp.size() - 1);
//					updateTable(usersToAddTemp,tbl_addUsersTable);
//				}
//				System.out.println("Items in Queue: " + undoQueue.size());
//				for(Action a : undoQueue) {
//					System.out.println(a.getClass().getCanonicalName());
//				}
				if(undoQueue.size() !=0) {
					undoQueue.get(undoQueue.size() - 1).run();
					undoQueue.remove(undoQueue.size() - 1);
				}

				updateTable(usersToAddTemp, tbl_addUsersTable);
			}	
		});
		panel.add(btn_newUsersUndo);
		
		JButton btn_newUsersAddAll = new JButton("Add All");
		panel.add(btn_newUsersAddAll);
		
		JButton btn_newUsersCancel = new JButton("Cancel");
		btn_newUsersCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(usersToAddTemp.size() != 0) {
					int res = JOptionPane.showConfirmDialog(null, "You have unsaved changes, are you sure you want to cancel?","Unsaved Changes", JOptionPane.YES_NO_OPTION);
					if(res == 0) {
						usersToAddTemp.clear();
						tabbedPane.setSelectedIndex(0);
						updateTable(usersToAddTemp, tbl_addUsersTable);
					}
					else {
						
					}
				}else {
					usersToAddTemp.clear();
					tabbedPane.setSelectedIndex(0);
					updateTable(usersToAddTemp, tbl_addUsersTable);
				}
			}
		});
		panel.add(btn_newUsersCancel);
		
		JPanel panel_addUser = new JPanel();
		panel_newUsers.add(panel_addUser, BorderLayout.NORTH);
		panel_addUser.setLayout(new MigLayout("", "[][][grow][]", "[][]"));
		
		JLabel lbl_newUserUsername = new JLabel("Username");
		panel_addUser.add(lbl_newUserUsername, "flowy,cell 1 0,alignx trailing");
		
		txt_newUserUsername = new JTextField();
		panel_addUser.add(txt_newUserUsername, "cell 2 0,growx");
		txt_newUserUsername.setColumns(10);
		
		JLabel lbl_newUserPermission = new JLabel("Role");
		panel_addUser.add(lbl_newUserPermission, "cell 1 1,alignx trailing");
		
		ArrayList<String> stringRoles = new ArrayList<String>();
		for(PermissionManager.AccountType at : PermissionManager.AccountType.values()) {
			stringRoles.add(PermissionManager.AccountTypeToString(at));
		}
		
		JComboBox<?> comboBox_newUserAssignRole = new JComboBox<Object>(stringRoles.toArray());
		panel_addUser.add(comboBox_newUserAssignRole, "cell 2 1,growx");
		
		JButton btn_newUserAddToList = new JButton("Add to list");
		btn_newUserAddToList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = txt_newUserUsername.getText();
				AccountType ac = comboBoxParser(comboBox_newUserAssignRole);
	
				txt_newUserUsername.setText("");
				
				User tempUser = new User(username, ac);
				usersToAddTemp.add(tempUser);
				updateTable(usersToAddTemp,tbl_addUsersTable);
				
				undoQueue.add(new undoAddUser(tempUser, usersToAddTemp));
			}
		});
		panel_addUser.add(btn_newUserAddToList, "cell 3 1");
		
		JScrollPane scrollPane_newUsers = new JScrollPane();
		panel_newUsers.add(scrollPane_newUsers, BorderLayout.CENTER);
		
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
				updateTable(filterListAccountType(allUsersList, comboBoxParser(comboBox_roleTypes)),tbl_viewUsersTable);
			}
		});
		panel_topViewUsers.add(comboBox_roleTypes);
		
		JLabel lbl_search = new JLabel("Search:");
		panel_topViewUsers.add(lbl_search);
		
		txt_searchField = new JTextField();
		txt_searchField.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateTable(filterListUsername(allUsersList, txt_searchField.getText().toString()), tbl_viewUsersTable);
			}
		});
		txt_searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTable(filterListUsername(allUsersList, txt_searchField.getText().toString()), tbl_viewUsersTable);
			}
		});
		panel_topViewUsers.add(txt_searchField);
		txt_searchField.setColumns(10);
		
		tbl_viewUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl_viewUsersTable.setModel(userTableModel);
		scrollPane_tableParent.setViewportView(tbl_viewUsersTable);
		updateTable(filterListAccountType(allUsersList, comboBoxParser(comboBox_roleTypes)), tbl_viewUsersTable);

		
		tbl_addUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl_addUsersTable.setModel(userTableModel);
		updateTable(usersToAddTemp, tbl_addUsersTable);
		scrollPane_newUsers.setViewportView(tbl_addUsersTable);
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
	
	private void updateTable(ArrayList<User> userList, JTable table) {
		for(User u : userList) {
			System.out.println(u.getUsername());
		}
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		
		tableModel.setRowCount(0);
		for(User u : userList) {
			System.out.println("User: " + u.getUsername() + " added");
			tableModel.addRow(new Object[] {u.getUsername(), PermissionManager.AccountTypeToString(u.getAccountType())});
		}
	}
}

interface Action {
	void run();
}

class undoRemove implements Action {
	User user;
	ArrayList<User> list;
	public undoRemove(User user,ArrayList<User> list) {
		this.user = user;
		this.list = list;
	}
	@Override
	public void run() {
		this.list.add(user);
	}	
}
class undoAddUser implements Action {
	User user;
	ArrayList<User> list;
	public undoAddUser(User user, ArrayList<User> list) {
		this.user = user;
		this.list = list;
	}
	@Override
	public void run() {
			this.list.remove(user);
	}
}