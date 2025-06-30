package nl.saxion.ptbc.missionLog;

import nl.saxion.ptbc.SaSaCommunicator;
import nl.saxion.ptbc.classes.Location;

import java.util.ArrayList;

/**
 * The {@code MissionStatus} class manages the state and logging of a mission.
 * It keeps track of the current location, mission destinations, and provides logging to the console
 * for mission progress, including destination updates and arrival confirmations.
 * <p>
 * All fields and methods are static, meaning this class is used as a utility to track mission status
 * without creating instances.
 */

public class MissionStatus {
    private static Location location;
    private static SaSaCommunicator sasa;

    public static void setCommunicator(SaSaCommunicator communicator) {
        MissionStatus.sasa = communicator;
    }

    private static ArrayList<Location> missions = new ArrayList<>();


    /**
     * Initializes a new mission by clearing the mission list and printing a start message.
     */
    public static void startMission() {
        missions.clear();
        String message = " Mission log\n==========\n 🚀Starting AutoDrive sequence...";
        System.err.println(message);
        sasa.send(message);
    }

    /**
     * Logs a destination by printing the destination number and location to the console.
     *
     * @param number the index or identifier for the destination
     * @param loc    the {@link Location} object representing the destination
     */
    public static void logDestination(int number, Location loc) {
        String message = "Destination " + number + " : " + loc;
        System.err.println(message);
        sasa.send(message);
    }

    /**
     * Logs the arrival at a given location. Updates the current location and,
     * if there are no remaining missions, triggers the finished mission message.
     *
     * @param loc the {@link Location} the mission has arrived at
     */
    public static void logArrived(Location loc) {
        location = loc;
        String message = "Status: Reached " + loc;
        System.err.println(message);
        sasa.send(message);

        if (missions.isEmpty()) {
            finishedMission();
        }
    }

    /**
     * Logs the successful completion of all mission destinations.
     */
    public static void finishedMission() {
        String message = "All destinations reached successfully✅";
        System.err.println("All destinations reached successfully✅");
        sasa.send(message);
    }

    public static Location getLocation() {
        return location;
    }

    public static void setMissions(ArrayList<Location> missions) {
        MissionStatus.missions = missions;
    }
}
