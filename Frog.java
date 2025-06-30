package nl.saxion.ptbc.classes;

import javafx.application.Platform;
import nl.saxion.ptbc.SaSaCommunicator;
import nl.saxion.ptbc.missionLog.MissionStatus;

import java.util.ArrayList;
import java.util.LinkedList;

public class Frog {
    private double x, y, z, angle, energy, solar, radarX, radarZ, radarAngle;
    private boolean arrived, activateMissionAutoDrive = false;
    private static final double arrivalDistance = 3.0;
    private SaSaCommunicator sasa;
    private static ArrayList<Location> autoDriveMissions = new ArrayList<>();

    public Frog() {
    }

    public Frog(double x, double y, double z, double angle, double energy, double solar) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
        this.energy = energy;
        this.solar = solar;
    }

    public void addCommunicatorToFrog(SaSaCommunicator saSaCommunicator) {
        this.sasa = saSaCommunicator;
    }

    public void update(double x, double y, double z, double angle, double energy, double solar) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
        this.energy = energy;
        this.solar = solar;
    }

    public void updateRadar(double x, double z, double angle) {
        this.radarX = x;
        this.radarZ = z;
        this.radarAngle = angle;
    }
    //Getters


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getRadarX() {
        return radarX;
    }

    public double getRadarZ() {
        return radarZ;
    }

    public double getRadarAngle() {
        return radarAngle;
    }

    //Getters
    public static ArrayList<Location> getAutoDriveMissionLog() {
        return autoDriveMissions;
    }

    public void autoDrive(LinkedList<Location> destinations, ArrayList<Obstacle> notConvertedObstacles) {
        //for autoDrive missionLog (Elham)
        arrived = false;
        //when auto drive reached destination -> arrived = true
        new Thread(() -> {
            int index = 1;

            System.out.println("🚀 Starting autoDrive with " + destinations.size() + " destinations");

            while (!destinations.isEmpty()) {
                Location destination = destinations.getFirst();

                int currentIndex = index++;
                Platform.runLater(() -> MissionStatus.logDestination(currentIndex, destination));

                while (true) {
                    if (!activateMissionAutoDrive) {
                        if (!Map.getAutodriveMarkers().containsKey(destination)) {
                            System.out.println("⛔ Destination was manually removed: " + destination);
                            destinations.remove(destination);
                            break;
                        }
                    }

                    double dx = destination.getX() - x;
                    double dz = destination.getZ() - z;
                    double distance = Math.sqrt(dx * dx + dz * dz);

                    if (distance < arrivalDistance) {
                        Location reached = destinations.removeFirst();
                        Map.getAutodriveMarkers().remove(reached);
                        Platform.runLater(Map::drawAutoDriveMap);
                        Platform.runLater(() -> MissionStatus.logArrived(reached));
                        sasa.send("DRIVE 0 0 0");
                        arrived = true;
                        break;
                    }

                    double head = 0;
                    ArrayList<Obstacle> snapshot = new ArrayList<>(notConvertedObstacles);

                    boolean nearObstacle = scan(snapshot);
                    double targetAngle = angleToPoint(destination);
                    double angleDiff = normalizeAngle(targetAngle - angle);

                    if (!nearObstacle) {
                        if (angleDiff >= 20) head = 5;
                        else if (angleDiff <= -20) head = -5;
                    }

                    head = scanObstaclesAhead(head, snapshot);
                    String command = String.format("DRIVE 0.4 %.2f 1.0", head);
                    sasa.send(command);
                    System.out.println("AutoDrive: " + command);

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        System.out.println("AutoDrive thread interrupted.");
                        return;
                    }
                }
            }

            Platform.runLater(MissionStatus::finishedMission);
            activateMissionAutoDrive = false;
            sasa.send("DRIVE 0 0 0");

        }).start();
    }

    /**
     * Executes the auto-drive operation for a sequence of mission locations.
     * The method runs on a separate thread, processes each mission sequentially,
     * and logs the progress of the mission in real time.
     *
     * <p>Each location is fetched from the mission log and passed to the auto-drive system.
     * The frog's progress is monitored until it reaches the destination, at which point
     * the location is removed from the mission log.</p>
     */
    public void autoDriveMissionLog(ArrayList<Obstacle> notConvertedObstacles) {
        LinkedList<Location> missionQueue = new LinkedList<>(autoDriveMissions);
        MissionStatus.startMission();
        MissionStatus.setMissions(new ArrayList<>(autoDriveMissions));
        autoDrive(missionQueue, notConvertedObstacles);
        activateMissionAutoDrive = true;
    }

    public double scanObstaclesAhead(double angle, ArrayList<Obstacle> snapshot) {

        final double safeDistance = 10.0;
        final double angleStep = 5.0;
        final double angleLimit = 30.0;
        final double coneWidth = 10.0;

        // checking if the current angle is safe
        if (!isObstacleInPath(angle, snapshot, safeDistance, coneWidth)) {
            return angle;
        }

        // Trying to find a safe angle
        for (double prediction = angleStep; prediction <= angleLimit; prediction += angleStep) {
            double leftAngle = normalizeAngle(angle - prediction);
            double rightAngle = normalizeAngle(angle + prediction);

            if (!isObstacleInPath(leftAngle, snapshot, safeDistance, coneWidth)) {
                return leftAngle;
            }

            if (!isObstacleInPath(rightAngle, snapshot, safeDistance, coneWidth)) {
                return rightAngle;
            }
        }

        //if no safer angle found, get ready for a crash lol
        return angle;


    }

    private boolean isObstacleInPath(double angle, ArrayList<Obstacle> obstacles, double safeDistance, double coneWidth) {
        for (Obstacle o : obstacles) {
            double x = o.getLocation().getX();
            double z = o.getLocation().getZ();
            double distance = Math.sqrt(x * x + z * z);

            if (distance > safeDistance) continue;

            double obstacleAngle = normalizeAngle(Math.toDegrees(Math.atan2(z, x)));
            double diff = normalizeAngle(obstacleAngle - angle - 90);

            if (Math.abs(diff) <= coneWidth) {
                System.err.println("🔴 Obstacle detected at " + distance + " meters, angle: " + diff);
                return true;
            }
        }
        return false;
    }


    public boolean scan(ArrayList<Obstacle> snapshot) {
        for (Obstacle o : snapshot) {
            double oXDistance = o.getLocation().getX();
            double oZDistance = o.getLocation().getZ();
            double oDistance = Math.sqrt(oXDistance * oXDistance + oZDistance * oZDistance);

            if (oDistance < 10) {
                return true;
            }
        }
        return false;
    }

    public double angleToPoint(Location destination) {

        double dx = destination.getX() - x;
        double dz = destination.getZ() - z;

        double angle = Math.toDegrees(Math.atan2(dx, dz));
        return normalizeAngle(angle);
    }

    public double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}
