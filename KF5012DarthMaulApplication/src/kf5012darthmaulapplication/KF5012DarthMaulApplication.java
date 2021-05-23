package kf5012darthmaulapplication;

import java.security.Security;
import guicomponents.LoginForm;

public class KF5012DarthMaulApplication {
	public static void main(String[] args) {
		try {
			//Set the keysize to unlimited (MAXINT)
			Security.setProperty("crypto.policy", "unlimited");
			
			//Generate a login form and set it to visible
			LoginForm LoginForm = new LoginForm();
			LoginForm.setVisible(true);
			
		}catch(Error e) {
			new ErrorDialog("An Error occured opening the Login Form", e);
			
		}catch(Exception e) {
			new ExceptionDialog("Failed to open applicaiton, maybe you have it already running?");
		}
	}
}
