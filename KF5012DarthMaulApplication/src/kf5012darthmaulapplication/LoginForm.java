package kf5012darthmaulapplication;

import java.awt.BorderLayout;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dbmgr.DBAbstraction;
import dbmgr.DBMgr;

import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JPasswordField;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;

public class LoginForm extends JFrame {
	DBAbstraction db;
	
	private JPanel contentPane;

	private JPanel contentPane;
	DBAbstraction db;
	
	private JPasswordField txt_password;
	private JTextField txt_username;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginForm frame = new LoginForm();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LoginForm() {
		try {
			db = DBAbstraction.getInstance();
		}catch(Exception ex) {
            Logger.getLogger(DBMgr.class.getName()).log(Level.SEVERE, null, ex);
		}
		setType(Type.UTILITY);
		setTitle("Login");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 225, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		Box verticalBox = Box.createVerticalBox();
		panel_1.add(verticalBox);
		
		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);
		
		JLabel lbl_username = new JLabel("Username");
		horizontalBox.add(lbl_username);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut_1);
		
		txt_username = new JTextField();
		horizontalBox.add(txt_username);
		txt_username.setColumns(10);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);
		
		JLabel lbl_password = new JLabel("Password");
		horizontalBox_1.add(lbl_password);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalBox_1.add(horizontalStrut);
		
		txt_password = new JPasswordField();
		horizontalBox_1.add(txt_password);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut_1);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_2);
		
		JLabel lbl_loginmessage = new JLabel("");
		horizontalBox_2.add(lbl_loginmessage);
		
		
		JButton btn_login = new JButton("Login");
		btn_login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = txt_username.getText();
				String password = new String(txt_password.getPassword());
				
				if(db.doesUserExist(username)) {
					System.out.println("Username Exists");
					try {
						if(SecurityManager.validatePassword(password, db.getHashedPassword(username))) {
							User authorisedUser = new User(username, PermissionManager.AccountType.SYSADMIN);
							MainWindow MainWindow = new MainWindow(authorisedUser);
							MainWindow.setVisible(true);
							dispose();
						}else {
							System.out.println("Password Fail");

							new ErrorDialog("Incorrect username or password");
						}
					} catch (Exception ex) {
						new ErrorDialog("Error with username/password function");
					}
				}else {
					System.out.println("Username does not exist");
					new ErrorDialog("Incorrect username or password");
				}
			}
		});
		panel.add(btn_login);
	}
}
