package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeControl extends GridPane {
    @FXML
    private AnchorPane weatherPane;

    @FXML
    private ImageView todayWeatherIcon;

    @FXML
    private Label todayTemp;

    @FXML
    private Label todayRainProb;

    @FXML
    private Label todayGroundCond;

    @FXML
    private Label todayDate;

    @FXML
    private VBox hikeContainer;

    @FXML
    private ImageView pin;

    private List<HikeControl> hikes = new ArrayList<>();

    public HomeControl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HomeControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        //Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD
        this.getStylesheets().add("mainDesign.css");

        fxmlLoader.load();
    }
    public void refresh() {
        AppSettings settings = AppSettings.getInstance();
        NaiveAPI api = NaiveAPI.getInstance();

        //Wipe out previous hikes (they might've changed)
        hikes.clear();
        hikeContainer.getChildren().clear();

        try {
            ForecastWeatherPoint point = api.getWeatherForPoint(settings.getUserLatitude(), settings.getUserLongitude());
            WeatherData currentWeather = point.getForecastAtTime(System.currentTimeMillis()/1000);


            //Get current temperature and rainfall probability
            double temp = currentWeather.getAvgTemperatureCelsius();
            double rainProb = currentWeather.getPrecipitationProbability();

            //Get current weather icon
            String icon = currentWeather.getIcon();

            //Display current weather icon
            Image bgImg = Utils.getBackgroundImage(icon);
            Image weatherIcon = Utils.getWeatherIcon(icon);

            BackgroundImage b = new BackgroundImage(bgImg,
                    BackgroundRepeat.REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT,
                    BackgroundSize.DEFAULT);
            weatherPane.setBackground(new Background(b));
            todayWeatherIcon.setImage(weatherIcon);

            //Display current temperature
            todayTemp.setText(Integer.toString((int) temp) + "°C");

            //Display current rainfall probability
            todayRainProb.setText(Integer.toString((int) (rainProb * 100)) + "%");

            //Display current ground conditions based on the current day's rainfall probability
            if(rainProb < 0.1)
                todayGroundCond.setText("dry");
            else if (rainProb < 0.25)
                todayGroundCond.setText("optimal");
            else
                todayGroundCond.setText("muddy");

            //Display today's date
            todayDate.setText(java.time.LocalDate.now().toString());

            for(Hike h : settings.getHikes()) {
                try {
                    HikeControl hike = new HikeControl(h);
                    hikes.add(hike);
                    hikeContainer.getChildren().add(hike);
                } catch (IOException e) {
                    System.err.println("Failed to create hike dialog");
                }
            }
        } catch (ForecastException e) {
            System.out.println("Can't get current forecast for home");
            // Display message about the forecast not being available in Home
            Alert For = new Alert(Alert.AlertType.WARNING);
            For.setTitle("Home Control");
            For.setHeaderText("Forecast Exception");
            For.setContentText("The forecast is not available in Home Control.");
        } catch (APIException e) {
            System.out.println("Can't access API for home");
            //Display message about the API not being available in home
            Alert AP = new Alert(Alert.AlertType.WARNING);
            AP.setTitle("Home Control");
            AP.setHeaderText("API Exception");
            AP.setContentText("The API is not available in Home Control.");
        }
    }
}
