package nl.saxion.ptbc.missionLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Data Access Object (DAO) class for inserting mission log entries into a SQLite database.
 * <p>
 * This class provides a static method to store a list of mission commands into the
 * {@code mission_logs} table in the {@code sasa.db} database. Each entry includes the command text
 * and a timestamp of when it was logged.
 */

public class MissionLogDAO {

    // url to database file
    private static final String url = "jdbc:sqlite:sasa.db";

    /**
     * Inserts a list of mission commands into the {@code mission_logs} table.
     * <p>
     * Each command is inserted with the current timestamp. The insertion is done as a batch
     * for improved performance.
     *
     * @param missionLog a list of command strings to be stored in the database
     */

    public static void insertMissionLog(List<String> missionLog) {
        createTableIfNotExists();

        // SQL statement that needs to be executed
        String sql = "INSERT INTO " +
                "mission_logs(command, timestamp) " +
                "VALUES (?, ?)";

        try (
                // set up the connection with the database and prepare SQL statement
                Connection conn = DriverManager.getConnection(url);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Add each command from the missionLog into a batch
            for (String command : missionLog) {
                stmt.setString(1, command);

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                stmt.setString(2, timestamp);

                stmt.addBatch();
            }

            // Add the whole batch to the database
            stmt.executeBatch();
            System.out.println("MissionLogs are stored in the database!");
        } catch (SQLException e) {
            System.err.println("Error while saving the missionLog: " + e.getMessage());
        }
    }

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

    public static void createTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS mission_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "command TEXT"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("mission_logs table ensured.");
        } catch (SQLException e) {
            System.err.println("Failed to create/check mission_logs table: " + e.getMessage());
        }
    }

    /**
     * A simple utility class to create a SQLite database table named {@code sent_mission} if it does not already exist.
     * <p>
     * The table includes the following columns:
     * <ul>
     *     <li>{@code id} - An auto-incrementing primary key.</li>
     *     <li>{@code command} - A text field intended to store logged command entries.</li>
     *     <li>{@code timestamp} - A timestamp column with the current time as the default value.</li>
     * </ul>
     * <p>
     * The script connects to the SQLite database file {@code sasa.db}, creates the table if needed,
     * and prints status messages to the console.
     */

    public static void createSentMissionTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS sent_mission (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "command TEXT," +
                "timestamp TEXT" +
                ")";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:sasa.db");
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("sent_mission table ensured.");
        } catch (SQLException e) {
            System.err.println("Could not create sent_mission table: " + e.getMessage());
        }
    }
}