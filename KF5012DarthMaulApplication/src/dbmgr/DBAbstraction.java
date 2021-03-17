/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbmgr;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
/**
 *
 * @author Emanuel Oliveira W19029581
 * The DBAbstraction class provides methods for other classes to use that permit interaction with a database.
 * These methods utilize constructed SQL, preventing SQL injection.
 */
public final class DBAbstraction 
{
    private final DBConnection db;
    private String error;
    
    
    /** 
     * The DBAbstraction class provides methods for other classes to use that permit interaction with a database.
     * These methods utilize constructed SQL, preventing SQL injection.
     */
    public DBAbstraction()
    {
        db = DBConnection.getInstance();
        error = "";
        createTables();
    }
    
    public String getError()
    {
        return error;
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
     * Tests whether a username exsits inside the database.
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
                           );
                                CREATE TABLE IF NOT EXISTS tblTasks(
                               task_id INTEGER PRIMARY KEY AUTOINCREMENT,
                               task_type INTEGER NOT NULL,
                               caretaker TEXT,
                               execution_day INTEGER,
                               FOREIGN KEY(task_type) REFERENCES tblTaskType (type_id) ON DELETE CASCADE,
                               FOREIGN KEY(caretaker) REFERENCES tblUsers (username) ON DELETE CASCADE,
                           );
                                CREATE TABLE IF NOT EXISTS tblTaskType(
                               type_id INTEGER PRIMARY KEY AUTOINCREMENT,
                               task_name TEXT NOT NULL,
                               task_descr TEXT
                           );
                                -- Tasks could ref log_id instead of log referencing task_id
                           -- That would require updating the task row instead of just adding to taskLog.
                           CREATE TABLE IF NOT EXISTS tblTaskLog(
                               log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                               task_id INTEGER NOT NULL,
                               log_time INTEGER NOT NULL,
                               completion_time INTEGER,
                               FOREIGN KEY(task_id) REFERENCES tblTasks(task_id) ON DELETE CASCADE,
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
            else return null;
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return null;
        }
    }
    
    /**
     * Sets a new password (encrypted) for a username, not regarding whether it actually exists in the database or not.
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
            db.executePrepared();
            return true;
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
            else return -1;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return -1;
        }
    }
    
    /**
     * Sets new permission flags for a username, not regarding whether it actually exists in the database or not.
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
            db.executePrepared();
            return true;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return false;
        }
    }
}
