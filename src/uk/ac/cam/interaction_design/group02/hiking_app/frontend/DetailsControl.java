package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import java.io.IOException;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


import javafx.fxml.FXMLLoader;

public class DetailsControl extends GridPane {

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

    @FXML
    LineChart<Number, Number> linechart;

    @FXML
    NumberAxis xAxis;

    Hike hike;

    @FXML
    public void initialize() throws APIException, ForecastException {
        IAPICache api = NaiveAPI.getInstance();

        double latitude = hike.getLatitude();
        double longitude = hike.getLongitude();
        long startTime = hike.getStartTime();
        long endTime = hike.getEndTime();

        // Get weather information
        ForecastWeatherPoint weatherPoint = api.getWeatherForPoint(latitude, longitude);
        WeatherData data = weatherPoint.getForecastAtTime(startTime);

        // Convert weather information to strings
        String humidityString = Math.round(data.getHumidity()*100) + "%";
        String visibilityString = Double.toString(data.getVisibility());
        String pressureString = Double.toString(data.getPressure());
        String highTempString = Double.toString(data.getHighTemperatureCelsius());
        String precipProbString = Math.round(data.getPrecipitationProbability()*100) + "%";
        String precipIntensityString = Double.toString(data.getPrecipitationIntensity());

        // Put strings onto the UI
        humidity.setText(humidityString);
        visibility.setText(visibilityString);
        air_pressure.setText(pressureString);
        high.setText(highTempString);
        probability.setText(precipProbString);
        intensity.setText(precipIntensityString);

        // Calculate chart values
        long length = endTime - startTime;

        XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
        series.setName("Temperature Series");
        List<Long> timestamps = List.of(startTime, startTime + length / 6, startTime + 2 * length / 6, startTime + 3 * length / 6,
                startTime + 4 * length / 6, startTime + 5 * length / 6, startTime + length);

        // Draw chart

        SimpleDateFormat formatter = new SimpleDateFormat("kk:mm");
        for (long time : timestamps) {
            Date dateTime = new Date(time * 1000);

            series.getData().add(new XYChart.Data<Number, Number>(time, weatherPoint.getForecastAtTime(time).getAvgTemperatureCelsius()));
        }
        xAxis.setLabel("Times");
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number n) {
                return formatter.format(new Date(n.longValue()*1000));
            }

            @Override
            public Number fromString(String s) {
                try {
                    return formatter.parse(s).getTime() / 1000;
                } catch (ParseException e) {
                    throw new RuntimeException("Failed to parse date");
                }
            }
        });

        linechart.getYAxis().setLabel("Temperatures");
        linechart.getData().addAll(series);

        System.out.println(linechart);
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
