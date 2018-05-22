package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class Utils {
    public static Image getWeatherIcon(String weatherType) {
        Image image = new WritableImage(1,1);
        switch (weatherType) {
            case "clear-day":
            case "clear-night":
            case "rain":
            case "snow":
            case "wind":
            case "fog":
            case "cloudy":
            case "partly-cloudy-day":
            case "partly-cloudy-night":
            case "sleet":
                image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
                break;
        }
        return image;
    }

    public static Image getBackgroundImage(String weatherType) {
        Image image = new WritableImage(1,1);
        switch (weatherType) {
            case "clear-day":
            case "clear-night":
            case "rain":
            case "snow":
            case "wind":
            case "fog":
            case "cloudy":
            case "partly-cloudy-day":
            case "partly-cloudy-night":
            case "sleet":
                image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
                break;
        }
        return image;
    }
}
