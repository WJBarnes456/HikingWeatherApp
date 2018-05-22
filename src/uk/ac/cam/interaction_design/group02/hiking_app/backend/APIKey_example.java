
package uk.ac.cam.interaction_design.group02.hiking_app.backend;

public class APIKey_example {
    private static final String DARKSKY_API_KEY = "YOUR_DARKSKY_API_KEY_HERE"; //Don't ever commit this.
    private static final String GOOGLE_MAPS_API_KEY = "YOUR_GOOGLE_MAPS_API_KEY_HERE"; //Don't ever commit this.

    public static String getDarkSkyKey() {
        return DARKSKY_API_KEY;
    }

    public static String getGoogleMapsKey() {
        return GOOGLE_MAPS_API_KEY;
    }
}