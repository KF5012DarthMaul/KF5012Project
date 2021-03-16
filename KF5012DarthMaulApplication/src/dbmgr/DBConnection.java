package dbmgr;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 *
 * @author Emanuel Oliveira W19029581
 */

/**
 * Singleton DB connection handler
 */
public class DBConnection 
{
    static private DBConnection instance;
    private Connection conn;
    private int cursor;
    private PreparedStatement prepStmt;
    
    static DBConnection getInstance()
    {
        if(instance == null)
            instance = new DBConnection();
        return instance;
    }
    
    private DBConnection()
    {
        connect("C:\\unrestricted_dir\\db.sq3");
    }
    
    private boolean connect(String filename)
    {
        try
        {
            String url = "jdbc:sqlite:"+filename;
            conn = DriverManager.getConnection(url);
            System.out.println("Connected");
        }
        catch(SQLException e)
        {
            System.out.println("Failed to connect");
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public void prepareStatement(String sql) throws SQLException
    {
        if(prepStmt == null)
        {
            prepStmt = conn.prepareStatement(sql);
            cursor = 1;
        }
    }
    
    public void add(String s) throws SQLException
    {
        prepStmt.setString(cursor++, s);
    }
    
    public void add(Integer i)throws SQLException
    {
        prepStmt.setInt(cursor++, i);
    }
    
    public boolean executePrepared()
    {
        if(conn == null)
        {
            System.out.println("There is no database connected.");
            prepStmt = null;
            return false;
        }
        try
        {
            prepStmt.execute();
            prepStmt = null;
        }
        catch(SQLException e)
        {
            System.out.println("Failed to execute");
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public ResultSet executePreparedQuery()
    {
        ResultSet result;
        if(conn == null)
        {
            System.out.println("There is no database connected.");
            prepStmt = null;
            return null;
        }
        try
        {
            result = prepStmt.executeQuery();
            prepStmt = null;
        }
        catch(SQLException e)
        {
            System.out.println("Failed to execute");
            System.out.println(e.getMessage());
            return null;
        }
        return result;
    }
    
    public boolean execute(String sql)
    {
        if(conn == null)
        {
            System.out.println("There is no database connected.");
            return false;
        }
        try
        {
            Statement sqlStatement = conn.createStatement();
            sqlStatement.execute(sql);
        }
        catch(SQLException e)
        {
            System.out.println("Failed to execute"+sql);
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public ResultSet executeQuery(String sql)
    {
        ResultSet result;
        if(conn == null)
        {
            System.out.println("There is no database connected.");
            return null;
        }
        try
        {
            Statement sqlStatement = conn.createStatement();
            result = sqlStatement.executeQuery(sql);
        }
        catch(SQLException e)
        {
            System.out.println("Failed to execute"+sql);
            System.out.println(e.getMessage());
            return null;
        }
        return result;
    }
}
