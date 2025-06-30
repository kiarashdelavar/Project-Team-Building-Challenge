package nl.saxion.ptbc.groundControl;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nl.saxion.ptbc.CSVLoader.CSVLoaderController;
import nl.saxion.ptbc.classes.Frog;
import nl.saxion.ptbc.classes.Location;
import nl.saxion.ptbc.classes.Map;
import nl.saxion.ptbc.classes.Obstacle;
import nl.saxion.ptbc.missionLog.MissionLogController;
import nl.saxion.ptbc.SaSaCommunicator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controller class for the Ground Control interface.
 * Handles user interactions with the GUI such as clicking the map,
 * sending/loading missions, and updating status.
 */

public class GroundControlController {
    @FXML
    public Button btnStopAutoDrive;
    @FXML
    private Pane mapPane;

    @FXML
    private Label floatingCoordinateLabel;


    private Map map = new Map();
    private static final double scale = 0.5;
    private List<Obstacle> obstacles = map.getKnownObstacles();
    private static GroundControlController instance;
    // Text area to display mission status and navigation information
    public TextArea missionStatusNavigationInfo;

    private HashMap<Location, Circle> missionPoints = new HashMap<>();

    @FXML
    private TextField batteryText;
    @FXML
    private TextField solarText;


    // Labels to display live info like status, battery, location, and target
    @FXML
    private Label status;

    @FXML
    private Label locationLabel;

    @FXML
    private Label targetLabel;

    private SaSaCommunicator sasa;
    private Polygon frogTriangle;

    @FXML
    protected void initialize() {
        instance = this;
        sasa = new SaSaCommunicator("GROUND_CONTROL", this::receive);
        frogTriangle = new Polygon();
        frogTriangle.getPoints().addAll(0.0, -6.0, 4.0, 6.0, -4.0, 6.0); // triangle shape
        frogTriangle.setFill(Color.HOTPINK);
        mapPane.getChildren().add(frogTriangle);
        //for hovering through the map and showing the coordinates
        floatingCoordinateLabel = new Label();
        floatingCoordinateLabel.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 3;");
        floatingCoordinateLabel.setVisible(false);

        mapPane.getChildren().add(floatingCoordinateLabel);

        // Show and update label on mouse move
        mapPane.setOnMouseMoved(this::handleMouseMoved);
        Map save = new Map(mapPane);


    }

    /**
     * Handles mouse movement over the {@code mapPane} by converting the cursor's pixel position
     * to logical coordinates and displaying them in a floating label.
     * <p>
     * The logical coordinates range from -500 to 500 in both X and Y directions.
     * The floating label appears near the cursor and updates in real time as the mouse moves within the map area.
     * If the mouse leaves the {@code mapPane}, the label is hidden.
     *
     * @param event the MouseEvent triggered when the mouse moves over the {@code mapPane}
     */
    private void handleMouseMoved(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        // Convert from pixel to logical coordinates
        double width = mapPane.getWidth();
        double height = mapPane.getHeight();
        double logicalX = (x / width) * 1000 - 500;
        double logicalY = ((height - y) / height) * 1000 - 500;

        // show the label when the mouse is inside the mapPane
        if (x >= 0 && x <= width && y >= 0 && y <= height) {
            floatingCoordinateLabel.setText(String.format("(%.1f, %.1f)", logicalX, logicalY));
            floatingCoordinateLabel.setLayoutX(x + 10);
            floatingCoordinateLabel.setLayoutY(y - 10);
            floatingCoordinateLabel.setVisible(true);
        } else {
            floatingCoordinateLabel.setVisible(false);
        }
    }

    /**
     * Handles the action event triggered to load and display the Mission Log window.
     * <p>
     * This method creates a new stage, loads the "mission-log.fxml" file, sets up a
     * scene with specified dimensions, and displays the stage with the title
     * "View mission log".
     * </p>
     *
     * @param actionEvent the event that triggered this method, typically a button click.
     * @throws IOException if the FXML file cannot be loaded.
     */
    public void OnLoadMission(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MissionLogController.class.getResource("mission-log.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("View mission log");
        stage.setScene(scene);
        stage.show();

    }

    /**
     * Handles the action event to stop the autonomous driving feature.
     * <p>
     * This method interacts with the {@code Map} class to halt the auto-drive functionality.
     * It is triggered by a user action, such as clicking a button.
     * </p>
     *
     * @param actionEvent the event that triggered this method, typically a button click.
     */
    public void OnStopAutoDrive(ActionEvent actionEvent) {
        Map.stopAutoDrive();
    }

    /**
     * Event handler that exports collected collision points to a CSV file.
     * <p>
     * If there are no obstacles (i.e., no collision points), a message is printed to the console and the method returns.
     * Otherwise, the method builds a CSV string from the X and Z coordinates of each obstacle and prompts the user
     * to choose a location to save the file using a {@link javafx.stage.FileChooser}.
     * Upon successful export, an information alert is shown; if an error occurs during export, an error alert is displayed.
     *
     * @param actionEvent the action event triggered by the export button (typically a button press)
     */
    public void OnExportCollectedCollisionPoints(ActionEvent actionEvent) {
        if (obstacles == null || obstacles.isEmpty()) {
            System.out.println("No Collision point to export");
            return;
        }

        try {
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("X,Z\n");

            for (Obstacle obstacle : obstacles) {
                csvContent.append(obstacle.getLocation().getX()).append(",")
                        .append(obstacle.getLocation().getZ()).append("\n");
            }

            //user should choose a path to save the csv
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export collision points");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file", "*.csv"));

            File file = fileChooser.showSaveDialog(mapPane.getScene().getWindow());

            if (file != null) {
                Files.write(file.toPath(), csvContent.toString().getBytes(StandardCharsets.UTF_8));

                //showing successful alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Collision points were successfully exported.");
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();

            //showing unsuccessful alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText("Failed to export collision points.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Handles the action event to load and display the CSV Loader window for importing collected collision points.
     * <p>
     * This method creates a new stage, loads the "CSV-loader.fxml" file, sets up a
     * scene with specified dimensions, and displays the stage with the title
     * "View mission log".
     * </p>
     *
     * @param actionEvent the event that triggered this method, typically a button click.
     * @throws IOException if the FXML file cannot be loaded.
     */
    public void OnImportCollectedCollisionPoints(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(CSVLoaderController.class.getResource("CSV-loader.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("View mission log");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Updates the location label in the Ground Control UI with the latest coordinates.
     *
     * @param newLocation A string representing the current location of the vehicle (e.g., "(8.0, -123.0)")
     */
    public void updateLocation(String newLocation) {
        locationLabel.setText("Location: " + newLocation);
    }

    /**
     * Updates the status label in the Ground Control UI with a new status message.
     * If the status includes "Connected", it will be styled in green and bold.
     *
     * @param newStatus A string representing the current connection or system status (e.g., "Connected")
     */
    public void updateStatus(String newStatus) {
        status.setText("Status: " + newStatus);

        // Style based on message
        if (newStatus.toLowerCase().contains("connected")) {
            status.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 16px;");
        } else {
            status.setStyle("-fx-text-fill: black; -fx-font-size: 16px;");
        }
    }

    /**
     * Updates the battery and solar text fields in the Ground Control GUI.
     * <p>
     * This method takes the normalized energy and solar values
     * (in range 0.0 to 1.0), converts them to percentage format,
     * and displays them in their respective read-only text fields.
     *
     * @param energy The battery level as a decimal (e.g., 0.95 for 95%)
     * @param solar  The solar charge level as a decimal (e.g., 0.75 for 75%)
     */
    public void updateBatteryAndSolar(double energy, double solar) {
        batteryText.setText(String.format("%.1f%%", energy * 100));
        solarText.setText(String.format("%.1f%%", solar * 100));
    }

    /**
     * Updates the target label in the Ground Control UI with the latest target coordinates.
     *
     * @param newTarget A string representing the target location (e.g., "(200.0, 450.0)")
     */
    public void updateTarget(String newTarget) {
        targetLabel.setText("Target: " + newTarget);
    }

    /**
     * Handles mouse clicks on the map pane.
     * Adds a red dot marker and sends a formatted "Go_To" command.
     */
    public void OnMapClicked(MouseEvent mouseEvent) {
        // Get click coordinates relative to the pane
        double x = (mouseEvent.getX() - mapPane.getPrefWidth() / 2) / scale;
        double y = (mouseEvent.getY() - mapPane.getPrefHeight() / 2) / scale;

        double realX = x * 2 * scale;
        double realY = y * -2 * scale;


        Location clickedLocation = new Location(realX, realY);
        Map.setLocationsAutodrive(clickedLocation);
        Circle redDot = new Circle(mouseEvent.getX(), mouseEvent.getY(), 5, Color.RED);

        redDot.setOnMouseClicked(e -> {
            Location found = null;
            for (Location loc : Map.getAutodriveMarkers().keySet()) {
                if (Map.getAutodriveMarkers().get(loc) == redDot) {
                    found = loc;
                    break;
                }
            }
            if (found != null) {
                Map.getAutodriveMarkers().remove(found);
                Map.getLocationsAutodrive().remove(found);
                Platform.runLater(Map::drawAutoDriveMap);
            }
            e.consume();
        });

        Map.setLocationsAutodrive(clickedLocation);
        Map.setAutodriveMarkers(clickedLocation, redDot);
        Platform.runLater(Map::drawAutoDriveMap);

        Map.drawAutoDriveMap();

        String command = String.format("GO TO %.2f %.2f", realX, realY);
        System.out.println("Sent to PILOT: " + command);

        updateTarget(String.format("(%.1f, %.1f)", x, y));

        send(command);
    }

    /**
     * Retrieves the current instance of the {@code Map} object.
     * <p>
     * This method checks if an instance of the enclosing class exists. If so, it
     * returns the associated {@code Map} object; otherwise, it returns {@code null}.
     * </p>
     *
     * @return the current {@code Map} object if an instance exists, or {@code null} if no instance is available.
     */
    public static Map getMap() {
        return instance != null ? instance.map : null;
    }

    /**
     * Receives and processes messages sent from the PilotApp to GroundControl.
     * <p>
     * This method handles two types of messages:
     * <ul>
     *   <li><b>"PILOT SEND STATUS"</b> - Updates the frog's position on the map and
     *   displays the latest energy and solar values in text fields.</li>
     *   <li><b>"PILOT OBSTACLE"</b> - Registers a new obstacle at given coordinates
     *   and visualizes it as a radar point on the map.</li>
     * </ul>
     * <p>
     * GUI updates are executed on the JavaFX Application Thread using {@code Platform.runLater}.d
     * <p>
     * Expected message formats:
     * <ul>
     *   <li>{@code "PILOT SEND STATUS <x> <z> <energy> <solar>"}</li>
     *   <li>{@code "PILOT OBSTACLE <x> <z>"}</li>
     * </ul>
     *
     * @param message The string message received from the PilotApp.
     */
    public void receive(String message) {
        if (message.startsWith("PILOT SEND STATUS")) {
            String[] parts1 = message.split(" ");

            double x = Double.parseDouble(parts1[3]);
            double z = Double.parseDouble(parts1[4]);
            double energy = Double.parseDouble(parts1[5]);
            double solar = Double.parseDouble(parts1[6]);
            double angle = Double.parseDouble(parts1[7]);

            double mapCenterX = mapPane.getPrefWidth() / 2;
            double mapCenterZ = mapPane.getPrefHeight() / 2;

            double frogX = mapCenterX + x * scale;
            double frogZ = mapCenterZ - z * scale;

            Platform.runLater(() -> {
                frogTriangle.setLayoutX(frogX);
                frogTriangle.setLayoutY(frogZ);
                frogTriangle.setRotate(angle);
                updateLocation(String.format("(%.1f, %.1f)", x, z));
                updateStatus("Connected");
                updateBatteryAndSolar(energy, solar);
                drawFrogPath(mapPane, frogX, frogZ);
                mapPane.getChildren().remove(frogTriangle);
                mapPane.getChildren().add(frogTriangle);
                drawMissionLogOnMap(mapPane, scale);
            });

        }
        if (message.startsWith("PILOT OBSTACLE")) {
            String[] parts2 = message.split(" ");
            try {
                double x = Double.parseDouble(parts2[2]);
                double z = Double.parseDouble(parts2[3]);

                Location loc = new Location(x, z);
                Obstacle ob = new Obstacle(loc);
                map.updateObstacleMap(ob); // Optional: already checks for near-duplicates

                Platform.runLater(() -> drawRadarPoint(mapPane, loc, scale));
                // Save only this one obstacle
                ArrayList<Obstacle> justThisOne = new ArrayList<>();
                justThisOne.add(ob);
            } catch (Exception e) {
                System.err.println("Invalid radar points!!!");
            }
        }
        //Elham added this for text field update
        if (message.startsWith("GROUND_CONTROL GO TO")) {
            String[] parts = message.replace(',', '.').split(" ");
            double x = Double.parseDouble(parts[3]);
            double y = Double.parseDouble(parts[4]);
            String goToText = "Go To : " + x + ", " + y + "\n";
            Platform.runLater(() -> missionStatusNavigationInfo.appendText(goToText));
        } else if (
                message.startsWith("PILOT Destination") ||
                        message.startsWith("PILOT Status: Reached") ||
                        message.startsWith("PILOT Mission log") ||
                        message.startsWith("PILOT All destinations")
        ) {
            Platform.runLater(() -> missionStatusNavigationInfo.appendText(message + "\n"));
        }

    }

    public void drawRadarPoint(Pane radarGraph, Location radarPointLocation, double scale) {
        double centerX = radarGraph.getPrefWidth() / 2;
        double centerZ = radarGraph.getPrefHeight() / 2;


        double pointX = centerX + radarPointLocation.getX() * scale;
        double pointZ = centerZ + (radarPointLocation.getZ() * scale) * -1;

        Circle point = new Circle(pointX, pointZ, 1, Color.YELLOW);
        radarGraph.getChildren().add(point);
    }

    /**
     * Draws a point on the map to represent a position in the frog's movement path.
     * Each call to this method adds a small green circle at the specified (x, y) coordinates,
     * visually showing the path the frog has taken over time.
     *
     * @param radarGraph the Pane on which the frog's path is being drawn
     * @param x          the X-coordinate of the frog's position
     * @param y          the Y-coordinate of the frog's position
     */
    public void drawFrogPath(Pane radarGraph, double x, double y) {
        Circle frogPosition = new Circle(x, y, 2, Color.LIGHTGREEN);
        radarGraph.getChildren().add(frogPosition);
    }

    /**
     * Draws the mission log on the map pane. It updates the map by removing old mission points
     * and plotting the current mission locations as blue circles.
     *
     * @param mission The pane where the mission log will be drawn.
     * @param scale   The scale factor to adjust the size and positioning of mission points on the map.
     */
    public void drawMissionLogOnMap(Pane mission, double scale) {
        //remove the completed missionLog point
        for (Circle point : missionPoints.values()) {
            mission.getChildren().remove(point);
        }
        missionPoints.clear();

        ArrayList<Location> locations = Frog.getAutoDriveMissionLog();
        double centerX = mission.getPrefWidth() / 2;
        double centerZ = mission.getPrefHeight() / 2;

        //draws the missions on the map and adds them to the Hashmap
        for (Location location : locations) {
            double pointX = centerX + location.getX() * scale;
            double pointZ = centerZ + (location.getZ() * scale) * -1;

            Circle point = new Circle(pointX, pointZ, 3, Color.BLUE);
            mission.getChildren().add(point);
            missionPoints.put(location, point);
        }
    }

    public void send(String command) {
        sasa.send(command);
    }


}
