package uk.ac.cam.interaction_design.group02.hiking_app.backend;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class used so that the entire application has access to the user's settings and hikes
 */
public class AppSettings {
    private static final String SETTINGS_FILE_NAME = "hikingWeatherApp.settings";

    private static AppSettings ourInstance = new AppSettings();

    private List<Hike> hikes;

    private double userLatitude;
    private double userLongitude;

    public static AppSettings getInstance() {
        return ourInstance;
    }

    private AppSettings() {
        //Initialise the settings from a file, if it exists
        File f = new File(SETTINGS_FILE_NAME);
        if(f.exists()) {
            try {
                loadSettingsFromFile(f);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        else {
            loadDefaults();
        }
    }

    /**
     * @param f The file to load settings from
     * @throws IOException When the file doesn't exist
     */
    private void loadSettingsFromFile(File f) throws IOException{
        FileInputStream fis = new FileInputStream(f);

        try (ObjectInputStream ois = new ObjectInputStream(fis)) {
            userLatitude = (Double) ois.readObject();
            userLongitude = (Double) ois.readObject();
            int hikeCount = (Integer) ois.readObject();
            hikes = new ArrayList<>();
            for(int i = 0; i < hikeCount; i++) {
                hikes.add((Hike) ois.readObject());
            }
        } catch (Exception e) {
            // Malformed settings file, so just trash it and load defaults
            f.delete();
            loadDefaults();
        }
    }

    /**
     * Loads default user parameters, used as a fallback if the settings file is malformed or doesn't exist
     */
    private void loadDefaults() {
        // No hikes by default
        hikes = new ArrayList<>();

        // Default location is LT1 in the CL
        userLatitude = 52.2109371;
        userLongitude = 0.0916283;
    }

    /**
     * Dump the current settings to disk by serialising it all
     * @throws IOException When there's a problem dumping serialised objects to disk
     */
    public void saveToDisk() throws IOException {
        File f = new File(SETTINGS_FILE_NAME);
        f.createNewFile(); //creates a new file to store settings if one doesn't already exist
        FileOutputStream fos = new FileOutputStream(f);

        try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(userLatitude);
            oos.writeObject(userLongitude);
            oos.writeObject(hikes.size());
            for (Hike h : hikes) {
                oos.writeObject(h);
            }
        }
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(double userLatitude) {
        this.userLatitude = userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(double userLongitude) {
        this.userLongitude = userLongitude;
    }

    public List<Hike> getHikes() {
        return hikes;
    }

    public void addHike(Hike hike) {
        hikes.add(hike);
    }

    /**
     * Debug method that just adds a junk hike in a day's time at the current location
     */
    public void addTestHike() {
        Hike h = new Hike("Hike name",
                getUserLatitude(), getUserLongitude(),
                System.currentTimeMillis()/1000 + 60*60*24,
                System.currentTimeMillis()/1000 + 60*60*48);
        addHike(h);
    }
}
