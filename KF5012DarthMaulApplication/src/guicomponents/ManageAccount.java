package guicomponents;

import javax.swing.JPanel;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import dbmgr.DBExceptions.UserDoesNotExistException;
import exceptions.UserManagerExceptions.UserAuthenticationFailed;
import kf5012darthmaulapplication.ErrorDialog;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.SecurityManager;
import kf5012darthmaulapplication.User;
import kf5012darthmaulapplication.UserManager;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JTextField;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

@SuppressWarnings("serial")
public class ManageAccount extends JPanel {
	private JPasswordField passfield_old;
	private JPasswordField passfield_new;
	private JPasswordField passfield_newConfirm;
	DBAbstraction db;
	UserManager UserManager = new UserManager();
	private JTextField txt_username;
	private JTextField txt_role;
	private JTextField txt_displayname;
	private JTextField txt_newDisplayName;

	/**
	 * Create the panel.
	 */
	public ManageAccount() {
		setMinimumSize(new Dimension(640, 400));
		try {
			db = DBAbstraction.getInstance();
		} catch (FailedToConnectException exception) {
			new ErrorDialog("Failed to make database connection");
		}
                User user = MainWindow.getCurrentUser();
		setLayout(new GridLayout(0, 1, 0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);
		
		JPanel panel_information = new JPanel();
		tabbedPane.addTab("My Information", null, panel_information, null);
		panel_information.setLayout(new MigLayout("", "[][][][grow][][grow]", "[][][][][][][][][][][][]"));
		
		JLabel lbl_username = new JLabel("Username");
		panel_information.add(lbl_username, "cell 1 1,alignx right");
		
		txt_username = new JTextField();
		txt_username.setEditable(false);
		txt_username.setText(user.getUsername());
		panel_information.add(txt_username, "cell 3 1,growx");
		txt_username.setColumns(10);
		
		JLabel lbl_displayname = new JLabel("Display Name");
		panel_information.add(lbl_displayname, "cell 1 2,alignx right");
		
		txt_displayname = new JTextField();
		txt_displayname.setText(user.getDisplayName());
		txt_displayname.setEditable(false);
		txt_displayname.setColumns(10);
		panel_information.add(txt_displayname, "cell 3 2,growx");
		
		JLabel lbl_role = new JLabel("Role");
		panel_information.add(lbl_role, "cell 1 3,alignx right");
		
		txt_role = new JTextField();
		txt_role.setEditable(false);
		txt_role.setText(PermissionManager.AccountTypeToString(user.getAccountType()));
		panel_information.add(txt_role, "cell 3 3,growx");
		txt_role.setColumns(10);
		
		JButton btn_logout = new JButton("Logout");
		btn_logout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.disposeWindow();
				LoginForm loginForm = new LoginForm();
				loginForm.setVisible(true);
			}
		});
		
		panel_information.add(btn_logout, "cell 1 11");
		
		JPanel panel_password = new JPanel();
		tabbedPane.addTab("Change Password", null, panel_password, null);
		panel_password.setLayout(new MigLayout("", "[][][][200.00,grow]", "[][][][][][][]"));
		
		JLabel lbl_oldPassword = new JLabel("Old Password");
		panel_password.add(lbl_oldPassword, "cell 1 1,alignx right");
		
		passfield_old = new JPasswordField();
		panel_password.add(passfield_old, "cell 3 1,growx,aligny baseline");
		
		JLabel lbl_newPassword = new JLabel("New Password");
		panel_password.add(lbl_newPassword, "cell 1 3,alignx right");
		
		passfield_new = new JPasswordField();
		panel_password.add(passfield_new, "cell 3 3,growx");
		
		JLabel lbl_newPasswordConfirm = new JLabel("Confirm Password");
		panel_password.add(lbl_newPasswordConfirm, "cell 1 4,alignx right");
		
		passfield_newConfirm = new JPasswordField();
		panel_password.add(passfield_newConfirm, "cell 3 4,growx");
		
		JCheckBox chkbx_showPassword = new JCheckBox("Show Password");
		chkbx_showPassword.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				boolean isChecked = chkbx_showPassword.isSelected();
				if(isChecked) {
					passfield_new.setEchoChar((char)0);
					passfield_newConfirm.setEchoChar((char)0);
				}else {
					passfield_new.setEchoChar('*');
					passfield_newConfirm.setEchoChar('*');
				}

			}
		});
		panel_password.add(chkbx_showPassword, "flowx,cell 3 5");
		
		JButton btn_generateRandomPassword = new JButton("Generate a Password");
		btn_generateRandomPassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkbx_showPassword.setSelected(true);
				passfield_new.setText(SecurityManager.generateRandomPasswordString());
			}
		});
		panel_password.add(btn_generateRandomPassword, "cell 3 5,alignx left");
		
		JButton btn_changePassword = new JButton("Save");
		btn_changePassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Verify old password
					try {
						if(SecurityManager.validatePassword(new String(passfield_old.getPassword()), db.getHashedPassword(user))) {
							//Old password verified
							if(new String(passfield_new.getPassword()).equals(new String(passfield_newConfirm.getPassword()))) {
								//Password field and Confirmation field match
								SecurityManager.generatePassword(new String(passfield_new.getPassword()));
								try {
									if(SecurityManager.passwordStrengthValidator(passfield_new.getPassword())) {
										if(UserManager.editUserPassword(user, user, SecurityManager.generatePassword(new String(passfield_new.getPassword())))) {
											//If the password was edited
											JOptionPane.showConfirmDialog(null, "Password successfully updated","Password updated", JOptionPane.DEFAULT_OPTION);
										}else {
											new ExceptionDialog("Failed to update password, try again later.");
											clearFields();
										}
									}else {
										clearFields();
									}
								} catch (UserAuthenticationFailed ex ) {
									new ErrorDialog("You are not authorised to edit this password");
									clearFields();
								}
							}else {
								JOptionPane.showConfirmDialog(null, "Password did not match","Password update failed", JOptionPane.DEFAULT_OPTION);
								clearFields();
							}
						}else {
							//Old password failed
							JOptionPane.showConfirmDialog(null, "Old password incorrect, try again","Failed to update password",JOptionPane.DEFAULT_OPTION);
							clearFields();
						}
					} catch (UserDoesNotExistException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
			private void clearFields() {
				passfield_old.setText(null);
				passfield_new.setText(null);
				passfield_newConfirm.setText(null);
			}
		});
		panel_password.add(btn_changePassword, "cell 3 5,alignx right");
		
		JTabbedPane tabbedPane_changeDisplayName = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Change Display name", null, tabbedPane_changeDisplayName, null);
		
		JPanel panel = new JPanel();
		tabbedPane_changeDisplayName.addTab("New tab", null, panel, null);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lbl_newDisplayName = new JLabel("New Display Name");
		GridBagConstraints gbc_lbl_newDisplayName = new GridBagConstraints();
		gbc_lbl_newDisplayName.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_newDisplayName.gridx = 2;
		gbc_lbl_newDisplayName.gridy = 1;
		panel.add(lbl_newDisplayName, gbc_lbl_newDisplayName);
		
		txt_newDisplayName = new JTextField();
		GridBagConstraints gbc_txt_newDisplayName = new GridBagConstraints();
		gbc_txt_newDisplayName.insets = new Insets(0, 0, 5, 5);
		gbc_txt_newDisplayName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txt_newDisplayName.gridx = 4;
		gbc_txt_newDisplayName.gridy = 1;
		panel.add(txt_newDisplayName, gbc_txt_newDisplayName);
		txt_newDisplayName.setColumns(10);
		
		JButton btn_applyNewDisplayName = new JButton("Apply");
		btn_applyNewDisplayName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newDisplayName = txt_newDisplayName.getText();
				if(newDisplayName.length() <= 0) return;
                                    user.setDisplayName(newDisplayName);
				try {
					db.updateUser(user);
				} catch (UserDoesNotExistException e1) {
					e1.printStackTrace();
				}
				txt_displayname.setText(newDisplayName);
			}
		});
		GridBagConstraints gbc_btn_applyNewDisplayName = new GridBagConstraints();
		gbc_btn_applyNewDisplayName.anchor = GridBagConstraints.WEST;
		gbc_btn_applyNewDisplayName.insets = new Insets(0, 0, 5, 5);
		gbc_btn_applyNewDisplayName.gridx = 4;
		gbc_btn_applyNewDisplayName.gridy = 3;
		panel.add(btn_applyNewDisplayName, gbc_btn_applyNewDisplayName);

	}

}
