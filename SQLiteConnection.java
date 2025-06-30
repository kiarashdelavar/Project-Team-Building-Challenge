package nl.saxion.ptbc.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:sasa.db"; // This will create the file in your project root

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Connected to the database.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}