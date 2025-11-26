package projectSem4.com.model.utils;

import java.sql.Connection;
import java.sql.SQLException;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;


public class DBUtil {
private static DBUtil _instance = null;
	
	public DBUtil() {
		// TODO Auto-generated constructor stub
	}
	
	public static DBUtil Instance() {
		_instance = _instance == null ? new DBUtil() : _instance;
		return _instance;
	}
	
	
	public Connection GetConnect() {
	    // Create datasource.
        
        
        Connection conn = null;
		try {
			SQLServerDataSource ds = new SQLServerDataSource();
	        ds.setUser(StringValue.USER_NAME);
	        ds.setPassword(StringValue.PASSWORD);
	        ds.setServerName(StringValue.SERVER_NAME);
	        ds.setPortNumber(Integer.parseInt(StringValue.PORT));
	        ds.setDatabaseName(StringValue.DATABASE_NAME);
	        ds.setTrustServerCertificate(true);
	       
            conn = ds.getConnection();
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
        	System.out.println("SQL Error: " + e.getMessage());
     
        }
		return conn;
	}
}
