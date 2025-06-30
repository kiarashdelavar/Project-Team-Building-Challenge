package nl.saxion.ptbc.missionLog;

import java.sql.*;

public class ReplayMission {
    private static final String DB_URL = "jdbc:sqlite:sasa.db";


    /**
     * The {@code ReplayMission} class is responsible for retrieving the most recently sent
     * mission command from the sasa database.
     * <p>
     * This class is used to support the "Replay Mission" functionality in the PilotApp by
     * querying the {@code sent_mission} table and returning the latest command that was sent
     * from ground control.
     */
    public static String replayLatestCommand() {
        String latestCommand = getLatestCommandFromDB();

        if (latestCommand == null) {
            System.out.println("No mission received from ground control");
            return "";
        }

        return latestCommand;
    }

    /**
     * Retrieves the latest mission command from the database.
     * <p>
     * This method is typically called when the user presses the "Replay Mission" button.
     * If no command is found, it prints a message and returns an empty string.
     *
     * @return the latest command string from the mission log, or an empty string if none found
     */

    private static String getLatestCommandFromDB() {
        String sql = "SELECT command FROM sent_mission ORDER BY id DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getString("command");
            }

        } catch (SQLException e) {
            System.err.println("Fout bij ophalen van laatste command: " + e.getMessage());
        }

        return null;
    }
}
