package nl.saxion.ptbc.groundControl;

import javafx.application.Application;
import javafx.stage.Stage;
import nl.saxion.ptbc.classes.Map;
import nl.saxion.ptbc.database.ObstacleDatabaseHandler;

import java.util.ArrayList;

public class GroundControlApp extends Application {

    /**
     * Calls the groundControlUtils startGroundControl method
     * <p>
     * This method starts the groundControl app
     * Also it sets the groundControl as a primaryStage
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        GroundControlUtils.startGroundControl(primaryStage);
    }

    /**
     * Called when the GroundControl application is shutting down.
     * <p>
     * This method performs cleanup by saving all currently known obstacles
     * to the SQLite database and logs the final count of stored obstacles.
     * <p>
     * It ensures that no collected obstacle data is lost between sessions.
     */
    @Override
    public void stop() {
        // Save all current known obstacles to DB
        Map currentMap = GroundControlController.getMap();
        if (currentMap != null) {
            int totalSaved = ObstacleDatabaseHandler.saveObstacles(new ArrayList<>(currentMap.getKnownObstacles()));
            System.out.println("✅ Final save: Obstacles flushed to DB: " + totalSaved);
        }

        // Log how many total unique entries are now in DB
        ObstacleDatabaseHandler.logFinalObstacleCount();
    }

}
