package guicomponents;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import kf5012darthmaulapplication.*;

import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					MainWindow frame = new MainWindow(new User("Default", 0));
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
	public MainWindow(User user) {
		setResizable(false);
		boolean bingBangBongYourSecurityIsGone = true;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 830, 595);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		if(bingBangBongYourSecurityIsGone || user.pm.hasPermission(PermissionManager.Permission.MANAGE_ACCOUNT)) {
			tabbedPane.addTab("My Account", null, new ManageAccount(user, this) , null);
		}
		
		if(bingBangBongYourSecurityIsGone || user.pm.hasPermission(PermissionManager.Permission.MANAGE_ALLOCATION )) {
			tabbedPane.addTab("Allocations", null, new ManageAllocation()  , null);
		}
		
		if(bingBangBongYourSecurityIsGone || user.pm.hasPermission(PermissionManager.Permission.MANAGE_TASKS)) {
			tabbedPane.addTab("Tasks", null, new ManageTasks(), null);
		}
		
		if(bingBangBongYourSecurityIsGone || user.pm.hasPermission(PermissionManager.Permission.MANAGE_USERS)){
			tabbedPane.addTab("Users", null, new ManageUsers(user), null);
		}
		
		if(bingBangBongYourSecurityIsGone || user.pm.hasPermission(PermissionManager.Permission.VIEW_REPORTS)){
			tabbedPane.addTab("Reports", null, new ViewReports(), null);
		}
	}

}
