package nl.saxion.ptbc.classes;

public class Obstacle {
    private double distance;
    private double Angle;
    private Location location;

    //Constructor
    public Obstacle(Location location) {
        this.location = location;
    }

    public Obstacle(double distance, double angle) {
        this.distance = distance;
        Angle = angle;
    }

    //Getters
    public Location getLocation() {
        return location;
    }

}