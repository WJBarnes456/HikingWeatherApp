package uk.ac.cam.interaction_design.group02.hiking_app.backend;

public class APIKey {
    private static final String Key = "YOUR_SECRET_KEY_HERE"; //Don't ever commit this.
    
    public static String getKey() {
        return Key;
    }
}
