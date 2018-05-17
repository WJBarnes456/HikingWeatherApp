package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;

import java.io.IOException;
import java.util.List;

public class HomeControl extends GridPane {
    @FXML
    private Text currentWeatherText;

    @FXML
    private VBox hikeContainer;

    private List<HikeControl> hikes;

    public HomeControl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HomeControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        //Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD

        fxmlLoader.load();

        //add here
    }
/*
    public void refresh() {
       AppSettings settings = AppSettings.getInstance();
        NaiveAPI api = NaiveAPI.getInstance();

        List<Node> hikeControls = hikeContainer.getChildren();

        hikeControls.clear(); //Wipe out previous hikes (they might've changed)

        try {
            ForecastWeatherPoint point = api.getWeatherForPoint(settings.getUserLatitude(), settings.getUserLongitude());
            WeatherData currentWeather = point.getForecastAtTime(System.currentTimeMillis()/1000);
            double temp = currentWeather.getAvgTemperatureCelsius();
            double humidity = currentWeather.getHumidity();
            currentWeatherText.setText(String.format("Current weather: %s degrees Celsius, humidity %s", temp, humidity));
        } catch (APIException e) {
            currentWeatherText.setText("API Call failed.");
        } catch (ForecastException e) {
            currentWeatherText.setText("Current weather not included in forecast");
        }

        for(Hike h : settings.getHikes()) {
            String weatherString;
            try {
                double hikeLat = h.getLatitude();
                double hikeLong = h.getLongitude();
                long hikeStart = h.getEndTime();

                ForecastWeatherPoint point = api.getWeatherForPoint(hikeLat, hikeLong);
                WeatherData forecast = point.getForecastAtTime(h.getStartTime());
                double temp = forecast.getAvgTemperatureFahrenheit();
                double humidity = forecast.getHumidity();

                weatherString = String.format("%s degrees Fahrenheit, humidity %s", temp, humidity);
            } catch (APIException e) {
                weatherString = "API call failed";
            } catch (ForecastException e) {
                weatherString = "Too far out, try again later";
            }
            hikeControls.add(new Text(h.getName() + ": " + weatherString));
        }
    }
*/
}
