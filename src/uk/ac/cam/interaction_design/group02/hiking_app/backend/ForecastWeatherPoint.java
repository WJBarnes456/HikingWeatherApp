package uk.ac.cam.interaction_design.group02.hiking_app.backend;

import java.util.List;

/**
 * Immutable class representing weather data about a single point
 */
public class ForecastWeatherPoint {
    private double longitude;
    private double latitude;

    private List<WeatherData> forecast;

    private long timeForecastGenerated;

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public long getTimeForecastGenerated() {
        return timeForecastGenerated;
    }

    /**
     * @param timeToGet UNIX Time for which we are getting the forecast
     * @return Closest forecast in time if within the period of any of the points
     * @throws ForecastException When there is no valid forecast for that time
     */
    public WeatherData getForecastAtTime(long timeToGet) throws ForecastException {
        WeatherData bestData = null;
        long bestDelta = Long.MAX_VALUE;

        // Find the closest valid forecast for this point
        for(WeatherData data : forecast) {
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

    /**
     * @param timeToGet The UNIX time the forecast is for
     * @param type Specified type of data (eg. forces daily data)
     * @return Closest forecast in time if within the period for any of the points
     * @throws ForecastException
     */
    public WeatherData getForecastOfTypeAtTime(long timeToGet, ForecastType type) throws ForecastException {
        WeatherData bestData = null;
        long bestDelta = Long.MAX_VALUE;

        // Find the closest valid forecast for this point
        for(WeatherData data : forecast) {
            ForecastType forecastType = data.getForecastType();
            long timeOfForecast = data.getTimeForData();
            long timeDelta = Math.abs(timeToGet - timeOfForecast);
            if(forecastType == type && timeDelta < type.getValidTime() && timeDelta < bestDelta) {
                bestDelta = timeDelta;
                bestData = data;
            }
        }

        // Ensure we never return a null bestData - this MUST be handled.
        if(bestData == null) {
            throw new ForecastException("No valid forecast of that type");
        } else {
            return bestData;
        }
    }

    public ForecastWeatherPoint(double latitude, double longitude, long timeForecastGenerated, List<WeatherData> weatherData) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeForecastGenerated = timeForecastGenerated;
        this.forecast = weatherData;
    }
}
