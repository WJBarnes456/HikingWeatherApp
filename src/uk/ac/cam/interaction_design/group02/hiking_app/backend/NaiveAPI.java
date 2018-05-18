package uk.ac.cam.interaction_design.group02.hiking_app.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.json.JSONException;
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

    /**
     * Tolerance for equality of locations in metres
     */
    private static final double LOCATION_TOLERANCE_METRES = 5;

    /**
     * URL for doing a typical data lookup - we're only interested in the data for that day.
     */
    private static final String TYPICAL_DATA_LOOKUP_URL = "https://api.darksky.net/forecast/%s/%s,%s,%s?exclude=currently,minutely,hourly&units=si";

    private List<ForecastWeatherPoint> forecastCache = new ArrayList<>();
    private List<TypicalWeatherPoint> typicalWeatherPoints = new ArrayList<>();
    private static NaiveAPI instance;

    /**
     * Use the OWM API to fetch the weather data for a point
     * TODO: Implement API usage
     * @return Weather for a point
     */
private AsyncHttpClient client = new AsyncHttpClient();
 
 public ForecastWeatherPoint fetchWeatherUsingAPI(double latitude, double longitude) throws APIException {
        List<WeatherData> data = new ArrayList<WeatherData>();

        client.get( "https://api.forecast.io/forecast/" + API_KEY + "/" + latitude + "," + longitude + "?units=si", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                    JSONObject root = new JSONObject(response);
                    JSONObject daily = root.getJSONObject("daily");
                    JSONObject hourly = root.getJSONObject("hourly");
                    JSONObject minutely = root.getJSONObject("minutely");
                    JSONObject currently = root.getJSONObject("currently");

                    JSONArray nHour = minutely.getJSONArray("data");
                    JSONArray nDay = hourly.getJSONArray("data");
                    JSONArray nDay = hourly.getJSONArray("data");

                    /*(long timeForData, double highTempCelsius, double lowTempCelsius, double pressure, double humidity,
                       double precipitationIntensity, double precipitationProbability,
                       double visibility, ForecastType forecastType, PrecipitationType precipitationType) */



                    //loop through next hour for each minute
                    for (int i = 0; i < nHour.length(); i++) {
                        JSONObject Min = nHour.getJSONObject(i);
                        PrecipitationType type = new PrecipitationType();

                        switch (Min.getString("precipType")){
                            case "snow":type = PrecipitationType.SNOW;
                                        break;
                            case "rain":type = PrecipitationType.RAIN;
                                        break;
                            case "sleet":type = PrecipitationType.SLEET;
                                        break;
                            default:type = PrecipitationType.NONE;
                                        break;
                        }
                            
                        
                        WeatherData toSave = new WeatherData(
                            Min.getLong("time"),
                            Min.getDouble("apparentTemperature"),
                            Min.getDouble("apparentTemperature"),
                            Min.getDouble("pressure"),
                            Min.getDouble("humidity"),
                            Min.getDouble("precipIntensity"),
                            Min.getDouble("precipProbability"),
                            Min.getDouble("visibility"),
                            ForecastType.MINUTELY,
                            type
                        );
                        data.add(toSave);
                    }

                    //loop through next day for each hour
                    for (int i = 0; i < nDay.length(); i++) {
                        JSONObject Hour = nDay.getJSONObject(i);
                        PrecipitationType type = new PrecipitationType();

                        switch (Hour.getString("precipType")){
                            case "snow":type = PrecipitationType.SNOW;
                                        break;
                            case "rain":type = PrecipitationType.RAIN;
                                        break;
                            case "sleet":type = PrecipitationType.SLEET;
                                        break;
                            default:type = PrecipitationType.NONE;
                                        break;
                        }
                            
                        
                        WeatherData toSave = new WeatherData(
                            Hour.getLong("time"),
                            Hour.getDouble("apparentTemperature"),
                            Hour.getDouble("apparentTemperature"),
                            Hour.getDouble("pressure"),
                            Hour.getDouble("humidity"),
                            Hour.getDouble("precipIntensity"),
                            Hour.getDouble("precipProbability"),
                            Hour.getDouble("visibility"),
                            ForecastType.HOURLY,
                            type
                        );
                        data.add(toSave);
                    }

                    //loop through next week for each day
                     for (int i = 0; i < nWeek.length(); i++) {
                        JSONObject Day = nWeek.getJSONObject(i);
                        PrecipitationType type = new PrecipitationType();

                        switch (Day.getString("precipType")){
                            case "snow":type = PrecipitationType.SNOW;
                                        break;
                            case "rain":type = PrecipitationType.RAIN;
                                        break;
                            case "sleet":type = PrecipitationType.SLEET;
                                        break;
                            default:type = PrecipitationType.NONE;
                                        break;
                        }
                            
                        
                        WeatherData toSave = new WeatherData(
                            Day.getLong("time"),
                            Day.getDouble("apparentTemperatureHigh"),
                            Day.getDouble("apparentTemperatureLow"),
                            Day.getDouble("pressure"),
                            Day.getDouble("humidity"),
                            Day.getDouble("precipIntensity"),
                            Day.getDouble("precipProbability"),
                            Day.getDouble("visibility"),
                            ForecastType.DAILY,
                            type
                        );
                        data.add(toSave);
                    }

                }
            }
        });
        
        return new ForecastWeatherPoint(latitude, longitude, System.currentTimeMillis()/1000, data);
    }

    /**
     * Fetch typical weather for a point-time combo using the API, caching it in the point
     * @param point The point to fetch weather for
     * @param time The time (rounded to the day) to fetch weather for
     * @return Typical weather for that day averaged over the past YEARS_FOR_TYPICAL years
     * @throws APIException When the API doesn't respond
     */
    private WeatherData fetchTypicalWeatherForPoint(TypicalWeatherPoint point, long time) throws APIException {
        double latitude = point.getLatitude();
        double longitude = point.getLongitude();

        double totalHighTempCelsius = 0;
        double totalLowTempCelsius = 0;
        double totalPressure = 0;
        double totalHumidity = 0;
        double totalVisibility = 0;

        double totalPrecipIntensity = 0;
        double totalPrecipProbability = 0;
        ForecastType forecastType = ForecastType.DAILY;
        List<PrecipitationType> precipitationTypes = new ArrayList<>();

        //Create a calendar so we can scale back years without worrying about leap years etc
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time*1000);

        //While the time on the calendar is after today
        while(c.getTime().after(new Date())) {
            // Scale back by a year
            c.add(Calendar.YEAR, -1);
        }

        //We're interested in the typical data, so take an average of the previous three years
        for(int i = 0; i < YEARS_FOR_TYPICAL; i++) {
            //Gets the current time on the calendar (which is set to the first corresponding date of year before now)
            long lookupTime = c.getTime().getTime() / 1000;

            //Go back a year (now we've extracted the epoch time, we can do this safely
            c.add(Calendar.YEAR, -1);

            // Fetch JSON file from DarkSky using code from https://stackoverflow.com/a/1485730
            try {
                StringBuilder result = new StringBuilder();
                URL totalLookupURL = new URL(String.format(TYPICAL_DATA_LOOKUP_URL, APIKey.getDarkSkyKey(), latitude, longitude, lookupTime));
                HttpURLConnection connectionToAPI = (HttpURLConnection) totalLookupURL.openConnection();
                connectionToAPI.setRequestMethod("GET");

                BufferedReader rd = new BufferedReader(new InputStreamReader(connectionToAPI.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                String resultFromAPI = result.toString();

                //Now we've fetched the result, convert it into a JSON object

                JSONObject dailyResult = new JSONObject(resultFromAPI).getJSONObject("daily")
                        .getJSONArray("data").getJSONObject(0);

                //Extract required information
                double highTempCelsius = dailyResult.getDouble("temperatureHigh");
                double lowTempCelsius = dailyResult.getDouble("temperatureLow");
                double pressure = dailyResult.getDouble("pressure");
                double humidity = dailyResult.getDouble("humidity");

                double precipIntensity = dailyResult.getDouble("precipIntensity");
                double precipProbability = dailyResult.getDouble("precipProbability");
                double visibility = dailyResult.getDouble("visibility");

                PrecipitationType type;
                try {
                    type = PrecipitationType.typeFromString(dailyResult.getString("precipType"));
                } catch (JSONException e) {
                    type = PrecipitationType.NONE;
                }

                //Sum up all the data
                totalHighTempCelsius += highTempCelsius;
                totalLowTempCelsius += lowTempCelsius;
                totalPressure += pressure;
                totalHumidity += humidity;

                totalPrecipIntensity += precipIntensity;
                totalPrecipProbability += precipProbability;
                totalVisibility += visibility;

                precipitationTypes.add(type);
            } catch (MalformedURLException e) {
                throw new APIException("Malformed URL! " + e.getMessage());
            } catch (IOException e) {
                throw new APIException("Failed to connect to API server");
            }
        }

        // Sum counts
        HashMap<PrecipitationType, Integer> counts = new HashMap<>();

        for(PrecipitationType type : precipitationTypes) {
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }

        // Work out the Precip Type with the highest count ie. mode
        int bestCount = Integer.MIN_VALUE;
        PrecipitationType modePrecipType = null;

        for(Map.Entry<PrecipitationType, Integer> entry : counts.entrySet()) {
            int count = entry.getValue();
            if(count > bestCount) {
                bestCount = count;
                modePrecipType = entry.getKey();
            }
        }

        // Create a datapoint from all the averages
        WeatherData typicalWeather = new WeatherData(time, totalHighTempCelsius/YEARS_FOR_TYPICAL,
                totalLowTempCelsius/YEARS_FOR_TYPICAL, totalPressure/YEARS_FOR_TYPICAL,
                totalHumidity/YEARS_FOR_TYPICAL, totalPrecipIntensity/YEARS_FOR_TYPICAL,
                totalPrecipProbability/YEARS_FOR_TYPICAL, totalVisibility/YEARS_FOR_TYPICAL,
                forecastType, modePrecipType);

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
            // It doesn't have a point at the right time, so fetch it, add it to the file and
            return fetchTypicalWeatherForPoint(bestPoint, time);
        }
    }

    public static NaiveAPI getInstance() {
        if(instance == null) {
            instance = new NaiveAPI();
        }
        return instance;
    }
}
