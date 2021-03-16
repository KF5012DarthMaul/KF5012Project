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
	enum permissionsEnum {
		JANITOR,
		HR,
		SYSADMIN
	}
	/**
	 * Constructor for User, Takes permission number and gives user an ArrayList of Permissions from an Enum
	 * @param username
	 * @param permissionNumber
	 */
	public User(String username, int permissionNumber){
		if(permissionNumber > (int) (Math.pow(2, permissionsEnum.values().length)) - 1) {
			new ErrorDialog("Given Permission Value exceeds scope", new Error("Permission value too big: Got" + permissionNumber));
			return;
		}
		
		this.username = username;
		generatePermissionList(permissionNumber);
	}
	/**
	 * Takes an integer number, converts it to a Binary string then the individual places of the binary number as boolean values to assign as a permission
	 * @param permissionNumber
	 */
	private void generatePermissionList(int permissionNumber) {
		char[] binArr = (Integer.toBinaryString(permissionNumber)).toCharArray();
		for(int i = binArr.length; i >= 0; i--) {
			if(binArr[i] == '1') {
				permissions.add(permissionsEnum.values()[i]); 
			}else continue;
		}
	}
	/**
	 * Gets an ArrayList of permissions the User has
	 * @return ArrayList<User.permissionsEnum>
	 */
	ArrayList<permissionsEnum> getPermissions(){
		return permissions;
	}
}
