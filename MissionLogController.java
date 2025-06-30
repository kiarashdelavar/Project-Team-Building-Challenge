package nl.saxion.ptbc.missionLog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class MissionLogController {

    private final ObservableList<MissionLog> missionLogList = FXCollections.observableArrayList();
    @FXML
    public Button deleteMissionLog;


    @FXML
    private TableView<MissionLog> missionTableView;
    @FXML
    private TableColumn<MissionLog, Integer> idColumn;
    @FXML
    private TableColumn<MissionLog, String> missionColumn;

    @FXML
    private TableColumn<MissionLog, String> timeStampColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        missionColumn.setCellValueFactory(new PropertyValueFactory<>("mission"));
        timeStampColumn.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));
        loadMissionLog();
    }

    /**
     * Loads all mission log entries from the {@code mission_logs} table in the SQLite database
     * and displays them in the {@link #missionTableView}.
     * <p>
     * This method performs the following actions:
     * <ul>
     *     <li>Clears the current observable list of mission logs.</li>
     *     <li>Connects to the {@code sasa.db} database using JDBC.</li>
     *     <li>Executes a {@code SELECT *} query on the {@code mission_logs} table.</li>
     *     <li>Iterates through the result set and creates {@link MissionLog} objects for each row.</li>
     *     <li>Adds the retrieved logs to the {@code missionLogList} observable list.</li>
     *     <li>Sets the updated list into the {@code missionTableView} to update the UI.</li>
     * </ul>
     * <p>
     * If an SQL exception occurs during the database interaction, it is wrapped and thrown as a {@link RuntimeException}.
     */
    @FXML
    public void loadMissionLog() {
        missionLogList.clear();

        String url = "jdbc:sqlite:sasa.db";
        String sql = "SELECT * FROM mission_logs";

        try {
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sql);

            while (result.next()) {
                int id = result.getInt("id");
                String mission = result.getString("command");
                String timestamp = result.getString("timestamp");
                missionLogList.add(new MissionLog(id, timestamp, mission));
            }

            missionTableView.setItems(missionLogList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Sends the currently selected mission log entry to the pilot by inserting it into the
     * {@code sent_mission} table in the sasa database.
     * <p>
     * This method is triggered by a UI button. It performs the following steps:
     * <ul>
     *     <li>Ensures the {@code sent_mission} table exists in the database by calling {@code MissionLogDAO.createSentMissionTableIfNotExists()}.</li>
     *     <li>Retrieves the selected mission from the UI table view.</li>
     *     <li>If a mission is selected, inserts it (with its timestamp) into the {@code sent_mission} table.</li>
     *     <li>If no mission is selected, it logs a warning and exits early.</li>
     *     <li>Handles and logs any SQL exceptions that occur during the process.</li>
     * </ul>
     */

    @FXML
    public void sendMissionToPilot() {

        MissionLogDAO.createSentMissionTableIfNotExists();

        MissionLog selectedLog = missionTableView.getSelectionModel().getSelectedItem();

        if (selectedLog == null) {
            System.out.println("No mission selected");
            return;
        }

        String sql = "INSERT INTO sent_mission(command, timestamp) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:sasa.db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, selectedLog.getMission());
            stmt.setString(2, selectedLog.getTimeStamp());
            stmt.executeUpdate();

            System.out.println("Mission sent to pilot");

        } catch (SQLException e) {
            System.err.println("Failed to send mission: " + e.getMessage());
        }
    }

    /**
     * Handles the deletion of a selected mission log entry from both the database and the UI table.
     * <p>
     * This method is triggered by the delete button in the UI. It performs the following steps:
     * <ul>
     *     <li>Retrieves the currently selected {@link MissionLog} from the {@code missionTableView}.</li>
     *     <li>If no mission is selected, it prints a message and exits.</li>
     *     <li>Prepares and executes a SQL {@code DELETE} statement to remove the selected mission from the {@code mission_logs} table in the {@code sasa.db} SQLite database.</li>
     *     <li>If the deletion is successful, removes the mission log entry from the {@code missionLogList} observable list to update the UI.</li>
     *     <li>If no rows are affected, it indicates that the mission was not found in the database.</li>
     *     <li>Handles any {@link SQLException} by logging the error message to the console.</li>
     * </ul>
     *
     * @param actionEvent the event triggered by pressing the delete button
     */
    @FXML
    public void OnDelete(ActionEvent actionEvent) {
        // Get selected item from TableView
        MissionLog selectedLog = missionTableView.getSelectionModel().getSelectedItem();

        if (selectedLog == null) {
            System.out.println("No mission selected for deletion.");
            return;
        }

        int idToDelete = selectedLog.getId();

        String sql = "DELETE FROM mission_logs WHERE id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:sasa.db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idToDelete);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Mission deleted successfully.");
                missionLogList.remove(selectedLog); // Also remove from the observable list
            } else {
                System.out.println("No mission found with the specified ID.");
            }

        } catch (SQLException e) {
            System.err.println("Failed to delete mission: " + e.getMessage());
        }
    }
}
