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
	ArrayList<permissionsEnum> permissions = new ArrayList<>();
	
	/**
	 * Permissions available to be given to a user object	
	 */
	public enum permissionsEnum {
		SYSADMIN, //0001
		HR, //0010
		JANITOR; // 0100
	}
	/**
	 * Constructor for User, Takes permission number and gives user an ArrayList of Permissions from an Enum
	 * @param username
	 * @param permissionNumber
	 * @throws Exception 
	 */
	public User(String username, int permissionNumber) throws Exception{
		if(permissionNumber > (int) (Math.pow(2, permissionsEnum.values().length)) - 1) {
			throw new Exception("Permission Value too big, got: " + permissionNumber);
		}
		
		this.username = username;
		generatePermissionList(permissionNumber);
	}
	/**
	 * Takes an integer number, converts it to a Binary string then the individual places of the binary number as boolean values to assign as a permission
	 * @param permissionNumber
	 */
	private void generatePermissionList(int permissionNumber) {
		PermissionManager pm = new PermissionManager();
		for(int i = 0; i < permissionsEnum.values().length; i++) {
			if(pm.hasPermission(i, permissionNumber)) {
				permissions.add(permissionsEnum.values()[i]);
			};
		}
	}
	/**
	 * Gets an ArrayList of permissions the User has
	 * @return ArrayList<User.permissionsEnum>
	 */
	public ArrayList<permissionsEnum> getPermissions(){
		return permissions;
	}
	public String getUsername() {
		return this.username;
	}
}
