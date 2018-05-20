package uk.ac.cam.interaction_design.group02.hiking_app.backend;

import java.io.Serializable;

/**
 * Mutable class representing a single hike
 */
public class Hike implements Serializable{
    private double longitude;
    private double latitude;
    /**
     * Unix time (seconds) of hike start
     */
    private long startTime;

    /**
     * Unix time (seconds) of hike end
     */
    private long endTime;

    private String name;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Hike(String name, double latitude, double longitude, long startTime, long endTime) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("Hike, name %s, at lat/lon (%s, %s)", name, latitude, longitude);
    }
}
