package dbmgr;
import dbmgr.DBExceptions.*;
import domain.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.PermissionManager.AccountType;
import kf5012darthmaulapplication.User;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.Period;

/**
 *
 * @author Emanuel Oliveira W19029581
 */
public final class DBAbstraction 
{
    private final DBConnection db;
    private static DBAbstraction instance;
    
    
    /** 
     * The DBAbstraction class provides methods for other classes to use that permit interaction with a database.<p>
     * These methods utilize constructed SQL and prepared statements, preventing SQL injection.
     */
    private DBAbstraction() throws FailedToConnectException
    {
        db = DBConnection.getInstance();
    }
    
    /**
     * Get the singleton instance of this class.
     * @return The singleton instance.
     * @throws FailedToConnectException
     */
    public static DBAbstraction getInstance() throws FailedToConnectException
    {
        if(instance == null)
            instance = new DBAbstraction();
        return instance;
    }
    
    private String randomString() 
    {
        Random random = new Random();
        int targetStringLength = 10+random.nextInt(32);
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) 
        {
            int c = 97 + random.nextInt(26);
            buffer.append((char) c);
        }
        return buffer.toString();
    }
    
    public void fillDB(String hashedPassword)
    {
        try {
            for(int i = 0; i < 6; i++)
                createUser(randomString(), hashedPassword, AccountType.CARETAKER.value);
            createUser(randomString(), hashedPassword, AccountType.MANAGER.value);
            createUser(randomString(), hashedPassword, AccountType.ESTATE.value);
            for(int i = 0; i < 2; i++)
                createUser(randomString(), hashedPassword, AccountType.HR_PERSONNEL.value);
            for(int i = 0; i < 1000; i++)
            {
                submitTask(new Task(0, randomString(), randomString()));
            }
            ArrayList<Task> taskList = getTaskList();
            ArrayList<TaskExecution> texecList = new ArrayList();
            LocalDateTime startingPoint = LocalDateTime.of(2021, Month.APRIL, 29, 10, 30);
            LocalDateTime plusOneHour = startingPoint.plusHours(1);
            Period p = new Period(startingPoint, plusOneHour);
            taskList.forEach(t -> {
                System.out.println(t.toString());
                texecList.add(new TaskExecution(t.name, p));
            });
            submitTaskExecutions(texecList);
            ArrayList<TaskExecution> tlist = getUnallocatedTaskList(p);
            tlist.forEach(t -> {
                t.getName();
            });
            ArrayList<TaskExecution> t2 = getUnallocatedTaskList(p);
            t2.forEach(t -> {System.out.println(t.toString());
            });
        } catch (UserAlreadyExistsException | EmptyResultSetException | EmptyInputException ex) {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Attempts to create a new user in the Database with 0 permissions.
     * If the user already exists, this function will return false.
     * @param username The username of the new user.
     * @param hashedPassword A password (encrypted)
     * @return True if succesful, false if not.
     * @throws UserAlreadyExistsException
     */
    public boolean createUser(String username, String hashedPassword) throws UserAlreadyExistsException
    {
       return createUser(username, hashedPassword, 0);
    }
    
    /**
     * Attempts to create a new user in the Database.
     * If the user already exists, this function will return false.
     * @param user The user object with data.
     * @param hashedPassword A password (encrypted).
     * @return A boolean representing success.
     * @throws UserAlreadyExistsException
     */
    public boolean createUser(User user, String hashedPassword) throws UserAlreadyExistsException
    {
       return createUser(user.getUsername(), hashedPassword, user.getAccountType().value);
    }
    
    /**
     * Attempts to create a new user in the Database.
     * If the user already exists, this function will return false.
     * @param username The username of the new user.
     * @param hashedPassword A password (encrypted).
     * @param perms Permission flags expressed for the new user. See User class for information on these.
     * @return A boolean representing success.
     * @throws UserAlreadyExistsException
     */
    public boolean createUser(String username, String hashedPassword, int perms) throws UserAlreadyExistsException
    {
        if(!doesUserExist(username))
        {
            try 
            {
                db.prepareStatement("INSERT INTO tblUsers (username, hashpass, permission_flags) VALUES (?, ?, ?)");
                db.add(username);
                db.add(hashedPassword);
                db.add(perms);
                return db.executePrepared();
            } 
            catch (SQLException ex) 
            {
                Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        else
        {
            throw new UserAlreadyExistsException();
        }
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
            return true;
        }
    }
    
    /**
     * Tests whether a user exists inside the database.
     * @param user The user to test.
     * @return Returns true if the user exists in the database, false if it doesn't.
     */
    public boolean doesUserExist(User user)
    {
        return doesUserExist(user.getUsername());
    }
    
    public void createTables()
    {
        db.execute("""
                   CREATE TABLE IF NOT EXISTS tblUsers(
                                                  username TEXT PRIMARY KEY,
                                                  hashpass TEXT NOT NULL,
                                                  permission_flags INTEGER
                                              );""");
        db.execute(""" 
                           CREATE TABLE IF NOT EXISTS tblTasks(
                                type_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                task_name TEXT NOT NULL,
                                task_desc TEXT
                           );""");
        db.execute(""" 
                           CREATE TABLE IF NOT EXISTS tblTaskExecutions(
                               task_id INTEGER PRIMARY KEY AUTOINCREMENT,
                               task_type INTEGER NOT NULL,
                               caretaker TEXT,
                               start_datetime INTEGER,
                               end_datetime INTEGER,
                               FOREIGN KEY(task_type) REFERENCES tblTasks (type_id) ON DELETE CASCADE,
                               FOREIGN KEY(caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE
                           );""");
        db.execute("""
                            CREATE TABLE IF NOT EXISTS tblTaskTemporalRules(
                                rules_id INTEGER PRIMARY KEY AUTOINCREMENT,
                                task_type INTEGER NOT NULL,
                                day_of_week INTEGER NOT NULL,
                                start_datetime INTEGER,
                                end_datetime INTEGER,
                                FOREIGN KEY(task_type) REFERENCES tblTasks (type_id) ON DELETE CASCADE
                            );""");
                           // Tasks could ref log_id instead of log referencing task_id
                           // That would require updating the task row instead of just adding to taskLog.
        db.execute(""" 
                        CREATE TABLE IF NOT EXISTS tblTaskLog(
                            log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            task_id INTEGER NOT NULL,
                            log_time INTEGER NOT NULL,
                            completion_time INTEGER,
                            FOREIGN KEY(task_id) REFERENCES tblTaskExecutions(task_id) ON DELETE CASCADE
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
     * @return A string containing the password (encrypted).
     * @throws UserDoesNotExistException
     */
    public String getHashedPassword(String username) throws UserDoesNotExistException
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
                throw new UserDoesNotExistException();
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
     /**
     * Gets the password (encrpyted) for a user from the database.
     * @param user The user whose password to retrieve.
     * @return A string containing the password (encrypted).
     * @throws UserDoesNotExistException
     */
    public String getHashedPassword(User user) throws UserDoesNotExistException
    {
        return getHashedPassword(user.getUsername());
    }
    
    /**
     * Sets a new password (encrypted) for a username.
     * @param username The username whose password (encrypted) to set.
     * @param hashedPassword The password (encrypted) to set.
     * @return A boolean representing success.
     * @throws UserDoesNotExistException
     */
    public boolean setHashedPassword(String username, String hashedPassword) throws UserDoesNotExistException
    {
        try 
        {
            if(doesUserExist(username))
            {
                db.prepareStatement("UPDATE tblUsers SET hashpass = ? WHERE username = ?");
                db.add(hashedPassword);
                db.add(username);
                return db.executePrepared();
            }
            else
                throw new UserDoesNotExistException();
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * Sets a new password (encrypted) for a user.
     * @param user The user whose password (encrypted) to set.
     * @param hashedPassword The password (encrypted) to set.
     * @return A boolean representing success.
     * @throws UserDoesNotExistException
     */
    public boolean setHashedPassword(User user, String hashedPassword) throws UserDoesNotExistException
    {
        return setHashedPassword(user.getUsername(), hashedPassword);
    }
    
    /**
     * Gets the permission flags as an integer for a key username from the database.
     * @param username The username whose permissions to retrieve.
     * @return A positive integer representing the permission flags.
     * @throws UserDoesNotExistException
     */
    public int getPermissions(String username) throws UserDoesNotExistException
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
                throw new UserDoesNotExistException();
            }
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    
    /**
     * Gets the permission flags for a user from the database and copies them to the user's permission field.
     * @param user The user whose permissions to retrieve and populate.
     * @return The new User object with the new permission set.
     * @throws UserDoesNotExistException
     */
    public User getPermissions(User user) throws UserDoesNotExistException
    {
        int perms = getPermissions(user.getUsername());
        // Can't edit a user's PM yet, so just return a new object for now
        user = new User(user.getUsername(), PermissionManager.intToAccountType(perms));
        return user;
    }
    
    public ArrayList<User> getAllUsers() {
		ArrayList<User> allUsers = new ArrayList<User>();
    	try {
			db.prepareStatement("SELECT username, permission_flags FROM tblUsers");
			ResultSet res = db.executePreparedQuery();
	    	while(res.next()) {
	    		allUsers.add(new User(res.getString("username"), PermissionManager.intToAccountType(res.getInt("permission_flags"))));
	    	}
		} catch (SQLException ex) {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
		}
    	return allUsers;

    }
    
    /**
     * Gets all fields of a user from the database.<p>
     * Populates a new User object and returns it.
     * @param user The user whose data to retrieve.
     * @return Returns the new user object.
     * @throws UserDoesNotExistException 
     */
    public User getUser(User user) throws UserDoesNotExistException
    {
        return getUser(user.getUsername());
    }
    
    /**
     * Gets all fields of a username from the database.<p>
     * Populates a new User object and returns it.
     * @param username The username of whose data to retrieve.
     * @return Returns the new user object.
     * @throws UserDoesNotExistException 
     */
    public User getUser(String username) throws UserDoesNotExistException
    {
        try 
        {
            db.prepareStatement("SELECT permission_flags FROM tblUsers WHERE username = ?");
            db.add(username);
            ResultSet res =  db.executePreparedQuery();
            User u;
            if(!res.isClosed())
            {
                int perms = res.getInt(1);
                AccountType at = PermissionManager.intToAccountType(perms);
                u = new User(username, at);
                return u;
            }
            else 
            {
                throw new UserDoesNotExistException();
            }
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    /*/**
     * Sets new permission flags for a username, disregarding whether it actually exists in the database or not.
     * @param username The username whose permissions to set.
     * @param perms The permission flags to set.
     * @return Always True, unless if an SQLException ocurred. If it did, returns False.
     */
    /*
    public boolean setPermissions(String username, int perms)
    {
        try 
        {
            db.prepareStatement("UPDATE tblUsers SET perms = ? WHERE username = ?");
            db.add(username);
            db.add(perms);
            return db.executePrepared();
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return false;
        }
    }*/
    
    
    /**
     * Update all fields for a user.
     * @param user The user object with data to update the database with.
     * @return A boolean representing success.
     * @throws UserDoesNotExistException 
     */
    public boolean updateUser(User user) throws UserDoesNotExistException
    {
        try 
        {
            if(doesUserExist(user))
            {
                db.prepareStatement("UPDATE tblUsers SET perms = ? WHERE username = ?");
                db.add(user.getAccountType().value);
                db.add(user.getUsername());
                return db.executePrepared();
            }
            else
            {
                throw new UserDoesNotExistException();
            }
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * Deletes a user from the database.
     * @param user The user to delete.
     * @return A boolean representing success.
     */
    public boolean deleteUser(User user)
    {
        try
        {
            db.prepareStatement("DELETE FROM tblUsers WHERE username = ?");
            db.add(user.getUsername());
            return db.executePrepared();
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * Adds a new task to the database. Allows for duplicates.<p>
     * This method automatically sets the ID of Task, so that TaskExecutions can reference it.
     * @param task The task object containing all task-related information.
     * @return A boolean representing success.
     */
    public boolean submitTask(Task task)
    {
        try 
        {
            db.prepareStatement("INSERT INTO tblTasks (task_name, task_desc) VALUES (?, ?)");
            db.add(task.name);
            db.add(task.desc);
            // Need to save all the temporal rules here
            if(db.executePrepared())
            {
                //task.id = getLastTaskID();
                return true;
            }
            else return false;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    // Retrieve the last inserted task's ID
    private int getLastTaskID()
    {
        try 
        {
            db.prepareStatement("SELECT type_id FROM tblTasks ORDER BY type_id DESC LIMIT 1;");
            ResultSet res =  db.executePreparedQuery();
            if(!res.isClosed())
                return res.getInt(1);
            else return -1;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    
    // Convert from 2 Epoch seconds of type Long to a Period object
    private Period periodFromEpoch(Long start, Long end)
    {
        LocalDateTime dtStart = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC);
        LocalDateTime dtEnd = LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.UTC);
        return new Period(dtStart, dtEnd);
    }


    
    // Unwrapper class that converts Period to Epoch seconds of type Long
    private final class PeriodUnwrapper
    {
        Long start;
        Long end;
        PeriodUnwrapper(Period p)
        {
            start = p.start().toEpochSecond(ZoneOffset.UTC);
            end = p.end().toEpochSecond(ZoneOffset.UTC);
        }
    }
    
    // Retrieve all non-priority tasks for a time period
    public ArrayList<TaskExecution> getUnallocatedTaskList(Period p) throws EmptyResultSetException
    {
        PeriodUnwrapper pw = new PeriodUnwrapper(p);
        try 
        {
            db.prepareStatement("SELECT task_type, start_datetime, end_datetime FROM tblTaskExecutions"
                    + " WHERE caretaker IS NULL AND start_datetime <= ? AND end_datetime >= ?");
            db.add(pw.start);
            db.add(pw.end);
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<TaskExecution> tasks = new ArrayList();
                do
                {
                    Period taskP = periodFromEpoch(res.getLong(2), res.getLong(3));
                    tasks.add(new TaskExecution("", taskP));
                }
                while(res.next());
                return tasks;
            }
            else
            {
                throw new EmptyResultSetException();
            }
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    // Retrieve all tasks that were not completed before today
    public ArrayList<TaskExecution> getUnallocatedOverflowTaskList()
    {
        return null;
    }
    
    // Retrieve all tasks that have time restrictions for today
    public ArrayList<TaskExecution> getUnallocatedDailyPriorityTaskList()
    {
        return null;
    }
    
    // Internal Task class for testing Task methods
    public class Task
    {
        int id;
        String name;
        String desc;
        public Task(int i, String n, String d){id=i;name=n;desc=d;}
        public Task(String n, String d){name=n;desc=d;}
        ConstrainedIntervaledPeriodSet temporalRules;
        @Override
        public String toString()
        {
            return "Task Object"
                    + "\n{"+
                   " \n id: "+ Integer.toString(id)+
                    "\n name: " + name+ 
                    "\n desc: " + desc +
                    "\n}";
        }
    }
    
    // Retrieve all unique tasks and temporal rules
    public ArrayList<Task> getTaskList() throws EmptyResultSetException
    {
        try 
        {
            db.prepareStatement("SELECT type_id, task_name, task_desc FROM tblTasks");
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<Task> tasks = new ArrayList();
                do
                {
                    tasks.add(new Task(res.getInt(1), res.getString(2), res.getString(3)));
                }
                while(res.next());
                return tasks;
            }
            else
            {
                throw new EmptyResultSetException();
            }
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    // Insert all new taskexecutions in one go
    public boolean submitTaskExecutions(List<TaskExecution> tasks) throws EmptyInputException
    {
        try 
        {
            if(tasks.isEmpty())
                throw new EmptyInputException();
            db.prepareStatement("INSERT INTO tblTaskExecutions (task_type, start_datetime, end_datetime) VALUES (?, ?, ?)");
            for(TaskExecution task: tasks)
            {
                db.add(0); // REPLACE WITH ACTUAL TASK POINTER
                PeriodUnwrapper pw = new PeriodUnwrapper(task.getPeriod());
                db.add(pw.start);
                db.add(pw.end);
                db.batch();
            }
            return db.executeBatch();
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    // Insert a new taskexecution
    public boolean submitTaskExecution(TaskExecution task)
    {
        try 
        {
            db.prepareStatement("INSERT INTO tblTaskExecutions (task_type, start_datetime, end_datetime) VALUES (?, ?, ?)");
            db.add(0); // REPLACE WITH ACTUAL TASK POINTER
            PeriodUnwrapper pw = new PeriodUnwrapper(task.getPeriod());
            db.add(pw.start);
            db.add(pw.end);
            return db.executePrepared();
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean submitVerificationExecution(VerificationExecution ver)
    {
        return false;
    }
    
    public boolean submitVerificationExecutions(List<VerificationExecution> vers) throws EmptyInputException
    {
        if(vers.isEmpty())
            throw new EmptyInputException();
        return false;
    }
    
    // Retrieve today's task list for a given user
    // Untested
    public ArrayList<TaskExecution> getUserDailyTaskList(String username) throws EmptyResultSetException
    {
        try 
        {
            db.prepareStatement("SELECT task_name, start_datetime, end_datetime FROM tblTaskExecutions "
                    + "WHERE username = ? AND start_datetime = date('now', 'start of day')"
                    + "+ \"JOIN tblTasks ON tblTasks.type_id = tblTaskExecutions.task_type\")");
            db.add(username);
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<TaskExecution> tasks = new ArrayList();
                do
                {
                    tasks.add(new TaskExecution(res.getString(1), periodFromEpoch(res.getLong(2), res.getLong(3))));
                }
                while(res.next());
                return tasks;
            }
            else 
            {
                throw new EmptyResultSetException();
            }
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
