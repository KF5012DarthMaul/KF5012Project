package kf5012darthmaulapplication;

/**
 * 
 * @author Scrub
 *
 */

public class User {
	String username;
	int encodedPermission;
	public PermissionManager pm;
	PermissionManager.AccountType accountType;
	
	/**
	 * @param username
	 * @param accountType
	 * @throws Exception
	 */
	public User(String username, PermissionManager.AccountType accountType){
		this.username = username;
		this.accountType = accountType;
		pm = new PermissionManager(this.accountType);
	}
	/**
	 * Gets an ArrayList of permissions the User has
	 * @return ArrayList<User.permissionsEnum>
	 */
	public String getUsername() {
		return this.username;
	}
}
