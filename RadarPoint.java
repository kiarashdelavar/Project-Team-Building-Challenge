package nl.saxion.ptbc.classes;

public class RadarPoint {
    private final double relativeX;
    private final double relativeZ;

    public RadarPoint(double relativeX, double relativeZ) {
        this.relativeX = relativeX;
        this.relativeZ = relativeZ;
    }

    public double getRelativeX() {
        return relativeX;
    }

    public double getRelativeZ() {
        return relativeZ;
    }
}
