package nl.saxion.ptbc.groundControl;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class GroundControlUtils {

    /**
     * Initializes and displays the Ground Control JavaFX application window.
     * Makes it possible to start the Ground Control with the PilotApp.
     * <p>
     * This method clears the obstacles table in the database, loads the
     * Ground Control FXML layout, sets up the scene on the provided Stage,
     * and shows the Stage.
     *
     * @param stage the primary JavaFX Stage on which the Ground Control UI will be set
     * @throws Exception if loading the FXML or any other setup step fails
     */
    public static void startGroundControl(Stage stage) {
        new Thread(() -> {
            try {

                URL fxmlLocation = GroundControlUtils.class.getResource("/groundControl/ground-control.fxml");
                Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlLocation));

                Platform.runLater(() -> {
                    stage.setTitle("Ground Control");
                    stage.setScene(new Scene(root));
                    stage.show();
                });
            } catch (Exception e) {
                System.err.println("Error starting GroundControl: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

}

