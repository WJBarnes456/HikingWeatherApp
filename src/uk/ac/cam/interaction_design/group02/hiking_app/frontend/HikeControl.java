package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;

import java.io.IOException;
import java.util.Date;

public class HikeControl extends Pane {
    @FXML
    private ImageView weatherIcon;

    @FXML
    private Label hikeDate;

    @FXML
    private Label maxTemp;

    @FXML
    private Label minTemp;

    @FXML
    private Label rainProb;

    @FXML
    private Label groundCond;

    @FXML
    private Label hikeName;

    public HikeControl(Hike hike) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HikeControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        //Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD

        fxmlLoader.load();

        NaiveAPI api = NaiveAPI.getInstance();

        double hikeLat = hike.getLatitude();
        double hikeLong = hike.getLongitude();

        try {
            ForecastWeatherPoint hikePoint = api.getWeatherForPoint(hikeLat, hikeLong);
            WeatherData forecast = hikePoint.getForecastAtTime(hike.getStartTime());

            String icon = forecast.getIcon();

            //Display hike weather icon
            Image image = new WritableImage(1,1);
            switch (icon) {
                case "clear-day": image = new Image(getClass().getResource(icon+".png").toExternalForm());
                case "clear-night": image = new Image(getClass().getResource(icon+".png").toExternalForm());
                case "rain": image = new Image(getClass().getResource(icon+".png").toExternalForm());
                case "snow": image = new Image(getClass().getResource(icon+".png").toExternalForm());
                case "wind": image = new Image(getClass().getResource(icon+".png").toExternalForm());
                case "fog": image = new Image(getClass().getResource(icon+".png").toExternalForm());
                case "cloudy": image = new Image(getClass().getResource(icon+".png").toExternalForm());
                case "partly-cloudy-day": image = new Image(getClass().getResource(icon+".png").toExternalForm());
                case "partly-cloudy-night": image = new Image(getClass().getResource(icon+".png").toExternalForm());
            }
            weatherIcon.setImage(image);

            double min = forecast.getLowTemperatureCelsius();
            double max = forecast.getHighTemperatureCelsius();
            double hikeRainProb = forecast.getPrecipitationProbability();
            Date date = new Date(hike.getStartTime()*1000);

            minTemp.setText(Integer.toString((int) min) + "°C");
            maxTemp.setText(Integer.toString((int) max) + "°C");

            if (hikeRainProb < 0.1)
                groundCond.setText("dry");
            else if (hikeRainProb < 0.25)
                groundCond.setText("optimal");
            else
                groundCond.setText("muddy");

            rainProb.setText(Double.toString(hikeRainProb));

            hikeDate.setText(date.toString());
        } catch (ForecastException e) {
            System.out.println("Can't get current forecast for hike");
            //Display message about the forecast not being available in Hike
            Alert For = new Alert(Alert.AlertType.WARNING);
            For.setTitle("Hike Control");
            For.setHeaderText("Forecast Exception");
            For.setContentText("The forecast is not available in Hike Control.");
            
        } catch (APIException e) {
           //Display message about the API not being available
            Alert AP = new Alert(Alert.AlertType.WARNING);
            AP.setTitle("Hike Control");
            AP.setHeaderText("API Exception");
            AP.setContentText("The API is not available in Hike Control.");
        }
    }
}
