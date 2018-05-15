package uk.ac.cam.interaction_design.group02.hiking_app.backend;

public enum PrecipitationType {
    RAIN,
    SNOW,
    SLEET,
    NONE;

    public static PrecipitationType typeFromString(String type) {
        switch(type) {
            case "rain":
                return RAIN;
            case "snow":
                return SNOW;
            case "sleet":
                return SLEET;
            default:
                return NONE;
        }
    }
}
