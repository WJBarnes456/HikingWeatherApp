package uk.ac.cam.interaction_design.group02.hiking_app.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

/**
 * Naive implementation of the weather API using O(n) cache searching (would scale better with k-d tree)
 * Implemented using Singleton pattern so it can be accessed from any module
 * TODO: Implement API calls
 */
public class NaiveAPI implements IAPICache {
    /**
     * long stating the Unix time for which a data point should be valid (1 hour)
     */
    private static final long DATA_VALIDITY = 60*60;

    /**
     * Years over which to average for getting typical data
     */
    private static final int YEARS_FOR_TYPICAL = 3;

    private static final long SECONDS_IN_YEAR = 365*24*60*60;

    /**
     * Tolerance for equality of locations in metres
     */
    private static final double LOCATION_TOLERANCE_METRES = 5;

    /**
     * URL for doing a typical data lookup - we're only interested in the data for that day.
     */
    private static final String TYPICAL_DATA_LOOKUP_URL = "https://api.darksky.net/forecast/%s/%s,%s,%s?exclude=currently,minutely,hourly";

    private List<ForecastWeatherPoint> forecastCache;
    private List<TypicalWeatherPoint> typicalWeatherPoints;
    private static NaiveAPI instance;

    /**
     * Use the OWM API to fetch the weather data for a point
     * TODO: Implement API usage
     * @return Weather for a point
     */
    private ForecastWeatherPoint fetchWeatherUsingAPI(double latitude, double longitude) throws APIException {
        List<WeatherData> data = List.of(new WeatherData(System.currentTimeMillis()/1000, 10, 0, 1000, 1,
                0.1, 0.5, ForecastType.MINUTELY, PrecipitationType.RAIN));
        return new ForecastWeatherPoint(latitude, longitude, System.currentTimeMillis()/1000, data);
    }

    private WeatherData fetchTypicalWeatherForPoint(TypicalWeatherPoint point, long time) throws APIException {
        double latitude = point.getLatitude();
        double longitude = point.getLongitude();

        double avgTemperatureCelsius = 0;
        double avgPressure = 0;
        double avgHumidity = 0;

        double avgPrecipitationIntensity = 0;
        double avgPrecipitationProbability = 0;
        ForecastType forecastType = ForecastType.DAILY;
        List<PrecipitationType> precipitationTypes = new ArrayList<>();

        //We're interested in the typical data, so take an average of the previous three years
        for(int i = 0; i < YEARS_FOR_TYPICAL; i++) {
            long lookupTime = time - (i+1)*SECONDS_IN_YEAR;

            // Fetch JSON file from DarkSky using code from https://stackoverflow.com/a/1485730
            try {
                StringBuilder result = new StringBuilder();
                URL totalLookupURL = new URL(String.format(TYPICAL_DATA_LOOKUP_URL, APIKey.getKey(), latitude, longitude, lookupTime));
                HttpURLConnection connectionToAPI = (HttpURLConnection) totalLookupURL.openConnection();
                connectionToAPI.setRequestMethod("GET");

                BufferedReader rd = new BufferedReader(new InputStreamReader(connectionToAPI.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                String resultFromAPI = result.toString();

                JSONObject dailyResult = new JSONObject(resultFromAPI).getJSONObject("daily")
                        .getJSONArray("data").getJSONObject(0);

                double highTempCelsius = dailyResult.getDouble("temperatureHigh");
                double lowTempCelsius = dailyResult.getDouble("temperatureLow");
                double pressure = dailyResult.getDouble("pressure");
                double humidity = dailyResult.getDouble("humidity");

                double precipIntensity = dailyResult.getDouble("precipIntensity");
                double precipProbability = dailyResult.getDouble("precipProbability");

                //TODO: Implement averaging
            } catch (MalformedURLException e) {
                throw new APIException("Malformed URL! " + e.getMessage());
            } catch (IOException e) {
                throw new APIException("Failed to connect to API server");
            }
        }

        //TODO: Actually replace this test data with real averaging
        WeatherData typicalWeather = new WeatherData(time, 5, -5, 1000,
                1, 0.5, 0.5, ForecastType.DAILY, PrecipitationType.SLEET);

        point.addDataPoint(typicalWeather);

        return typicalWeather;
    }

    private boolean isStale(ForecastWeatherPoint point, long currentTime) {
        long timeForecastGenerated = point.getTimeForecastGenerated();
        long timeDelta = timeForecastGenerated - currentTime;

        return (timeDelta < DATA_VALIDITY);
    }
    /**
     * Discards timed-out (stale) cache entries
     */
    private void cleanCache() {
        long currentTime = System.currentTimeMillis()/1000;

        forecastCache.removeIf(x -> isStale(x, currentTime));
    }

    /**
     * @param latitude Latitude of point being fetched in degrees
     * @param longitude Longitude of point being fetched in degrees
     * @param tolerance Acceptable distance difference in metres
     * @return The closest point to the given position within tolerance
     * @throws APIException Exception when API call to fetch point fails
     */
    @Override
    public ForecastWeatherPoint getWeatherInTolerance(double latitude, double longitude, double tolerance) throws APIException {
        // Running this every time isn't ideal, but prevents case where there are multiple suitable points, and closest is stale
        cleanCache(); //If this call is removed, check stale and remove in loop.

        ForecastWeatherPoint bestPoint = null;
        double bestDistance = tolerance;

        for(ForecastWeatherPoint point : forecastCache) {
            double distance = Haversine.getDistance(point.getLatitude(), point.getLongitude(), latitude, longitude);
            if(distance < bestDistance) {
                bestPoint = point;
                bestDistance = distance;
            }
        }

        if(bestPoint == null) {
            ForecastWeatherPoint point = fetchWeatherUsingAPI(latitude, longitude);
            forecastCache.add(point);
            return point;
        } else {
            return bestPoint;
        }
    }


    @Override
    public ForecastWeatherPoint getWeatherForPoint(double latitude, double longitude) throws APIException {
        return getWeatherInTolerance(latitude, longitude, LOCATION_TOLERANCE_METRES);
    }

    @Override
    public WeatherData getTypicalWeatherForPoint(double latitude, double longitude, long time) throws APIException {
        TypicalWeatherPoint bestPoint = null;
        double bestDistance = LOCATION_TOLERANCE_METRES;

        for(TypicalWeatherPoint point : typicalWeatherPoints) {
            double distance = Haversine.getDistance(point.getLatitude(), point.getLongitude(), latitude, longitude);
            if(distance < bestDistance) {
                bestPoint = point;
                bestDistance = distance;
            }
        }

        if(bestPoint == null) {
            bestPoint = new TypicalWeatherPoint(latitude, longitude, new ArrayList<>());
            typicalWeatherPoints.add(bestPoint);
        }

        // Now we have a bestPoint, see if it has a point at the right time
        try {
            return bestPoint.getDataAboutTime(time);
        } catch(ForecastException e) {
            return fetchTypicalWeatherForPoint(bestPoint, time);
        }
    }

    private NaiveAPI() {
        forecastCache = new ArrayList<>();
    }

    public static NaiveAPI getInstance() {
        if(instance == null) {
            instance = new NaiveAPI();
        }
        return instance;
    }
}
