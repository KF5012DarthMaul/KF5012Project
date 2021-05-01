package guicomponents;

import javax.swing.JPanel;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import dbmgr.DBExceptions.UserDoesNotExistException;
import kf5012darthmaulapplication.ErrorDialog;
import kf5012darthmaulapplication.SecurityManager;
import kf5012darthmaulapplication.User;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ManageAccount extends JPanel {
	private JPasswordField passfield_old;
	private JPasswordField passfield_new;
	private JPasswordField passfield_newConfirm;
	DBAbstraction db;

	/**
	 * Create the panel.
	 */
	public ManageAccount(User user) {
		try {
			db = DBAbstraction.getInstance();
		} catch (FailedToConnectException exception) {
			new ErrorDialog("Failed to make database connection");
		}
		
		
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panel_information = new JPanel();
		tabbedPane.addTab("My Information", null, panel_information, null);
		
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
		
		JButton btn_changePassword = new JButton("Save");
		btn_changePassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Verify old password
					try {
						if(SecurityManager.validatePassword(new String(passfield_old.getPassword()), db.getHashedPassword(user))) {
							//Old password verified
						}else {
							//Old password failed
							JOptionPane.showConfirmDialog(null, "Old password incorrect, try again","Failed to update password",JOptionPane.DEFAULT_OPTION);
							passfield_old.setText(null);
							passfield_new.setText(null);
							passfield_newConfirm.setText(null);
						}
					} catch (UserDoesNotExistException e1) {
						e1.printStackTrace();
					}
				
				// Verify new password is strong enough
				
				// Verify password == passwordConfirm
			}
		});
		panel_password.add(btn_changePassword, "cell 3 6,alignx right");

	}

}
