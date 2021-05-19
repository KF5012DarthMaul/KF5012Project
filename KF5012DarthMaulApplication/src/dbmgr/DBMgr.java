package dbmgr;

import domain.Completion;
import domain.Task;
import domain.TaskCompletionQuality;
import domain.TaskExecution;
import domain.TaskPriority;
import domain.Verification;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import kf5012darthmaulapplication.User;
import temporal.ConstrainedIntervaledPeriodSet;
import temporal.IntervaledPeriodSet;
import temporal.Period;

/**
 *
 * @author Emanuel Oliveira W19029581
 */
public class DBMgr {

    public static void main(String[] args) {
        DBAbstraction db;
        try {
            db = DBAbstraction.getInstance();

            User hr0 = null;
            User hr1 = null;
            try {
                hr0 = db.getUser("hr_0");
                hr1 = db.getUser("hr_1");
            } catch (DBExceptions.UserDoesNotExistException ex) {
                Logger.getLogger(DBMgr.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Create a task and submit it to the db
            Task t = new Task();
            t.setName("TEST");
            db.submitTask(t);
            
            // Retrieve it for no reason
            ArrayList<Task> ts = db.getTaskList();
            t = ts.get(0);
            
            // Populate some fields
            IntervaledPeriodSet ips = new IntervaledPeriodSet(new Period(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1)), Duration.ofMinutes(20));
            IntervaledPeriodSet ipsc = new IntervaledPeriodSet(new Period(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1)), Duration.ofMinutes(20));
            ConstrainedIntervaledPeriodSet scheduleConstraint = new ConstrainedIntervaledPeriodSet(ips, ipsc);
            t.setScheduleConstraint(scheduleConstraint);
            t.setAllocationConstraint(hr0);
            
            // Create a verification for this Task
            Verification verf = new Verification();
            verf.setStandardDeadline(Duration.ofMinutes(15));
            verf.setAllocationConstraint(hr1);
            t.setVerification(verf);
            t.setName("A");
            t.getVerification().setNotes("yes");
            
            // Submit it again, this will just update the data in the database and not actually create a new task.
            // The easiest way to duplicate a task would be to set its ID to null and submit it.
            // Since t now references a Verification, that verification is automatically parsed by the DBAbstraction class and inserted into the DB.
            // Modifying the Verification that it is linked to will modify the data present in the DB, refering to that Verification.
            // Much like the task, setting the ID of the Verification to null will allow you to create a duplicate of the Verification in the DB.
            // This can be useful when shallow-copying from one task to another, but keeping separate instances of the Verification.
            // If you do that, retrieve the list of tasks again before modifying them, otherwise the 2 separate Task objects will still refer and modify the same Verification object!
            db.submitTask(t);
            
            // Task Execution example
            // New Task execution references previous Task, t
            // The second parameter of TaskExecution is of type Task and should never be null, as it violates the not-null constraint in the DB
            // However, you can assign it as null in the constructor and call tex.setTask(t) later, if for some reason that is necessary.
            TaskExecution tex = new TaskExecution(null, t, "test execution", TaskPriority.NORMAL, new Period(LocalDateTime.now()), db.getUser("caretaker_3"), null, null);
            
            // Create a completion for it, with the same user completing it as the allocated one.
            Completion c = new Completion(null, tex.getAllocation(), LocalDateTime.now(), LocalDateTime.now(), TaskCompletionQuality.ADEQUATE, "test completion");
            // Reference it in the task execution object
            tex.setCompletion(c);
            
            // Since tex now references a completion, it will get parsed by submitTaskExecution and inserted into the database.
            db.submitTaskExecution(tex);
            
            // Retrieving all completed tasks and checking for a specific user
            ArrayList<TaskExecution> completed = db.getTaskExecutionList();
            List<TaskExecution> completedByCT3 = completed.stream().filter(task -> task.getCompletion().getStaff().getUsername().equals("caretaker_3")).collect(toList());
            
        } catch (DBExceptions.FailedToConnectException | DBExceptions.UserDoesNotExistException ex) {
            Logger.getLogger(DBMgr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
