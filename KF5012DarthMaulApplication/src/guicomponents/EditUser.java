package guicomponents;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import dbmgr.DBExceptions.UserDoesNotExistException;
import kf5012darthmaulapplication.User;
import kf5012darthmaulapplication.UserManager;

import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Insets;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import kf5012darthmaulapplication.SecurityManager;

@SuppressWarnings("serial")
public class EditUser extends JFrame {

	private JPanel contentPane;
	private JPasswordField passfield_resetPassword;

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					EditUser frame = new EditUser();
//					frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the frame.
	 */
	DBAbstraction db;
	UserManager um = new UserManager();
	public EditUser(User user) {
		try {
			db = DBAbstraction.getInstance();
		} catch (FailedToConnectException e1) {
			e1.printStackTrace();
		}

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		
		JLabel lbl_editUserTitle = new JLabel("Edit User");
		panel.add(lbl_editUserTitle);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lbl_editUserResetPassword = new JLabel("Reset Password");
		GridBagConstraints gbc_lbl_editUserResetPassword = new GridBagConstraints();
		gbc_lbl_editUserResetPassword.anchor = GridBagConstraints.EAST;
		gbc_lbl_editUserResetPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_editUserResetPassword.gridx = 1;
		gbc_lbl_editUserResetPassword.gridy = 1;
		panel_1.add(lbl_editUserResetPassword, gbc_lbl_editUserResetPassword);
		
		passfield_resetPassword = new JPasswordField();
		GridBagConstraints gbc_passfield_resetPassword = new GridBagConstraints();
		gbc_passfield_resetPassword.insets = new Insets(0, 0, 5, 5);
		gbc_passfield_resetPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_passfield_resetPassword.gridx = 2;
		gbc_passfield_resetPassword.gridy = 1;
		panel_1.add(passfield_resetPassword, gbc_passfield_resetPassword);
		passfield_resetPassword.setEchoChar((char)0);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
		gbc_horizontalGlue.insets = new Insets(0, 0, 5, 0);
		gbc_horizontalGlue.gridx = 3;
		gbc_horizontalGlue.gridy = 1;
		panel_1.add(horizontalGlue, gbc_horizontalGlue);
		
		JButton btn_editUserGeneratePassword = new JButton("Generate New Password");
		btn_editUserGeneratePassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				passfield_resetPassword.setText(SecurityManager.generateRandomPasswordString());
			}
		});
		GridBagConstraints gbc_btn_editUserGeneratePassword = new GridBagConstraints();
		gbc_btn_editUserGeneratePassword.anchor = GridBagConstraints.NORTHWEST;
		gbc_btn_editUserGeneratePassword.insets = new Insets(0, 0, 0, 5);
		gbc_btn_editUserGeneratePassword.gridx = 2;
		gbc_btn_editUserGeneratePassword.gridy = 2;
		panel_1.add(btn_editUserGeneratePassword, gbc_btn_editUserGeneratePassword);
		
		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3, BorderLayout.SOUTH);
		
		JButton btn_editUserApply = new JButton("Apply");
		btn_editUserApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(SecurityManager.passwordStrengthValidator(passfield_resetPassword.getPassword())) {
					try {
						db.setHashedPassword(user, SecurityManager.generatePassword(passfield_resetPassword.getPassword().toString()));
						dispose();
					} catch (UserDoesNotExistException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		panel_3.add(btn_editUserApply);
		
		JButton btn_editUserCancel = new JButton("Cancel");
		btn_editUserCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		panel_3.add(btn_editUserCancel);
	}

}
