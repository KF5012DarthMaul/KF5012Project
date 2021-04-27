/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbmgr;
import domain.TaskExecution;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
/**
 *
 * @author Emanuel Oliveira W19029581
 * The DBAbstraction class provides methods for other classes to use that permit interaction with a database.
 * These methods utilize constructed SQL, preventing SQL injection.
 */
import kf5012darthmaulapplication.PermissionManager.AccountType;
import kf5012darthmaulapplication.User;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.Period;
public final class DBAbstraction 
{
    private final DBConnection db;
    private static DBAbstraction instance;
    private String error;
    
    
    /** 
     * The DBAbstraction class provides methods for other classes to use that permit interaction with a database.
     * These methods utilize constructed SQL, preventing SQL injection.
     * If you retrieve an unexpected result, such as -1, call getError() or getError(true) to verify.
     */
    private DBAbstraction()
    {
        db = DBConnection.getInstance();
        error = "";
        createTables();
        fillDB("password");
    }
    
    public static DBAbstraction getInstance()
    {
        if(instance == null)
            instance = new DBAbstraction();
        return instance;
    }
    
    private String randomString() 
    {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        int targetStringLength = random.nextInt(32);
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) 
        {
            int randomLimitedInt = leftLimit + (int) 
              (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
    
    private void fillDB(String hashedPassword)
    {
        for(int i = 0; i < 6; i++)
            createUser(randomString(), hashedPassword, AccountType.CARETAKER.value);
        createUser(randomString(), hashedPassword, AccountType.MANAGER.value);
        createUser("admin", hashedPassword, AccountType.SYSADMIN.value);
        //createUser(randomString(), hashedPassword, AccountType.ESTATE.value);
        for(int i = 0; i < 2; i++)
            createUser(randomString(), hashedPassword, AccountType.HR_PERSONNEL.value);
        for(int i = 0; i < 10; i++)
        {
            submitTask(new Task(0, randomString(), randomString()));
        }
        ArrayList<Task> taskList = getTaskList();
        ArrayList<TaskExecution> texecList = new ArrayList();
        taskList.forEach(t -> {
            System.out.println(t.toString());
            LocalDateTime startingPoint = LocalDateTime.of(2021, Month.APRIL, 28, 10, 30);
            LocalDateTime plusOneHour = startingPoint.plusHours(1);
            texecList.add(new TaskExecution("", new Period(startingPoint, plusOneHour)));
        });
        submitTaskExecutions(texecList);
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
     * @param user The user object with data.
     * @param hashedPassword A password (encrypted)
     * @return True if succesful, false if not.
     */
    public boolean createUser(User user, String hashedPassword)
    {
       //return createUser(user.getUsername(), hashedPassword, user.getAccountType());
        return false;
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
                return false;
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
            return db.executePrepared();
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
            return db.executePrepared();
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return false;
        }
    }
    
    
    // Update all records for a specific user
    public boolean updateUser(User user)
    {
        return false;
    }
    
    
    // Delete a user
    public boolean deleteUser(User user)
    {
        return false;
    }
    
    /**
     * Adds a new task to the database. Allows for duplicates.
     * @param task The task object containing all task-related information.
     * @return Always True, unless if an SQLException ocurred. If it did, returns False.
     */
    public boolean submitTask(Task task)
    {
        try 
        {
            db.prepareStatement("INSERT INTO tblTasks (task_name, task_desc) VALUES (?, ?)");
            db.add(task.name);
            db.add(task.desc);
            return db.executePrepared();
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return false;
        }
    }
    
    // Retrieve all non-priority tasks for today
    public ArrayList<TaskExecution> getUnallocatedDailyTaskList()
    {
        return null;
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
                    + "{\n"+
                   "  id: "+ Integer.toString(id)+
                    "  name: " + name+ 
                    "  desc:\n" + desc +
                    "}";
        }
    }
    
    
    // Retrieve all unique tasks and temporal rules
    public ArrayList<Task> getTaskList()
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
                error = "No tasks were retrieved";
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
    
    
    // Insert all new taskexecutions in one go
    public boolean submitTaskExecutions(List<TaskExecution> tasks)
    {
        try 
        {
            db.prepareStatement("INSERT INTO tblTaskExecutions (task_type, start_datetime, end_datetime) VALUES (?, ?, ?)");
            for(TaskExecution t: tasks)
            {
                db.add(0); // REPLACE WITH ACTUAL TASK POINTER
                Period p = t.getPeriod();
                long start = p.start().toEpochSecond(ZoneOffset.UTC);
                long end = p.end().toEpochSecond(ZoneOffset.UTC);
                db.add(start);
                db.add(end);
                db.batch();
            }
            return db.executeBatch();
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
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
            Period p = task.getPeriod();
            long start = p.start().toEpochSecond(ZoneOffset.UTC);
            long end = p.end().toEpochSecond(ZoneOffset.UTC);
            db.add(start);
            db.add(end);
            return db.executePrepared();
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return false;
        }
    }
    
    // Retrieve today's task list for a given user
    // Untested
    public ArrayList<TaskExecution> getUserDailyTaskList(String username)
    {
        try 
        {
            db.prepareStatement("SELECT task_name, start_datetime, end_datetime FROM tblTaskExecutions "
                    + "WHERE username = ? AND start_datetime = date('now', 'start of day'"
                    + "+ \"JOIN tblTasks ON tblTasks.type_id = tblTaskExecutions.task_type\")");
            db.add(username);
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<TaskExecution> tasks = new ArrayList();
                do
                {
                    LocalDateTime start = LocalDateTime.ofEpochSecond(res.getLong(2), 0, ZoneOffset.UTC);
                    LocalDateTime end = LocalDateTime.ofEpochSecond(res.getLong(3), 0, ZoneOffset.UTC);
                    tasks.add(new TaskExecution(res.getString(1), new Period(start, end)));
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
