package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import kf5012darthmaulapplication.*;
import kf5012darthmaulapplication.PermissionManager.AccountType;

import java.util.ArrayList;
import java.util.Random;
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

	
	@Test
	void testCARETAKEPermissionAssignment() {
		ArrayList<PermissionManager.Permission> expectedPermissions = new ArrayList<>();
		expectedPermissions.add(PermissionManager.Permission.MANAGE_ACCOUNT);
		expectedPermissions.add(PermissionManager.Permission.MANAGE_TASKS);
		expectedPermissions.add(PermissionManager.Permission.MANAGE_ALLOCATION);
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
		expectedPermissions.add(PermissionManager.Permission.MANAGE_ACCOUNT);
		expectedPermissions.add(PermissionManager.Permission.MANAGE_TASKS);
		expectedPermissions.add(PermissionManager.Permission.VIEW_REPORTS);

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
		expectedPermissions.add(PermissionManager.Permission.MANAGE_USERS);
		expectedPermissions.add(PermissionManager.Permission.MANAGE_ACCOUNT);
		try {
			User user = new User("test", PermissionManager.AccountType.HR_PERSONNEL);
			ArrayList<PermissionManager.Permission> actualPermissions = user.pm.getPermissions();
			
			assertEquals(expectedPermissions,actualPermissions);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	
	@Test
	void testUserPermissionCheck() {
		User user = new User("test",AccountType.ESTATE);
		assertTrue(user.pm.hasPermission(PermissionManager.Permission.MANAGE_TASKS));
	}
	
	@Test
	void testIntToAccountType() {
		Random rand = new Random();
		
		int x = rand.nextInt(PermissionManager.AccountType.values().length);
		AccountType expectedAccount = PermissionManager.AccountType.values()[x];
		AccountType actualAccount = PermissionManager.intToAccountType(x);
		
		assertEquals(expectedAccount, actualAccount);
	}
}