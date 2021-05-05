package kf5012darthmaulapplication;

import java.security.Security;
import org.bouncycastle.jce.provider.*;

import dbmgr.DBAbstraction;
import guicomponents.LoginForm;

public class KF5012DarthMaulApplication {
	public static void main(String[] args) {
		try {
			
			//Generate Temp User
			DBAbstraction db;
			db = DBAbstraction.getInstance();
			
			System.out.println(db.getUser("test").getAccountType());
			if(!db.doesUserExist("test")) {
				db.createUser("test", SecurityManager.generatePassword("password"), PermissionManager.AccountType.HR_PERSONNEL.ordinal());
				System.out.println("TEST: User created fresh");
			}else {
				db.deleteUser(new User("test", PermissionManager.AccountType.HR_PERSONNEL));
				db.createUser("test", SecurityManager.generatePassword("password"), PermissionManager.AccountType.HR_PERSONNEL.ordinal());
				System.out.println("TEST: User deleted then created again");
			}
			Security.addProvider(new BouncyCastleProvider());
			Security.setProperty("crypto.policy", "unlimited");
			
			LoginForm LoginForm = new LoginForm();
			LoginForm.setVisible(true);
		}catch(Error e) {
			new ErrorDialog("An Error occured opening the Login Form", e);
		}catch(Exception e) {
			new ExceptionDialog(e.getMessage());
		}
	}
}
