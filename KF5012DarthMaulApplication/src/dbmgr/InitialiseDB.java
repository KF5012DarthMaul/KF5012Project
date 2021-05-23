package dbmgr;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import dbmgr.DBExceptions.FailedToConnectException;
import domain.Completion;
import domain.Task;
import domain.TaskCompletionQuality;
import domain.TaskExecution;
import domain.TaskPriority;
import domain.Verification;
import domain.VerificationExecution;
import kf5012darthmaulapplication.ExceptionDialog;
import kf5012darthmaulapplication.SecurityManager;
import kf5012darthmaulapplication.User;
import kf5012darthmaulapplication.PermissionManager.AccountType;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.IntervaledPeriodSet;
import temporal.Period;

/**
 * An application to initialise the database tables and fill it with test data.
 * 
 * @author Emanuel Oliveira W19029581
 * @author William Taylor
 */
public class InitialiseDB {
    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("h:mma d/M/yyyy");

	private DBAbstraction db = null;
	private DBConnection dbConn = null;
	
	/**
	 * Initialise the DB (create tables and fill with test data).
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
                    InitialiseDB initDB = new InitialiseDB();
                    initDB.initialise();});
	}
	
	/**
	 * Create an object that can initialise the database.
	 */
	public InitialiseDB() {
                try {
			dbConn = DBConnection.getInstance();
		} catch (FailedToConnectException e) {
			new ExceptionDialog(
				"Could not connect to database. Click 'Refresh' to retry loading tasks.", e);
		}
	}
	
	/**
	 * Initialise or reinitialise the database with test data.
	 */
	public void initialise() {
            dropTables();
            createTables();
            fillDB();
	}

    /* Utility methods
     * -------------------- */
    
    private LocalDateTime dt(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, dateTimeFormatter);
    }
    
//    private String randomString() 
//    {
//        Random random = new Random();
//        int targetStringLength = 10+random.nextInt(32);
//        StringBuilder buffer = new StringBuilder(targetStringLength);
//        for (int i = 0; i < targetStringLength; i++) 
//        {
//            int c = 97 + random.nextInt(26);
//            buffer.append((char) c);
//        }
//        return buffer.toString();
//    }

    /* Creating DB
     * -------------------- */

    public void dropTables() {
    	try {
	    	dbConn.execute("DROP TABLE IF EXISTS tblUsers;");
	    	dbConn.execute("DROP TABLE IF EXISTS tblTasks;");
			dbConn.execute("DROP TABLE IF EXISTS tblTaskMaps;");
			dbConn.execute("DROP TABLE IF EXISTS tblVerifications;");
			dbConn.execute("DROP TABLE IF EXISTS tblTaskExecutions;");
			dbConn.execute("DROP TABLE IF EXISTS tblVerfExecutions;");
			dbConn.execute("DROP TABLE IF EXISTS tblCompletions;");
			dbConn.execute("DROP TABLE IF EXISTS tblSystemLog;");
			
    	} catch (Exception e) {
			new ExceptionDialog("Could not create database tables.", e);
    	}
    }
    
    public void createTables()
    {
    	try {
	    	dbConn.execute("""
                    CREATE TABLE IF NOT EXISTS tblUsers(
                        username TEXT PRIMARY KEY,
                        display_name TEXT NOT NULL,
                        hashpass TEXT NOT NULL,
                        account_type INTEGER NOT NULL
                    );""");
	    	dbConn.execute(""" 
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
                        deleted INTEGER,
                        FOREIGN KEY(verification_id) REFERENCES tblVerications (verf_id) ON DELETE CASCADE,
                        FOREIGN KEY(allocation) REFERENCES tblUsers (username) ON DELETE CASCADE
                    );""");
    		dbConn.execute("""
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
    		dbConn.execute("""
                    CREATE TABLE IF NOT EXISTS tblVerifications(
                        verf_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        verf_notes TEXT,
                        verf_priority INTEGER NOT NULL,
                        verf_duration INTEGER,
                        verf_caretaker TEXT
                   );""");
    		dbConn.execute(""" 
                    CREATE TABLE IF NOT EXISTS tblTaskExecutions(
                        exe_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        task_id INTEGER NOT NULL,
                        exe_notes TEXT,
                        exe_prio INTEGER NOT NULL,
                        period_constraint_start INTEGER NOT NULL,
                        period_constraint_end INTEGER,
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
    		dbConn.execute("""
                    CREATE TABLE IF NOT EXISTS tblVerfExecutions(
                        exe_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        verf_id INTEGER,
                        exe_notes TEXT,
                        exe_duration INTEGER NOT NULL,
                        caretaker TEXT,
                        compl_id INTEGER,
                        FOREIGN KEY (verf_id) REFERENCES tblVerifications(verf_id) ON DELETE CASCADE,
                        FOREIGN KEY (caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE,
                        FOREIGN KEY (compl_id) REFERENCES tblCompletion(compl_id) ON DELETE CASCADE
                   );""");
    		dbConn.execute(""" 
                    CREATE TABLE IF NOT EXISTS tblCompletions(
                        compl_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        caretaker TEXT NOT NULL,
                        start_time INTEGER NOT NULL,
                        compl_time INTEGER NOT NULL,
                        quality INTEGER,
                        notes TEXT,
                        FOREIGN KEY(caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE
                    );""");
    		dbConn.execute("""
                    CREATE TABLE IF NOT EXISTS tblSystemLog(
                        log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        log_info TEXT,
                        log_timestamp INTEGER NOT NULL
                   );""");
    	} catch (Exception e) {
			new ExceptionDialog("Could not create database tables.", e);
    	}
    	
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

    /* Populating DB
     * -------------------- */

    public void fillDB()
    {
        try {
            /* Populate Users
             * -------------------------------------------------- */
            try {
                db = DBAbstraction.getInstance();
            } 
            catch (FailedToConnectException e) {
                new ExceptionDialog(
                    "Could not connect to database. Click 'Refresh' to retry loading tasks.", e);
                return;
            }
            String password = "Password123#";
            int hrCount = 5;
            int managerCount = 7;
            int estateCount = 3;
            int caretakerCount = 15;

            for(int i = 0 ; i < hrCount; i++) {
                    String username = "hr_" + i;
                    db.createUser(username, username,SecurityManager.generatePassword(password),AccountType.HR_PERSONNEL);
            }
            for(int i = 0 ; i < managerCount; i++) {
                    String username = "manager_" + i;
                    db.createUser(username, username,SecurityManager.generatePassword(password),AccountType.MANAGER);
            }
            for(int i = 0 ; i < estateCount; i++) {
                    String username = "estate_" + i;
                    db.createUser(username, username,SecurityManager.generatePassword(password),AccountType.ESTATE);
            }
            for(int i = 0 ; i < caretakerCount; i++) {
                    String username = "caretaker_" + i;
                    db.createUser(username, username,SecurityManager.generatePassword(password),AccountType.CARETAKER);
            }
            
            // Also, a test user
            db.createUser("test", "Super User",SecurityManager.generatePassword("password"),AccountType.HR_PERSONNEL);

            /* Populate Tasks
             * -------------------------------------------------- */
            
            List<Task> allTasks = new ArrayList<>();
            List<TaskExecution> allTaskExecs = new ArrayList<>();
            
            Task t_repeating = new Task(
                null,
                "Check toilets", "",
                null, null, null,
                TaskPriority.NORMAL,
                new ConstrainedIntervaledPeriodSet(
                    new IntervaledPeriodSet(
                        new Period(dt("9:45am 9/5/2021"), dt("10:00am 9/5/2021")),
                        Duration.ofHours(2)
                    ),
                    new IntervaledPeriodSet(
                        new Period(dt("9:00am 9/5/2021"), dt("5:00pm 9/5/2021")),
                        Duration.ofDays(1)
                    )
                ),
                null, null
            );
            
            allTasks.add(t_repeating);
            
            // Some on the 9th
            LocalDateTime start;
            
            start = dt("9:45am 9/5/2021");
            allTaskExecs.add(new TaskExecution(
                null, t_repeating, "", TaskPriority.NORMAL,
                new Period(start, dt("10:00am 9/5/2021")),
                new Period(start, Duration.ofMinutes(0)),
                null, null, null
            ));
            start = dt("11:45am 9/5/2021");
            allTaskExecs.add(new TaskExecution(
                null, t_repeating, "", TaskPriority.NORMAL,
                new Period(start, dt("12:00pm 9/5/2021")),
                new Period(start, Duration.ofMinutes(0)),
                null, null, null
            ));
            start = dt("1:45pm 9/5/2021");
            allTaskExecs.add(new TaskExecution(
                null, t_repeating, "", TaskPriority.NORMAL,
                new Period(start, dt("2:00pm 9/5/2021")),
                new Period(start, Duration.ofMinutes(0)),
                null, null, null
            ));

            // Some on the 10th
            start = dt("9:45am 10/5/2021");
            allTaskExecs.add(new TaskExecution(
                null, t_repeating, "", TaskPriority.NORMAL,
                new Period(start, dt("10:00am 10/5/2021")),
                new Period(start, Duration.ofMinutes(0)),
                null, null, null
            ));
            start = dt("11:45am 10/5/2021");
            allTaskExecs.add(new TaskExecution(
                null, t_repeating, "", TaskPriority.NORMAL,
                new Period(start, dt("12:00pm 10/5/2021")),
                new Period(start, Duration.ofMinutes(0)),
                null, null, null
            ));
            start = dt("1:45pm 10/5/2021");
            allTaskExecs.add(new TaskExecution(
                null, t_repeating, "", TaskPriority.NORMAL,
                new Period(start, dt("2:00pm 10/5/2021")),
                new Period(start, Duration.ofMinutes(0)),
                null, null, null
            ));

            // Some on the today
			LocalDateTime startOfToday = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
			
            allTaskExecs.add(new TaskExecution(
                null, t_repeating, "", TaskPriority.NORMAL,
                new Period(startOfToday.plusHours(10), startOfToday.plusHours(11)),
                new Period(startOfToday.plusHours(10), Duration.ofMinutes(0)),
                null, null, null
            ));
            allTaskExecs.add(new TaskExecution(
                null, t_repeating, "", TaskPriority.NORMAL,
                new Period(startOfToday.plusHours(12), startOfToday.plusHours(13)),
                new Period(startOfToday.plusHours(12), Duration.ofMinutes(0)),
                null, null, null
            ));
            allTaskExecs.add(new TaskExecution(
                null, t_repeating, "", TaskPriority.NORMAL,
                new Period(startOfToday.plusHours(14), startOfToday.plusHours(15)),
                new Period(startOfToday.plusHours(14), Duration.ofMinutes(0)),
                null, null, null
            ));

            // A low-priority one-off task without a deadline
            Task t_noDeadline = new Task(
                null,
                "Fix Window on bike shed", "",
                null, null, null,
                TaskPriority.LOW,
                new ConstrainedIntervaledPeriodSet(
                    new IntervaledPeriodSet(
                        new Period(dt("9:45am 9/5/2021"), (Duration) null),
                        null
                    ),
                    null
                ),
                null, null
            );

            allTasks.add(t_noDeadline);
            
            allTaskExecs.add(new TaskExecution(
                null, t_noDeadline, "", TaskPriority.LOW,
                new Period(dt("1:00pm 9/5/2021"), dt("3:00pm 9/5/2021")),
                new Period(dt("1:00pm 9/5/2021"), Duration.ofMinutes(0)),
                null, null, null
            ));

            // A one-off task with no executions that is relative to the time
            // the program is run (eg. to test viewing tasks with no executions,
            // task generation, and allocation)
            Task t_noExecs = new Task(
                null,
                "Water the garden", "",
                null, null, null,
                TaskPriority.LOW,
                new ConstrainedIntervaledPeriodSet(
                    new IntervaledPeriodSet(
                        // Start no earlier than 3 hours after the program is
                        // run, and finish no later than 2 hours after that.
                        new Period(
                            LocalDateTime.now().plus(Duration.ofHours(3)),
                            Duration.ofDays(2) // It can wait for a bit
                        ),
                        null
                    ),
                    null
                ),
                null, null
            );
            
            allTasks.add(t_noExecs);

            // A high-priority one-off task with deadline and verification.
            User myUser = db.getUser("caretaker_3");//new User("myuser", AccountType.CARETAKER);
            //User myUser = new User("myuser", AccountType.CARETAKER);
            
            Task t_requiresVerif = new Task(
                null,
                "Fix Broken Pipe",
                "The waste pipe outside of the toilets on the 3rd floor of Big Building is broken and leaking. Health hazard - fix ASAP.",
                null, null, null,
                TaskPriority.HIGH,
                new ConstrainedIntervaledPeriodSet(
                    new IntervaledPeriodSet(
                        new Period(dt("1:32pm 10/5/2021"), dt("5:00pm 10/5/2021")), null
                    ),
                    null
                ),
                null,
                null
            );
            Verification verification = new Verification(null, null, "", TaskPriority.HIGH, Duration.ofHours(3), null);
            t_requiresVerif.setVerification(verification);

            allTasks.add(t_requiresVerif);
            
            // The task execution has been allocated to myUser
            TaskExecution t3Exec = new TaskExecution(
                null, t_requiresVerif, "", TaskPriority.HIGH,
                new Period(dt("3:30pm 9/5/2021"), dt("4:15pm 9/5/2021")),
                new Period(dt("3:30pm 9/5/2021"), Duration.ofMinutes(0)),
                myUser,
                null, null
            );
            
            // The verification execution
            VerificationExecution t3VerExec = new VerificationExecution(
                null, verification, t3Exec, "", Duration.ofHours(3), null, null
            );
            t3Exec.setVerification(t3VerExec);

            // Add both
            allTaskExecs.add(t3Exec);
            
            //This is a example of a completed task
            Task t_complete = new Task(
                null,
                "Make beds", "",
                null, null, null,
                TaskPriority.LOW,
                new ConstrainedIntervaledPeriodSet(
                    new IntervaledPeriodSet(
                        // Start no earlier than 3 hours after the program is
                        // run, and finish no later than 2 hours after that.
                        new Period(
                            LocalDateTime.now().plus(Duration.ofHours(3)),
                            Duration.ofDays(2) // It can wait for a bit
                        ),
                        null
                    ),
                    null
                ),
                null, null
            );
            
            allTasks.add(t_complete);
            
            TaskExecution tCompleteExec = new TaskExecution(
                null, t_complete, "", TaskPriority.HIGH,
                new Period(dt("3:30pm 9/5/2021"), dt("4:15pm 9/5/2021")),
                new Period(dt("3:30pm 9/5/2021"), Duration.ofMinutes(0)),
                null,
                new Completion(null, myUser, LocalDateTime.now(), LocalDateTime.now(), TaskCompletionQuality.ADEQUATE, "test completion"), 
                null
                
            );
            allTaskExecs.add(tCompleteExec);

            // Add some recurring tasks of different lengths (and a null length)
            // with no execs.
            Task t_onceAWeek = new Task(
                null,
                "Clean windowsills",
                "Because completed 5 days a week this task is not.",
                null, null, null,
                TaskPriority.NORMAL,
                new ConstrainedIntervaledPeriodSet(
                    new IntervaledPeriodSet(
                        new Period(dt("1:00pm 10/5/2021"), dt("5:00pm 10/5/2021")),
                        Duration.ofHours(24)
                    ),
                    new IntervaledPeriodSet(
                        new Period(dt("12:00pm 14/5/2021"), dt("12:00pm 15/5/2021")),
                        Duration.ofHours(24*7)
                    )
                ),
                null,
                null
            );
            allTasks.add(t_onceAWeek);

            Verification checkAntennaVer = new Verification(null, null, "", TaskPriority.NORMAL, Duration.ofHours(1), null);
            Task t_onceADay = new Task(
                null,
                "Check antenna",
                "Needs to be checked every day. Usually done in the early afternoon.",
                null, null, null,
                TaskPriority.NORMAL,
                new ConstrainedIntervaledPeriodSet(
                    new IntervaledPeriodSet(
                        new Period(dt("1:00pm 12/5/2021"), dt("3:00pm 12/5/2021")),
                        Duration.ofHours(24)
                    ),
                    new IntervaledPeriodSet(
                        new Period(dt("12:00pm 10/5/2021"), dt("12:00pm 15/5/2021")),
                        Duration.ofHours(24*7)
                    )
                ),
                null,
                checkAntennaVer
            );
            checkAntennaVer.setTask(t_onceADay);
            allTasks.add(t_onceADay);
            
            Task t_covidCleaning = new Task(
                null,
                "Wipe surfaces",
                "Should wipe commonly touched surfaces regularly",
                null, null, null,
                TaskPriority.LOW, // Eh, meh.
                new ConstrainedIntervaledPeriodSet(
                    new IntervaledPeriodSet(
                        new Period(dt("9:00am 17/5/2021"), dt("9:30am 17/5/2021")),
                        Duration.ofHours(3)
                    ),
                    new IntervaledPeriodSet(
                        new Period(dt("9:00am 17/5/2021"), dt("5:00pm 17/5/2021")),
                        Duration.ofHours(24)
                    )
                ),
                null,
                null
            );
            allTasks.add(t_covidCleaning);
        
            /* Add Tasks and Task Executions
             * -------------------- */
            
            try {
                allTasks.forEach(t -> {
                    db.submitTask(t);
                });
                db.submitTaskExecutions(allTaskExecs);
            } catch (Exception e) {
                // Keep going after this failure - other parts of the DB may be
                // possible to be populated.
            }

//            for(int i = 0; i < 1000; i++)
//            {
//                submitTask(new Task(0, randomString(), randomString()));
//            }
//            ArrayList<Task> taskList = getTaskList();
//            ArrayList<TaskExecution> texecList = new ArrayList();
//            LocalDateTime startingPoint = LocalDateTime.of(2021, Month.APRIL, 29, 10, 30);
//            LocalDateTime plusOneHour = startingPoint.plusHours(1);
//            Period p = new Period(startingPoint, plusOneHour);
//            taskList.forEach(t -> {
//                System.out.println(t.toString());
//                texecList.add(new TaskExecution(t.name, p));
//            });
//            submitTaskExecutions(texecList);
//            ArrayList<TaskExecution> tlist = getUnallocatedTaskList(p);
//            tlist.forEach(t -> {
//                t.getName();
//            });
//            ArrayList<TaskExecution> t2 = getUnallocatedTaskList(p);
//            t2.forEach(t -> {System.out.println(t.toString());
//            });
        }
        catch (Exception e) {
			new ExceptionDialog("Could not initialise database data.", e);
        }
    }
}
