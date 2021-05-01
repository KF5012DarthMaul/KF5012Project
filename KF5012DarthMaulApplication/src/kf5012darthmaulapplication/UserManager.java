package kf5012darthmaulapplication;

import dbmgr.DBAbstraction;
import dbmgr.DBExceptions.FailedToConnectException;
import dbmgr.DBExceptions.UserAlreadyExistsException;
import dbmgr.DBExceptions.UserDoesNotExistException;

import exceptions.UserManagerExceptions;
import exceptions.UserManagerExceptions.UserAuthenticationFailed;
import exceptions.UserManagerExceptions.UserDoesNotExist;

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
	 * @throws UserAlreadyExists 
	 * @throws UserAlreadyExistsException 
	 */
	
	public static void addUser(User authorisedUser, User newUser, String hashedPassword) throws UserAuthenticationFailed, UserAlreadyExistsException {
		if(verifyAuthorisedUser(authorisedUser)) {
			if(!db.createUser(newUser, hashedPassword)) throw new UserAlreadyExistsException();
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
	 * @throws UserAlreadyExists 
	 * @throws UserAlreadyExistsException 
	 */
	public static void addUser(User authorisedUser, String newUserName, int newAccountValue, String hashedPassword) throws UserAuthenticationFailed, UserAlreadyExistsException {
		addUser(authorisedUser, new User(newUserName, PermissionManager.intToAccountType(newAccountValue)), hashedPassword);
	}
	/**
	 * Takes in the User Currently logged in as the AuthorisedUser and then takes a User Object for the new user.
	 * Taks a String username and a PermissionManager.AccountType and adds that to the database
	 * @param authorisedUser
	 * @param newUserName
	 * @param newUserAccountType
	 * @throws UserAuthenticationFailed
	 * @throws UserAlreadyExists 
	 * @throws UserAlreadyExistsException 
	 */
	public static void addUser(User authorisedUser, String newUserName, PermissionManager.AccountType newUserAccountType, String hashedPassword) throws UserAuthenticationFailed, UserAlreadyExistsException{
		addUser(authorisedUser, new User(newUserName, newUserAccountType), hashedPassword);
	}
	/**
	 * Removes a user from the user table
	 * @param authorisedUser
	 * @param user
	 * @throws UserAuthenticationFailed
	 * @throws UserDoesNotExist 
	 */
	public static void removeUser(User authorisedUser,User user) throws UserAuthenticationFailed, UserDoesNotExist {
		if(verifyAuthorisedUser(authorisedUser)) {
			if(!db.deleteUser(user)) throw new UserManagerExceptions.UserDoesNotExist();
		}else {
			throw new UserManagerExceptions.UserAuthenticationFailed();
		}	
	}
	/**
	 * Removes a user from the user table
	 * @param authorisedUser
	 * @param name
	 * @throws UserAuthenticationFailed
	 * @throws UserDoesNotExist 
	 */
	public static void removeUser(User authorisedUser,String name) throws UserAuthenticationFailed, UserDoesNotExist {
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
