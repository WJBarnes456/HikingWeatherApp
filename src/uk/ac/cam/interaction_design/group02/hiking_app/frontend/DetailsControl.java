package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import java.io.IOException;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
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
    Label temperature;

    @FXML
    Label probability;

    @FXML
    Label intensity;

    @FXML
    LineChart<Number, Number> linechart;

    @FXML
    Label hikeName;

    @FXML
    NumberAxis xAxis;

    Hike hike;

    @FXML
    public void initialize() throws APIException {
        IAPICache api = NaiveAPI.getInstance();

        double latitude = hike.getLatitude();
        double longitude = hike.getLongitude();
        long startTime = hike.getStartTime();
        long endTime = hike.getEndTime();

        hikeName.setText(hike.getName());

        // Get weather information
        ForecastWeatherPoint weatherPoint = api.getWeatherForPoint(latitude, longitude);

        try {
            WeatherData data = weatherPoint.getForecastAtTime(startTime);

            setWeatherLabels(data);

            // Calculate chart values
            long length = endTime - startTime;

            XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
            series.setName("Temperature Series");
            List<Long> timestamps = List.of(startTime, startTime + length / 6, startTime + 2 * length / 6, startTime + 3 * length / 6,
                    startTime + 4 * length / 6, startTime + 5 * length / 6, startTime + length);
            // Draw chart

            SimpleDateFormat formatter = new SimpleDateFormat("kk:mm");
            for (long time : timestamps) {
                try {
                    WeatherData forecast = weatherPoint.getForecastAtTime(time);

                    //Initialise the data points that we can actually access between values
                    XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(forecast.getTimeForData(), forecast.getAvgTemperatureCelsius());


                    // Add the data
                    series.getData().add(dataPoint);
                } catch (ForecastException fe) {
                    // Truncate the chart if we lose forecasts towards the end
                    System.err.println("Chart tried to fetch non-existent forecast");
                }
            }

            // Set up x Axis
            xAxis.setLabel("Times");
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(startTime);
            xAxis.setUpperBound(endTime);
            xAxis.setTickUnit(length / 6);

            xAxis.setTickLabelFormatter(new StringConverter<Number>() {
                @Override
                public String toString(Number n) {
                    return formatter.format(new Date(n.longValue() * 1000));
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

            // Set up Y axis
            linechart.getYAxis().setLabel("Temperatures");

            linechart.getData().addAll(series);

            // Dodgy hack - present the actual points from showing up (the user doesn't need to know)
            // From this StackOverflow answer https://stackoverflow.com/a/39674496
            for(XYChart.Data<Number, Number> dataPoint : series.getData()) {
                StackPane stackPane = (StackPane) dataPoint.getNode();
                stackPane.setVisible(false);
            }
        } catch (ForecastException fe) {
            // The hike is too far in advance to even get daily values. We should show typicals instead.
            WeatherData data = api.getTypicalWeatherForPoint(longitude, latitude, startTime);

            //Let the user know that we're showing typical data
            hikeName.setText(hike.getName() + " (Typical Data)");
            setWeatherLabels(data);

        }
    }

    private void setWeatherLabels(WeatherData data) {
        // Convert weather information to strings
        String humidityString = Math.round(data.getHumidity() * 100) + "%";
        String visibilityString = Double.toString(data.getVisibility());
        String pressureString = Double.toString(data.getPressure());
        String tempString = Math.round(data.getAvgTemperatureCelsius()) + "°C";
        String precipProbString = Math.round(data.getPrecipitationProbability() * 100) + "%";
        String precipIntensityString = Double.toString(data.getPrecipitationIntensity());

        // Put strings onto the UI
        humidity.setText(humidityString);
        visibility.setText(visibilityString);
        air_pressure.setText(pressureString);
        temperature.setText(tempString);
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
