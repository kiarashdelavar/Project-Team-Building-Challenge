package nl.saxion.ptbc.database;

import java.sql.*;

public class SQLiteSelectTable {
    public static void main(String[] args) {

        // Url to database file
        String url = "jdbc:sqlite:sasa.db";

        // SQL statement
        String sql = "SELECT * FROM mission_logs";

        try {
            // connect to the database and create the sql statement
            Connection conn = DriverManager.getConnection(url);

            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);

            // Loop through the results and print them into the terminal
            while (result.next()) {
                String id = result.getString(1);
                String timestamp = result.getString(2);
                String command = result.getString(3);

                System.out.println(id + " | " + timestamp + " | " + command);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
