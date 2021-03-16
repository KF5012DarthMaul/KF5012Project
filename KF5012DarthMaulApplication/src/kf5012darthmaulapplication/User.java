package kf5012darthmaulapplication;

import java.util.ArrayList;

public class User {
	String username;
	int encodedPermission;
	ArrayList<permissionsEnum> permissions = new ArrayList<>();
	
	enum permissionsEnum {
		JANITOR,
		HR,
		SYSADMIN
	}
	
	public User(String username, int permissionNumber) {
		this.username = username;
		generatePermissionList(permissionNumber);
	}
	
	private void generatePermissionList(int permissionNumber) {
		char[] binArr = (Integer.toBinaryString(permissionNumber)).toCharArray();
		for(int i = binArr.length; i >= 0; i--) {
			if(binArr[i] == '1') {
				permissions.add(permissionsEnum.values()[i]); 
			}else continue;
		}
	}
	ArrayList<permissionsEnum> getPermissions(){
		return permissions;
	}
}
