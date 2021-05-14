package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;

import org.junit.Test;


import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import dbmgr.DBExceptions.UserAlreadyExistsException;
import exceptions.UserManagerExceptions.UserAuthenticationFailed;
import exceptions.UserManagerExceptions.UserDoesNotExist;
import kf5012darthmaulapplication.PermissionManager.AccountType;
import kf5012darthmaulapplication.SecurityManager;
import kf5012darthmaulapplication.User;
import kf5012darthmaulapplication.UserManager;

public class TestUserManager {
	static DBAbstraction db;
	
	static String username = "username1";
	static String password = "Password1#";
	static User user = new User(username, AccountType.HR_PERSONNEL);

	static User tempUser = new User("tempUser", AccountType.CARETAKER);
	static User tempUser2 = new User("tempUser2", AccountType.CARETAKER);
	
	UserManager um = new UserManager();
	
	@BeforeClass
	public static void cleanBeforeRun(){
		try {
			db = DBAbstraction.getInstance();
			db.createTables();
			db.deleteUser(user);
		} catch (FailedToConnectException e) {
			System.out.println("Error with TestUserManager BeforeAll Setup function");
			fail(e);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Error with TestUserManager BeforeAll Setup function");
			fail(e);
			e.printStackTrace();

		}
	}
	
	@Before
	public void setup() {
		try {
			String hashedPass = SecurityManager.generatePassword(password);
			db.createUser(user, hashedPass);
		} catch (Exception e) {
			// IF USER ALREADY EXISTS CONTINUE
//			System.out.println("Error with TestUserManager BeforeEach Setup function");
//			fail(e);
//			e.printStackTrace();
		}
		
		if(!db.doesUserExist(tempUser)) {
			try {
				String hashedPass = SecurityManager.generatePassword(password);
				db.createUser(tempUser, hashedPass);
			} catch (UserAlreadyExistsException e) {
				System.out.println("Error with testRemoveUser: User Already exists");
				fail(e);
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Error with testRemoveUser: "+ e);
				fail(e);
				e.printStackTrace();

			}
		}
	}
	
	@After
	public void cleanafter() {
		db.deleteUser(user);
		db.deleteUser(tempUser);
	}
	
	@AfterClass
	public static void cleanall() {
		db.deleteUser(user);
		db.deleteUser(tempUser);
		db.deleteUser(tempUser2);

	}
	
	@Test
	public void testAddUser() {
		try {
			String hashedPass = SecurityManager.generatePassword(password);
			um.addUser(user, tempUser2, hashedPass);
		} catch (UserAuthenticationFailed e) {
			fail("testAddUserFail: User Authenitication Failed");
		} catch (UserAlreadyExistsException e) {
			fail("testAddUserFail: User Already Exists");
		} catch (Exception e) {
			fail("testAddUserFail: "+ e);
		}
	}
	
	@Test
	public void testRemoveUser() {
		try {
			um.removeUser(user, tempUser);
		} catch (UserAuthenticationFailed e) {
			System.out.println("Error with testRemoveUser: User Authentication Failed");
			fail(e);
		} catch(UserDoesNotExist e) {
			System.out.println("Error with testRemoveUser: User Does Not Exists");
			fail(e);
		}
	}
	
	@Test
	public void testEditUserPassword() {
		try {
			String hashedPass = SecurityManager.generatePassword(password);
			um.editUserPassword(user, tempUser, hashedPass);
		} catch (UserAuthenticationFailed e) {
			System.out.println("Error with testEditUserPassword: User Authentication Failed");
			fail(e);
		} catch (UserDoesNotExist e) {
			System.out.println("Error with testRemoveUser: User Does Not Exist");
			fail(e);
		} catch (Exception e) {
			System.out.println("Error with testRemoveUser: "+ e);
			fail(e);
		}
	}
}
