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
import javax.swing.JFileChooser;
import java.io.File;   
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 *
 * @author supad
 */
public class DBAbstraction {
    private final DBConnection db;
    private String Error;
    
    public DBAbstraction()
    {
        db = DBConnection.getInstance();
        Error = "";
        createTables();
    }
    
    public String getError()
    {
        return Error;
    }
    
    public boolean createUser(String username, String hashedPassword)
    {
        if(!doesUserExist(username))
        {
            try {
                db.prepareStatement("INSERT INTO tblUsers (username, hashpass) values (?, ?)");
                db.add(username);
                db.add(hashedPassword);
                db.executePrepared();
            } catch (SQLException ex) {
                Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            Error = "Username already exists";
            return false;
        }
        return true;
    }
    
    public boolean doesUserExist(String username)
    {
        return false;
    }
    
    private void createTables()
    {
        try 
        {
            URI uri = getClass().getResource("db.sql").toURI();
            String mainPath = Paths.get(uri).toString();
            Path path = Paths.get(mainPath);
            db.execute(Files.readString(path));
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (IOException | URISyntaxException ex) 
        {
                Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*while(true)
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    File selectedFile = fileChooser.getSelectedFile();
                    System.out.println("Reading Selected file: " + selectedFile.getAbsolutePath());
                    db.execute(Files.readString(selectedFile.toPath()));
                            } catch (FileNotFoundException ex) {
                    Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }*/
    }
    
    public String getHashedPassword(String username)
    {
        try {
            db.prepareStatement("SELECT hashpass from tblUsers where username like ?");
            db.add(username);
            ResultSet pass =  db.executePreparedQuery();
            if(pass.first())
                return pass.getString(1);
            else 
                return null;
        } 
        catch (SQLException ex) {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            Error = ex.getLocalizedMessage();
            return null;
        }
    }
    
    public String getHashedPassword(int uid)
    {
        try {
            db.prepareStatement("SELECT hashpass from tblUsers where user_id like ?");
            db.add(uid);
            ResultSet pass =  db.executePreparedQuery();
            if(pass.first())
                return pass.getString(1);
            else 
                return null;
        } 
        catch (SQLException ex) {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            Error = ex.getLocalizedMessage();
            return null;
        }
    }
    
    private int getUIDFromUsername(String username)
    {
        try {
                db.prepareStatement("SELECT user_id from tblUsers where username like ?");
                db.add(username);
                ResultSet uid =  db.executePreparedQuery();
                if(uid.first())
                    return uid.getInt(1);
                else 
                    return -1;
            } 
            catch (SQLException ex) {
                Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
                Error = ex.getLocalizedMessage();
                return -1;
            }
    }
    
    public void resetUserPassword(String username, String newHashedPassword)
    {
       int uID = getUIDFromUsername(username);
       changeUserPassword(uID, newHashedPassword);
    }
    
    public void changeUserPassword(int uID, String password)
    {
        try {
            db.prepareStatement("UPDATE tblUsers SET hashpass = ? where user_id like ?");
            db.add(password);
            db.add(uID);
            db.executePrepared();
        } 
        catch (SQLException ex) {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            Error = ex.getLocalizedMessage();
        }
    }
    
    public int getPermissionsFromUID(int uid)
    {
        try {
            db.prepareStatement("SELECT permission_flags from tblUsers where user_id like ?");
            db.add(uid);
            ResultSet perms =  db.executePreparedQuery();
            if(perms.first())
                return perms.getInt(1);
            else 
                return -1;
        } 
        catch (SQLException ex) {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            Error = ex.getLocalizedMessage();
            return -1;
        }
    }
}
