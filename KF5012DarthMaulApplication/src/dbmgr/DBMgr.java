package dbmgr;

import domain.Task;
import domain.Verification;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            db.fillDB();
            Task t = new Task();
            User hr0 = null;
            User hr1 = null;
            try {
                hr0 = db.getUser("hr_0");
                hr1 = db.getUser("hr_1");
            } catch (DBExceptions.UserDoesNotExistException ex) {
                Logger.getLogger(DBMgr.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            t.setName("TEST");
            IntervaledPeriodSet ips = new IntervaledPeriodSet(new Period(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1)), Duration.ofMinutes(20));
            IntervaledPeriodSet ipsc = new IntervaledPeriodSet(new Period(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1)), Duration.ofMinutes(20));
            ConstrainedIntervaledPeriodSet scheduleConstraint = new ConstrainedIntervaledPeriodSet(ips, ipsc);
            db.submitTask(t);
            ArrayList<Task> ts = db.getTaskList();
            Task v = ts.get(0);
            v.setScheduleConstraint(scheduleConstraint);
            v.setAllocationConstraint(hr0);
            Verification verf = new Verification();
            verf.setStandardDeadline(Duration.ofMinutes(15));
            verf.setAllocationConstraint(hr1);
            v.setVerification(verf);
            v.setName("A");
            v.getVerification().setNotes("yes");
            db.submitTask(v);
        } catch (DBExceptions.FailedToConnectException | DBExceptions.EmptyResultSetException ex) {
            Logger.getLogger(DBMgr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
