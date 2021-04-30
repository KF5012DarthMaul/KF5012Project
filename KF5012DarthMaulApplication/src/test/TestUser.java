package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import kf5012darthmaulapplication.*;

import java.util.ArrayList;
class TestUser {

	@Test
	void testUsernameMatches() {
		String username = "username";
		User user = new User(username,PermissionManager.AccountType.CARETAKER);
		assertEquals(user.getUsername(),username);
	}
	@Test
	void testAccountTypeMatches() {
		PermissionManager.AccountType[] accounts = PermissionManager.AccountType.values();
		PermissionManager.AccountType accountType = accounts[(int) Math.floor(Math.random() * accounts.length)];
		User user = new User("test", accountType);
		assertEquals(user.getAccountType(), accountType);
	}
	void testAccountTypeValues() {
		
	}
	/**
	 * Testing Bitwise Permission Assignment
	 * Order:
	 * 	CREATE_USER,
	 *	CHANGE_USER_PASSWORD,
	 *	ASSIGN_TASK,
	 *	SWAP_TASK,
	 *  GET_TASK;
	 * 
	 */
	@Test
	void testCARETAKEPermissionAssignment() {
		ArrayList<PermissionManager.Permission> expectedPermissions = new ArrayList<>();
		expectedPermissions.add(PermissionManager.Permission.SWAP_TASK);
		expectedPermissions.add(PermissionManager.Permission.GET_TASK);
		try {
			User user = new User("test", PermissionManager.AccountType.CARETAKER);
			ArrayList<PermissionManager.Permission> actualPermissions = user.pm.getPermissions();
			
			assertEquals(expectedPermissions, actualPermissions);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	
	@Test
	void testMANAGERPermissionAssignment() {
		ArrayList<PermissionManager.Permission> expectedPermissions = new ArrayList<>();
		expectedPermissions.add(PermissionManager.Permission.ASSIGN_TASK);
		expectedPermissions.add(PermissionManager.Permission.SWAP_TASK);
		expectedPermissions.add(PermissionManager.Permission.GET_TASK);

		try {
			User user = new User("test", PermissionManager.AccountType.MANAGER);
			ArrayList<PermissionManager.Permission> actualPermissions = user.pm.getPermissions();
			
			assertEquals(expectedPermissions,actualPermissions);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	
	@Test
	void testHRPERSONNELPermissionAssignment() {
		ArrayList<PermissionManager.Permission> expectedPermissions = new ArrayList<>();
		expectedPermissions.add(PermissionManager.Permission.CREATE_USER);
		try {
			User user = new User("test", PermissionManager.AccountType.HR_PERSONNEL);
			ArrayList<PermissionManager.Permission> actualPermissions = user.pm.getPermissions();
			
			assertEquals(expectedPermissions,actualPermissions);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	
	@Test
	void testSYSADMINPermissionAssignment() {
		ArrayList<PermissionManager.Permission> expectedPermissions = new ArrayList<>();
		expectedPermissions.add(PermissionManager.Permission.CREATE_USER);
		expectedPermissions.add(PermissionManager.Permission.CHANGE_USER_PASSWORD);
		expectedPermissions.add(PermissionManager.Permission.ASSIGN_TASK);
		expectedPermissions.add(PermissionManager.Permission.SWAP_TASK);
		expectedPermissions.add(PermissionManager.Permission.GIVE_TASKS);

		try {
			User user = new User("test", PermissionManager.AccountType.SYSADMIN);
			ArrayList<PermissionManager.Permission> actualPermissions = user.pm.getPermissions();
			
			assertEquals(expectedPermissions,actualPermissions);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
}