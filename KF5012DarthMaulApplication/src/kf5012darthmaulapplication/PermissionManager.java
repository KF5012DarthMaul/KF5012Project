package kf5012darthmaulapplication;

import kf5012darthmaulapplication.User.permissionsEnum;

public class PermissionManager {
	permissionsEnum enumerator;
	public PermissionManager() {
		
	}
	
	boolean hasPermission(int enumIndex, int permissionNumber) {
		int permissionVal = 1 << enumIndex;
		return (permissionNumber & permissionVal) == permissionVal ;
	}
}
