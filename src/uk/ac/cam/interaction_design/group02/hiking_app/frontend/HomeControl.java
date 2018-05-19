package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeControl extends GridPane {
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

    public HomeControl() throws IOException, ForecastException, APIException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HomeControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        //Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD
        this.getStylesheets().add("mainDesign.css");

        fxmlLoader.load();
    }
    public void refresh() throws APIException, ForecastException, IOException {
       AppSettings settings = AppSettings.getInstance();
        NaiveAPI api = NaiveAPI.getInstance();

        //Wipe out previous hikes (they might've changed)
        hikes.clear();

        ForecastWeatherPoint point = api.getWeatherForPoint(settings.getUserLatitude(), settings.getUserLongitude());
        WeatherData currentWeather = point.getForecastAtTime(System.currentTimeMillis()/1000);

        //Get current temperature and rainfall probability
        double temp = currentWeather.getAvgTemperatureCelsius();
        double rainProb = currentWeather.getPrecipitationProbability();

        //Displaying current temperature
        todayTemp.setText(Integer.toString((int) temp) + "°C");

        //Displaying current rainfall probability
        todayRainProb.setText(Integer.toString((int) (rainProb * 100)) + "%");

        //Displaying current ground conditions based on the current day's rainfall probability
        if(rainProb < 0.1)
            todayGroundCond.setText("dry");
        else if (rainProb < 0.25)
            todayGroundCond.setText("optimal");
        else
            todayGroundCond.setText("muddy");

        //Displaying today's date
        todayDate.setText(java.time.LocalDate.now().toString());

        /*Hike x = new Hike("Hike Trial", settings.getUserLatitude(), settings.getUserLongitude(), 1626748084, 1626748700);
        HikeControl y = new HikeControl(x);
        hikeContainer.getChildren().add(y);*/

        for(Hike h : settings.getHikes()) {
            HikeControl hike = new HikeControl(h);
            hikes.add(hike);
            hikeContainer.getChildren().add(hike);
        }
    }
}
