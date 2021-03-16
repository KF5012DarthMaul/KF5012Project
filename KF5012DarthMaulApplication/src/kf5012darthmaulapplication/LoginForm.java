package kf5012darthmaulapplication;

import java.awt.BorderLayout;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dbmgr.*;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import net.miginfocom.swing.MigLayout;
import javax.swing.SpringLayout;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.Box;
import javax.swing.JPasswordField;
import java.awt.Component;
import java.awt.Window.Type;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoginForm extends JFrame {

	private JPanel contentPane;
	DBAbstraction db = new DBAbstraction();
	
	int loginAttempts = 0;
	int loginAttemptsPermitted = 3;
	
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
				String password = txt_password.getPassword().toString();
				if(loginAttempts >= loginAttemptsPermitted) {
					lbl_loginmessage.setText("Exeeded Maximum Attempts");
					new ErrorDialog("Exeeded Maximum Login Attempts, Please try again later");
					txt_username.setEnabled(false);
					txt_password.setEnabled(false);
					btn_login.setEnabled(false);
					return;
				}
				if(!db.doesUserExist(username)) {
					loginAttempts++;
					lbl_loginmessage.setText("Username or Password Invalid");
				}
			}
		});
		panel.add(btn_login);

	}
}
