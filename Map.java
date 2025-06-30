package nl.saxion.ptbc.classes;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Map {
    private ArrayList<Obstacle> knownObstacles;
    private Set<String> knownObstacleKeys;
    private Location baseLocation;
    private static Pane pane;
    private static ArrayList<Location> locationsAutodrive = new ArrayList<>();
    private static HashMap<Location, Circle> autodriveMarkers = new HashMap<>();

    //Constructor:
    public Map() {
        this.knownObstacles = new ArrayList<>();
        Location baseLoc = new Location(0, 0);
        this.knownObstacleKeys = new HashSet<>(); // Initialize the key set
        this.baseLocation = baseLoc;
    }

    public Map(Pane pane) {
        Map.pane = pane;
    }

    /**
     * Draws the auto-drive map by adding markers to the UI pane.
     * <p>
     * This method removes any existing auto-drive markers from the pane and then
     * iterates through the {@code autodriveMarkers} collection, adding each marker
     * back to the pane with a designated ID of "autodrive-marker".
     * </p>
     */
    public static void drawAutoDriveMap() {
        pane.getChildren().removeIf(node -> "autodrive-marker".equals(node.getId()));

        for (Circle dot : autodriveMarkers.values()) {
            dot.setId("autodrive-marker");
            pane.getChildren().add(dot);
        }
    }

    /**
     * Stops the auto-drive functionality by clearing markers and resetting data.
     * <p>
     * This method removes all nodes with the ID "autodrive-marker" from the pane,
     * clears the {@code autodriveMarkers} collection, and resets the {@code locationsAutodrive} list.
     * </p>
     */
    public static void stopAutoDrive() {
        pane.getChildren().removeIf(node -> "autodrive-marker".equals(node.getId()));
        autodriveMarkers.clear();
        locationsAutodrive.clear();
    }

    /**
     * Updates the obstacle map with a new obstacle, ensuring no duplicates are added.
     * <p>
     * This method generates a unique key based on the X and Z coordinates of the
     * provided {@code Obstacle}, rounded to two decimal places. If the key does not
     * already exist in the set of known obstacle keys, the obstacle is added to the
     * list of known obstacles, and its key is tracked. The total number of unique
     * obstacles is then printed to the console.
     * </p>
     *
     * @param newObstacle the {@code Obstacle} to be added to the map.
     */
    public void updateObstacleMap(Obstacle newObstacle) {
        String key = String.format("%.2f:%.2f",
                newObstacle.getLocation().getX(),
                newObstacle.getLocation().getZ());

        if (!knownObstacleKeys.contains(key)) {
            knownObstacles.add(newObstacle);
            knownObstacleKeys.add(key);
            System.out.println("🧱 Unique obstacles so far: " + knownObstacles.size());
        }
    }

    //Getters:
    public static ArrayList<Location> getLocationsAutodrive() {
        return locationsAutodrive;
    }

    public ArrayList<Obstacle> getKnownObstacles() {
        return knownObstacles;
    }

    public static HashMap<Location, Circle> getAutodriveMarkers() {
        return autodriveMarkers;
    }

    //Setters:
    public static void setLocationsAutodrive(Location location) {
        locationsAutodrive.add(location);
    }

    public static void setAutodriveMarkers(Location location, Circle circle) {
        autodriveMarkers.put(location, circle);
    }
}
