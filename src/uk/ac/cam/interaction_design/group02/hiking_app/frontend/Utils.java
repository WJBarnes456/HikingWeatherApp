package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class Utils {
    public static Image getWeatherIcon(String weatherType) {
        Image image = new WritableImage(1,1);
        switch (weatherType) {
            case "clear-day": image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
            case "clear-night": image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
            case "rain": image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
            case "snow": image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
            case "wind": image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
            case "fog": image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
            case "cloudy": image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
            case "partly-cloudy-day": image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
            case "partly-cloudy-night": image = new Image(Utils.class.getResource(weatherType+".png").toExternalForm());
        }
        return image;
    }

    public static Image getBackgroundImage(String weatherType) {
        Image image = new WritableImage(1,1);
        switch (weatherType) {
            case "clear-day": image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
            case "clear-night": image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
            case "rain": image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
            case "snow": image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
            case "wind": image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
            case "fog": image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
            case "cloudy": image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
            case "partly-cloudy-day": image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
            case "partly-cloudy-night": image = new Image(Utils.class.getResource(weatherType+".jpg").toExternalForm());
        }
        return image;
    }
}
