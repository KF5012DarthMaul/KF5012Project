package dbmgr;
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
    private final ArrayList<User> userCache;
    
    private final Map<Integer, Task> taskCache;
    private final Map<Integer, TaskExecution> taskExecutionCache;
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
        taskExecutionCache = new HashMap<>();
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

    /* Main API
     * -------------------- */
    
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
    /*public AccountType getAccountType(String username) throws UserDoesNotExistException
    {
        return getUser(username).getAccountType();
    }*/
    
    private ArrayList<User> getAllUsersInternal()
    {
        ArrayList<User> allUsers = new ArrayList<>();
        try 
        {
            db.prepareStatement("SELECT username, display_name, account_type FROM tblUsers");
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                while(res.next())
                {
                    allUsers.add(new User(res.getString(1), PermissionManager.intToAccountType(res.getInt(3))));
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
     * @return 
     */
    public ArrayList<User> getAllUsers()
    {
        ArrayList<User> allUsers = new ArrayList<>();
        allUsers.addAll(userCache);
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
                db.add(user.getAccountType().ordinal());
                db.add(user.getUsername());
                db.add(user.getUsername());
                userCache.remove(getUser(user.getUsername()));
                userCache.add(user);
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

    private int getAutoIncrement(String s)
    {
        try {
            db.prepareStatement(
                    "SELECT seq FROM sqlite_sequence"
                            + " WHERE name = ?");
            db.add(s);
            ResultSet res =  db.executePreparedQuery();
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

    // Retrieve the last inserted task's ID
    private int getLastTaskID()
    {
        return getAutoIncrement("tblTasks");
    }

    // Retrieve the last inserted task's ID
    private int getLastVerificationID()
    {
        return getAutoIncrement("tblVerifications");
    }

    private int getLastCompletionID()
    {
        return getAutoIncrement("tblCompletions");
    }

    private int getLastVerificationExecutionID()
    {
        return getAutoIncrement("tblVerfExecutions");
    }

    private int getLastTaskExecutionID()
    {
        return getAutoIncrement("tblTaskExecutions");
    }
    
    // Retrieve all unique tasks and temporal rules
    public ArrayList<Task> getTaskList()
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
                    + " FROM tblTaskMaps ORDER BY task_id, map_id");
            ResultSet maps = db.executePreparedQuery();
            db.prepareStatement(
                    "SELECT verf_id, verf_notes, verf_priority, verf_duration, verf_caretaker"
                    + " FROM tblVerifications");
            ResultSet verfResSet = db.executePreparedQuery();
            if(!res.isClosed())
            {
                // Get a list of verifications
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
                        verfList.add(new Verification(verfID, null, verfNotes, priority, duration, allocation));
                    }
                }

                while(res.next())
                {
                    int taskID = res.getInt(1);
                    if (!taskCache.containsKey(taskID)) 
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
                        // This should never fail unless the database has been compromised externally,
                        // in which case data integrity has gone out the window
                        Verification verification = res.wasNull() ? null : verfList.stream().filter(verf -> verf.getID().equals(verification_id)).findFirst().get();
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
                        taskCache.put(t.getID(), t);
                    }
                }
                ArrayList<Task> allTasks = new ArrayList<>();
                allTasks.addAll(taskCache.values());
                return allTasks;
            }
        }
        catch (SQLException | UserDoesNotExistException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    private ArrayList<Completion> getCompletionList()
    {
        try 
        {
            db.prepareStatement("SELECT compl_id, caretaker, start_time, compl_time, quality, notes"
                    + " FROM tblCompletions");
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
        }
        catch(SQLException | UserDoesNotExistException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    private ArrayList<VerificationExecution> getVerificationExecutionList()
    {
        try 
        {
            ArrayList<Completion> completions = getCompletionList();
            ArrayList<Task> taskList = getTaskList();
            ArrayList<VerificationExecution> execList = new ArrayList<>();
            db.prepareStatement("SELECT exe_id, verf_id, exe_notes, exe_duration, caretaker, compl_id"
                    + " FROM tblVerfExecutions");
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                while(res.next())
                {
                   int id = res.getInt(1);
                   int verfID = res.getInt(2);
                   Verification verf = taskList.stream().filter(task -> task.getVerification() != null && task.getVerification().getID().equals(verfID)).findFirst().get().getVerification();
                   String notes = res.getString(3);
                   Duration d = Duration.ofMinutes(res.getInt(4));
                   String caretaker = res.getString(5);
                   User u = res.wasNull() ? null : getUser(caretaker);
                   int compid = res.getInt(6);
                   // Could be not complete!
                   Completion c = res.wasNull() ? null : completions.stream().filter(comp -> comp.getID().equals(compid)).findFirst().orElse(null);;
                   execList.add(new VerificationExecution(id, verf, null, notes, d, u, c));
                }
                return execList;
            }
        }
        catch(SQLException | UserDoesNotExistException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    // GET TASK EXECUTIONS

    // Retrieve today's task list for a given user
    // Untested
    public ArrayList<TaskExecution> getUserDailyTaskList(String username)
    {
        try 
        {
            db.prepareStatement("SELECT exe_id, task_id, exe_notes, start_datetime, end_datetime, caretaker FROM tblTaskExecutions"
                    + " WHERE caretaker = ? AND start_datetime > date('now', '+1 day', 'start of day')");
            db.add(username);
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                ArrayList<TaskExecution> tasks = new ArrayList<>();
                while(res.next())
                {

                    //tasks.add(new TaskExecution(res.getString(1), periodFromEpoch(res.getLong(2), res.getLong(3))));
                }
                return tasks;
            }
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return new ArrayList<>();
    }
    
    public ArrayList<TaskExecution> getTaskExecutionList()
    {
        ArrayList<Task> tasks = getTaskList();
        ArrayList<Completion> completions = getCompletionList();
        ArrayList<VerificationExecution> verificationExes = getVerificationExecutionList();
        try 
        {
            db.prepareStatement("SELECT exe_id, task_id, exe_notes, exe_prio, start_datetime, end_datetime, caretaker, compl_id, verf_exe_id FROM tblTaskExecutions");
            ResultSet res = db.executePreparedQuery();
            if(!res.isClosed())
            {
                while(res.next())
                {
                    int id = res.getInt(1);
                    if (!taskExecutionCache.containsKey(id)) 
                    {
                        int taskID = res.getInt(2);
                        Task task = tasks.stream().filter(t -> t.getID().equals(taskID)).findFirst().get();
                        String notes = res.getString(3);
                        TaskPriority prio = TaskPriority.values()[res.getInt(4)];
                        Period taskP = periodFromEpoch(res.getLong(5), res.getLong(6));
                        String caretaker = res.getString(7);
                        User u = res.wasNull() ? null : getUser(caretaker);
                        int compID = res.getInt(8);
                        Completion c = null;
                        if(!res.wasNull())
                            c = completions.stream().filter(comp -> comp.getID().equals(compID)).findFirst().orElse(null);
                        int verfExeID = res.getInt(9);
                        TaskExecution exe = new TaskExecution(id, task, notes, prio, taskP, u, c, null);
                        if(!res.wasNull())
                        {
                            VerificationExecution verfExe = verificationExes.stream().filter(verf -> verf.getID().equals(verfExeID)).findFirst().orElse(null);
                            if(verfExe != null)
                                verfExe.setTaskExec(exe);
                            exe.setVerification(verfExe);
                        }
                        taskExecutionCache.put(exe.getID(), exe);
                    }
                    
                    ArrayList<TaskExecution> allTasks = new ArrayList<>();
                    allTasks.addAll(taskExecutionCache.values());
                    return allTasks;
                }
            }
        }
        catch (SQLException | UserDoesNotExistException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }
    
    // Retrieve all non-priority tasks for a time period
    public ArrayList<TaskExecution> getUnallocatedTaskExecutionList(Period p)
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
                ArrayList<TaskExecution> exes = new ArrayList<>();
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

    /*public ArrayList<TaskExecution> getCompletedTaskExecutionsForUser(User user) throws EmptyResultSetException
    {
        return getCompletedTaskExecutionsForUser(user.getUsername());
    }

    // FIXME: ONLY GET COMPLETIONS OF USERNAME
    public ArrayList<TaskExecution> getCompletedTaskExecutionsForUser(String username) throws EmptyResultSetException
    {
        ArrayList<Task> tasks = getTaskList();
        ArrayList<Completion> completions = getCompletionList();
        ArrayList<VerificationExecution> verificationExes = getVerificationExecutionList();
        try 
        {
            // LOOK AT THIS SQL STATEMENT TO FIX PROBLEM YES. VERY GOOD.
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
    }*/

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
                submitVerification(task.getVerification());
            }
            if(task.getID() == null)
            {
                db.prepareStatement(
                    "INSERT INTO tblTasks (task_name, task_desc, task_priority, "
                    + "task_intervaled_period_start, intervaled_period_end, period_interval, "
                    + "intervaled_period_constraint_start, invervaled_period_constraint_end, constraint_interval,"
                    + "allocation, verification_id)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }
            else
            {
                db.prepareStatement(
                        "UPDATE tblTasks SET task_name = ?, task_desc = ?, task_priority = ?, task_intervaled_period_start = ?, intervaled_period_end = ?, period_interval = ?, intervaled_period_constraint_start = ?, invervaled_period_constraint_end = ?, constraint_interval = ?, allocation = ?, verification_id = ?"
                                    + " WHERE task_id = ?");
            }

            db.add(task.getName());
            db.add(task.getNotes());
            db.add(task.getStandardPriority().ordinal());

            ConstrainedIntervaledPeriodSet sch = task.getSchedule();
            IntervaledPeriodSet periodSet = sch.periodSet();

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

            IntervaledPeriodSet periodSetConstraint = sch.periodSetConstraint();
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

            if(task.getID() == null)
            {
                db.executePrepared();
                task.setID(getLastTaskID());
            }
            else
            {
                db.add(task.getID());
                db.executePrepared();
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
    
    private <T> List<T> createListOfSingleItem(T item)
    {
        List<T> l = new ArrayList<>();
        l.add(item);
        return l;
    }
    
    // Insert a new taskexecution
    public boolean submitTaskExecution(TaskExecution exe)
    {
        try 
        {
            return submitTaskExecutions(createListOfSingleItem(exe));
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
            String[] statements = 
            {
               "INSERT INTO tblTaskExecutions (task_id, exe_notes, exe_prio, start_datetime, end_datetime, caretaker, compl_id, verf_exe_id)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",

                "UPDATE tblTaskExecutions SET task_id = ?, exe_notes = ?, exe_prio = ?, start_datetime = ?, end_datetime = ?, caretaker = ?, compl_id = ?, verf_exe_id = ?"
                    + " WHERE exe_id = ?"
            };
            int taskExeID = getLastTaskExecutionID()+1;
            int verfExeID = getLastVerificationExecutionID()+1;
            int completionID = getLastCompletionID()+1;
            ArrayList<VerificationExecution> verfExecutions = new ArrayList<>();
            ArrayList<Completion> completions = new ArrayList<>();
            for(int i = 0; i < 1; i++)
            {
                db.prepareStatement(statements[i]);
                for(TaskExecution exe: tasks)
                {
                    if(i == 0 && exe.getID() != null ||
                       i == 1 && exe.getID() == null)
                        continue;
                    db.add(exe.getTask().getID());
                    db.add(exe.getNotes());
                    db.add(exe.getPriority().ordinal());
                    PeriodUnwrapper pw = new PeriodUnwrapper(exe.getPeriod());
                    db.add(pw.start);
                    db.add(pw.end);
                    User u = exe.getAllocation();
                    if(u != null)
                        db.add(u.getUsername());
                    else
                        db.addNull();

                    Completion c = exe.getCompletion();
                    if(c != null)
                    {
                        if(c.getID() == null)
                        {
                            db.add(completionID++);
                        }
                        else
                        {
                            db.add(c.getID());
                        }
                        completions.add(c);
                    }
                    else
                    {
                        db.addNull(); // Completion of a new task execution should always be null
                    }

                    // If there is a verification execution linked, we must add it to a batch list
                    VerificationExecution verf = exe.getVerification();
                    if(verf != null)
                    {
                        if(verf.getID() == null)
                            db.add(verfExeID++);
                        else
                            db.add(verf.getID());

                        verfExecutions.add(verf);
                    }
                    else
                    {
                        db.addNull();
                    }
                    
                    if(i == 1 && exe.getID() != null)
                    {
                        db.add(exe.getID());
                    }
                    else if(i == 0 && exe.getID() == null)
                    {
                        exe.setID(taskExeID++);
                    }
                    
                    taskExecutionCache.put(exe.getID(), exe);
                    db.batch();
                }
                db.executeBatch();
            }
            try
            {
                submitCompletions(completions);
            }
            catch(EmptyInputException ex)
            {}
            try
            {
                submitVerificationExecutions(verfExecutions);
            }
            catch(EmptyInputException ex)
            {}
            return true;
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private boolean submitVerification(Verification verf)
    {
        try 
        { 
            if(verf.getID() == null)
            {
                db.prepareStatement("INSERT INTO tblVerifications (verf_notes, verf_priority, verf_duration, verf_caretaker)"
                    + " VALUES (?, ?, ?, ?)");
            }
            else
            {
                db.prepareStatement("UPDATE tblVerification SET verf_notes = ?, verf_priority = ?, verf_duration = ?, verf_caretaker = ?"
                        + " WHERE verf_id = ?");
            }
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

            if(verf.getID() == null)
            {
                db.executePrepared();
                verf.setID(getLastVerificationID()+1);
            }
            else
            {
                db.add(verf.getID());
                db.executePrepared();
            }
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private boolean submitVerificationExecution(VerificationExecution verf)
    {
        try 
        {
            return submitVerificationExecutions(createListOfSingleItem(verf));
        } 
        catch (EmptyInputException ex) 
        {
            return false;
        }
    }

    private boolean submitVerificationExecutions(List<VerificationExecution> verfs) throws EmptyInputException
    {
        try 
        {
            if(verfs.isEmpty())
                throw new EmptyInputException();
            String[] statements = 
            {
                "INSERT INTO tblVerfExecutions (verf_id, exe_notes, exe_duration, caretaker, compl_id)"
                    + " VALUES (?, ?, ?, ?, ?)",

                "UPDATE tblVerfExecutions SET verf_id = ?, exe_notes = ?, exe_duration = ?, caretaker = ?, compl_id = ?"
                    + " WHERE exe_id = ?"
            };
            int completionID = getLastCompletionID()+1;
            int verfExecID = getLastVerificationExecutionID()+1;
            ArrayList<Completion> completions = new ArrayList<>();
            for(int i = 0; i < 1; i++)
            {
                db.prepareStatement(statements[i]);
                for(VerificationExecution verf: verfs)
                {
                    if(i == 0 && verf.getID() != null ||
                       i == 1 && verf.getID() == null)
                        continue;
                    db.add(verf.getVerification().getID());
                    db.add(verf.getNotes());
                    db.add(verf.getDeadline().toMinutes());
                    User u = verf.getAllocation();
                    if(u != null)
                        db.add(u.getUsername());
                    else
                        db.addNull();
                    
                    if(verf.getCompletion() != null)
                    {
                        if(verf.getCompletion().getID() == null)
                        {
                            db.add(completionID++);
                        }
                        else
                        {
                            db.add(verf.getCompletion().getID());
                        }
                        completions.add(verf.getCompletion());
                    }
                    else
                    {
                        db.addNull();
                    }
                    
                    if(i == 1 && verf.getID() != null)
                    {
                        db.add(verf.getID());
                    }
                    else if(i == 0 && verf.getID() == null)
                    {
                        verf.setID(verfExecID++);
                    }
                    db.batch();
                }
                db.executeBatch();
            }
            submitCompletions(completions);
            return true;
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private boolean submitCompletion(Completion comp)
    {
        try 
        {
            List l = new ArrayList(){{add(comp);}};
            return submitTaskExecutions(l);
        } 
        catch (EmptyInputException ex) 
        {
            return false;
        }
    }

    private boolean submitCompletions(List<Completion> completions) throws EmptyInputException
    {
        try 
        {
            if(completions.isEmpty())
                throw new EmptyInputException();
            String[] statements = 
            {
                "INSERT INTO tblCompletions (caretaker, start_time, compl_time, quality, notes)"
                + " VALUES (?, ?, ?, ?, ?)",
                "UPDATE tblCompletions SET caretaker = ?, start_time = ?, compl_time = ?, quality = ?, notes = ?"
                + " WHERE compl_id = ?"
            };
            int completionID = getLastCompletionID()+1;
            for(int i = 0; i < 1; i++)
            {
                db.prepareStatement(statements[i]);
                for(Completion comp: completions)
                {
                    if(i == 0 && comp.getID() != null ||
                       i == 1 && comp.getID() == null)
                        continue;
                    db.add(comp.getStaff().getUsername());
                    db.add(comp.getStartTime().toEpochSecond(ZoneOffset.UTC));
                    db.add(comp.getCompletionTime().toEpochSecond(ZoneOffset.UTC));
                    db.add(comp.getWorkQuality().ordinal());
                    db.add(comp.getNotes());
                    if(i == 1 && comp.getID() != null)
                    {
                        db.add(comp.getID());
                    }
                    else if(i == 0 && comp.getID() == null)
                    {
                        comp.setID(completionID++);
                    }
                    db.batch();
                }
                db.executeBatch();
            }
            return true;
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private void logDB(String info)
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
