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
 * @author supad
 */
public final class DBAbstraction 
{
    private final DBConnection db;
    private String error;
    
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
    
    public boolean createUser(String username, String hashedPassword)
    {
        if(!doesUserExist(username))
        {
            try 
            {
                db.prepareStatement("INSERT INTO tblUsers (username, hashpass) VALUES (?, ?)");
                db.add(username);
                db.add(hashedPassword);
                db.executePrepared();
            } 
            catch (SQLException ex) 
            {
                Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            error = "Username already exists";
            return false;
        }
        return true;
    }
    
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
            return false;
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
        }*/ /*while(true)
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
    
    public void changeUserPassword(String username, String password)
    {
        try 
        {
            db.prepareStatement("UPDATE tblUsers SET hashpass = ? WHERE username = ?");
            db.add(password);
            db.add(username);
            db.executePrepared();
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
        }
    }
    
    public int getPermissionsFromUsername(String username)
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
}
