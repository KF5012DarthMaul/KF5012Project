package kf5012darthmaulapplication;

import java.util.*;

public class PermissionManager {
	
	ArrayList<Permission> Permissions = new ArrayList<>();
	AccountType accountType;
	
	public PermissionManager(PermissionManager.AccountType accountType) {
		this.Permissions = this.generatePermissionsList(accountType);
	}
	
	public enum AccountType {
		SYSADMIN(0b00111),
		HR_PERSONNEL(0b00001),
		MANAGER(0b11100),
		CARETAKER(0b11000);
		
		public final int value;
		private AccountType(int value) {
			this.value = value;
		}
	}
	public enum Permission {
		CREATE_USER,
		CHANGE_USER_PASSWORD,
		ASSIGN_TASK,
		SWAP_TASK,
		GET_TASK;
		
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
	public boolean hasPermission(AccountType accountType, Permission permission) {
		int bitmask = (int) Math.pow(2, permission.ordinal());
		boolean flag = (bitmask & accountType.value) == bitmask;
		return flag;
	}
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
}
