package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import java.io.IOException;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.Hike;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.ForecastWeatherPoint;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.NaiveAPI;
import uk.ac.cam.interaction_design.group02.hiking_app.backend.WeatherData;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;


import javafx.fxml.FXMLLoader;

public class DetailsControl implements Initializable {
	
	//Use the fxml fx:id tag as the name of the object
	// You can find these in the fxml file, it is normally the first attribute.
	// The name has to be exactly the same as the fx:id in the fxml file
	@FXML Label humidity;
	@FXML Label visibility;
	@FXML Label air_pressure;
	@FXML Label high;
	@FXML Label probability;
	@FXML Label intensity
	
	@FXML
    	public void initialize() {
		
		double latitude = hike.getLatitude();
		double longitude = hike.getLongitude();
		long startTime = hike.getStartTime();
		
		ForecastWeatherPoint weatherPoint = fetchWeatherUsingAPI(latitude,longitude);
		WeatherData data = weatherPoint.getForecastAtTime(startTime);
		
		humidity.setText(data.getHumidity());
		visibility.setText(data.getVisibility());
		air_pressure.setText(data.getPressure());
		high.setText(data.getHighTemperatureCelsius());
		probability.setText(data.getPrecipitationProbability());
		intensity.setText(data.getPrecipitationIntensity());

    	}
	
	public DetailsControl(Hike hike) throws IOException {
		
        	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DetailsControl.fxml"));
        	fxmlLoader.setRoot(this);
        	fxmlLoader.setController(this);
        	//Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD

        	fxmlLoader.load();
    	}
	
	
	
	
}
