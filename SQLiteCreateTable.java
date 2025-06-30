package nl.saxion.ptbc.database;
import java.sql.*;

/**
 * A simple utility class to create a SQLite database table named {@code mission_logs} if it does not already exist.
 * <p>
 * The table includes the following columns:
 * <ul>
 *     <li>{@code id} - An auto-incrementing primary key.</li>
 *     <li>{@code timestamp} - A timestamp column with the current time as the default value.</li>
 *     <li>{@code command} - A text field intended to store logged command entries.</li>
 * </ul>
 * <p>
 * The script connects to the SQLite database file {@code sasa.db}, creates the table if needed,
 * and prints status messages to the console.
 */

public class SQLiteCreateTable {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:sasa.db";

        String sql = "CREATE TABLE IF NOT EXISTS mission_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "command TEXT"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Table created or already exists.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
