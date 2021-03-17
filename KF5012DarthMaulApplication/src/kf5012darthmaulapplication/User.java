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
	 */
	public User(String username, int permissionNumber){
		if(permissionNumber > (int) (Math.pow(2, permissionsEnum.values().length)) - 1) {
			new ErrorDialog("Given Permission Value exceeds scope", new Error("Permission value too big: Got " + permissionNumber));
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
		System.out.println(permissionNumber);
		String binVal = (Integer.toBinaryString(permissionNumber));
		System.out.println(binVal);
		
//		for(int i = 0; i <= binVal.length() - 1; i++) {
//			System.out.println("Checking Index: " + i);
//			System.out.println("Checking Value: " + binVal.charAt(i));
//			System.out.println("Permission Value: " + permissionsEnum.values()[i]);
//			if(binVal.charAt(i) == '1') {
//				permissions.add(permissionsEnum.values()[i]); 
//			}else continue;
//		}
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
	ArrayList<permissionsEnum> getPermissions(){
		return permissions;
	}
}
