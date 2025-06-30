package nl.saxion.ptbc.pilot;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import nl.saxion.ptbc.classes.RadarPoint;

import java.util.List;

/**
 * {@code RadarView} is a JavaFX UI component that visualizes radar data on a 2D grid.
 * <p>
 * It displays a central dot representing the frog and red dots for each detected radar point (obstacle),
 * relative to the frog's position and direction.
 */
public class RadarView extends Pane {
    private final double scale = 1.0;

    /**
     * Constructs a {@code RadarView} with a fixed preferred size of 300x300 pixels.
     */
    public RadarView() {
        this.setPrefSize(300, 300);
    }

    /**
     * Draws the radar view with the given radar points.
     * <p>
     * The center of the pane represents the frog. Obstacles are drawn as red circles,
     * with positions determined by their relative coordinates. Positive Z-values appear
     * below the center, and positive X-values appear to the right.
     *
     * @param points a list of {@link RadarPoint} objects representing detected obstacles
     */
    public void draw(List<RadarPoint> points) {
        getChildren().clear();

        double centerX = getPrefWidth() / 2;
        double centerZ = getPrefHeight() / 2;

        Circle frogDot = new Circle(centerX, centerZ, 5, Color.YELLOW);
        getChildren().add(frogDot);

        for (RadarPoint point : points) {

            double relX = point.getRelativeX();
            double relZ = point.getRelativeZ();

            double x = centerX + relX * scale;
            double z = centerZ - relZ * scale;

            Circle obstacle = new Circle(x, z, 3, Color.RED);
            getChildren().add(obstacle);
        }
    }
}
