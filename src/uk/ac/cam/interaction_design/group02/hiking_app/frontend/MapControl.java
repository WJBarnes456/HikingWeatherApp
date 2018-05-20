package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import netscape.javascript.JSObject;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MapControl extends BorderPane implements MapComponentInitializedListener {
    private GoogleMapView mapView;

    private GoogleMap map;

    private List<Marker> hikeMarkers = new ArrayList<>();

    private InfoWindow clickInfoWindow;
    private Marker clickMarker;
    private LatLong myPosition;
    private int day;

    private boolean initialised = false;


    @FXML
    private Button todayButton;
    private StringProperty todayString = new SimpleStringProperty();

    @FXML
    private Button tomorrowButton;
    private StringProperty tomorrowString = new SimpleStringProperty();

    @FXML
    private Button twoDaysButton;
    private StringProperty twoDaysString = new SimpleStringProperty();

    @FXML
    private Button threeDaysButton;
    private StringProperty threeDaysString = new SimpleStringProperty();

    @FXML
    private Button fourDaysButton;
    private StringProperty fourDaysString = new SimpleStringProperty();

    @FXML
    private Button laterButton;


    private void markSet(Button b) {
        b.getStyleClass().add("set");
    }

    private void markUnset(Button b) {
        b.getStyleClass().remove("set");
    }

    private void markAllUnset() {
        markUnset(todayButton);
        markUnset(tomorrowButton);
        markUnset(twoDaysButton);
        markUnset(threeDaysButton);
        markUnset(fourDaysButton);
        markUnset(laterButton);
    }

    @FXML
    private void handled0Button(ActionEvent e) {
        markAllUnset();
        markSet(todayButton);
        day = 0;
    }

    @FXML
    private void handled1Button(ActionEvent e) {
        markAllUnset();
        markSet(tomorrowButton);
        day = 1;
    }

    @FXML
    private void handled2Button(ActionEvent e) {
        markAllUnset();
        markSet(twoDaysButton);
        day = 2;
    }

    @FXML
    private void handled3Button(ActionEvent e) {
        markAllUnset();
        markSet(threeDaysButton);
        day = 3;
    }

    @FXML
    private void handled4Button(ActionEvent e) {
        markAllUnset();
        markSet(fourDaysButton);
        day = 4;
    }

    @FXML
    private void handleaddhike(ActionEvent e) {
        AppSettings settings = AppSettings.getInstance();
        Hike hike = new Hike("gril z ziomeczkami",
                            myPosition.getLatitude(),
                            myPosition.getLongitude(),
                            System.currentTimeMillis() / 1000+86400*day,
                            System.currentTimeMillis() / 1000+86400*day+7200);
        settings.addHike(hike);
    }

    @FXML
    public void initialize() {
        mapView = new GoogleMapView(null, APIKey.getGoogleMapsKey());
        mapView.addMapInializedListener(this);
        day = 0;
        AppSettings settings = AppSettings.getInstance();
        myPosition = new LatLong(   settings.getUserLatitude(),
                                    settings.getUserLongitude());
        initializeButtons();
    }


    public MapControl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MapControl.fxml"));
        fxmlLoader.setRoot(this); //Required since this is loading a component
        fxmlLoader.setController(this); //Commented out while we're temporarily delegating to ext. controller
        //Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD
        fxmlLoader.load();
    }

    @Override
    public void mapInitialized() {
        AppSettings settings = AppSettings.getInstance();

        this.setCenter(mapView);

        //Set the initial properties of the map.
        MapOptions mapOptions = new MapOptions();

        mapOptions.center(new LatLong(settings.getUserLatitude(), settings.getUserLongitude()))
                .mapType(MapTypeIdEnum.TERRAIN)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .zoom(8);

        map = mapView.createMap(mapOptions);

        map.addMouseEventHandler(UIEventType.click, this::handleMapClick);

        initialised = true;
        refresh();
    }

    private void handleMapClick(GMapMouseEvent event) {
        cleanMarker();

        // Now handle the actual click event
        LatLong latLong = event.getLatLong();

        InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
        try {
            NaiveAPI api = NaiveAPI.getInstance();
            ForecastWeatherPoint point = api.getWeatherForPoint(latLong.getLatitude(), latLong.getLongitude());
            WeatherData currentWeather = point.getForecastAtTime(System.currentTimeMillis() / 1000+86400*day);
            //Get current temperature and rainfall probability
            double temp = currentWeather.getAvgTemperatureCelsius();
            double rainProb = currentWeather.getPrecipitationProbability();
            String temptext = "Temperature: " + Integer.toString((int) temp) + "°C";
            String raintext = "Rain:" + Integer.toString((int) (rainProb * 100)) + "%" ;
            String groutext = "Ground Conditions:";
            if(rainProb < 0.1)
                groutext+="dry";
            else if (rainProb < 0.25)
                groutext+="optimal";
            else
                groutext+="muddy";
            infoWindowOptions.content("<h2>Location clicked</h2> " +
                    temptext + "<br>" +
                    raintext + "<br>" +
                    groutext + "<br>" );
        } catch(ForecastException e) {
            infoWindowOptions.content("<h2>Location clicked</h2> " +
                    "Lat: " + latLong.getLatitude() + "<br>" +
                    "Long: " + latLong.getLongitude());
        }
        catch(APIException e) {
            infoWindowOptions.content("<h2>Location clicked</h2> " +
                    "Lat: " + latLong.getLatitude() + "<br>" +
                    "Long: " + latLong.getLongitude());
        }
        // TODO: Implement hike addition, getting weather for this clicked point

        MarkerOptions clickMarkerOptions = new MarkerOptions();
        clickMarkerOptions.position(latLong);

        clickMarker = new Marker(clickMarkerOptions);
        map.addMarker(clickMarker);

        clickInfoWindow = new InfoWindow(infoWindowOptions);

        clickInfoWindow.open(map, clickMarker);
    }


    /**
     * Method used to show information about a hike when clicked on
     * @param hikeMarker The marker of the hike to show information on
     * @param hike The raw hike object to show information on
     */
    private void handleHikeMarkerClick(Marker hikeMarker, Hike hike) {
        cleanMarker();

        try {
            DetailsControl.showDetailsPopup(hike);
        }
        catch (IOException e) {
            // Fallback if DetailsControl throws an IOException and can't load
            InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
            infoWindowOptions.content("<h2>"+hike.getName()+"</h2>");

            clickInfoWindow = new InfoWindow(infoWindowOptions);
            clickInfoWindow.open(map, hikeMarker);
        }
    }

    /**
     * Helper method used to remove the clickmarker and clickinfowindow when handling events
     */
    private void cleanMarker() {
        // Close the window if one already exists
        if(clickInfoWindow != null) {
            clickInfoWindow.close();
            clickInfoWindow = null;
        }

        // Get rid of the temporary click marker as well if we don't need it
        if(clickMarker != null) {
            map.removeMarker(clickMarker);
            clickMarker = null;
        }
    }

    public void refresh() {
        if(initialised) {
            AppSettings settings = AppSettings.getInstance();

            map.removeMarkers(hikeMarkers);

            //Add hike markers to the map
            hikeMarkers.clear();
            for (Hike h : settings.getHikes()) {
                LatLong hikeLocation = new LatLong(h.getLatitude(), h.getLongitude());

                //Cache the options - we need them for detecting whether someone has clicked on a hike later.
                MarkerOptions hikeOptions = new MarkerOptions();
                hikeOptions.position(hikeLocation);
                hikeOptions.title(h.getName());

                Marker hikeMarker = new Marker(hikeOptions);

                map.addUIEventHandler(hikeMarker, UIEventType.click, (JSObject j) -> {
                    handleHikeMarkerClick(hikeMarker, h);
                });

                hikeMarkers.add(hikeMarker);

                map.addMarker(hikeMarker);
            }
        }
    }

    private void initializeButtons() {
        // stores the dates for five consecutive days, starting from the current date, in the format MMM dd E
        String[][] dateParameters = new String[5][3];

        // initializes the calendar with the current date
        Calendar calendar = Calendar.getInstance();
        String[] date = calendar.getTime().toString().split(" "); //Date = [Day of Week, Month, Day]
        dateParameters[0][0] = date[0];
        dateParameters[0][1] = date[2];
        dateParameters[0][2] = date[1];

        // adds the following dates from 1 day later to 4 days later (inclusive)
        for (int i = 1; i <= 4; i++) {
            calendar.add(Calendar.DATE, 1);
            date = calendar.getTime().toString().split(" ");
            dateParameters[i][0] = date[0]; //
            dateParameters[i][1] = date[2]; //
            dateParameters[i][2] = date[1]; //
        }

        // initializes the five buttons to display the five dates
        todayString.setValue(String.format("%s %s %s", dateParameters[0][0], dateParameters[0][1], dateParameters[0][2]));

        tomorrowString.setValue(String.format("%s %s %s", dateParameters[1][0], dateParameters[1][1], dateParameters[1][2]));

        twoDaysString.setValue(String.format("%s %s %s", dateParameters[2][0], dateParameters[2][1], dateParameters[2][2]));

        threeDaysString.setValue(String.format("%s %s %s", dateParameters[3][0], dateParameters[3][1], dateParameters[3][2]));

        fourDaysString.setValue(String.format("%s %s %s", dateParameters[4][0], dateParameters[4][1], dateParameters[4][2]));
    }

    public String getTodayString() {
        return todayString.get();
    }

    public StringProperty todayStringProperty() {
        return todayString;
    }

    public String getTomorrowString() {
        return tomorrowString.get();
    }

    public StringProperty tomorrowStringProperty() {
        return tomorrowString;
    }

    public String getTwoDaysString() {
        return twoDaysString.get();
    }

    public StringProperty twoDaysStringProperty() {
        return twoDaysString;
    }

    public String getThreeDaysString() {
        return threeDaysString.get();
    }

    public StringProperty threeDaysStringProperty() {
        return threeDaysString;
    }

    public String getFourDaysString() {
        return fourDaysString.get();
    }

    public StringProperty fourDaysStringProperty() {
        return fourDaysString;
    }
}