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
        private static User currentUser;
        private static MainWindow instance;
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
	public MainWindow() {
                if(instance != null)
                    disposeWindow();
                instance = this;
		setResizable(false);
		boolean bingBangBongYourSecurityIsGone = false;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 830, 595);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		if(bingBangBongYourSecurityIsGone || currentUser.pm.hasPermission(PermissionManager.Permission.MANAGE_ACCOUNT)) {
			tabbedPane.addTab("My Account", null, new ManageAccount() , null);
		}
		
		if(bingBangBongYourSecurityIsGone || currentUser.pm.hasPermission(PermissionManager.Permission.MANAGE_ALLOCATION )) {
			tabbedPane.addTab("Allocations", null, new ManageAllocation()  , null);
		}
		
		if(bingBangBongYourSecurityIsGone || currentUser.pm.hasPermission(PermissionManager.Permission.MANAGE_TASKS)) {
			tabbedPane.addTab("Tasks", null, new ManageTasks(), null);
		}
		
		if(bingBangBongYourSecurityIsGone || currentUser.pm.hasPermission(PermissionManager.Permission.MANAGE_USERS)){
			tabbedPane.addTab("Users", null, new ManageUsers(), null);
		}
		
		if(bingBangBongYourSecurityIsGone || currentUser.pm.hasPermission(PermissionManager.Permission.VIEW_REPORTS)){
			tabbedPane.addTab("Reports", null, new ViewReports(), null);
		}
	}
        public static User getCurrentUser()
        {
            return currentUser;
        }
        
        public static void disposeWindow()
        {
            instance.dispose();
        }
        public static void setUser(User user)
        {
            currentUser = user;
        }
}
