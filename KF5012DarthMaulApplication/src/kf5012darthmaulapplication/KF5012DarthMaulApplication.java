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
                        // Init the DB
			DBAbstraction db = DBAbstraction.getInstance();
                        User inituser = new User("default", "Default Initial User", AccountType.INITUSER);
                        var userList = db.getAllUsers();
                        if(userList.isEmpty())
                        {
                            InitialiseDB initdb = new InitialiseDB();
                            initdb.dropTables();
                            initdb.createTables();
                        }
                        boolean hasHR = !userList.stream().filter(u -> u.accountType == AccountType.HR_PERSONNEL).collect(Collectors.toList()).isEmpty();
                        if(!hasHR)
                        {
                            if(!db.doesUserExist("default"))
                                db.createUser(inituser, SecurityManager.generatePassword("northumbria"));
                            else
                                db.setHashedPassword(db.getUser("default"), SecurityManager.generatePassword("northumbria"));
                        }
                        else if(db.doesUserExist("default"))
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
