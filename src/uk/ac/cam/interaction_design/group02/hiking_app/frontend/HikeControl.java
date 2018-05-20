package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;

import java.io.IOException;
import java.util.Date;

public class HikeControl extends AnchorPane {
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

            //Display hike weather icon
            Image icon = Utils.getWeatherIcon(forecast.getIcon());
            weatherIcon.setImage(icon);

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

            rainProb.setText(Math.round(hikeRainProb*100) + "%");

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
