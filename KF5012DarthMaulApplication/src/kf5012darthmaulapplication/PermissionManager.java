package kf5012darthmaulapplication;

import java.util.*;

public class PermissionManager {
	
	ArrayList<Permission> Permissions = new ArrayList<>();
	AccountType accountType;
	
	public PermissionManager(PermissionManager.AccountType accountType) {
		this.Permissions = this.generatePermissionsList(accountType);
	}
	public PermissionManager() {}
	
	public enum AccountType {
		HR_PERSONNEL(0b00011),
		MANAGER(0b10110),
		CARETAKER(0b01110),
		ESTATE(0b00110);
		
		public final int value;
		private AccountType(int value) {
			this.value = value;
		}
	}
	public enum Permission {
		MANAGE_USERS,
		MANAGE_ACCOUNT,
		MANAGE_TASKS,
		MANAGE_ALLOCATION,
		VIEW_REPORTS;
		
		private final int bitmask;
		private Permission() {
			this.bitmask = (int) Math.pow(2, this.ordinal());
		}
	}
	
	public AccountType getAccountType(int x) {
		return AccountType.values()[x];
	}
	public ArrayList<Permission> getPermissions(){
		return Permissions;
	}
	/**
	 * Returns True|False based on a PermissionManager.Permission and PermissionManager.AccountType
	 * 
	 * @param accountType
	 * @param permission
	 * @return boolean
	 */
	public boolean hasPermission(AccountType accountType, Permission permission) {
		int bitmask = (int) Math.pow(2, permission.ordinal());
		boolean flag = (bitmask & accountType.value) == bitmask;
		return flag;
	}
	/**
	 * 
	 * @param accountType
	 * @param permission
	 * @return
	 */
	public boolean hasPermission(Permission permission) {
			boolean flag = false;
			for(Permission p : this.Permissions) {
				if(p.equals(permission)) flag = true;
			}
			return flag;
	}
	/**
	 * Generates an ArrayList of all the permissions a PermissionManager.AccountType can have. takes a PermissionManager.AccountType as an input
	 *
	 * @param Permission.AccountType
	 * @return ArrayList<Permission>
	 */
	public ArrayList<Permission> generatePermissionsList(AccountType AccountType){
		int permissionNumber = AccountType.value;
		ArrayList<Permission> permissionList = new ArrayList<>();
		for(Permission p : Permission.values()) {
			if((p.bitmask & permissionNumber) == p.bitmask) {
				permissionList.add(p);
			}else continue;
		}
		return permissionList;
	}
	/**
	 * Takes an Integer and returns an AccountType Enum Value
	 * 
	 * @param Integer permission value
	 * @return Enum PermissionManager.AccountType
	 */
	public static AccountType intToAccountType(int x) {
		return AccountType.values()[x];
	}
}
