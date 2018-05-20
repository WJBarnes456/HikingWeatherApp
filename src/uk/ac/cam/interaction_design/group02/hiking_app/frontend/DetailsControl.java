package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import java.io.IOException;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;


import javafx.fxml.FXMLLoader;

public class DetailsControl extends Pane {

    //Use the fxml fx:id tag as the name of the object
    // You can find these in the fxml file, it is normally the first attribute.
    // The name has to be exactly the same as the fx:id in the fxml file
    @FXML
    Label humidity;

    @FXML
    Label visibility;

    @FXML
    Label air_pressure;

    @FXML
    Label high;

    @FXML
    Label probability;

    @FXML
    Label intensity;

    Hike hike;


    @FXML
    public void initialize() throws APIException, ForecastException {
        IAPICache api = NaiveAPI.getInstance();

        double latitude = hike.getLatitude();
        double longitude = hike.getLongitude();
        long startTime = hike.getStartTime();

        ForecastWeatherPoint weatherPoint = api.getWeatherForPoint(latitude, longitude);
        WeatherData data = weatherPoint.getForecastAtTime(startTime);

        String humidityString = Double.toString(data.getHumidity());
        String visibilityString = Double.toString(data.getVisibility());
        String pressureString = Double.toString(data.getPressure());
        String highTempString = Double.toString(data.getHighTemperatureCelsius());
        String precipProbString = Double.toString(data.getPrecipitationProbability());
        String precipIntensityString = Double.toString(data.getPrecipitationIntensity());

        humidity.setText(humidityString);
        visibility.setText(visibilityString);
        air_pressure.setText(pressureString);
        high.setText(highTempString);
        probability.setText(precipProbString);
        intensity.setText(precipIntensityString);
    }

    public DetailsControl(Hike hike) throws IOException {
        this.hike = hike;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DetailsControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        //Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD

        fxmlLoader.load();
    }

    /**
     * Helper method used to easily show a dialog for a hike
     *
     * @param hike The hike to show details for
     */
    public static void showDetailsPopup(Hike hike) throws IOException {
        Stage detailsStage = new Stage();
        detailsStage.setScene(new Scene(new DetailsControl(hike)));
        detailsStage.showAndWait();
    }
}
