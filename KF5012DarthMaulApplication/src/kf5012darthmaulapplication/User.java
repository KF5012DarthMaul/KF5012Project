package kf5012darthmaulapplication;

import java.util.ArrayList;

/**
 * 
 * @author Scrub
 *
 */

public class User {
	String username;
	int encodedPermission;
	PermissionManager pm;
	PermissionManager.AccountType accountType;
	ArrayList<PermissionManager.Permission> Permissions = new ArrayList<>();
	
	/**
	 * @param username
	 * @param accountType
	 * @throws Exception
	 */
	public User(String username, PermissionManager.AccountType accountType) throws Exception{
		this.username = username;
		this.accountType = accountType;
		pm = new PermissionManager(accountType);
	}
	/**
	 * Gets an ArrayList of permissions the User has
	 * @return ArrayList<User.permissionsEnum>
	 */
	public String getUsername() {
		return this.username;
	}
	public ArrayList<PermissionManager.Permission> getPermissions(){
		return pm.getPermissions();
	}
}
