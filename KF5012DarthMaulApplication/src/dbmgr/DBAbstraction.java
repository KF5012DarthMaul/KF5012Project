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
public class DBAbstraction 
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
            ResultSet pass =  db.executePreparedQuery();
            return pass.first();
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
            ResultSet pass =  db.executePreparedQuery();
            if(pass.first())
                return pass.getString(1);
            else 
                return null;
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
            ResultSet perms =  db.executePreparedQuery();
            if(perms.first())
                return perms.getInt(1);
            else 
                return -1;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DBAbstraction.class.getName()).log(Level.SEVERE, null, ex);
            error = ex.getLocalizedMessage();
            return -1;
        }
    }
}
