package kf5012darthmaulapplication;

import java.security.Security;
import org.bouncycastle.jce.provider.*;

import dbmgr.DBAbstraction;
import guicomponents.LoginForm;

public class KF5012DarthMaulApplication {
	public static void main(String[] args) {
		try {
			Security.addProvider(new BouncyCastleProvider());
			Security.setProperty("crypto.policy", "unlimited");
			
			LoginForm LoginForm = new LoginForm();
			LoginForm.setVisible(true);
			
		}catch(Error e) {
			new ErrorDialog("An Error occured opening the Login Form", e);
			
		}catch(Exception e) {
			new ExceptionDialog("Failed to open applicaiton, maybe you have it already running?");
		}
	}
}
