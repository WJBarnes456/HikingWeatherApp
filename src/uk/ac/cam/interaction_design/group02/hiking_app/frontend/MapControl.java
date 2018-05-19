package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import netscape.javascript.JSObject;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.APIKey_example;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.AppSettings;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.Hike;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapControl extends BorderPane implements MapComponentInitializedListener {
    private GoogleMapView mapView;

    private GoogleMap map;

    private List<Marker> hikeMarkers = new ArrayList<>();

    private InfoWindow clickInfoWindow;
    private Marker clickMarker;

    private boolean initialised = false;

    // All of the buttons we need for changing dates and that
    // TODO: Implement date changing

    @FXML
    private Button todayButton;

    @FXML
    private Button tomorrowButton;

    @FXML
    private Button twoDaysButton;

    @FXML
    private Button threeDaysButton;

    @FXML
    private Button fourDaysButton;

    @FXML
    private Button laterButton;


    private void markSet(Button b) {
        b.setStyle("-fx-background-color: #246249;" +
                "-fx-text-fill: #78a895");
    }

    private void markUnset(Button b) {
        b.setStyle("-fx-background-color: #286d51;" +
                "-fx-text-fill: #c5d9d1");
    }

    private void markAllUnset() {
        markUnset(todayButton);
        markUnset(tomorrowButton);
        markUnset(twoDaysButton);
        markUnset(threeDaysButton);
        markUnset(fourDaysButton);
    }

    @FXML
    public void initialize() {
        mapView = new GoogleMapView(null, APIKey_example.getGoogleMapsKey());
        mapView.addMapInializedListener(this);
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
        infoWindowOptions.content("<h2>Location clicked</h2> " +
                        "Lat: " + latLong.getLatitude() + "<br>" +
                "Long: " + latLong.getLongitude());
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
    private void handleMarkerClick(Marker hikeMarker, Hike hike) {
        cleanMarker();
        InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
        infoWindowOptions.content("<h2>"+hike.getName()+"</h2>");
        // TODO: Show weather about a location when the hike is clicked

        clickInfoWindow = new InfoWindow(infoWindowOptions);
        clickInfoWindow.open(map, hikeMarker);
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
                    handleMarkerClick(hikeMarker, h);
                });

                hikeMarkers.add(hikeMarker);

                map.addMarker(hikeMarker);
            }
        }
    }
}
