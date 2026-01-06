package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public DBConnection() {
    }

    public Connection getDBConnection() throws SQLException {
        String dbURL = System.getenv("JDBC_URL");
        String dbUsername = System.getenv("USERNAME");
        String dbPassword = System.getenv("PASSWORD");

        return DriverManager.getConnection(dbURL, dbUsername, dbPassword);
    }
    public void closeConnection (AutoCloseable... ressources){
        for (AutoCloseable ressource : ressources){
            if (ressource != null){
                try{
                    ressource.close();
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
