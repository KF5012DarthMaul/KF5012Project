package dbmgr;
import dbmgr.DBExceptions.*;
import domain.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.PermissionManager.AccountType;
import kf5012darthmaulapplication.User;
import kf5012darthmaulapplication.SecurityManager;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.IntervaledPeriodSet;
import temporal.Period;

/**
 * This DBAbstraction class provides methods for other classes to use that permit interaction with a database. 
 * These methods utilize constructed SQL and prepared statements, preventing SQL injection.<p>
 * This is a singleton pattern class. Call DBAbstraction.getInstance() to get a reference to the class object.
 * @author Emanuel Oliveira W19029581
 */
public final class DBAbstraction 
{
    private final DBConnection db;
    private static DBAbstraction instance;
    //private ArrayList<Task> taskCache;
    private ArrayList<User> userCache;
    /** 
     * This DBAbstraction class provides methods for other classes to use that permit interaction with a database.<p>
     * These methods utilize constructed SQL and prepared statements, preventing SQL injection.<p>
     * This is a singleton pattern class. Call DBAbstraction.getInstance() to get a reference to the class object.
     */
    private DBAbstraction() throws FailedToConnectException
    {
        db = DBConnection.getInstance();
        createTables();
        userCache = getAllUsers();
        //taskCache = new ArrayList();
        
        /*try 
        {
            //taskCache = getTaskList();
        } 
        catch (EmptyResultSetException ex) 
        {
            // Don't need to do anything here
        }*/
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
    
    public void fillDB()
    {
        try {
        	String password = "Password123#";
        	int hrCount = 5;
        	int managerCount = 7;
        	int estateCount = 3;
        	int caretakerCount = 15;

        	for(int i = 0 ; i < hrCount; i++) {
        		String username = "hr_" + i;
        		createUser(username,SecurityManager.generatePassword(password),AccountType.HR_PERSONNEL);
        	}
        	for(int i = 0 ; i < managerCount; i++) {
        		String username = "manager_" + i;
        		createUser(username,SecurityManager.generatePassword(password),AccountType.MANAGER);
        	}
        	for(int i = 0 ; i < estateCount; i++) {
        		String username = "estate_" + i;
        		createUser(username,SecurityManager.generatePassword(password),AccountType.ESTATE);
        	}
        	for(int i = 0 ; i < caretakerCount; i++) {
        		String username = "caretaker_" + i;
        		createUser(username,SecurityManager.generatePassword(password),AccountType.CARETAKER);
        	}
        	
        	
            for(int i = 0; i < 1000; i++)
            {
                //submitTask(new Task(0, randomString(), randomString()));
            }
            //ArrayList<Task> taskList = getTaskList();
            //ArrayList<TaskExecution> texecList = new ArrayList();
            //LocalDateTime startingPoint = LocalDateTime.of(2021, Month.APRIL, 29, 10, 30);
            //LocalDateTime plusOneHour = startingPoint.plusHours(1);
            //Period p = new Period(startingPoint, plusOneHour);
//            taskList.forEach(t -> {
//                System.out.println(t.toString());
//                texecList.add(new TaskExecution(t.name, p));
//            });
            //submitTaskExecutions(texecList);
            //ArrayList<TaskExecution> tlist = getUnallocatedTaskList(p);
            //tlist.forEach(t -> {
            //    t.getName();
            //});
            //rrayList<TaskExecution> t2 = getUnallocatedTaskList(p);
            //t2.forEach(t -> {System.out.println(t.toString());
            //});
        } 
        catch (Exception ex) {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createTables()
    {
        db.execute("""
                    CREATE TABLE IF NOT EXISTS tblUsers(
                        username TEXT PRIMARY KEY,
                        display_name TEXT NOT NULL,
                        hashpass TEXT NOT NULL,
                        account_type INTEGER NOT NULL
                    );""");
        db.execute(""" 
                    CREATE TABLE IF NOT EXISTS tblTasks(
                        task_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        task_name TEXT NOT NULL,
                        task_desc TEXT NOT NULL,
                        task_priority INTEGER NOT NULL,
                        task_intervaled_period_start INTEGER NOT NULL,
                        intervaled_period_end INTEGER,
                        period_interval INTEGER,
                        intervaled_period_constraint_start INTEGER,
                        invervaled_period_constraint_end INTERGER,
                        constraint_interval INTEGER,
                        allocation TEXT,
                        verification_id INTEGER,
                        FOREIGN KEY(verification_id) REFERENCES tblVerications (verf_id) ON DELETE CASCADE,
                        FOREIGN KEY(allocation) REFERENCES tblUsers (username) ON DELETE CASCADE
                    );""");
        db.execute("""
                    CREATE TABLE IF NOT EXISTS tblTaskMaps(
                        map_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        task_id INTEGER NOT NULL,
                        caretaker TEXT NOT NULL,
                        preferences INTEGER NOT NULL,
                        efficiency INTEGER NOT NULL,
                        effectiveness INTEGER NOT NULL,
                        FOREIGN KEY(task_id) REFERENCES tblTasks (task_id) ON DELETE CASCADE,
                        FOREIGN KEY(caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE
                   );""");
        db.execute("""
                    CREATE TABLE IF NOT EXISTS tblVerifications(
                        verf_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        verf_notes TEXT,
                        verf_priority INTEGER NOT NULL,
                        verf_duration INTEGER,
                        verf_caretaker TEXT
                   );""");
        db.execute(""" 
                    CREATE TABLE IF NOT EXISTS tblTaskExecutions(
                        exe_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        task_id INTEGER NOT NULL,
                        exe_notes TEXT,
                        exe_prio INTEGER NOT NULL,
                        start_datetime INTEGER NOT NULL,
                        end_datetime INTEGER,
                        caretaker TEXT,
                        compl_id INTEGER,
                        verf_exe_id INTEGER,
                        FOREIGN KEY(task_id) REFERENCES tblTasks (task_id) ON DELETE CASCADE,
                        FOREIGN KEY(caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE,
                        FOREIGN KEY(compl_id) REFERENCES tblCompletions (compl_id) ON DELETE CASCADE,
                        FOREIGN KEY(verf_exe_id) REFERENCES tblVerfExecutions (exe_id) ON DELETE CASCADE
                    );""");
        db.execute("""
                    CREATE TABLE IF NOT EXISTS tblVerfExecutions(
                        exe_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        verf_id INTEGER NOT NULL,
                        exe_notes TEXT,
                        exe_duration INTEGER NOT NULL,
                        caretaker TEXT,
                        completion_id INTEGER,
                        FOREIGN KEY (verf_id) REFERENCES tblVerifications(verf_id) ON DELETE CASCADE,
                        FOREIGN KEY (caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE,
                        FOREIGN KEY (completion_id) REFERENCES tblCompletion(verf_id) ON DELETE CASCADE
                   );""");
        db.execute(""" 
                    CREATE TABLE IF NOT EXISTS tblCompletion(
                        compl_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        caretaker TEXT NOT NULL,
                        start_time INTEGER NOT NULL,
                        compl_time INTEGER NOT NULL,
                        quality INTEGER,
                        notes TEXT,
                        FOREIGN KEY(caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE
                    );""");
        db.execute("""
                    CREATE TABLE IF NOT EXISTS tblSystemLog(
                        log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        log_info TEXT,
                        log_timestamp INTEGER NOT NULL
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
    
    // Convert from 2 Epoch seconds of type Long to a Period object
    private Period periodFromEpoch(Long start, Long end)
    {
        LocalDateTime dtStart = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC);
        if(end == null || end == 0)
            return new Period(dtStart);
        LocalDateTime dtEnd = LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.UTC);
        return new Period(dtStart, dtEnd);
    }

    // Convert from 1 Epoch seconds of type Long to a Period object
    private Period periodFromEpoch(Long start)
    {
        LocalDateTime dtStart = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC);
        return new Period(dtStart);
    }

    // Unwrapper class that converts Period to Epoch seconds of type Long
    private final class PeriodUnwrapper
    {
        Long start;
        Long end;
        PeriodUnwrapper(Period p)
        {
            start = p.start().toEpochSecond(ZoneOffset.UTC);
            if(p.end() == null)
                end = null;
            else
                end = p.end().toEpochSecond(ZoneOffset.UTC);
        }
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
        return createUser(user.getUsername(), hashedPassword, user.getAccountType());
    }
    
    /**
     * Attempts to create a new user in the Database.
     * If the user already exists, this function will return false.
     * @param username The username of the new user.
     * @param hashedPassword A password (encrypted).
     * @param accountType The Account type of the user
     * @return A boolean representing success.
     * @throws UserAlreadyExistsException
     */
    public boolean createUser(String username, String hashedPassword, AccountType accountType) throws UserAlreadyExistsException
    {
        if(!doesUserExist(username))
        {
            try 
            {
                db.prepareStatement(
                        "INSERT INTO tblUsers (username, hashpass, display_name, account_type)"
                        + " VALUES (?, ?, ?, ?)");
                db.add(username);
                db.add(hashedPassword);
                db.add(username);
                db.add(accountType.ordinal());
                boolean b = db.executePrepared();
                userCache.add(new User(username, accountType));
                return b;
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
     * Tests whether a user exists inside the database.
     * @param user The user to test.
     * @return Returns true if the user exists in the database, false if it doesn't.
     */
    public boolean doesUserExist(User user)
    {
        return userCache.contains(user) || doesUserExist(user.getUsername());
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
            getUser(username);
            return true;
        }
        catch (UserDoesNotExistException ex)
        {
            return false;
        }
    }
    
    /**
     * Gets all fields of a user from the database.<p>
     * Populates a new User object and returns it.
     * @param username The user whose data to retrieve.
     * @return Returns the new user object.
     * @throws  UserDoesNotExistException
     */
    public User getUser(String username) throws UserDoesNotExistException
    {
        Stream<User> userStream = userCache.stream();
        try
        {
            return userStream.filter(user -> user.getUsername().equals(username)).findFirst().get();
        }
        catch(NoSuchElementException ex)
        {
            throw new UserDoesNotExistException();
        }
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
                db.prepareStatement("UPDATE tblUsers SET hashpass = ?"
                        + " WHERE username = ?");
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
     * Gets the permission flags as an integer for a key username.
     * @param username The username whose permissions to retrieve.
     * @return A positive integer representing the permission flags.
     * @throws UserDoesNotExistException
     */
    public AccountType getAccountType(String username) throws UserDoesNotExistException
    {
        return getUser(username).getAccountType();
    }
 
    /**
     * @return 
     */
    public ArrayList<User> getAllUsers()
    {
	ArrayList<User> allUsers = new ArrayList<>();
    	try 
        {
            db.prepareStatement("SELECT username, account_type FROM tblUsers");
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                while(res.next())
                {
                    allUsers.add(new User(res.getString(1), PermissionManager.intToAccountType(res.getInt(2))));
                }
            }
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
    	return allUsers;
    }
    
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
                db.prepareStatement(
                        "UPDATE tblUsers SET account_type = ?, display_name = ?"
                                + " WHERE username = ?");
                db.add(PermissionManager.accountTypetoInt(user.getAccountType()));
                db.add(user.getUsername());
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
            boolean b = db.executePrepared();
            userCache.remove(user);
            return b;
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
            db.prepareStatement(
                    "SELECT task_id FROM tblTasks"
                     + " ORDER BY task_id DESC LIMIT 1");
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
    
   // Retrieve the last inserted task's ID
    private int getLastVerificationID()
    {
        try 
        {
            db.prepareStatement(
                    "SELECT verf_id FROM tblVerifications"
                    + " ORDER BY verf_id DESC LIMIT 1");
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
    
    // Retrieve all unique tasks and temporal rules
    public ArrayList<Task> getTaskList() throws EmptyResultSetException
    {
        try 
        {
            db.prepareStatement(
                    "SELECT task_id, task_name, task_desc, task_priority, "
                    + "task_intervaled_period_start, intervaled_period_end, period_interval, "
                    + "intervaled_period_constraint_start, invervaled_period_constraint_end, constraint_interval,"
                    + "allocation, verification_id FROM tblTasks");
            ResultSet res = db.executePreparedQuery();
            db.prepareStatement(
                    "SELECT map_id, task_id, caretaker, preferences, efficiency, effectiveness"
                    + " FROM tblTaskMaps ORDER BY task_id");
            ResultSet maps = db.executePreparedQuery();
            db.prepareStatement(
                    "SELECT verf_id, verf_notes, verf_priority, verf_duration, verf_caretaker"
                    + " FROM tblVerifications");
            ResultSet verfResSet = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<Task> tasks = new ArrayList();
                
                // Get a list of validations
                ArrayList<Verification> verfList = new ArrayList<>();
                if(!verfResSet.isClosed())
                {
                     while(verfResSet.next())
                    {
                        int verfID = verfResSet.getInt(1);
                        String verfNotes = verfResSet.getString(2);
                        int verfPrio = verfResSet.getInt(3);
                        int verfDura = verfResSet.getInt(4);
                        Duration duration = verfResSet.wasNull() ? null : Duration.ofMinutes(verfDura);
                        String verfCaretaker = verfResSet.getString(5);
                        User allocation = verfResSet.wasNull() ? null : getUser(verfCaretaker);
                        TaskPriority priority = TaskPriority.values()[verfPrio];
                        verfList.add(new Verification(verfID, verfNotes, priority, duration, allocation));
                    }
                }
                
                while(res.next())
                {
                    int taskID = res.getInt(1);
                    String taskName = res.getString(2);
                    String taskDesc = res.getString(3);
                    int taskPrio = res.getInt(4);
                    Period taskPeriodSetPeriod = periodFromEpoch(res.getLong(5), res.getLong(6));
                    
                    Duration taskPeriodSetInterval = Duration.ofMinutes(res.getLong(7));
                    taskPeriodSetInterval = res.wasNull() ? null : taskPeriodSetInterval;
                    
                    Long taskPeriodSetConstraintPeriodStart = res.getLong(8);
                    IntervaledPeriodSet periodSetConstraint = null;
                    if(!res.wasNull())
                    {
                        Period taskPeriodSetConstraintPeriod = periodFromEpoch(taskPeriodSetConstraintPeriodStart, res.getLong(9));
                        Duration taskPeriodSetConstraintInterval = Duration.ofMinutes(res.getLong(10));
                        taskPeriodSetConstraintInterval = res.wasNull() ? null : taskPeriodSetConstraintInterval;
                        periodSetConstraint = new IntervaledPeriodSet(taskPeriodSetConstraintPeriod, taskPeriodSetConstraintInterval);
                    }
                    
                    String caretaker = res.getString(11);
                    // If caretaker is null, set null, else call getUser
                    User allocationConstraint = res.wasNull() ? null : getUser(caretaker);
                    int verification_id = res.getInt(12);
                    // This should never fail unless the database has been compromised externally,
                    // in which case data integrity has gone out the window
                    Verification verification = res.wasNull() ? null : verfList.stream().filter(verf -> verf.getID().equals(verification_id)).findFirst().get();
                    IntervaledPeriodSet periodSet = new IntervaledPeriodSet(taskPeriodSetPeriod, taskPeriodSetInterval);
                    ConstrainedIntervaledPeriodSet scheduleConstraint = new ConstrainedIntervaledPeriodSet(periodSet, periodSetConstraint);
                    
                    Map<User, Integer> preferences = new HashMap<>();
                    Map<User, Duration> efficiency = new HashMap<>();
                    Map<User, Integer> effectiveness = new HashMap<>();
                    if(!maps.isClosed())
                    {
                        while(maps.next())
                        {
                            int mapTaskID = maps.getInt(2);
                            if(taskID == mapTaskID)
                            {
                                String mapCaretaker = maps.getString(3);
                                User u = getUser(mapCaretaker);
                                Integer pref = maps.getInt(4);
                                Integer effic = maps.getInt(5);
                                Integer effect = maps.getInt(6);
                                Duration efficD = Duration.ofMinutes(effic);
                                preferences.put(u, pref);
                                efficiency.put(u, efficD);
                                effectiveness.put(u, effect);
                            }
                            else
                            {
                                break;
                            }
                        }
                    }
                    TaskPriority priority = TaskPriority.values()[taskPrio];
                    tasks.add(new Task(taskID, taskName, taskDesc, preferences, efficiency, effectiveness, priority, scheduleConstraint, allocationConstraint, verification));
                }
                return tasks;
            }
            else
            {
                throw new EmptyResultSetException();
            }
        }
        catch (SQLException | UserDoesNotExistException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public ArrayList<Completion> getCompletionList() throws EmptyResultSetException
    {
        try 
        {
            db.prepareStatement("SELECT compl_id, caretaker, start_time, compl_time, quality, notes"
                    + " FROM tblCompletion");
            ArrayList<Completion> completions = new ArrayList<>();
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                while(res.next())
                {
                    int compID = res.getInt(1);
                    String caretaker = res.getString(2);
                    User user = getUser(caretaker);
                    Long st = res.getLong(3);
                    Long et = res.getLong(4);
                    TaskCompletionQuality tcq = TaskCompletionQuality.values()[res.getInt(5)];
                    String notes = res.getString(6);
                    LocalDateTime startTime = LocalDateTime.ofEpochSecond(st, 0, ZoneOffset.UTC);
                    LocalDateTime endTime = LocalDateTime.ofEpochSecond(et, 0, ZoneOffset.UTC);
                    completions.add(new Completion(compID, user, startTime, endTime, tcq, notes));
                }
                return completions;
            }
            else
            {
                throw new EmptyResultSetException();
            }
        }
        catch(SQLException | UserDoesNotExistException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public ArrayList<VerificationExecution> getVerificationExecutionList() throws EmptyResultSetException
    {
        try 
        {
            ArrayList<Completion> completions = getCompletionList();
            ArrayList<Task> taskList = getTaskList();
            ArrayList<VerificationExecution> execList = new ArrayList();
            db.prepareStatement("SELECT exe_id, verf_id, exe_notes, exe_duration, caretaker, completion_id"
                    + " FROM tblVerfExecutions");
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                while(res.next())
                {
                   int id = res.getInt(1);
                   int verfID = res.getInt(2);
                   Verification verf = taskList.stream().filter(task -> task.getVerification().getID().equals(verfID)).findFirst().get().getVerification();
                   String notes = res.getString(3);
                   Duration d = Duration.ofMinutes(res.getInt(4));
                   String caretaker = res.getString(5);
                   User u = res.wasNull() ? null : getUser(caretaker);
                   int compid = res.getInt(6);
                   // Could be not complete!
                   Completion c = res.wasNull() ? null : completions.stream().filter(comp -> comp.getID().equals(compid)).findFirst().get();
                   execList.add(new VerificationExecution(id, verf, null, notes, d, u, c));
                }
                return execList;
            }
            else
            {
                throw new EmptyResultSetException();
            }
        }
        catch(SQLException | UserDoesNotExistException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    // GET TASK EXECUTIONS
    
    // Retrieve today's task list for a given user
    // Untested
    public ArrayList<TaskExecution> getUserDailyTaskList(String username) throws EmptyResultSetException
    {
        try 
        {
            db.prepareStatement("SELECT exe_id, task_id, exe_notes, start_datetime, end_datetime, caretaker FROM tblTaskExecutions"
                    + " WHERE caretaker = ? AND start_datetime > date('now', '+1 day', 'start of day')");
            db.add(username);
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<TaskExecution> tasks = new ArrayList();
                while(res.next())
                {
                    
                    //tasks.add(new TaskExecution(res.getString(1), periodFromEpoch(res.getLong(2), res.getLong(3))));
                }
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
    
    // Retrieve all non-priority tasks for a time period
    public ArrayList<TaskExecution> getUnallocatedTaskExecutionList(Period p) throws EmptyResultSetException
    {
        PeriodUnwrapper pw = new PeriodUnwrapper(p);
        try 
        {
            db.prepareStatement(
                    "SELECT exe_id, task_id, exe_notes, start_datetime, end_datetime, caretaker FROM tblTaskExecutions"
                    + " WHERE caretaker IS NULL AND start_datetime <= ? AND end_datetime >= ?");
            db.add(pw.start);
            db.add(pw.end == null ? 0 : pw.end);
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<TaskExecution> exes = new ArrayList();
                ArrayList<Task> tasks = getTaskList();
                while(res.next())
                {
                    int id = res.getInt(1);
                    int taskID = res.getInt(2);
                    Task task = tasks.stream().filter(t -> t.getID().equals(taskID)).findFirst().get();
                    String notes = res.getString(3);
                    Period taskP = periodFromEpoch(res.getLong(4), res.getLong(5));
                    String caretaker = res.getString(6);
                    User u = getUser(caretaker);
                    int prio = res.getInt(7);
                    TaskPriority taskPrio = TaskPriority.values()[prio];
                    exes.add(new TaskExecution(id, task, notes, taskPrio, taskP, u, null, null));
                }
                return exes;
            }
            else
            {
                throw new EmptyResultSetException();
            }
        } 
        catch (SQLException | UserDoesNotExistException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    public ArrayList<TaskExecution> getCompletedTasksForUser(User user) throws EmptyResultSetException
    {
        return getCompletedTasksForUser(user.getUsername());
    }
    
    public ArrayList<TaskExecution> getCompletedTasksForUser(String username) throws EmptyResultSetException
    {
        ArrayList<Task> tasks = getTaskList();
        ArrayList<Completion> completions = getCompletionList();
        ArrayList<VerificationExecution> verificationExes = getVerificationExecutionList();
        try 
        {
            db.prepareStatement( "SELECT exe_id, task_id, exe_notes, exe_prio, start_datetime, end_datetime, caretaker, compl_id, verf_exe_id FROM tblTaskExecutions"
                    + " WHERE caretaker = ? AND compl_id IS NOT NULL");
            db.add(username);
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<TaskExecution> exes = new ArrayList();
                while(res.next())
                {
                    int id = res.getInt(1);
                    int taskID = res.getInt(2);
                    Task task = tasks.stream().filter(t -> t.getID().equals(taskID)).findFirst().get();
                    String notes = res.getString(3);
                    TaskPriority prio = TaskPriority.values()[res.getInt(4)];
                    Period taskP = periodFromEpoch(res.getLong(5), res.getLong(6));
                    String caretaker = res.getString(7);
                    User u = res.wasNull() ? null : getUser(caretaker);
                    int compID = res.getInt(8);
                    Completion c = completions.stream().filter(comp -> comp.getID().equals(compID)).findFirst().get();
                    int verfExeID = res.getInt(9);
                    TaskExecution exe = new TaskExecution(id, task, notes, prio, taskP, u, c, null);
                    if(!res.wasNull())
                    {
                        VerificationExecution verfExe = verificationExes.stream().filter(verf -> verf.getID().equals(verfExeID)).findFirst().get();
                        verfExe.setTaskExec(exe);
                        exe.setVerification(verfExe);
                    }
                    exes.add(exe);
                }
                return exes;
            }
            else
            {
                throw new EmptyResultSetException();
            }
            
        } 
        catch (SQLException | UserDoesNotExistException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public ArrayList<TaskExecution> getCompletedTasksList() throws EmptyResultSetException
    {
        ArrayList<Task> tasks = getTaskList();
        ArrayList<Completion> completions = getCompletionList();
        ArrayList<VerificationExecution> verificationExes = getVerificationExecutionList();
        try 
        {
            db.prepareStatement( "SELECT exe_id, task_id, exe_notes, exe_prio, start_datetime, end_datetime, caretaker, compl_id, verf_exe_id FROM tblTaskExecutions"
                    + " WHERE compl_id IS NOT NULL");
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<TaskExecution> exes = new ArrayList();
                while(res.next())
                {
                    int id = res.getInt(1);
                    int taskID = res.getInt(2);
                    Task task = tasks.stream().filter(t -> t.getID().equals(taskID)).findFirst().get();
                    String notes = res.getString(3);
                    TaskPriority prio = TaskPriority.values()[res.getInt(4)];
                    Period taskP = periodFromEpoch(res.getLong(5), res.getLong(6));
                    String caretaker = res.getString(7);
                    User u = res.wasNull() ? null : getUser(caretaker);
                    int compID = res.getInt(8);
                    Completion c = completions.stream().filter(comp -> comp.getID().equals(compID)).findFirst().get();
                    int verfExeID = res.getInt(9);
                    TaskExecution exe = new TaskExecution(id, task, notes, prio, taskP, u, c, null);
                    if(!res.wasNull())
                    {
                        VerificationExecution verfExe = verificationExes.stream().filter(verf -> verf.getID().equals(verfExeID)).findFirst().get();
                        verfExe.setTaskExec(exe);
                        exe.setVerification(verfExe);
                    }
                    exes.add(exe);
                }
                return exes;
            }
            else
            {
                throw new EmptyResultSetException();
            }
            
        } 
        catch (SQLException | UserDoesNotExistException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    // SUBMISSIONS
    
    /**
     * Adds a new task to the database. Allows for duplicates.<p>
     * Automatically sets the ID of Task, so that TaskExecutions can reference it.
     * Automatically submits Verifications.
     * @param task The task object containing all task-related information.
     * @return A boolean representing success.
     */
    public boolean submitTask(Task task)
    {
        try     
        {
            if(task.getVerification() != null)
            {
                if(submitVerification(task.getVerification()))
                    db.add(task.getVerification().getID());
            }
            db.prepareStatement(
                    "INSERT INTO tblTasks (task_name, task_desc, task_priority, "
                    + "task_intervaled_period_start, intervaled_period_end, period_interval, "
                    + "intervaled_period_constraint_start, invervaled_period_constraint_end, constraint_interval,"
                    + "allocation, verification_id)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            db.add(task.getName());
            db.add(task.getNotes());
            db.add(task.getStandardPriority().ordinal());
            
            ConstrainedIntervaledPeriodSet schCon = task.getScheduleConstraint();
            IntervaledPeriodSet periodSet = schCon.periodSet();
            
            PeriodUnwrapper periodSetPeriod = new PeriodUnwrapper(periodSet.referencePeriod());
            db.add(periodSetPeriod.start);
            db.add(periodSetPeriod.end);
            
            Duration periodSetInterval = periodSet.interval();
            if(periodSetInterval != null)
            {
                long pSI = periodSetInterval.toMinutes();
                db.add(pSI);
            }
            else
                db.addNull();
            
            IntervaledPeriodSet periodSetConstraint = schCon.periodSetConstraint();
            if(periodSetConstraint != null)
            {

                PeriodUnwrapper constrainPeriod = new PeriodUnwrapper(periodSetConstraint.referencePeriod());
                db.add(constrainPeriod.start);
                db.add(constrainPeriod.end);
                Duration constrainInterval = periodSetConstraint.interval();
                if(constrainInterval != null)
                {
                    long cI = constrainInterval.toMinutes();
                    db.add(cI);
                }
                else
                    db.addNull();
            }
            else
            {
                for(int i =0;i<3;i++)
                    db.addNull();
            }
            
            if(task.getAllocationConstraint() != null)
            {
                db.add(task.getAllocationConstraint().getUsername());
            }
            else
            {
                db.addNull();
            }
            if(task.getVerification() != null)
            {
                db.add(task.getVerification().getID());
            }
            else
                db.addNull();
            
            // Need to save all the temporal rules here
            if(db.executePrepared())
            {
                task.setID(getLastTaskID());
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
    
    // Insert a new taskexecution
    public boolean submitTaskExecution(TaskExecution exe)
    {
        try 
        {
            List l = new ArrayList(){{add(exe);}};
            return submitTaskExecutions(l);
        } 
        catch (EmptyInputException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    // Insert all new taskexecutions in one go
    public boolean submitTaskExecutions(List<TaskExecution> tasks) throws EmptyInputException
    {
        try 
        {
            if(tasks.isEmpty())
                throw new EmptyInputException();
            db.prepareStatement(
                    "INSERT INTO tblTaskExecutions (task_id, exe_notes, exe_prio, start_datetime, end_datetime, caretaker, compl_id, verf_exe_id)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            for(TaskExecution task: tasks)
            {
                db.add(task.getTask().getID());
                db.add(task.getNotes());
                PeriodUnwrapper pw = new PeriodUnwrapper(task.getPeriod());
                db.add(pw.start);
                db.add(pw.end);
                User u = task.getAllocation();
                if(u != null)
                    db.add(u.getUsername());
                else
                    db.addNull();
                db.addNull(); // Completion of a new task execution should always be null
                VerificationExecution verf = task.getVerification();
                if(verf != null)
                    db.add(verf.getID());
                else
                    db.addNull();
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
    
    public boolean submitVerification(Verification verf)
    {
        try 
        { 
            db.prepareStatement("INSERT INTO tblVerifications (verf_notes, verf_priority, verf_duration, verf_caretaker)"
                    + " VALUES (?, ?, ?, ?)");
            db.add(verf.getNotes());
            db.add(verf.getStandardPriority().ordinal());
            
            Duration d = verf.getStandardDeadline();
            if(d != null)
                db.add(d.toMinutes());
            else
                db.addNull();
            
            User u = verf.getAllocationConstraint();
            if(u != null)
                db.add(u.getUsername());
            else
                db.addNull();
            
            if(db.executePrepared())
                verf.setID(getLastVerificationID());
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public boolean submitVerificationExecution(VerificationExecution verf)
    {
        try 
        {
            List l = new ArrayList(){{add(verf);}};
            return submitTaskExecutions(l);
        } 
        catch (EmptyInputException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean submitVerificationExecutions(List<VerificationExecution> verfs) throws EmptyInputException
    {
        try 
        {
            if(verfs.isEmpty())
                throw new EmptyInputException();
            db.prepareStatement(
                    "INSERT INTO tblTaskExecutions (verf_id, exe_notes, exe_duration, caretaker, compl_id)"
                    + " VALUES (?, ?, ?, ?, ?)");
            for(VerificationExecution verf: verfs)
            {
                db.add(verf.getID());
                db.add(verf.getNotes());
                db.add(verf.getDeadline().toMinutes());
                User u = verf.getAllocation();
                if(u != null)
                    db.add(u.getUsername());
                else
                    db.addNull();
                db.addNull(); // Completion of a new verification execution should always be null
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
    
    public boolean submitCompletion(Completion comp)
    {
        try 
        {
            db.prepareStatement(
                    "INSERT INTO tblCompletions (caretaker, start_time, compl_time, quality, notes)"
                    + " VALUES (?, ?, ?, ?, ?)");
            db.add(comp.getStaff().getUsername());
            db.add(comp.getStartTime().toEpochSecond(ZoneOffset.UTC));
            db.add(comp.getCompletionTime().toEpochSecond(ZoneOffset.UTC));
            db.add(comp.getWorkQuality().ordinal());
            db.add(comp.getNotes());
            return db.executePrepared();
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void logDB(String info)
    {
        try
        {
            db.prepareStatement("INSERT INTO tblSystemLog (log_info, log_timestamp)"
                    + " VALUES (?, ?)");
            db.add(info);
            db.add(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            db.executePrepared();
        }
        catch(SQLException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
