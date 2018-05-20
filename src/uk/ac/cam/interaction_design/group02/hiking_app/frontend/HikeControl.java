package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;

import java.io.IOException;
import java.util.Date;

public class HikeControl extends Pane {
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
            //TODO: Display some message about the forecast not being available
        } catch (APIException e) {
            System.out.println("Can't access API for hike");
            //TODO: Display some message about the API not being available
        }
    }
}
