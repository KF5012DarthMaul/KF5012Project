package kf5012darthmaulapplication;

import dbmgr.DBAbstraction;
import dbmgr.InitialiseDB;
import java.security.Security;
import guicomponents.LoginForm;
import java.util.stream.Collectors;
import kf5012darthmaulapplication.PermissionManager.AccountType;

public class KF5012DarthMaulApplication {
	public static void main(String[] args) {
		try {
			//Set the keysize to unlimited (MAXINT)
			Security.setProperty("crypto.policy", "unlimited");
                        
			DBAbstraction db = DBAbstraction.getInstance();
                        // Default user
                        User inituser = new User("default", "Default Initial User", AccountType.INITUSER);
                        var userList = db.getAllUsers();
                        boolean hasHR = !userList.stream().filter(u -> u.accountType == AccountType.HR_PERSONNEL).collect(Collectors.toList()).isEmpty();
                        // Check if there are any users in the system, if there are none, that could mean the DB is uninitialized
                        if(userList.isEmpty()) 
                        {
                            // Initiliaze the DB
                            InitialiseDB initdb = new InitialiseDB();
                            initdb.dropTables();
                            initdb.createTables();
                        }
                        // Check if there are any HR users registered in the DB
                        if(!hasHR)
                        {
                            // If there aren't, create the default user if it doesnt exist already
                            if(!db.doesUserExist("default"))
                                db.createUser(inituser, SecurityManager.generatePassword("northumbria"));
                            else // If it exists, reset its password to northumbria
                                db.setHashedPassword(db.getUser("default"), SecurityManager.generatePassword("northumbria"));
                        }
                        else if(db.doesUserExist("default")) // if there are HR users, delete the default user if it exists
                        {
                            db.deleteUser(inituser);
                        }
                        inituser = null;
			//Generate a login form and set it to visible
			LoginForm LoginForm = new LoginForm();
			LoginForm.setVisible(true);
			
		}catch(Error e) {
			new ErrorDialog("An Error occured opening the Login Form", e);
			
		}catch(Exception e) {
			new ExceptionDialog("Failed to open applicaiton, maybe you have it already running?");
		}
	}
}
