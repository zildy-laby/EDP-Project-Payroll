package payrolldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Updated URL for SQL Server Authentication
    private static final String URL = "jdbc:sqlserver://HP-ZILDJIAN;databaseName=PayrollDB;encrypt=false;";
    private static final String USER = "sa"; // Your new SQL user
    private static final String PASSWORD = "zild"; // Your new password

    public static Connection getConnection() throws SQLException {
        try {
            // The driver needs to know its authentication method
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            throw e; // Rethrow to show the error
        }
    }
}