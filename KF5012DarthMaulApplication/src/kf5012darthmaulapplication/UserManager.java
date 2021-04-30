package kf5012darthmaulapplication;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import dbmgr.DBExceptions.UserDoesNotExistException;

import exceptions.UserManagerExceptions;
import exceptions.UserManagerExceptions.UserAuthenticationFailed;

public class UserManager {
	static DBAbstraction db;
	UserManager(){
		try {
			db = DBAbstraction.getInstance();
			} catch (FailedToConnectException e) {
			new ErrorDialog("Database initialisation failed... try again later");
			e.printStackTrace();
		}
	}
	
	private static boolean verifyAuthorisedUser(User authorisedUser) {
		boolean localFlag = false;
		boolean databaseFlag = false;
		boolean userFlag = true; 
		if(!authorisedUser.pm.hasPermission(PermissionManager.Permission.MANAGE_USERS)) localFlag = true;
		try {
			if(db.getPermissions(authorisedUser).pm.hasPermission(PermissionManager.Permission.MANAGE_USERS)) databaseFlag = true;
			return false;
		} catch (UserDoesNotExistException e) {
			userFlag = false;
		}
		return localFlag && databaseFlag && userFlag;
	}
	/**
	 * Takes in the User Currently logged in as the AuthorisedUser and then takes a User Object for the new user.
	 * Gets the information from the newUser and adds it to the user database
	 * @param authorisedUser
	 * @param newUser
	 * @throws UserAuthenticationFailed
	 */
	
	public static void addUser(User authorisedUser, User newUser) throws UserAuthenticationFailed {
		if(verifyAuthorisedUser(authorisedUser)) {
			
		}else {
			throw new UserManagerExceptions.UserAuthenticationFailed();
		}
	}
	/**
	 * Takes in the User Currently logged in as the AuthorisedUser and then takes a User Object for the new user.
	 * Takes the username string and an int value for account type and adds it to the user database
	 * @param authorisedUser
	 * @param newUserName
	 * @param newAccountValue
	 * @throws UserAuthenticationFailed
	 */
	public static void addUser(User authorisedUser, String newUserName, int newAccountValue) throws UserAuthenticationFailed {
		addUser(authorisedUser, new User(newUserName, PermissionManager.intToAccountType(newAccountValue)));
	}
	/**
	 * Takes in the User Currently logged in as the AuthorisedUser and then takes a User Object for the new user.
	 * Taks a String username and a PermissionManager.AccountType and adds that to the database
	 * @param authorisedUser
	 * @param newUserName
	 * @param newUserAccountType
	 * @throws UserAuthenticationFailed
	 */
	public static void addUser(User authorisedUser, String newUserName, PermissionManager.AccountType newUserAccountType) throws UserAuthenticationFailed{
		addUser(authorisedUser, new User(newUserName, newUserAccountType));
	}
	/**
	 * Removes a user from the user table
	 * @param authorisedUser
	 * @param user
	 * @throws UserAuthenticationFailed
	 */
	public static void removeUser(User authorisedUser,User user) throws UserAuthenticationFailed {
		if(verifyAuthorisedUser(authorisedUser)) {
			
		}else {
			throw new UserManagerExceptions.UserAuthenticationFailed();
		}

		
	}
	/**
	 * Removes a user from the user table
	 * @param authorisedUser
	 * @param name
	 * @throws UserAuthenticationFailed
	 */
	public static void removeUser(User authorisedUser,String name) throws UserAuthenticationFailed {
		try {
			removeUser(authorisedUser, db.getUser(name));
		} catch (UserDoesNotExistException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Edits a Users password (AuthorisedUser)
	 * Edits a Users own password (Generic User)
	 * @param authorisedUser
	 * @param user
	 * @throws UserAuthenticationFailed
	 */
	public static void editUserPassword(User authorisedUser, User user, String newHashedPassword) throws UserAuthenticationFailed {
		if(verifyAuthorisedUser(authorisedUser) || authorisedUser.equals(user)) {
			try {
				db.setHashedPassword(user, newHashedPassword);
			} catch (UserDoesNotExistException e) {
				e.printStackTrace();
			}
		}else {
			throw new UserManagerExceptions.UserAuthenticationFailed();
		}

		
	}
}
