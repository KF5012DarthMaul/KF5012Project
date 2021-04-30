package kf5012darthmaulapplication;

import java.security.Security;
import org.bouncycastle.jce.provider.*;

import dbmgr.DBAbstraction;

public class KF5012DarthMaulApplication {
	public static void main(String[] args) {
		try {
			
			//Generate Temp User
			DBAbstraction db;
			db = DBAbstraction.getInstance();
			if(!db.doesUserExist("test")) {
				db.createUser("test", SecurityManager.generatePassword("password"), PermissionManager.AccountType.SYSADMIN.value);

			}			
			Security.addProvider(new BouncyCastleProvider());
			Security.setProperty("crypto.policy", "unlimited");
			
			SecurityManager sm = new SecurityManager();
			
			LoginForm LoginForm = new LoginForm();
			LoginForm.setVisible(true);
		}catch(Error e) {
			new ErrorDialog("An Error occured opening the Login Form", e);
		}catch(Exception e) {
			new ExceptionDialog(e.getMessage());
		}
	}
}
