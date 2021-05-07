package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import dbmgr.DBExceptions.UserAlreadyExistsException;
import dbmgr.DBExceptions.UserDoesNotExistException;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.PermissionManager.AccountType;
import kf5012darthmaulapplication.User;

class TestDatabase {
	
	static DBAbstraction db;
	static String username = "tempUser1";
	static String password = "asdasd";
	static AccountType accType = AccountType.CARETAKER;
	static User tempUser1 = new User("tempUser1", accType);
	
	@BeforeAll
	static public void getConnection() {
		try {
			db = DBAbstraction.getInstance();
			db.createUser(username,password, PermissionManager.accountTypetoInt(accType));
		} catch (FailedToConnectException | UserAlreadyExistsException e) {
			fail(e);
		}
	}
	
	@AfterAll
	static public void cleanup() {
		db.deleteUser(tempUser1);
	}
	
	@Test
	public void testDoesUserExist() {
		if(!db.doesUserExist(tempUser1)) fail();
	}
	
	@Test
	public void testDoesUserNotExist() {
		if(db.doesUserExist("FAKEUSER")) fail();
	}
	
	@Test
	public void testGetHashedPassword() {
		try {
			String expected = password;
			String actual = db.getHashedPassword(tempUser1);
			assertEquals(expected, actual);
		} catch (UserDoesNotExistException e) {
			fail(e);
		}
	}
	
	@Test
	public void testSetHashedPassword() {
		try {
			db.setHashedPassword(tempUser1, password);
		} catch (UserDoesNotExistException e) {
			fail(e);
		}
	}
	
	@Test
	public void testGetPermissions() {
		try {
			int actual = db.getPermissions(tempUser1.getUsername());
			System.out.println();
			int expected = PermissionManager.accountTypetoInt(accType);
			assertEquals(expected,actual);
		} catch (UserDoesNotExistException e) {
			fail(e);
		}
	}
	
	@Test
	public void testGetUser() {
		try {
			User actual = db.getUser(username);
			assertEquals(tempUser1.getUsername(), actual.getUsername());
			assertEquals(tempUser1.getAccountType(), actual.getAccountType());
			assertEquals(tempUser1.pm.getPermissions(), actual.pm.getPermissions());
		} catch (UserDoesNotExistException e) {
			fail(e);
		}
	}

}
