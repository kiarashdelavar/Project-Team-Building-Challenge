package nl.saxion.ptbc.database;

import nl.saxion.ptbc.classes.Location;
import nl.saxion.ptbc.classes.Obstacle;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
/**
 * Handles database operations related to storing and retrieving obstacles.
 * Uses an SQLite database to persist obstacle (x, z) coordinates.
 */
public class ObstacleDatabaseHandler {

    // Path to the SQLite database file
    private static final String DB_URL = "jdbc:sqlite:sasa.db";

    /**
     * Saves only new unique obstacles to the SQLite database.
     * Obstacles are filtered using a rounded (x,z) key to avoid near-duplicate entries due to floating-point inaccuracies.
     *
     * @param obstacles the list of obstacles to attempt saving
     * @return the number of new obstacles that were successfully inserted into the database
     */
    public static int saveObstacles(ArrayList<Obstacle> obstacles) {
        int newCount = 0; // Counter for how many new obstacles were added

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();

            // Ensure the Obstacles table exists
            stmt.execute("CREATE TABLE IF NOT EXISTS Obstacles (x REAL, z REAL)");

            // Load already existing obstacle coordinates from DB to avoid duplicates
            Set<String> existingKeys = new HashSet<>();
            ResultSet rs = stmt.executeQuery("SELECT x, z FROM Obstacles");
            while (rs.next()) {
                // Round coordinates to 2 decimal places to avoid float precision issues
                String key = String.format("%.2f,%.2f", rs.getDouble("x"), rs.getDouble("z"));
                existingKeys.add(key);
            }

            // Prepare statement for batch insert
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Obstacles (x, z) VALUES (?, ?)");

            for (Obstacle obs : obstacles) {
                String key = String.format("%.2f,%.2f", obs.getLocation().getX(), obs.getLocation().getZ());

                // Only add if it's not already in the database
                if (!existingKeys.contains(key)) {
                    ps.setDouble(1, obs.getLocation().getX());
                    ps.setDouble(2, obs.getLocation().getZ());
                    ps.addBatch(); // Add to batch for faster execution
                    existingKeys.add(key); // Track to avoid duplicate within this save
                    newCount++;
                }
            }

            // Execute all INSERTS at once
            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace(); // Show error if something goes wrong
        }

        return newCount;
    }

    /**
     * Loads all previously saved obstacles from the database.
     *
     * @return an ArrayList containing all obstacle entries found in the database
     */
    public static ArrayList<Obstacle> loadObstacles() {
        ArrayList<Obstacle> obstacles = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Query all obstacle coordinates
            ResultSet rs = conn.createStatement().executeQuery("SELECT x, z FROM Obstacles");

            while (rs.next()) {
                double x = rs.getDouble("x");
                double z = rs.getDouble("z");

                // Create a new Obstacle object from DB row and add to the list
                obstacles.add(new Obstacle(new Location(x, z)));
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Print any errors
        }

        return obstacles; // Return full list
    }

    /**
     * Optional: Call this from GroundControl or PilotApp to log total count when closing.
     */

    /**
     * Logs the total number of unique obstacles currently stored in the database.
     * Typically used when the application shuts down to give feedback about saved data.
     */
    public static void logFinalObstacleCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // Run a query to count all rows (obstacles) in the Obstacles table
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM Obstacles");

            if (rs.next()) {
                int total = rs.getInt(1); // Get the count from the first column
                System.out.println("✅ Total unique obstacles in database: " + total);
            }

        } catch (SQLException e) {
            // Print any SQL errors for debugging purposes
            e.printStackTrace();
        }
    }
}
