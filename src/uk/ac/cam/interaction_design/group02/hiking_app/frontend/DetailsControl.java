package uk.ac.cam.interaction_design.group02.hiking_app.frontend;

import java.io.IOException;

import uk.ac.cam.interaction_design.group02.hiking_app.backend.*;

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
	@FXML Label intensity;

	private Hike hike;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		double latitude = hike.getLatitude();
		double longitude = hike.getLongitude();
		long startTime = hike.getStartTime();

		ForecastWeatherPoint weatherPoint = null;
		try {
			weatherPoint = NaiveAPI.fetchWeatherUsingAPI(latitude,longitude);
		} catch (APIException e) {
			e.printStackTrace();
		}
		WeatherData data = null;
		try {
			data = weatherPoint.getForecastAtTime(startTime);
		} catch (ForecastException e) {
			e.printStackTrace();
		}

		humidity.setText(Double.toString(data.getHumidity()));
		visibility.setText(Double.toString(data.getVisibility()));
		air_pressure.setText(Double.toString(data.getPressure()));
		high.setText(Double.toString(data.getHighTemperatureCelsius()));
		probability.setText(Double.toString(data.getPrecipitationProbability()));
		intensity.setText(Double.toString(data.getPrecipitationIntensity()));

    	}
	
	public DetailsControl(Hike hike) throws IOException {
		
        	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DetailsControl.fxml"));
        	fxmlLoader.setRoot(this);
        	fxmlLoader.setController(this);
        	//Controller HAS to be set here, otherwise you get a self-cyclic instantiation => VERY BAD

        	fxmlLoader.load();
    	}

	}
