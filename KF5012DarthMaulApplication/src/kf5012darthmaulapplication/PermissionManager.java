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
		HR_PERSONNEL(0b000011),
		MANAGER(0b110110),
		CARETAKER(0b001110),
		ESTATE(0b000110),
                SU(0b111111);
		
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
		VIEW_REPORTS,
                REMOVE_TASKS;
		
		private final int bitmask;
		private Permission() {
			this.bitmask = 1 << this.ordinal(); // 1, 2, 4, 8, 16, etc...
		}
	}
	
	public static AccountType getAccountType(int x) {
		if(x >= AccountType.values().length || x < 0) return null;
		else return AccountType.values()[x];
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
	public static boolean hasPermission(AccountType accountType, Permission permission) {
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
	/**
	 * Convers an AccountType to integer value
	 * @param type
	 * @return int
	 */
	public static int accountTypetoInt(AccountType type) {
		return type.ordinal();
	}
	/**
	 * Given an account type enum, will return the String verion
	 * @param type
	 * @return
	 */
	public static String AccountTypeToString(AccountType type) {
		String result = "";
		switch(type) {
		case CARETAKER:
			result = "Caretaker";
			break;
		case ESTATE:
			result = "Estate";
			break;
		case HR_PERSONNEL:
			result = "Human Resources";
			break;
		case MANAGER:
			result = "Manager";
			break;
		default:
			result = "No role found!";
			break;
		}
		return result;
	}
}
