package nl.saxion.ptbc.missionLog;

public class MissionLog {
    private int id;
    private String timeStamp;
    private String mission;

    public MissionLog(int id, String timeStamp, String mission) {
        this.id = id;
        this.mission = mission;
        this.timeStamp = timeStamp;
    }

    public String getMission() {
        return mission;
    }

    public int getId() {
        return id;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}