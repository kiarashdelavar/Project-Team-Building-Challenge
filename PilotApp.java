package nl.saxion.ptbc.pilot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import nl.saxion.ptbc.SaSaCommunicator;
import nl.saxion.ptbc.classes.*;
import nl.saxion.ptbc.database.ObstacleDatabaseHandler;
import nl.saxion.ptbc.groundControl.GroundControlUtils;
import nl.saxion.ptbc.missionLog.MissionLogDAO;
import nl.saxion.ptbc.missionLog.MissionStatus;
import nl.saxion.ptbc.missionLog.ReplayMission;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static nl.saxion.ptbc.frog.StartFrogApp.startFrogApp;


public class PilotApp extends Application {
    private Button upButton;
    private Button downButton;
    private Button leftButton;
    private Button rightButton;

    private boolean isLogging = false;
    private ArrayList<String> missionLog = new ArrayList<>();
    private RadarSystem radarSystem = new RadarSystem();
    private RadarView radarView = new RadarView();

    private double forwardSpeed;
    private double backwardSpeed;
    private double leftSpeed;
    private double rightSpeed;

    private Map map = new Map();

    private SaSaCommunicator sasa;

    private Frog frog = new Frog();

    private TextArea commandArea;  // So we can update the UI with incoming messages

    private ArrayList<Obstacle> notConvertedObstacles = new ArrayList<>();


    /**
     * Initializes and starts the PilotApp JavaFX user interface.
     * <p>
     * This method is automatically invoked when the application is launched.
     * It sets up the UI layout (radar graph, mission controls, directional buttons, and command area)
     * and initializes communication with the Frog and Ground Control systems.
     * <p>
     * Key functionality includes:
     * <ul>
     *   <li>Starts the FrogApp in a background thread.</li>
     *   <li>Launches the GroundControl application on the JavaFX thread.</li>
     *   <li>Initializes a {@link SaSaCommunicator} to manage message communication.</li>
     *   <li>Sends recurring setup commands (RADAR ON, STATUS ON) to ensure proper Frog functionality.</li>
     *   <li>Creates the dashboard layout with movement controls, mission log buttons, and radar graph area.</li>
     * </ul>
     *
     * @param primaryStage The primary stage for this application, where the scene and UI will be rendered.
     * @throws Exception if any UI or communication initialization fails.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        new Thread(() -> {
            try {
                // Start FrogApp
                startFrogApp();

                // Start GroundControlApp
                Platform.runLater(() -> {
                    try {
                        Stage groundControlStage = new Stage();
                        GroundControlUtils.startGroundControl(groundControlStage);
                    } catch (Exception e) {
                        System.err.println("Error starting GroundControl: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error starting FrogApp: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();


        // COMMUNICATION SETUP
        sasa = new SaSaCommunicator(
                "PILOT",
                SaSaCommunicator.DEFAULT_HOST,
                SaSaCommunicator.DEFAULT_PORT,
                this::receive,
                true
        );

        frog.addCommunicatorToFrog(sasa);

        new Thread(() -> {
            while (true) {   // repeat forever
                try {
                    sasa.send("RADAR ON");     // ask Frog to enable radar
                    sasa.send("STATUS ON");    // ask Frog to send status
                    Thread.sleep(3000);  // wait 3 seconds before trying again
                } catch (Exception e) {
                    System.out.println("Retrying setup commands...");
                }
            }
        }).start();
        primaryStage.setTitle("The Pilot Dashboard"); //Set name for title

        radarView.setPrefSize(600, 600);
        radarView.setStyle("-fx-border-color: black; -fx-background-color: lightgray;");
        Label radarLabel = new Label("Real Time Radar Graph");
        radarLabel.setLayoutX(220);
        radarLabel.setLayoutY(20);
        radarView.getChildren().add(radarLabel);

        //Elham
        MissionStatus.setCommunicator(sasa);

        // ARROW CONTROLS
        upButton = new Button("↑");
        downButton = new Button("↓");
        leftButton = new Button("←");
        rightButton = new Button("→");
        Button stopButton = new Button("Stop");

        GridPane arrowPad = new GridPane();
        arrowPad.setAlignment(Pos.CENTER);
        arrowPad.setHgap(10);
        arrowPad.setVgap(10);
        arrowPad.add(upButton, 1, 0);
        arrowPad.add(leftButton, 0, 1);
        arrowPad.add(rightButton, 2, 1);
        arrowPad.add(downButton, 1, 2);
        arrowPad.add(stopButton, 1, 1);

        upButton.setOnAction(e -> sendManualCommand("UP"));
        downButton.setOnAction(e -> sendManualCommand("DOWN"));
        leftButton.setOnAction(e -> sendManualCommand("LEFT"));
        rightButton.setOnAction(e -> sendManualCommand("RIGHT"));
        stopButton.setOnAction(e -> sendManualCommand("STOP"));

        // MISSION BUTTONS
        Button recordMissionLog = new Button("Record mission log");
        Button startMissionButton = new Button("Start pre-programmed Mission");
        Button replayMissionButton = new Button("Replay mission");

        startMissionButton.setOnAction(this::startMissionLogButton);
        recordMissionLog.setOnAction(this::startMissionLogButton);

        VBox missionButtonsColumn = new VBox(10);
        missionButtonsColumn.setAlignment(Pos.CENTER_LEFT);

        HBox firstRow = new HBox(10, recordMissionLog, startMissionButton);
        HBox secondRow = new HBox(replayMissionButton);

        missionButtonsColumn.getChildren().addAll(firstRow);
        missionButtonsColumn.getChildren().addAll(secondRow);

        //COMMAND AREA
        Label commandLabel = new Label("Commands from mission control");
        commandArea = new TextArea();
        commandArea.setPrefHeight(100);

        VBox commandBox = new VBox(5, commandLabel, commandArea);

        //COMBINE EVERYTHING!
        VBox bottomControls = new VBox(15, missionButtonsColumn, commandBox);
        bottomControls.setPadding(new Insets(10));

        VBox rightSide = new VBox(20, arrowPad, bottomControls);
        rightSide.setPadding(new Insets(10));
        rightSide.setAlignment(Pos.TOP_CENTER);

        Frog.getAutoDriveMissionLog().add(new Location(46.00, -124.67));
        Frog.getAutoDriveMissionLog().add(new Location(119.3, -142.00));
//        Frog.getAutoDriveMissionLog().add(new Location(238.00, -120.67));
//        Frog.getAutoDriveMissionLog().add(new Location(314.00, -58.00));

        BorderPane root = new BorderPane();
        root.setLeft(radarView);
        root.setCenter(rightSide);
        root.setPrefSize(1000, 600);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        startMissionButton.setOnAction(e -> {
            frog.autoDriveMissionLog(notConvertedObstacles);
        });

        // if the replay mission button is pressed, send the driving commands to the frog
        replayMissionButton.setOnAction(e -> {
            String command = ReplayMission.replayLatestCommand();
            System.out.println("Replaying: " + command);
            commandArea.appendText("Replaying: " + command + "\n");
            if (command.startsWith("PILOT ")) {
                command = command.substring("PILOT ".length());
            }
            sasa.send(command);
            sasa.send(command);
        });
    }

    /**
     * Sends manual drive commands based on directional input and updates internal motion state.
     * <p>
     * This method constructs and sends a movement command string to the robot via the {@code sasa} interface.
     * It adjusts speed values for forward, backward, left, and right directions, allowing for gradual acceleration
     * and simple turning maneuvers. After sending the command:
     * <ul>
     *     <li>The message is logged to the console and displayed in the UI's {@code commandArea}.</li>
     *     <li>If mission logging is enabled, the command is also added to the {@code missionLog}.</li>
     * </ul>
     *
     * <p>Direction options and their behavior:
     * <ul>
     *     <li>{@code "UP"}: Moves the robot forward with increasing speed (up to 1).</li>
     *     <li>{@code "DOWN"}: Moves the robot backward with decreasing speed (down to -1).</li>
     *     <li>{@code "LEFT"}: Turns the robot slightly left while moving forward.</li>
     *     <li>{@code "RIGHT"}: Turns the robot slightly right while moving forward.</li>
     *     <li>Any other value: Stops the robot completely.</li>
     * </ul>
     *
     * @param direction a string indicating the direction command to send (e.g., "UP", "DOWN", "LEFT", "RIGHT")
     */
    private void sendManualCommand(String direction) {
        String command;
        switch (direction) {
            // Going forward
            case "UP" -> {
                command = "DRIVE " + Math.min(forwardSpeed += 0.25, 1) + " 0 0";
                backwardSpeed = 0;
                leftSpeed = 0;
                rightSpeed = 0;
            }
            // Going Backward
            case "DOWN" -> {
                command = "DRIVE " + Math.max(backwardSpeed -= 0.25, -1) + " 0 0";
                forwardSpeed = 0;
                leftSpeed = 0;
                rightSpeed = 0;
            }
            // Slight forward + left turn
            case "LEFT" -> {
                command = "DRIVE " + Math.min(leftSpeed += 0.25, 1) + " -30 0";
                forwardSpeed = 0;
                backwardSpeed = 0;
                rightSpeed = 0;
            }
            // Slight forward + right turn
            case "RIGHT" -> {
                command = "DRIVE " + Math.min(rightSpeed += 0.25, 1) + " 30 0";
                forwardSpeed = 0;
                backwardSpeed = 0;
                leftSpeed = 0;
            }
            // Stop the frog
            default -> {
                command = "DRIVE 0 0 0";
                forwardSpeed = 0;
                backwardSpeed = 0;
                leftSpeed = 0;
                rightSpeed = 0;
            }
        }
        sasa.send(command);
        String message = "PILOT " + command;
        System.out.println("Sending command: " + message);
        commandArea.appendText("Sent: " + message + "\n");

        if (isLogging) {
            missionLog.add(message);
        }
    }

    /**
     * Handles the start/stop toggle for mission logging when the associated button is clicked.
     * <p>
     * When the button is pressed, this method flips the logging state (`isLogging`) and performs actions accordingly:
     * <ul>
     *     <li>If logging is started:
     *         <ul>
     *             <li>Clears any existing log entries in {@code missionLog}.</li>
     *             <li>Updates the UI to reflect that logging has started.</li>
     *             <li>Changes the button text to "Stop mission log".</li>
     *         </ul>
     *     </li>
     *     <li>If logging is stopped:
     *         <ul>
     *             <li>Prints the contents of {@code missionLog} to the console.</li>
     *             <li>Updates the UI to show the number of entries recorded.</li>
     *             <li>Changes the button text to "Start mission log".</li>
     *             <li>Saves the mission log to the database via {@code MissionLogDAO.insertMissionLog}.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param event the action event triggered by clicking the mission log button
     */
    private void startMissionLogButton(ActionEvent event) {
        // Flip the value of the logging boolean (true becomes false, and false becomes true etc.)
        isLogging = !isLogging;

        if (isLogging) {
            missionLog.clear();
            System.out.println("Recording Mission log");
            commandArea.appendText("Recording Mission log\n");
            ((Button) event.getSource()).setText("Stop recording mission log");
        } else {
            System.out.println("Recording mission log stopped. Log contents:");
            missionLog.forEach(System.out::println);
            commandArea.appendText("Mission log stopped. " + missionLog.size() + " entries recorded.\n");
            ((Button) event.getSource()).setText("Record mission log");

            // Add the missionLog arrayList to the database
            MissionLogDAO.insertMissionLog(missionLog);
        }
    }

    // Called when any message is received from Frog
    public void receive(String message) {
        if (message == null || message.isBlank()) return;

        if (!message.startsWith("FROG STATUS") && !message.startsWith("FROG RADAR BLIP")) {
            Platform.runLater(() -> {
                if (commandArea.getParagraphs().size() > 200) {
                    commandArea.clear(); //remove old commands to stop freezing
                    commandArea.appendText("---- Log cleared to prevent freezing ----\n");
                }
                commandArea.appendText("Received: " + message + "\n");
            });
        }

        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            System.out.println(" Skipping malformed message: " + message);
            return;
        }
        if (isLogging && message.startsWith("PILOT DRIVE")) {
            synchronized (missionLog) {
                missionLog.add(message);
            }
        }

        /**
         * Handles incoming "FROG STATUS" messages from the Frog simulation.
         *
         * Extracts position (x, z), energy level, and solar level from the message,
         * then updates the internal Frog object with these values.
         *
         * After updating, it forwards the relevant data to the Ground Control
         * via a formatted "SEND STATUS" message.
         *
         * Expected format: "FROG STATUS <time> <x> <y> <z> <energy> <solar>"
         *
         * Example message:
         * FROG STATUS 168.53 0.12 -92.36 79.96 0.98 0.00
         */
        if (message.startsWith("FROG STATUS")) {
            String[] parts1 = message.split(" ");
            try {
                frog.update(Double.parseDouble(parts1[2]), Double.parseDouble(parts1[3]),
                        Double.parseDouble(parts1[4]), Double.parseDouble(parts1[5]),
                        Double.parseDouble(parts1[6]), Double.parseDouble(parts1[7]));
                sasa.send("SEND STATUS " + parts1[2] + " " + parts1[4] + " " + parts1[6] + " " + parts1[7] + " " + parts1[5]);
            } catch (Exception e) {
                System.err.println("Invalid STATUS message: " + message);
            }
        }

        // radar points
        if (message.startsWith("FROG RADAR START")) {
            radarSystem.clear();
            String[] parts1 = message.split(" ");
            try {
                frog.updateRadar(Double.parseDouble(parts1[3]), Double.parseDouble(parts1[5]), Double.parseDouble(parts1[6]));
                notConvertedObstacles.clear();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (message.startsWith("FROG RADAR BLIP")) {
            String[] parts2 = message.split(" ");
            try {
                double obstacleX = Double.parseDouble(parts2[3]);
                double obstacleZ = Double.parseDouble(parts2[5]);

                Location unconvertedRadarPoint = new Location(obstacleX, obstacleZ);
                Location radarPoint = convertToAbsolute(frog.getRadarX(), frog.getRadarZ(), frog.getRadarAngle(), obstacleX, obstacleZ);

                sasa.send("OBSTACLE " + radarPoint.getX() + " " + radarPoint.getZ());

                notConvertedObstacles.add(new Obstacle(unconvertedRadarPoint));
                map.updateObstacleMap(new Obstacle(radarPoint));

                radarSystem.addRadarBlip(obstacleX, obstacleZ);

                Platform.runLater(() -> {
                    List<RadarPoint> filtered = radarSystem.getFilteredPoints();
                    radarView.draw(filtered);
                });
            } catch (Exception e) {
                System.err.println("Invalid radar points!!!");
            }
        }

        //AUTO DRIVE
        if (message.startsWith("GROUND_CONTROL GO TO")) {
            String[] parts3 = message.replace(',', '.').split(" ");
            double x = Double.parseDouble(parts3[3]);
            double y = Double.parseDouble(parts3[4]);
            Location destination = new Location(x, y);
            Map.setLocationsAutodrive(destination);
            frog.autoDrive(new LinkedList<>(Map.getLocationsAutodrive()), notConvertedObstacles);
            System.err.println(message);
        }


    }


    public Location convertToAbsolute(double xr, double zr, double angleDeg, double xp, double zp) {
        double angleRadian = Math.toRadians(angleDeg);
        double x = xr + xp * Math.cos(angleRadian) + zp * Math.sin(angleRadian);
        double z = zr + zp * Math.cos(angleRadian) - xp * Math.sin(angleRadian);
        return new Location(x, z);
    }


    /**
     * Called automatically when the PilotApp is closing.
     * <p>
     * Performs necessary shutdown procedures including:
     * - Saving all currently known obstacles to the database.
     * - Sending "RADAR OFF" and "STATUS OFF" signals to stop background processes.
     * - Closing the SaSaCommunicator connection cleanly.
     * <p>
     * This helps ensure that no obstacle data is lost and
     * the Pilot system shuts down gracefully.
     *
     * @throws Exception if any unexpected error occurs during shutdown.
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        // Save known obstacles automatically when app is closed
        ObstacleDatabaseHandler.saveObstacles(map.getKnownObstacles());
        if (sasa != null) {
            sasa.send("RADAR OFF");
            sasa.send("STATUS OFF");
            sasa.close();
            System.err.println("PilotApp closed. Radar and Status turned off.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
