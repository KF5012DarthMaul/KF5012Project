package dbmgr;

import java.sql.SQLException;

/**
 *
 * @author Emanuel Oliveira W19029581
 */
public final class DBExceptions {
    public static class FailedToConnectException extends Exception {

        public FailedToConnectException(SQLException ex) {
            super(ex);
        }
    }
    public static class UserAlreadyExistsException extends Exception {

        public UserAlreadyExistsException() {
            super("The user already exists");
        }
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
    
    public static class UserDoesNotExistException extends Exception {

        public UserDoesNotExistException() {
        }
    }
    private DBExceptions(){}
}
