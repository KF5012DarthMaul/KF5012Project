package dbmgr;

import dbmgr.DBExceptions.FailedToConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

/**
 * Singleton DB connection handler
 * 
 * @author Emanuel Oliveira W19029581
 */
public final class DBConnection 
{
    /**
     * Wrapper class for {@link java.sql.PreparedStatement}<p>
     * Uses a cursor to add fields and supports batching.
     */
    public class DBPreparedStatement
    {
        private int cursor;
        private PreparedStatement prepStmt;
        private int batchCount;
        
       /**
        * Adds a batch to the statement, allowing for multiple insertions in one go.<p>
        * Automatically executes the batch at 100 pending statements.
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
        * @return True if successful, False if an error occured
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
        * @param o The string to add to the statement.
        * @throws SQLException 
        * @throws NullPointerException
        */
       public void add(String o) throws SQLException, NullPointerException
       {
           if(o == null)
               addNull();
           else
               prepStmt.setString(cursor++, o);
       }

       /** 
        * Adds an integer to the prepared statement.<p>
        * Will fail if the prepared statement has not been initialized. Call preparedStatement() to prevent this.<p>
        * Will fail if called more than there are ? fields available in the prepared statement.
        * @param o The integer to add to the statement.
        * @throws SQLException 
        * @throws NullPointerException
        */
       public void add(Integer o)throws SQLException, NullPointerException
       {
           if(o == null)
               addNull();
           else
               prepStmt.setInt(cursor++, o);
       }

       /** 
        * Adds a long integer to the prepared statement.<p>
        * Will fail if the prepared statement has not been initialized. Call preparedStatement() to prevent this.<p>
        * Will fail if called more than there are ? fields available in the prepared statement.
        * @param o The long integer to add to the statement.
        * @throws SQLException 
        * @throws NullPointerException
        */
       public void add(Long o)throws SQLException, NullPointerException
       {
           if(o == null)
               addNull();
           else
               prepStmt.setLong(cursor++, o);
       }

       /** 
        * Adds a boolean to the prepared statement.<p>
        * Will fail if the prepared statement has not been initialized. Call preparedStatement() to prevent this.<p>
        * Will fail if called more than there are ? fields available in the prepared statement.
        * @param o The boolean to add to the statement.
        * @throws SQLException 
        * @throws NullPointerException
        */
       public void add(boolean o)throws SQLException, NullPointerException
       {
           prepStmt.setBoolean(cursor++, o);
       }

       /** 
        * Adds a boolean to the prepared statement.<p>
        * Will fail if the prepared statement has not been initialized. Call preparedStatement() to prevent this.<p>
        * Will fail if called more than there are ? fields available in the prepared statement.
        * @throws SQLException 
        * @throws NullPointerException
        */
       public void addNull()throws SQLException, NullPointerException
       {
           prepStmt.setNull(cursor++, Types.NULL);
       }

       /**
        * Executes the prepared statement.<p>
        * If prepareStatement() was not called before this method, a NullPointerException will be thrown.<p>
        * Will fail if the prepared statement's ? fields were not fully populated via add().
        * @return True if successful, False if an error occured
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
        * If the prepared statement's ? fields were not fully populated via add(), this method will fail.<p>
        * The ResultSet will automatically close if it contains no results. Call isClosed() to verify if this has happened.
        * @return If successful, a ResultSet containing all queried columns. NULL if an error occured.
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
    
        /**
        * Prepare a statement, avoiding SQL injection.<p>
        * Instance is nulled when executePrepated or executePreparedQuery are called.<p>
        * After calling this function, add() should be called to populate the ? fields
        * @param sql The SQL code to prepare with.
        * @throws SQLException 
        */
       public DBPreparedStatement(String sql) throws SQLException
       {
           prepStmt = conn.prepareStatement(sql);
           cursor = 1;
       }
    }
    
    static private DBConnection instance;
    private Connection conn;

    /**
    * Prepare a statement, avoiding SQL injection.<p>
    * Instance is nulled when executePrepated or executePreparedQuery are called.<p>
    * After calling this function, add() should be called to populate the ? fields
    * @param sql The SQL code to prepare with.
     * @return a new Prepared statement object
    * @throws SQLException 
    */
   public DBPreparedStatement prepareStatement(String sql) throws SQLException
   {
       return new DBPreparedStatement(sql);
   }
    
    /**
     * Get the singleton instance of this class.
     * @return The singleton instance.
     */
    static DBConnection getInstance() throws FailedToConnectException
    {
        if(instance == null)
            instance = new DBConnection();
        return instance;
    }
    
    /**
     * Creates an instance and connects the database to a file.
     * @throws dbmgr.DBExceptions.FailedToConnectException 
     */
    private DBConnection() throws FailedToConnectException
    {
        try {
            connect(".\\db.sq3");
        } catch (SQLException ex) {
            throw new FailedToConnectException(ex);
        }
    }
    
    /**
     * Connects the database driver to a database file.
     * @param filename The location of the database file.
     * @return Success
     * @throws SQLException 
     */
    private boolean connect(String filename) throws SQLException
    {
        String url = "jdbc:sqlite:"+filename;
        conn = DriverManager.getConnection(url);
        System.out.println("Connected");
        return conn != null;
    }
    
    /**
     * Execute sql code with no protection.
     * @param sql
     * @return Suceess
     */
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
    
    /**
     * Execute sql code with no protection.
     * @param sql
     * @return A {@link java.sql.ResultSet} with the results from the query.
     */
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
