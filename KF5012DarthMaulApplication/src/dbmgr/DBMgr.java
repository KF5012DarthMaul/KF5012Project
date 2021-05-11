package dbmgr;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Emanuel Oliveira W19029581
 */
public class DBMgr {

    public static void main(String[] args) {
        DBAbstraction db;
        try {
            db = DBAbstraction.getInstance();
            db.createTables();
            db.fillDB();
        } catch (DBExceptions.FailedToConnectException ex) {
            Logger.getLogger(DBMgr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
