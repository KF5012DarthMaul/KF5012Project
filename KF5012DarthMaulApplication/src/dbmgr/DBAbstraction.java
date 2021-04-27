/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbmgr;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
/**
 *
 * @author Emanuel Oliveira W19029581
 * The DBAbstraction class provides methods for other classes to use that permit interaction with a database.
 * These methods utilize constructed SQL, preventing SQL injection.
 */
import kf5012darthmaulapplication.PermissionManager.AccountType;
public final class DBAbstraction 
{
    private final DBConnection db;
    private String error;
    
    
    /** 
     * The DBAbstraction class provides methods for other classes to use that permit interaction with a database.
     * These methods utilize constructed SQL, preventing SQL injection.
     * If you retrieve an unexpected result, such as -1, call getError() or getError(true) to verify.
     */
    public DBAbstraction()
    {
        db = DBConnection.getInstance();
        error = "";
        createTables();
        fillDB("password");
    }
    
    
    public String randomString() 
    {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        int targetStringLength = random.nextInt(32);
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int) 
              (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
    
    public void fillDB(String hashedPassword)
    {
        for(int i = 0; i < 6; i++)
            createUser(randomString(), hashedPassword, AccountType.CARETAKER.value);
        createUser(randomString(), hashedPassword, AccountType.MANAGER.value);
        createUser("admin", hashedPassword, AccountType.SYSADMIN.value);
        //createUser(randomString(), hashedPassword, AccountType.ESTATE.value);
        for(int i = 0; i < 2; i++)
            createUser(randomString(), hashedPassword, AccountType.HR_PERSONNEL.value);
    }
    
    /**
     * Retrieve the latest error without clearing the error variable.
     * @return A string containing the error.
     */
    public String getError()
    {
        return error;
    }
    
    /**
     * Retrieve the latest error and optionally clear the error variable.
     * @param clear Pass true as a parameter to clear the variable.
     * @return A string containing the error.
     */
    public String getError(boolean clear)
    {
        String cpy = error;
        if(clear)
            error = "";
        return cpy;
    }
    
    /**
     * Attempts to create a new user in the Database.
     * If the user already exists, this function will return false.
     * @param username The username of the new user.
     * @param hashedPassword A password (encrypted)
     * @return True if succesful, false if not.
     */
    public boolean createUser(String username, String hashedPassword)
    {
       return createUser(username, hashedPassword, 0);
    }
    
    /**
     * Attempts to create a new user in the Database.
     * If the user already exists, this function will return false.
     * @param username The username of the new user.
     * @param hashedPassword A password (encrypted).
     * @param perms Permission flags expressed in bits for the new user. See User class for information on these.
     * @return True if succesful, false if not.
     */
    public boolean createUser(String username, String hashedPassword, int perms)
    {
        if(!doesUserExist(username))
        {
            try 
            {
                db.prepareStatement("INSERT INTO tblUsers (username, hashpass, permission_flags) VALUES (?, ?, ?)");
                db.add(username);
                db.add(hashedPassword);
                db.add(perms);
                db.executePrepared();
            } 
            catch (SQLException ex) 
            {
                Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
                error = ex.getLocalizedMessage();
            }
        }
        else
        {
            error = "Username already exists";
            return false;
        }
        return true;
    }
    
    /**
     * Tests whether a username exists inside the database.
     * @param username The username to test.
     * @return Returns true if the username exists in the database, false if it doesn't.
     */
    public boolean doesUserExist(String username)
    {
        try 
        {
            db.prepareStatement("SELECT username FROM tblUsers WHERE username = ?");
            db.add(username);
            ResultSet res =  db.executePreparedQuery();
            return !res.isClosed();
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return true;
        }
    }
    
    private void createTables()
    {
        db.execute("""
                           CREATE TABLE IF NOT EXISTS tblUsers(
                               username TEXT PRIMARY KEY,
                               hashpass TEXT NOT NULL,
                               permission_flags INTEGER
                           );
                                CREATE TABLE IF NOT EXISTS tblTasks(
                               task_id INTEGER PRIMARY KEY AUTOINCREMENT,
                               task_type INTEGER NOT NULL,
                               caretaker TEXT,
                               execution_day INTEGER,
                               FOREIGN KEY(task_type) REFERENCES tblTaskType (type_id) ON DELETE CASCADE,
                               FOREIGN KEY(caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE,
                           );
                                CREATE TABLE IF NOT EXISTS tblTaskType(
                               type_id INTEGER PRIMARY KEY AUTOINCREMENT,
                               task_name TEXT NOT NULL,
                               task_descr TEXT
                           );
                                -- Tasks could ref log_id instead of log referencing task_id
                           -- That would require updating the task row instead of just adding to taskLog.
                           CREATE TABLE IF NOT EXISTS tblTaskLog(
                               log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                               task_id INTEGER NOT NULL,
                               log_time INTEGER NOT NULL,
                               completion_time INTEGER,
                               FOREIGN KEY(task_id) REFERENCES tblTasks(task_id) ON DELETE CASCADE,
                           );"""); 
        /*while(true)
            {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) 
            {
            try
            {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("Reading Selected file: " + selectedFile.getAbsolutePath());
            db.execute(Files.readString(selectedFile.toPath()));
            }
            catch (FileNotFoundException ex)
            {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;
            }
        }*/ 
    }
    
    /**
     * Gets the password (encrpyted) for a key username from the database.
     * @param username The username whose password to retrieve.
     * @return A string containing the password (encrypted), or NULL if the username does not exist.
     */
    public String getHashedPassword(String username)
    {
        try 
        {
            db.prepareStatement("SELECT hashpass FROM tblUsers WHERE username = ?");
            db.add(username);
            ResultSet res =  db.executePreparedQuery();
            if(!res.isClosed())
                return res.getString(1);
            else
            {
                error = "User does not exist";
                return null;
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return null;
        }
    }
    
    /**
     * Sets a new password (encrypted) for a username, disregarding whether it actually exists in the database or not.
     * @param username The username whose password (encrypted) to set.
     * @param hashedPassword The password (encrypted) to set.
     * @return Always True, unless if an SQLException ocurred. If it did, returns False.
     */
    public boolean setHashedPassword(String username, String hashedPassword)
    {
        try 
        {
            db.prepareStatement("UPDATE tblUsers SET hashpass = ? WHERE username = ?");
            db.add(hashedPassword);
            db.add(username);
            db.executePrepared();
            return true;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return false;
        }
    }
    
    /**
     * Gets the permission flags as an integer for a key username from the database.
     * @param username The username whose permissions to retrieve.
     * @return A positive integer representing the permission flags, -1 if an error occured or the user does not exist.
     */
    public int getPermissions(String username)
    {
        try 
        {
            db.prepareStatement("SELECT permission_flags FROM tblUsers WHERE username = ?");
            db.add(username);
            ResultSet res =  db.executePreparedQuery();
            if(!res.isClosed())
                return res.getInt(1);
            else 
            {
                error = "User does not exist";
                return -1;
            }
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return -1;
        }
    }
    
    /**
     * Sets new permission flags for a username, disregarding whether it actually exists in the database or not.
     * @param username The username whose permissions to set.
     * @param perms The permission flags to set.
     * @return Always True, unless if an SQLException ocurred. If it did, returns False.
     */
    public boolean setPermissions(String username, int perms)
    {
        try 
        {
            db.prepareStatement("UPDATE tblUsers SET perms = ? WHERE username = ?");
            db.add(username);
            db.add(perms);
            db.executePrepared();
            return true;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return false;
        }
    }
    
    /**
     * Adds a new task with name and description to the database. Allows for duplicates.
     * @param taskName The string defining the name of the task
     * @param taskDesc The string defining the description of the task.
     * @return Always True, unless if an SQLException ocurred. If it did, returns False.
     */
    public boolean createTask(String taskName, String taskDesc)
    {
        try 
        {
            db.prepareStatement("INSERT INTO tblTaskType (task_name, task_desc) VALUES (?, ?)");
            db.add(taskName);
            db.add(taskDesc);
            db.executePrepared();
            return true;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return false;
        }
    }
    
    /*public class WeekSchedule
    {
        int length;
        boolean Monday;
        boolean Tuesday;
        boolean Wednesday;
        boolean Thursday;
        boolean Friday;
        boolean Saturday;
        boolean Sunday;
        public void setWeekdays()
        {
            Monday = true;
            Tuesday = true;
            Wednesday = true;
            Thursday = true;
            Friday = true;
        }
        public void unsetWeekdays()
        {
            Monday = false;
            Tuesday = false;
            Wednesday = false;
            Thursday = false;
            Friday = false;
        }
        public void setWeekend()
        {
            Saturday = true;
            Sunday = true;
        }
        public void unsetWeekend()
        {
            Saturday = false;
            Sunday = false;
        }
    }*/
    
    // Date functions need to be checked for validity
    /*public boolean scheduleTaskOnce(int taskID, Date day)
    {
        try 
        {
            db.prepareStatement("INSERT INTO tblTasks (task_type, execution_day) VALUES (?, ?)");
            db.add(taskID);
            db.add(day.getTime());
            db.executePrepared();
            return true;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return false;
        }
    }*/
    
    public class CaretakerTask
    {
        Integer taskid;
        //String username;
        String taskname;
        String taskdesc;
        public CaretakerTask(int tid, String tname, String tdesc)
        {
            taskid = tid;
            taskname = tname;
            taskdesc = tdesc;
        }
    }
    
    // Retrieve today's task list
    // Untested
    // Probably creates 4 new Strings per result, instead of reusing String objects
    // Could be optimized for that case if necessary.
    public ArrayList<CaretakerTask> retrieveTodayTaskList(String username)
    {
        try 
        {
            db.prepareStatement("SELECT (task_id, task_name, task_desc) FROM tblTasks WHERE username = ? AND execution_day = date('now')"
                    + "JOIN tblTaskTypes ON tblTaskTypes.type_id = tblTasks.task_type");
            db.add(username);
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<CaretakerTask> tasks = new ArrayList();
                do
                {
                    tasks.add(new CaretakerTask(res.getInt(1), res.getString(2), res.getString(3)));
                }
                while(res.next());
                return tasks;
            }
            else 
            {
                error = "No tasks are available";
                return null;
            }
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return null;
        }
    }
    
}
