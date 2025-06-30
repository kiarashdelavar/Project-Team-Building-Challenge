package nl.saxion.ptbc.pilot;

import nl.saxion.ptbc.classes.RadarPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code RadarSystem} class manages radar blips (obstacle detections) relative to a frog's position.
 * <p>
 * It stores relative positions of detected obstacles and provides functionality to filter
 * out points that are beyond a maximum detection range. This class is thread-safe to allow
 * concurrent modifications and reads, e.g., from a game loop and UI thread simultaneously.
 */
public class RadarSystem {
    private final List<RadarPoint> radarPoints = Collections.synchronizedList(new ArrayList<>());
    private final double maxDistance = 200.0;


    /**
     * Adds a new radar blip with coordinates relative to the frog.
     *
     * @param relativeX the X coordinate of the obstacle relative to the frog
     * @param relativeZ the Z coordinate of the obstacle relative to the frog
     */
    public void addRadarBlip(double relativeX, double relativeZ) {
        radarPoints.add(new RadarPoint(relativeX, relativeZ));
    }

    /**
     * Returns a list of radar points that are within the maximum detection range.
     * <p>
     * This method is thread-safe and returns a snapshot of the current radar points.
     *
     * @return a list of {@code RadarPoint}s within range
     */
    public List<RadarPoint> getFilteredPoints() {
        List<RadarPoint> result = new ArrayList<>();
        synchronized (radarPoints) {
            for (RadarPoint point : radarPoints) {
                double distance = Math.sqrt(point.getRelativeX() * point.getRelativeX() + point.getRelativeZ() * point.getRelativeZ());
                if (distance <= maxDistance) {
                    result.add(point);
                }
            }
        }
        return result;
    }

    // Clears all stored radar points
    public void clear() {
        radarPoints.clear();
    }

}
