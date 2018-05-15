package uk.ac.cam.interaction_design.group02.hiking_app.backend;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable class representing typical weather for a point
 */
public class TypicalWeatherPoint {
    private double longitude;
    private double latitude;

    private List<WeatherData> typicalDataPoints = new ArrayList<>();

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    /**
     * @param timeToGet UNIX Time for which we are getting the forecast
     * @return Closest forecast in time if within the period of any of the points
     * @throws ForecastException When there is no valid forecast for that time
     */
    public WeatherData getDataAboutTime(long timeToGet) throws ForecastException {
        WeatherData bestData = null;
        long bestDelta = Long.MAX_VALUE;

        // Find the closest valid forecast for this point
        for(WeatherData data : typicalDataPoints) {
            long timeOfForecast = data.getTimeForData();
            long timeDelta = Math.abs(timeToGet - timeOfForecast);
            if(timeDelta < data.getForecastType().getValidTime() && timeDelta < bestDelta) {
                bestDelta = timeDelta;
                bestData = data;
            }
        }

        // Ensure we never return a null bestData - this MUST be handled.
        if(bestData == null) {
            throw new ForecastException("Time lies outside forecast range");
        } else {
            return bestData;
        }
    }

    public void addDataPoint(WeatherData d) {
        typicalDataPoints.add(d);
    }

    public TypicalWeatherPoint(double latitude, double longitude, List<WeatherData> weatherData) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
