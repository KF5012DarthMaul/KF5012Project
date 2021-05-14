package guicomponents;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.bouncycastle.util.Arrays.Iterator;

import dbmgr.DBAbstraction;

import dbmgr.DBExceptions.FailedToConnectException;
import dbmgr.DBExceptions.UserAlreadyExistsException;
import kf5012darthmaulapplication.ErrorDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.PermissionManager.AccountType;
import kf5012darthmaulapplication.User;
import kf5012darthmaulapplication.SecurityManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import java.awt.FlowLayout;
import javax.swing.JButton;
import net.miginfocom.swing.MigLayout;
import javax.swing.JPasswordField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

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
    JTable tbl_removeUsersSelected = new JTable() {
		public boolean editCellAt(int row, int column, java.util.EventObject e) {
			return false;
		}
    };
    JTable tbl_removeUsersAll = new JTable() {
		public boolean editCellAt(int row, int column, java.util.EventObject e) {
			return false;
		}
    };
	Object[] tblUsers_columnNames = {"Username", "Roles"};
    DefaultTableModel userTableModel = new DefaultTableModel(
				new Object[][] {}, tblUsers_columnNames
			);
    DefaultTableModel newUserTableModel = new DefaultTableModel(
    			new Object[][] {}, tblUsers_columnNames
    		);
    DefaultTableModel removeUsersTableModelSelected = new DefaultTableModel(
				new Object[][] {}, tblUsers_columnNames
    		);
    DefaultTableModel removeUsersTableModelAll = new DefaultTableModel(
				new Object[][] {}, tblUsers_columnNames
    		);
	ArrayList<User> allUsersList;
	private JTextField txt_searchField;
	private JTextField txt_newUserUsername;
		
	ArrayList<User> usersToAdd = new ArrayList<User>();
	HashMap<User, String> passwordMap = new HashMap<User,String>();
		
	ArrayList<Action> undoQueue = new ArrayList<>();
	
	
	ArrayList<User> removeTableUsers = new ArrayList<User>();
	ArrayList<User> usersToRemove = new ArrayList<User>();
	
	private JPasswordField passfield_newUser;
	public ManageUsers(User selfUser) {
		try {
			db = DBAbstraction.getInstance();		
		} catch (FailedToConnectException e) {
			e.printStackTrace();
		}
		allUsersList = db.getAllUsers();
		removeTableUsers = listWithoutSelf( db.getAllUsers(), selfUser);
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);
		
		JPanel panel_editUsers = new JPanel();
		tabbedPane.addTab("View Users", null, panel_editUsers, null);
		panel_editUsers.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_middleViewUsers = new JPanel();
		panel_editUsers.add(panel_middleViewUsers, BorderLayout.CENTER);
		
		JScrollPane scrollPane_tableParent = new JScrollPane();
		panel_middleViewUsers.add(scrollPane_tableParent);
		
		JPanel panel_newUsers = new JPanel();
		tabbedPane.addTab("Add Users", null, panel_newUsers, null);
		panel_newUsers.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_newUsersActions = new JPanel();
		panel_newUsers.add(panel_newUsersActions, BorderLayout.SOUTH);
		panel_newUsersActions.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btn_newUserRemoveSelected = new JButton("Remove Selected");
		btn_newUserRemoveSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = tbl_addUsersTable.getSelectedRow();
				if(selectedIndex != -1) {
					String hashPass = null;
					try {
						hashPass = SecurityManager.generatePassword(passfield_newUser.getPassword().toString());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					if(hashPass != null) {
						undoQueue.add(new UndoRemove(usersToAdd.get(selectedIndex), usersToAdd, passwordMap, passwordMap.get(usersToAdd.get(selectedIndex))));
						usersToAdd.remove(selectedIndex);
						updateTable(usersToAdd, tbl_addUsersTable);
					}
				}
			}
		});
		panel_newUsersActions.add(btn_newUserRemoveSelected);
		
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

				updateTable(usersToAdd, tbl_addUsersTable);
			}	
		});
		panel_newUsersActions.add(btn_newUsersUndo);
		
		JButton btn_newUsersAddAll = new JButton("Add All");
		btn_newUsersAddAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(usersToAdd.size() == 0) return; 
				for(User u : usersToAdd) {
					try {
						db.createUser(u, passwordMap.get(u));
					} catch (UserAlreadyExistsException e1) {
						e1.printStackTrace();
					}
				}
				JOptionPane.showConfirmDialog(null, "Added " + usersToAdd.size() + " users.","Added Users", JOptionPane.OK_OPTION);
				usersToAdd.clear();
				updateTable(usersToAdd, tbl_addUsersTable);
				allUsersList = db.getAllUsers();
				updateTable(allUsersList, tbl_viewUsersTable);
				undoQueue.clear();
				removeTableUsers = listWithoutSelf(db.getAllUsers(), selfUser);
				updateTable(removeTableUsers, tbl_removeUsersAll);
				tabbedPane.setSelectedIndex(0);
			}
		});
		panel_newUsersActions.add(btn_newUsersAddAll);
		
		JButton btn_newUsersCancel = new JButton("Cancel");
		btn_newUsersCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(usersToAdd.size() != 0) {
					int res = JOptionPane.showConfirmDialog(null, "You have unsaved changes, are you sure you want to cancel?","Unsaved Changes", JOptionPane.YES_NO_OPTION);
					if(res == 0) {
						usersToAdd.clear();
						tabbedPane.setSelectedIndex(0);
						updateTable(usersToAdd, tbl_addUsersTable);
					}
				}else {
					usersToAdd.clear();
					tabbedPane.setSelectedIndex(0);
					updateTable(usersToAdd, tbl_addUsersTable);
				}
			}
		});
		panel_newUsersActions.add(btn_newUsersCancel);
		
		JPanel panel_addUser = new JPanel();
		panel_newUsers.add(panel_addUser, BorderLayout.NORTH);
		panel_addUser.setLayout(new MigLayout("", "[][][grow][]", "[][][]"));
		
		JLabel lbl_newUserUsername = new JLabel("Username");
		panel_addUser.add(lbl_newUserUsername, "flowy,cell 1 0,alignx trailing");
		
		txt_newUserUsername = new JTextField();
		panel_addUser.add(txt_newUserUsername, "cell 2 0,growx");
		txt_newUserUsername.setColumns(10);
		
		JLabel lbl_addUsersPassword = new JLabel("Password");
		panel_addUser.add(lbl_addUsersPassword, "cell 1 1,alignx trailing");
		
		passfield_newUser = new JPasswordField();
		panel_addUser.add(passfield_newUser, "cell 2 1,growx");
		passfield_newUser.setEchoChar((char) 0);

		
		JButton btn_newUsersGeneratePassword = new JButton("Generate");
		btn_newUsersGeneratePassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				passfield_newUser.setText(SecurityManager.generateRandomPasswordString());
			}
		});
		panel_addUser.add(btn_newUsersGeneratePassword, "cell 3 1,growx");
		
		JLabel lbl_newUserPermission = new JLabel("Role");
		panel_addUser.add(lbl_newUserPermission, "cell 1 2,alignx trailing");
		
		ArrayList<String> stringRoles = new ArrayList<String>();
		for(PermissionManager.AccountType at : PermissionManager.AccountType.values()) {
			stringRoles.add(PermissionManager.AccountTypeToString(at));
		}
		
		JComboBox<?> comboBox_newUserAssignRole = new JComboBox<Object>(stringRoles.toArray());
		panel_addUser.add(comboBox_newUserAssignRole, "cell 2 2,growx");
		
		JButton btn_newUserAddToList = new JButton("Add to list");
		btn_newUserAddToList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = txt_newUserUsername.getText();
				if(!db.doesUserExist(username) || findUsernameInTable(usersToAdd, username)) {

					AccountType ac = comboBoxParser(comboBox_newUserAssignRole);
					
					if(SecurityManager.passwordStrengthValidator(passfield_newUser.getPassword())) {
						try {
							txt_newUserUsername.setText("");								
							User tempUser = new User(username, ac);
							usersToAdd.add(tempUser);
							passwordMap.put(tempUser , SecurityManager.generatePassword(passfield_newUser.getPassword().toString()));
							updateTable(usersToAdd,tbl_addUsersTable);						
							undoQueue.add(new UndoAdd(tempUser, usersToAdd, passwordMap, passwordMap.get(tempUser)));
						} catch (Exception e1) {
							new ErrorDialog("Error adding user to list: " + e1);
						}
					}
				}else {
					new ErrorDialog("User with this username already exists");
				}
				passfield_newUser.setText(null);
			}
		});
		panel_addUser.add(btn_newUserAddToList, "cell 3 2,growx");
		
		JScrollPane scrollPane_newUsers = new JScrollPane();
		panel_newUsers.add(scrollPane_newUsers, BorderLayout.CENTER);
		
		JPanel panel_removeUsers = new JPanel();
		tabbedPane.addTab("Remove Users", null, panel_removeUsers, null);
		panel_removeUsers.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_removeUsersTop = new JPanel();
		panel_removeUsers.add(panel_removeUsersTop, BorderLayout.NORTH);
		
		JPanel panel_removeUsersBottom = new JPanel();
		panel_removeUsers.add(panel_removeUsersBottom, BorderLayout.SOUTH);
		
		JButton btn_removeUsersAddSelected = new JButton("Add Selected");
		btn_removeUsersAddSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = tbl_removeUsersAll.getSelectedRow();
				if(selectedIndex != -1) {
					usersToRemove.add(removeTableUsers.get(selectedIndex));
					removeTableUsers.remove(selectedIndex);
					
					updateTable(removeTableUsers, tbl_removeUsersAll);
					updateTable(usersToRemove, tbl_removeUsersSelected);
				}
			}
		});
		panel_removeUsersBottom.add(btn_removeUsersAddSelected);
		
		JButton btn_removeSelected = new JButton("Apply");
		btn_removeSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(User u : usersToRemove) {
					db.deleteUser(u);
				}
				int res = JOptionPane.showConfirmDialog(null, "This Action will remove " + usersToRemove.size() + " users, Are you sure?","Removed Users", JOptionPane.OK_OPTION);
				if(res == JOptionPane.YES_OPTION) {
					usersToRemove.clear();
					tabbedPane.setSelectedIndex(0);
					updateTable(listWithoutSelf(db.getAllUsers(), selfUser), tbl_removeUsersAll);
					updateTable(usersToRemove, tbl_removeUsersSelected);
					updateTable(db.getAllUsers(), tbl_viewUsersTable);
				}else {
					return;
				}
			}
		});
		panel_removeUsersBottom.add(btn_removeSelected);
		
		JButton btn_removeUsersCancel = new JButton("Cancel");
		btn_removeUsersCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPane.setSelectedIndex(0);
				removeTableUsers.addAll(usersToRemove);
				usersToRemove.clear();
				updateTable(removeTableUsers, tbl_removeUsersAll);
				updateTable(usersToRemove, tbl_removeUsersSelected);
			}
		});
		panel_removeUsersBottom.add(btn_removeUsersCancel);
		
		JButton btn_removeUsersRemoveSelected = new JButton("Undo Selected");
		btn_removeUsersRemoveSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = tbl_removeUsersSelected.getSelectedRow();
				if(selectedIndex != -1) {
					removeTableUsers.add(usersToRemove.get(selectedIndex));
					usersToRemove.remove(selectedIndex);
					
					updateTable(removeTableUsers, tbl_removeUsersAll);
					updateTable(usersToRemove, tbl_removeUsersSelected);
				}
			}
		});
		panel_removeUsersBottom.add(btn_removeUsersRemoveSelected);
		
		JPanel panel_removeUsersMiddle = new JPanel();
		panel_removeUsers.add(panel_removeUsersMiddle, BorderLayout.CENTER);
		GridBagLayout gbl_panel_removeUsersMiddle = new GridBagLayout();
		gbl_panel_removeUsersMiddle.columnWidths = new int[]{0, 0, 0};
		gbl_panel_removeUsersMiddle.rowHeights = new int[]{0, 0};
		gbl_panel_removeUsersMiddle.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_removeUsersMiddle.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_removeUsersMiddle.setLayout(gbl_panel_removeUsersMiddle);
		
		JScrollPane scrollPane_removeUserLeft = new JScrollPane();
		GridBagConstraints gbc_scrollPane_removeUserLeft = new GridBagConstraints();
		gbc_scrollPane_removeUserLeft.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_removeUserLeft.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_removeUserLeft.gridx = 0;
		gbc_scrollPane_removeUserLeft.gridy = 0;
		panel_removeUsersMiddle.add(scrollPane_removeUserLeft, gbc_scrollPane_removeUserLeft);
		
		JScrollPane scrollPane_removeUserRight = new JScrollPane();
		GridBagConstraints gbc_scrollPane_removeUserRight = new GridBagConstraints();
		gbc_scrollPane_removeUserRight.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_removeUserRight.gridx = 1;
		gbc_scrollPane_removeUserRight.gridy = 0;
		panel_removeUsersMiddle.add(scrollPane_removeUserRight, gbc_scrollPane_removeUserRight);
						
		
		JPanel panel_topViewUsers = new JPanel();
		panel_editUsers.add(panel_topViewUsers, BorderLayout.NORTH);
		
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
		
		JPanel panel_bottomViewUsers = new JPanel();
		panel_editUsers.add(panel_bottomViewUsers, BorderLayout.SOUTH);
		
		JButton btn_viewUsersEditSelected = new JButton("Edit Selected");
		btn_viewUsersEditSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = tbl_viewUsersTable.getSelectedRow();
				if(selectedIndex != -1) {
					User userToEdit = allUsersList.get(selectedIndex);
					
					JFrame editUser = new EditUser(userToEdit);
					editUser.setVisible(true);
				}

			}
		});
		panel_bottomViewUsers.add(btn_viewUsersEditSelected);
		
		tbl_addUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl_addUsersTable.setModel(newUserTableModel);
		scrollPane_newUsers.setViewportView(tbl_addUsersTable);
		updateTable(usersToAdd, tbl_addUsersTable);
		
		tbl_removeUsersAll.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl_removeUsersAll.setModel(removeUsersTableModelAll);
		scrollPane_removeUserLeft.setViewportView(tbl_removeUsersAll);
		updateTable(removeTableUsers, tbl_removeUsersAll);
		
		tbl_removeUsersSelected.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl_removeUsersSelected.setModel(removeUsersTableModelSelected);
		scrollPane_removeUserRight.setViewportView(tbl_removeUsersSelected);
		
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
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		AbstractTableModel aTableModel = (AbstractTableModel) table.getModel();
		tableModel.setRowCount(0);
		for(User u : userList) {
			tableModel.addRow(new Object[] {u.getUsername(), PermissionManager.AccountTypeToString(u.getAccountType())});
		}
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				aTableModel.fireTableDataChanged();
			}
		});
	}
	private boolean findUsernameInTable(ArrayList<User> userList, String username) {
		for(User c : userList) {
			if(c.getUsername().equals(username)) return true;
		}
		return false;
	}
	private ArrayList<User> listWithoutSelf(ArrayList<User> userList, User self){
		ArrayList<User> newUserList = new ArrayList<User>();
		for(User u : userList) {
			if(u.getUsername().equals(self.getUsername())) continue;
			newUserList.add(u);
		}
		return newUserList;
	};
}

interface Action {
	void run();
}

class UndoRemove implements Action {
	User user;
	ArrayList<User> list;
	HashMap<User, String> passwordMap;
	String hashedPassword;
	public UndoRemove(User user,ArrayList<User> list, HashMap<User, String> passwordMap, String hashedPassword) {
		this.user = user;
		this.list = list;
		this.passwordMap = passwordMap;
		this.hashedPassword = hashedPassword;
		
	}
	@Override
	public void run() {
		this.list.add(user);
		this.passwordMap.put(user, hashedPassword);
	}	
}
class UndoAdd implements Action {
	User user;
	ArrayList<User> list;
	HashMap<User, String> passwordMap;
	String hashedPassword;
	public UndoAdd(User user, ArrayList<User> list, HashMap<User, String> passwordMap,String hashedPassword) {
		this.user = user;
		this.list = list;
		this.passwordMap = passwordMap;
		this.hashedPassword = hashedPassword;
	}
	@Override
	public void run() {
			this.list.remove(user);
			this.passwordMap.remove(user);
	}
}