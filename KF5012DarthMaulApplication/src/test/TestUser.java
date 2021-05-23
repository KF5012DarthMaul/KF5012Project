package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

import kf5012darthmaulapplication.*;
import kf5012darthmaulapplication.PermissionManager.AccountType;

import java.util.ArrayList;
import java.util.Random;

public class TestUser {
	
	@Test
	public void testUsernameMatches() {
		String username = "username";
		User user = new User(username,username,PermissionManager.AccountType.CARETAKER);
		assertEquals(user.getUsername(),username);
	}
	
	@Test
	public void testAccountTypeMatches() {
		PermissionManager.AccountType[] accounts = PermissionManager.AccountType.values();
		PermissionManager.AccountType accountType = accounts[(int) Math.floor(Math.random() * accounts.length)];
		User user = new User("test", "test", accountType);
		assertEquals(user.getAccountType(), accountType);
	}

	
	@Test
	public void testCARETAKEPermissionAssignment() {
		ArrayList<PermissionManager.Permission> expectedPermissions = new ArrayList<>();
		expectedPermissions.add(PermissionManager.Permission.MANAGE_ACCOUNT);
		expectedPermissions.add(PermissionManager.Permission.MANAGE_TASKS);
		expectedPermissions.add(PermissionManager.Permission.MANAGE_ALLOCATION);
		try {
			User user = new User("test", "test", PermissionManager.AccountType.CARETAKER);
			ArrayList<PermissionManager.Permission> actualPermissions = user.pm.getPermissions();
			
			assertEquals(expectedPermissions, actualPermissions);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	
	@Test
	public void testMANAGERPermissionAssignment() {
		ArrayList<PermissionManager.Permission> expectedPermissions = new ArrayList<>();
		expectedPermissions.add(PermissionManager.Permission.MANAGE_ACCOUNT);
		expectedPermissions.add(PermissionManager.Permission.MANAGE_TASKS);
		expectedPermissions.add(PermissionManager.Permission.VIEW_REPORTS);
		expectedPermissions.add(PermissionManager.Permission.REMOVE_TASKS);

		try {
			User user = new User("test","test", PermissionManager.AccountType.MANAGER);
			ArrayList<PermissionManager.Permission> actualPermissions = user.pm.getPermissions();
			
			assertEquals(expectedPermissions,actualPermissions);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	
	@Test
	public void testHRPERSONNELPermissionAssignment() {
		ArrayList<PermissionManager.Permission> expectedPermissions = new ArrayList<>();
		expectedPermissions.add(PermissionManager.Permission.MANAGE_USERS);
		expectedPermissions.add(PermissionManager.Permission.MANAGE_ACCOUNT);
		try {
			User user = new User("test","test", PermissionManager.AccountType.HR_PERSONNEL);
			ArrayList<PermissionManager.Permission> actualPermissions = user.pm.getPermissions();
			
			assertEquals(expectedPermissions,actualPermissions);
		}catch(Exception ex) {
			fail("User Generation failed: " + ex.getMessage());
		}
	}
	
	@Test
	public void testUserPermissionCheck() {
		User user = new User("test","test",AccountType.ESTATE);
		assertTrue(user.pm.hasPermission(PermissionManager.Permission.MANAGE_TASKS));
	}
	
	@Test
	public void testIntToAccountType() {
		Random rand = new Random();
		
		int x = rand.nextInt(PermissionManager.AccountType.values().length);
		AccountType expectedAccount = PermissionManager.AccountType.values()[x];
		AccountType actualAccount = PermissionManager.intToAccountType(x);
		
		assertEquals(expectedAccount, actualAccount);
	}
	
	@Test
	public void testAccountTypeDoesNotHavePermission() {
		AccountType ac = PermissionManager.AccountType.HR_PERSONNEL;
		assertFalse(PermissionManager.hasPermission(ac, PermissionManager.Permission.VIEW_REPORTS));
	}
	
	@Test
	public void testAccountTypeDoesHavePermission() {
		AccountType ac = PermissionManager.AccountType.HR_PERSONNEL;
		assertTrue(PermissionManager.hasPermission(ac, PermissionManager.Permission.MANAGE_USERS));
	}
	
	@Test
	public void testAUsereDoesNotHavePermission() {
		User user = new User("test", "test",AccountType.HR_PERSONNEL);
		assertFalse(user.pm.hasPermission(PermissionManager.Permission.VIEW_REPORTS));
	}
	
	@Test
	public void testUserDoesHavePermission() {
		User user = new User("test", "test", AccountType.HR_PERSONNEL);
		assertTrue(user.pm.hasPermission(PermissionManager.Permission.MANAGE_USERS));
	}
	
	@Test
	public void testGetAccountType() {
		assertNull(PermissionManager.getAccountType(-1));
		assertNull(PermissionManager.getAccountType(AccountType.values().length));
		assertNull(PermissionManager.getAccountType(Integer.MAX_VALUE));
		assertNull(PermissionManager.getAccountType(Integer.MIN_VALUE));
		assertEquals(AccountType.HR_PERSONNEL, PermissionManager.getAccountType(0));
		assertEquals(AccountType.SU, PermissionManager.getAccountType(AccountType.values().length - 1));
	}
	
}