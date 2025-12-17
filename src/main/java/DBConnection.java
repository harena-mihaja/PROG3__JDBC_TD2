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
}
