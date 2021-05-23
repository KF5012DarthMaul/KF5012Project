package dbmgr;

import dbmgr.DBConnection.DBPreparedStatement;
import dbmgr.DBExceptions.*;
import domain.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import kf5012darthmaulapplication.PermissionManager;
import kf5012darthmaulapplication.PermissionManager.AccountType;
import kf5012darthmaulapplication.User;
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
    private final List<User> userCache;
    
    // These caches are populated once
    private boolean tasksCached; // set by getAllTasks()
    private boolean execsCached; // set by getAllTasksExecutions()
    private final Map<Integer, Task> taskCache;
    private final Map<Integer, Task> deletedTaskCache;
    private final Map<Integer, TaskExecution> taskExecutionCache;
    private final Map<Integer, Verification> verificationCache;
    private final Map<Integer, Completion> completionCache;
    /** 
     * This DBAbstraction class provides methods for other classes to use that permit interaction with a database.<p>
     * These methods utilize constructed SQL and prepared statements, preventing SQL injection.<p>
     * This is a singleton pattern class. Call DBAbstraction.getInstance() to get a reference to the class object.
     */
    private DBAbstraction() throws FailedToConnectException
    {
        db = DBConnection.getInstance();
        userCache = getAllUsersInternal();
        taskCache = new HashMap<>();
        deletedTaskCache = new HashMap<>();
        taskExecutionCache = new HashMap<>();
        verificationCache = new HashMap<>();
        completionCache = new HashMap<>();
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

    /* Utilities
     * -------------------- */
    
    /** 
     * Convert from 2 Epoch seconds of type Long to a {@link Period} object.<p>
     * If the second parameter is 0 or null, the {@link Period} is initialized with only the start.<p>
     * The first parameeter cannot be null.<p>
     * @param start
     * @param end
     * @return A new {@link Period} Object
     */
    private Period periodFromEpoch(Long start, Long end)
    {
        LocalDateTime dtStart = LocalDateTime.ofEpochSecond(start, 0, ZoneOffset.UTC);
        if(end == null || end == 0)
            return new Period(dtStart);
        LocalDateTime dtEnd = LocalDateTime.ofEpochSecond(end, 0, ZoneOffset.UTC);
        return new Period(dtStart, dtEnd);
    }

    /** 
     * Unwrapper class that converts Period to Epoch seconds of type Long
     */
    private final class PeriodUnwrapper
    {
        Long start;
        Long end;
        /**
         * Unwraps a {@link Period} into 2 long integers.
         * @param p The {@link Period} to unwrap
         */
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
     * Creates a list of type T containing a single item of type T.
     * @param <T> The type
     * @param item The item that the {@link List} will contain.
     * @return A {@link List} of type T containing that item
     */
    private <T> List<T> listOfSingleItem(T item)
    {
        List<T> l = new ArrayList<>();
        l.add(item);
        return l;
    }
    
    /* Main API
     * -------------------- */
    
    /**
     * Attempts to create a new {@link User} in the Database.<p>
     * If the {@link User} already exists, this function will return false.
     * @param user The {@link User} object with data.
     * @param hashedPassword A password (encrypted).
     * @return A boolean representing success.
     * @throws UserAlreadyExistsException
     */
    public boolean createUser(User user, String hashedPassword) throws UserAlreadyExistsException
    {
        return createUser(user.getUsername(), user.getDisplayName(), hashedPassword, user.getAccountType());
    }

    /**
     * Attempts to create a new {@link User} in the Database.<p>
     * If the {@link User} already exists, this function will return false.
     * @param username The username of the new {@link User}.
     * @param name The {@link User}'s display name
     * @param hashedPassword A password (encrypted).
     * @param accountType The Account type of the {@link User}
     * @return A boolean representing success.
     * @throws UserAlreadyExistsException
     */
    public boolean createUser(String username, String name, String hashedPassword, AccountType accountType) throws UserAlreadyExistsException
    {
        if(!doesUserExist(username))
        {
            try 
            {
                DBPreparedStatement stmt = db.prepareStatement(
                        "INSERT INTO tblUsers (username, hashpass, display_name, account_type)"
                        + " VALUES (?, ?, ?, ?)");
                stmt.add(username);
                stmt.add(hashedPassword);
                stmt.add(name);
                stmt.add(accountType.ordinal());
                boolean b = stmt.executePrepared();
                userCache.add(new User(username, name, accountType));
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
     * Tests whether a {@link User} exists inside the database.
     * @param user The {@link User} to test.
     * @return Returns true if the {@link User} exists in the database, false if it doesn't.
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
     * Gets all fields of a {@link User} from the database.<p>
     * Populates a new {@link User} object and returns it.
     * @param username The {@link User} whose data to retrieve.
     * @return Returns the new {@link User} object.
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
     * Gets the password (encrpyted) for a key {@link User} from the database.
     * @param username The username whose password to retrieve.
     * @return A string containing the password (encrypted).
     * @throws UserDoesNotExistException
     */
    public String getHashedPassword(String username) throws UserDoesNotExistException
    {
        try 
        {
            DBPreparedStatement stmt = db.prepareStatement("SELECT hashpass FROM tblUsers WHERE username = ?");
            stmt.add(username);
            ResultSet res =  stmt.executePreparedQuery();
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
     * Gets the password (encrpyted) for a {@link User} from the database.
     * @param user The {@link User} whose password to retrieve.
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
                DBPreparedStatement stmt = db.prepareStatement("UPDATE tblUsers SET hashpass = ?"
                        + " WHERE username = ?");
                stmt.add(hashedPassword);
                stmt.add(username);
                return stmt.executePrepared();
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
     * Sets a new password (encrypted) for a {@link User}.
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
     * Used to internally fill up the list of immutable {@link User} objects
     * @return The {@link ArrayList} of users.
     */
    private List<User> getAllUsersInternal()
    {
        List<User> allUsers = new ArrayList<>();
        try 
        {
            DBPreparedStatement stmt = db.prepareStatement("SELECT username, display_name, account_type FROM tblUsers");
            ResultSet res = stmt.executePreparedQuery();
            if(!res.isClosed())
            {
                while(res.next())
                {
                    allUsers.add(new User(res.getString(1), res.getString(2), PermissionManager.intToAccountType(res.getInt(3))));
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
     * Gets an {@link ArrayList} containing all {@link User}s currently registered in the database.
     * @return The {@link ArrayList} of {@link User}s.
     */
    public ArrayList<User> getAllUsers()
    {
        ArrayList<User> allUsers = new ArrayList<>(userCache);
        return allUsers;
    }

    /**
     * Update all fields for a {@link User}.
     * @param user The {@link User} object with data to update the database with.
     * @return A boolean representing success.
     * @throws UserDoesNotExistException 
     * @bug Breaks references to user objects with the same username.
     */
    public boolean updateUser(User user) throws UserDoesNotExistException
    {
        try 
        {
            if(doesUserExist(user))
            {
                DBPreparedStatement stmt = db.prepareStatement(
                        "UPDATE tblUsers SET account_type = ?, display_name = ?"
                                + " WHERE username = ?");
                stmt.add(user.getAccountType().ordinal());
                stmt.add(user.getDisplayName());
                stmt.add(user.getUsername());
                userCache.remove(getUser(user.getUsername()));
                userCache.add(user);
                return stmt.executePrepared();
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
     * Deletes a {@link User} from the database.
     * @param user The {@link User} to delete.
     * @return A boolean representing success.
     */
    public boolean deleteUser(User user)
    {
        try
        {
            DBPreparedStatement stmt = db.prepareStatement("DELETE FROM tblUsers WHERE username = ?");
            stmt.add(user.getUsername());
            boolean b = stmt.executePrepared();
            userCache.remove(user);
            return b;
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    
    /** 
     * Gets the last sequence integer for an auto-incremented primary key in a given table
     * @param s The name of the table
     * @return Integer
     */
    private int getAutoIncrement(String table)
    {
        try {
            DBPreparedStatement stmt = db.prepareStatement(
                    "SELECT seq FROM sqlite_sequence"
                            + " WHERE name = ?");
            stmt.add(table);
            ResultSet res = stmt.executePreparedQuery();
            if(!res.isClosed())
                return res.getInt(1);
            else return 0;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    /**
     * Retrieve the last inserted task's ID
     * @return int
     */
    private int getLastTaskID()
    {
        return getAutoIncrement("tblTasks");
    }

    /**
     * Retrieve the last inserted verification's ID
     * @return int
     */
    private int getLastVerificationID()
    {
        return getAutoIncrement("tblVerifications");
    }
    
    /**
     * Retrieve the last inserted completion's ID
     * @return int
     */
    private int getLastCompletionID()
    {
        return getAutoIncrement("tblCompletions");
    }
    
    /**
     * Retrieve the last inserted verification execution's ID
     * @return int
     */
    private int getLastVerificationExecutionID()
    {
        return getAutoIncrement("tblVerfExecutions");
    }
    
    /**
     * Retrieve the last inserted task execution's ID
     * @return int
     */
    private int getLastTaskExecutionID()
    {
        return getAutoIncrement("tblTaskExecutions");
    }
    
    /**
     * Internally fills up a cache.<p>
     * Returns the {@link List} of non-deleted {@link Task}s from the database, including all temporal rules<p>
     * user preference and {@link Verification} objects.<p>
     * @return The list of {@link Task}s with all valid fields populated.
     */
    public List<Task> getTaskList()
    {
        if(!tasksCached)
        {
            try 
            {
                DBPreparedStatement getTaskStmt = db.prepareStatement(
                        "SELECT task_id, task_name, task_desc, task_priority, "
                        + "task_intervaled_period_start, intervaled_period_end, period_interval, "
                        + "intervaled_period_constraint_start, invervaled_period_constraint_end, constraint_interval,"
                        + "allocation, verification_id, deleted FROM tblTasks");
                ResultSet res = getTaskStmt.executePreparedQuery(); // Get the non deleted tasks
                DBPreparedStatement getMapsStmt = db.prepareStatement(
                        "SELECT map_id, task_id, caretaker, preferences, efficiency, effectiveness"
                        + " FROM tblTaskMaps ORDER BY task_id, map_id");
                ResultSet maps = getMapsStmt.executePreparedQuery(); // Get the user preference, efficiency and effectiveness.
                DBPreparedStatement getVerfsStmt = db.prepareStatement(
                        "SELECT verf_id, verf_notes, verf_priority, verf_duration, verf_caretaker"
                        + " FROM tblVerifications");
                ResultSet verfResSet = getVerfsStmt.executePreparedQuery(); // Get the verifications.
                if(!res.isClosed()) // If there are any results
                {
                    if(!verfResSet.isClosed()) // If there are any results
                    {
                         while(verfResSet.next()) // Iterate over the verifications
                        {
                            int verfID = verfResSet.getInt(1);
                            String verfNotes = verfResSet.getString(2);
                            int verfPrio = verfResSet.getInt(3);
                            int verfDura = verfResSet.getInt(4);
                            Duration duration = verfResSet.wasNull() ? null : Duration.ofMinutes(verfDura); // If duration wasn't null in the DB, set it to a duration
                            String verfCaretaker = verfResSet.getString(5);
                            User allocation = verfResSet.wasNull() ? null : getUser(verfCaretaker);
                            TaskPriority priority = TaskPriority.values()[verfPrio];
                            verificationCache.put(verfID, new Verification(verfID, null, verfNotes, priority, duration, allocation)); // Add it to the cache
                        }
                    }

                    while(res.next()) // Iterate over all retrieved tasks
                    {
                        int taskID = res.getInt(1);
                        if (!taskCache.containsKey(taskID)) // Might prevent overriding
                        {
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
                            Verification verification = verificationCache.get(verification_id); // get the verification or null
                            
                            IntervaledPeriodSet periodSet = new IntervaledPeriodSet(taskPeriodSetPeriod, taskPeriodSetInterval);
                            ConstrainedIntervaledPeriodSet schedule = new ConstrainedIntervaledPeriodSet(periodSet, periodSetConstraint);

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
                            Task t = new Task(taskID, taskName, taskDesc, preferences, efficiency, effectiveness, priority, schedule, allocationConstraint, verification);
                            if(verification != null)
                            {
                                verification.setTask(t);
                            }
                            boolean deleted = res.getInt(13) != 0;
                            if(deleted)
                                deletedTaskCache.put(t.getID(), t);
                            else
                                taskCache.put(t.getID(), t);
                        }
                    }
                }
                tasksCached = true;
            }
            catch (SQLException | UserDoesNotExistException ex) 
            {
                Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        List<Task> allTasks = new ArrayList<>(taskCache.values());
        return allTasks;
    }

    /**
     * Fills up the internal cache of {@link Completion} objects to be referenced in other methods.
     */
    private void fillCompletionCache()
    {
        try 
        {
            DBPreparedStatement stmt = db.prepareStatement("SELECT compl_id, caretaker, start_time, compl_time, quality, notes"
                    + " FROM tblCompletions");
            ResultSet res = stmt.executePreparedQuery();
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
                    completionCache.put(compID, new Completion(compID, user, startTime, endTime, tcq, notes));
                }
            }
        }
        catch(SQLException | UserDoesNotExistException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets all {@link VerificationExecution} objects from the database.
     * @return A {@link Map} mapping Integers to {@link VerificationExecution}.
     */
    private Map<Integer, VerificationExecution> getVerificationExecutionList()
    {
        try 
        {
            Map<Integer, VerificationExecution> execList = new HashMap<>();
            DBPreparedStatement stmt = db.prepareStatement("SELECT exe_id, verf_id, exe_notes, exe_duration, caretaker, compl_id"
                    + " FROM tblVerfExecutions");
            ResultSet res = stmt.executePreparedQuery();
            if(!res.isClosed())
            {
                while(res.next())
                {
                    int id = res.getInt(1);
                    int verfID = res.getInt(2);
                    Verification verf = verificationCache.get(verfID);
                    String notes = res.getString(3);
                    Duration d = Duration.ofMinutes(res.getInt(4));
                    String caretaker = res.getString(5);
                    User u = res.wasNull() ? null : getUser(caretaker);
                    int compID = res.getInt(6);
                    // Could be not complete!
                    Completion c = completionCache.get(compID);
                    execList.put(id, new VerificationExecution(id, verf, null, notes, d, u, c));
                }
                return execList;
            }
        }
        catch(SQLException | UserDoesNotExistException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new HashMap<>();
    }

    // GET TASK EXECUTIONS
    
    /**
     * Gets an {@link ArrayList} containing all {@link TaskExecution} objects and their respective<p>
     * {@link Task}, {@link Completion}, {@link VerificationExecution} and {@link User} objects.
     * @return An {@link ArrayList} containing all {@link TaskExecution} objects.
     */
    public ArrayList<TaskExecution> getTaskExecutionList()
    {
        if(!execsCached)
        {
        	if(!tasksCached)
                getTaskList();
        	
            fillCompletionCache();
            Map<Integer, Task> allTasks = new HashMap<>(taskCache);
            allTasks.putAll(deletedTaskCache); // Allow referencing of deleted tasks
            Map<Integer, VerificationExecution> verificationExes = getVerificationExecutionList();
            try 
            {
                DBPreparedStatement stmt = db.prepareStatement("SELECT exe_id, task_id, exe_notes, exe_prio, period_constraint_start, period_constraint_end, start_datetime, end_datetime, caretaker, compl_id, verf_exe_id"
                        + " FROM tblTaskExecutions");
                ResultSet res = stmt.executePreparedQuery();
                if(!res.isClosed())
                {
                    while(res.next())
                    {
                        int id = res.getInt(1);
                        int taskID = res.getInt(2);
                        Task task = allTasks.get(taskID);
                        String notes = res.getString(3);
                        TaskPriority prio = TaskPriority.values()[res.getInt(4)];
                        Period taskPConstraint = periodFromEpoch(res.getLong(5), res.getLong(6));
                        Period taskP = periodFromEpoch(res.getLong(7), res.getLong(8));
                        String caretaker = res.getString(9);
                        User u = res.wasNull() ? null : getUser(caretaker);
                        int compID = res.getInt(10);
                        Completion c = completionCache.get(compID);
                        int verfExeID = res.getInt(11);
                        TaskExecution exe = new TaskExecution(id, task, notes, prio, taskPConstraint, taskP, u, c, null);
                        if(!res.wasNull())
                        {
                            VerificationExecution verfExe = verificationExes.get(verfExeID);
                            if(verfExe != null)
                                verfExe.setTaskExec(exe);
                            exe.setVerification(verfExe);
                        }
                        taskExecutionCache.put(exe.getID(), exe);
                    }
                }
                execsCached = true;
            }
            catch (SQLException | UserDoesNotExistException ex) 
            {
                Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ArrayList<TaskExecution> allTasks = new ArrayList<>(taskExecutionCache.values());
        return allTasks;
    }
    
    // SUBMISSIONS

    /**
     * Adds a new or updates an existing {@link Task} in the database database. Allows for duplicate names.<p>
     * Automatically sets the ID of {@link Task}, so that {@link TaskExecution} objects can reference it.<p>
     * Automatically submits a linked {@link Verification}.<p>
     * Insert and updates are determined by:<p>
     * {@code statement = task.getID() == null ? insertStatement : updateStatement;}
     * @param task The task object containing all task-related information.
     * @return A boolean representing success.
     */
    public boolean submitTask(Task task)
    {
        try     
        {
            if(task.getVerification() != null)
            {
                submitVerification(task.getVerification());
            }
            DBPreparedStatement stmt;
            if(task.getID() == null)
            {
                stmt = db.prepareStatement(
                    "INSERT INTO tblTasks (task_name, task_desc, task_priority, "
                    + "task_intervaled_period_start, intervaled_period_end, period_interval, "
                    + "intervaled_period_constraint_start, invervaled_period_constraint_end, constraint_interval,"
                    + "allocation, verification_id)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }
            else
            {
                stmt = db.prepareStatement(
                        "UPDATE tblTasks SET task_name = ?, task_desc = ?, task_priority = ?, task_intervaled_period_start = ?, intervaled_period_end = ?, period_interval = ?, intervaled_period_constraint_start = ?, invervaled_period_constraint_end = ?, constraint_interval = ?, allocation = ?, verification_id = ?"
                                    + " WHERE task_id = ?");
            }

            stmt.add(task.getName());
            stmt.add(task.getNotes());
            stmt.add(task.getStandardPriority().ordinal());

            ConstrainedIntervaledPeriodSet sch = task.getSchedule();
            IntervaledPeriodSet periodSet = sch.periodSet();

            PeriodUnwrapper periodSetPeriod = new PeriodUnwrapper(periodSet.referencePeriod());
            stmt.add(periodSetPeriod.start);
            stmt.add(periodSetPeriod.end);

            Duration periodSetInterval = periodSet.interval();
            if(periodSetInterval != null)
            {
                long pSI = periodSetInterval.toMinutes();
                stmt.add(pSI);
            }
            else
                stmt.addNull();

            IntervaledPeriodSet periodSetConstraint = sch.periodSetConstraint();
            if(periodSetConstraint != null)
            {

                PeriodUnwrapper constrainPeriod = new PeriodUnwrapper(periodSetConstraint.referencePeriod());
                stmt.add(constrainPeriod.start);
                stmt.add(constrainPeriod.end);
                Duration constrainInterval = periodSetConstraint.interval();
                if(constrainInterval != null)
                {
                    long cI = constrainInterval.toMinutes();
                    stmt.add(cI);
                }
                else
                    stmt.addNull();
            }
            else
            {
                for(int i =0;i<3;i++)
                    stmt.addNull();
            }

            if(task.getAllocationConstraint() != null)
            {
                stmt.add(task.getAllocationConstraint().getUsername());
            }
            else
            {
                stmt.addNull();
            }

            if(task.getVerification() != null)
            {
                stmt.add(task.getVerification().getID());
            }
            else
                stmt.addNull();

            if(task.getID() == null)
            {
                stmt.executePrepared();
                task.setID(getLastTaskID());
            }
            else
            {
                stmt.add(task.getID());
                stmt.executePrepared();
            }
            
            taskCache.put(task.getID(), task);
            return true;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * Adds a new or updates an existing {@link TaskExecution} in the database database.<p>
     * Automatically sets the ID of {@link TaskExecution}.<p>
     * Automatically submits the linked {@link VerificationExecution} and {@link Completion} objects.<p>
     * Insert and updates are determined by:<p>
     * {@code statement = exe.getID() == null ? insertStatement : updateStatement;}
     * @param exe The {@link TaskExecution} object containing all {@link TaskExecution}-related information.
     * @return A boolean representing success.
     */
    public boolean submitTaskExecution(TaskExecution exe)
    {
        return submitTaskExecutions(listOfSingleItem(exe));
    }

    /**
     * Adds new or updates existing {@link TaskExecution}s in the database database from the {@link List}.<p>
     * Automatically sets the ID of {@link TaskExecution}s.<p>
     * Automatically submits the linked {@link VerificationExecution} and {@link Completion} objects.<p>
     * Insert and updates are determined by:<p>
     * {@code for(TaskExecution exe: exes)}<p>
     * {@code statement = exe.getID() == null ? insertStatement : updateStatement;}
     * @param exes The {@link List} of {@link TaskExecution} objects containing all {@link TaskExecution}-related information.
     * @return A boolean representing success.
     */
    public boolean submitTaskExecutions(List<TaskExecution> exes)
    {
        try 
        {
            if(exes == null || exes.isEmpty())
                return false;
            DBPreparedStatement insertStatement = db.prepareStatement("INSERT INTO tblTaskExecutions (task_id, exe_notes, exe_prio, period_constraint_start, period_constraint_end, start_datetime, end_datetime, caretaker, compl_id, verf_exe_id)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            DBPreparedStatement updateStatement = db.prepareStatement("UPDATE tblTaskExecutions SET task_id = ?, exe_notes = ?, exe_prio = ?, period_constraint_start = ?, period_constraint_end = ?, start_datetime = ?, end_datetime = ?, caretaker = ?, compl_id = ?, verf_exe_id = ?"
                    + " WHERE exe_id = ?");
            int taskExeID = getLastTaskExecutionID()+1;
            int verfExeID = getLastVerificationExecutionID()+1;
            int completionID = getLastCompletionID()+1;
            ArrayList<VerificationExecution> verfExecutions = new ArrayList<>();
            ArrayList<Completion> completions = new ArrayList<>();
            for(TaskExecution exe: exes)
            {
                DBPreparedStatement stmt = exe.getID() == null ? insertStatement : updateStatement;
                stmt.add(exe.getTask().getID());
                stmt.add(exe.getNotes());
                stmt.add(exe.getPriority().ordinal());
                
                PeriodUnwrapper pw = new PeriodUnwrapper(exe.getPeriodConstraint());
                stmt.add(pw.start);
                stmt.add(pw.end);
                
                pw = new PeriodUnwrapper(exe.getPeriod());
                stmt.add(pw.start);
                stmt.add(pw.end);
                
                User u = exe.getAllocation();
                if(u != null)
                    stmt.add(u.getUsername());
                else
                    stmt.addNull();

                Completion c = exe.getCompletion();
                if(c != null)
                {
                    if(c.getID() == null)
                    {
                        stmt.add(completionID++);
                    }
                    else
                    {
                        stmt.add(c.getID());
                    }
                    completions.add(c);
                }
                else
                {
                    stmt.addNull(); // Completion of a new task execution should always be null
                }

                // If there is a verification execution linked, we must add it to a batch list
                VerificationExecution verf = exe.getVerification();
                if(verf != null)
                {
                    if(verf.getID() == null)
                        stmt.add(verfExeID++);
                    else
                        stmt.add(verf.getID());

                    verfExecutions.add(verf);
                }
                else
                {
                    stmt.addNull();
                }

                if(exe.getID() != null)
                {
                    stmt.add(exe.getID());
                }
                else
                {
                    exe.setID(taskExeID++);
                }

                taskExecutionCache.put(exe.getID(), exe);
                stmt.batch();
            }
            insertStatement.executeBatch();
            updateStatement.executeBatch();
            submitCompletions(completions);
            submitVerificationExecutions(verfExecutions);
            return true;
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Adds a new or updates an existing {@link Verification} in the database database.<p>
     * Automatically sets the ID of the {@link Verification}.<p>
     * Insert and updates are determined by:<p>
     * {@code statement = verf.getID() == null ? insertStatement : updateStatement;}
     * @param verf The {@link List} of {@link Verification} objects containing all {@link Verification}-related information.
     * @return A boolean representing success.
     */
    private boolean submitVerification(Verification verf)
    {
        try 
        { 
            DBPreparedStatement stmt;
            if(verf.getID() == null)
            {
                stmt = db.prepareStatement("INSERT INTO tblVerifications (verf_notes, verf_priority, verf_duration, verf_caretaker)"
                    + " VALUES (?, ?, ?, ?)");
            }
            else
            {
                stmt = db.prepareStatement("UPDATE tblVerifications SET verf_notes = ?, verf_priority = ?, verf_duration = ?, verf_caretaker = ?"
                        + " WHERE verf_id = ?");
            }
            stmt.add(verf.getNotes());
            stmt.add(verf.getStandardPriority().ordinal());

            Duration d = verf.getStandardDeadline();
            if(d != null)
                stmt.add(d.toMinutes());
            else
                stmt.addNull();

            User u = verf.getAllocationConstraint();
            if(u != null)
                stmt.add(u.getUsername());
            else
                stmt.addNull();

            if(verf.getID() == null)
            {
                stmt.executePrepared();
                verf.setID(getLastVerificationID());
            }
            else
            {
                stmt.add(verf.getID());
                stmt.executePrepared();
            }
            verificationCache.put(verf.getID(), verf);
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Adds new or updates existing {@link VerificationExecution}s in the database database from the {@link List}.<p>
     * Automatically sets the ID of {@link VerificationExecution}s.<p>
     * Automatically submits the linked {@link Completion} objects.<p>
     * Insert and updates are determined by:<p>
     * {@code for(VerificationExecution verf: verfs)}<p>
     * {@code statement = verf.getID() == null ? insertStatement : updateStatement;}
     * @param exes The {@link List} of {@link VerificationExecution} objects containing all {@link VerificationExecution}-related information.
     * @return A boolean representing success.
     */
    private boolean submitVerificationExecutions(List<VerificationExecution> verfs)
    {
        try 
        {
            if(verfs == null || verfs.isEmpty())
                return false;
            DBPreparedStatement insertStatement = db.prepareStatement("INSERT INTO tblVerfExecutions (verf_id, exe_notes, exe_duration, caretaker, compl_id)"
                    + " VALUES (?, ?, ?, ?, ?)");
            DBPreparedStatement updateStatement = db.prepareStatement("UPDATE tblVerfExecutions SET verf_id = ?, exe_notes = ?, exe_duration = ?, caretaker = ?, compl_id = ?"
                    + " WHERE exe_id = ?");
            int completionID = getLastCompletionID()+1;
            int verfExecID = getLastVerificationExecutionID()+1;
            ArrayList<Completion> completions = new ArrayList<>();
            for(VerificationExecution verf: verfs)
            {
                DBPreparedStatement stmt = verf.getID() == null ? insertStatement : updateStatement;
                if(verf.getVerification() != null)
                    stmt.add(verf.getVerification().getID());
                else
                    stmt.addNull();

                stmt.add(verf.getNotes());
                stmt.add(verf.getDeadline().toMinutes());
                User u = verf.getAllocation();
                if(u != null)
                    stmt.add(u.getUsername());
                else
                    stmt.addNull();

                if(verf.getCompletion() != null)
                {
                    if(verf.getCompletion().getID() == null)
                    {
                        stmt.add(completionID++);
                    }
                    else
                    {
                        stmt.add(verf.getCompletion().getID());
                    }
                    completions.add(verf.getCompletion());
                }
                else
                {
                    stmt.addNull();
                }

                if(verf.getID() != null)
                {
                    stmt.add(verf.getID());
                }
                else
                {
                    verf.setID(verfExecID++);
                }
                stmt.batch();
            }
            insertStatement.executeBatch();
            updateStatement.executeBatch();
            submitCompletions(completions);
            return true;
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    /**
     * Adds new or updates existing {@link Completion}s in the database database from the {@link List}.<p>
     * Automatically sets the ID of {@link Completion}s.<p>
     * Insert and updates are determined by:<p>
     * {@code for(Completion comp: completions)}<p>
     * {@code statement = comp.getID() == null ? insertStatement : updateStatement;}
     * @param exes The {@link List} of {@link Completion} objects containing all {@link Completion}-related information.
     * @return A boolean representing success.
     */
    private boolean submitCompletions(List<Completion> completions)
    {
        try 
        {
            if(completions == null || completions.isEmpty())
                return false;
            DBPreparedStatement insertStatement = db.prepareStatement("INSERT INTO tblCompletions (caretaker, start_time, compl_time, quality, notes)"
                + " VALUES (?, ?, ?, ?, ?)");
            DBPreparedStatement updateStatement = db.prepareStatement("UPDATE tblCompletions SET caretaker = ?, start_time = ?, compl_time = ?, quality = ?, notes = ?"
                + " WHERE compl_id = ?");
            int completionID = getLastCompletionID()+1;
            for(Completion comp: completions)
            {
                DBPreparedStatement stmt = comp.getID() == null ? insertStatement : updateStatement;
                stmt.add(comp.getStaff().getUsername());
                stmt.add(comp.getStartTime().toEpochSecond(ZoneOffset.UTC));
                stmt.add(comp.getCompletionTime().toEpochSecond(ZoneOffset.UTC));
                stmt.add(comp.getWorkQuality().ordinal());
                stmt.add(comp.getNotes());
                if(comp.getID() != null)
                {
                    stmt.add(comp.getID());
                }
                else if(comp.getID() == null)
                {
                    comp.setID(completionID++);
                }
                stmt.batch();
                completionCache.put(comp.getID(), comp);
            }
            insertStatement.executeBatch();
            updateStatement.executeBatch();
            return true;
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
   /**
    * Marks a {@link Task} as deleted in the database.
    * @param t The task to mark as deleted.
    * @return A boolean representing success.
    */
    public boolean deleteTask(Task t)
    {
        return deleteTasks(listOfSingleItem(t));
    }
    
    /**
     * Marks all {@link Task}s in a {@link List} as deleted.
     * @param tasks The {@link List} of tasks to mark as deleted.
     * @return A boolean representing success.
     */
    public boolean deleteTasks(List<Task> tasks)
    {
        try 
        {
            DBPreparedStatement stmt = db.prepareStatement("UPDATE tblTasks SET deleted = ?"
                + " WHERE task_id = ?");
            for(Task task: tasks)
            {
                stmt.add(1);
                stmt.add(task.getID());
                stmt.batch();
                taskCache.remove(task.getID());
                deletedTaskCache.put(task.getID(), task);
            }
            stmt.executeBatch();
            return true;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    // Delete Task Execution?
    public boolean deleteTaskExecution(TaskExecution exe)
    {
        return deleteTaskExecutions(listOfSingleItem(exe));
    }
    
    /**
     * Delete all {@link TaskExecution}s and linked {@link VerificationExecution}s in a {@link List} from the database.
     * @param execs The {@link List} of {@link TaskExecution}s and associated {@link VerificationExecution}s to delete.
     * @return A boolean representing success.
     */
    public boolean deleteTaskExecutions(List<TaskExecution> execs)
    {
        try 
        {
            DBPreparedStatement taskStmt = db.prepareStatement("DELETE FROM tblTaskExecutions "
                + " WHERE exe_id = ?");
            DBPreparedStatement verfStmt = db.prepareStatement("DELETE FROM tblVerfExecutions "
                + " WHERE exe_id = ?");
            for(TaskExecution exec: execs)
            {
                taskStmt.add(exec.getID());
                if(exec.getVerification() != null)
                {
                    verfStmt.add(exec.getVerification().getID());
                    verfStmt.batch();
                }
                taskExecutionCache.remove(exec.getID());
                taskStmt.batch();
            }
            taskStmt.executeBatch();
            verfStmt.executeBatch();
            return true;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
