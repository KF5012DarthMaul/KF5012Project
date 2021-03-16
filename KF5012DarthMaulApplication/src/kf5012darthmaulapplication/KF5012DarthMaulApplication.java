package kf5012darthmaulapplication;

public class KF5012DarthMaulApplication {
	public static void main(String[] args) {
		System.out.println("Loading...");
		try {
			LoginForm LoginForm = new LoginForm();
			LoginForm.setVisible(true);
		}catch(Error e) {
			new ErrorDialog("An Error occured opening the Login Form", e);
		}
	}
}
