package dbmgr;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
/**
 *
 * @author Emanuel Oliveira W19029581
 */

/**
 * Singleton DB connection handler
 */
public final class DBConnection 
{
    static private DBConnection instance;
    private Connection conn;
    private int cursor;
    private PreparedStatement prepStmt;
    private int batchCount;
    /**
     * Get the singleton instance of this class.
     * @return The singleton instance.
     */
    static DBConnection getInstance()
    {
        if(instance == null)
            instance = new DBConnection();
        return instance;
    }
    
    private DBConnection()
    {
        connect("..\\db.sq3");
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
    
    /**
     * Prepare a statement, avoiding SQL injection.<p>
     * If an instance already exists, another won't be created.<p>
     * Instance is cleared when executePrepated or executePreparedQuery are called.<p>
     * After calling this function, add() should be called to populate the ? fields
     * @param sql The SQL code to prepare with.
     * @throws SQLException 
     */
    public void prepareStatement(String sql) throws SQLException
    {
        if(prepStmt == null)
        {
            prepStmt = conn.prepareStatement(sql);
            cursor = 1;
        }
    }
    
    /**
     * Adds a batch to the statement, allowing for multiple insertions in one go.<p>
     * @throws SQLException 
     */
    public void batch() throws SQLException
    {
        prepStmt.addBatch();
        cursor = 1;
        batchCount++;
        if (batchCount % 100 == 0) // Every 100 rows
        {
            prepStmt.executeBatch();
            batchCount = 0;
        }
    }
    
    /**
     * Executes any batches remaining.<p>
     * Will fail if the prepared statement has not been initialized. Call preparedStatement() to prevent this.<p>
     * @return True if sucessful, False if an error occured
     * @throws NullPointerException 
     */
    public boolean executeBatch() throws NullPointerException
    {
         if(conn == null)
        {
            System.out.println("There is no database connected.");
            return false;
        }
        try
        {
            if(batchCount > 0)
            {
                prepStmt.executeBatch();
                batchCount = 0;
            }
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
    
    /** 
     * Adds a string to the prepared statement.<p>
     * Will fail if the prepared statement has not been initialized. Call preparedStatement() to prevent this.<p>
     * Will fail if called more than there are ? fields available in the prepared statement.
     * @param s The string to add to the statement.
     * @throws SQLException 
     * @throws NullPointerException
     */
    public void add(String s) throws SQLException, NullPointerException
    {
        prepStmt.setString(cursor++, s);
    }
    
    /** 
     * Adds an integer to the prepared statement.<p>
     * Will fail if the prepared statement has not been initialized. Call preparedStatement() to prevent this.<p>
     * Will fail if called more than there are ? fields available in the prepared statement.
     * @param i The integer to add to the statement.
     * @throws SQLException 
     * @throws NullPointerException
     */
    public void add(Integer i)throws SQLException, NullPointerException
    {
        prepStmt.setInt(cursor++, i);
    }
    
    /** 
     * Adds a long integer to the prepared statement.<p>
     * Will fail if the prepared statement has not been initialized. Call preparedStatement() to prevent this.<p>
     * Will fail if called more than there are ? fields available in the prepared statement.
     * @param i The long integer to add to the statement.
     * @throws SQLException 
     * @throws NullPointerException
     */
    public void add(Long i)throws SQLException, NullPointerException
    {
        prepStmt.setLong(cursor++, i);
    }
    
    /** 
     * Adds a boolean to the prepared statement.<p>
     * Will fail if the prepared statement has not been initialized. Call preparedStatement() to prevent this.<p>
     * Will fail if called more than there are ? fields available in the prepared statement.
     * @param b The boolean to add to the statement.
     * @throws SQLException 
     * @throws NullPointerException
     */
    public void add(boolean b)throws SQLException, NullPointerException
    {
        prepStmt.setBoolean(cursor++, b);
    }
    
    /**
     * Executes the prepared statement.<p>
     * If prepareStatement() was not called before this method, a NullPointerException will be thrown.<p>
     * Will fail if the prepared statement's ? fields were not fully populated via add().
     * @return True if sucessful, False if an error occured
     * @throws NullPointerException 
     */
    public boolean executePrepared() throws NullPointerException
    {
        if(conn == null)
        {
            System.out.println("There is no database connected.");
            return false;
        }
        try
        {
            prepStmt.execute();
            prepStmt = null;
            return true;
        }
        catch(SQLException e)
        {
            System.out.println("Failed to execute");
            System.out.println(e.getMessage());
            return false;
        }
    }
    
    /**
     * Executes the prepared statement.<p>
     * If prepareStatement() was not called before this method, a NullPointerException will be thrown.<p>
     * If the prepared statement's ? fields were not fully populated via add(), this method will fail.
     * @return If succesful, a ResultSet containing all queried columns. NULL if an error occured.
     * @throws NullPointerException 
     */
    public ResultSet executePreparedQuery() throws NullPointerException
    {
        if(conn == null)
        {
            System.out.println("There is no database connected.");
            return null;
        }
        try
        {
            PreparedStatement stmt = prepStmt;
            prepStmt = null;
            return stmt.executeQuery();
        }
        catch(SQLException e)
        {
            System.out.println("Failed to execute");
            System.out.println(e.getMessage());
            return null;
        }
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
            return true;
        }
        catch(SQLException e)
        {
            System.out.println("Failed to execute"+sql);
            System.out.println(e.getMessage());
            return false;
        }
    }
    
    public ResultSet executeQuery(String sql)
    {
        if(conn == null)
        {
            System.out.println("There is no database connected.");
            return null;
        }
        try
        {
            Statement sqlStatement = conn.createStatement();
            return sqlStatement.executeQuery(sql);
        }
        catch(SQLException e)
        {
            System.out.println("Failed to execute"+sql);
            System.out.println(e.getMessage());
            return null;
        }
    }
}
