package uk.ac.cam.interaction_design.group02.hiking_app.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.json.JSONArray;
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
    private static final long DATA_VALIDITY = 60 * 60;

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

    private static final String FORECAST_DATA_LOOKUP_URL = "https://api.darksky.net/forecast/%s/%s,%s?units=si";

    private List<ForecastWeatherPoint> forecastCache = new ArrayList<>();
    private List<TypicalWeatherPoint> typicalWeatherPoints = new ArrayList<>();
    private static NaiveAPI instance;

    /**
     * Get precipitation type from object
     * @param object Forecast object to extract preciptype from
     * @return Correct precipitation type
     */
    private PrecipitationType getPrecipType(JSONObject object) {
        try {
            return PrecipitationType.typeFromString(object.getString("precipType"));
        } catch (JSONException e) {
            return PrecipitationType.NONE;
        }
    }

    /**
     * Use the API to fetch weather for a given point
     *
     * @param latitude  Latitude of the point to fetch weather for
     * @param longitude Longitude of the point to fetch weather for
     * @return Forecast data for a location
     * @throws APIException When the API isn't working (eg. hit rate limit, malformed request)
     */
    private ForecastWeatherPoint fetchWeatherUsingAPI(double latitude, double longitude) throws APIException {
        List<WeatherData> data = new ArrayList<>();

        try {
            URL totalLookupURL = new URL(String.format(FORECAST_DATA_LOOKUP_URL, APIKey.getDarkSkyKey(), latitude, longitude));
            JSONObject root = doAPIRequest(totalLookupURL);

            JSONObject daily = root.getJSONObject("daily");
            JSONObject hourly = root.getJSONObject("hourly");
            JSONObject minutely = root.getJSONObject("minutely");
            JSONObject currently = root.getJSONObject("currently");

            JSONArray nHour = minutely.getJSONArray("data");
            JSONArray nDay = hourly.getJSONArray("data");
            JSONArray nWeek = daily.getJSONArray("data");

            double currentTemperature = currently.getDouble("temperature");
            double currentPressure = currently.getDouble("pressure");
            double currentHumidity = currently.getDouble("humidity");
            double currentVisibility = currently.getDouble("visibility");

            //loop through next hour for each minute
            for (int i = 0; i < nHour.length(); i++) {
                JSONObject min = nHour.getJSONObject(i);
                PrecipitationType type = getPrecipType(min);

                String icon;
                try {
                    icon = min.getString("icon");
                } catch(JSONException e) {
                    icon = "default";
                }

                // Minutely forecast doesn't provide temperature, pressure or humidity - just assume same as current.
                WeatherData toSave = new WeatherData(
                        min.getLong("time"),
                        currentTemperature,
                        currentTemperature,
                        currentPressure,
                        currentHumidity,
                        min.getDouble("precipIntensity"),
                        min.getDouble("precipProbability"),
                        currentVisibility,
                        ForecastType.MINUTELY,
                        type,
                        icon
                );
                data.add(toSave);
            }

            //loop through next day for each hour
            for (int i = 0; i < nDay.length(); i++) {
                JSONObject hour = nDay.getJSONObject(i);
                PrecipitationType type = getPrecipType(hour);

                WeatherData toSave = new WeatherData(
                        hour.getLong("time"),
                        hour.getDouble("temperature"),
                        hour.getDouble("temperature"),
                        hour.getDouble("pressure"),
                        hour.getDouble("humidity"),
                        hour.getDouble("precipIntensity"),
                        hour.getDouble("precipProbability"),
                        hour.getDouble("visibility"),
                        ForecastType.HOURLY,
                        type,
                        hour.getString("icon")
                );
                data.add(toSave);
            }

            //loop through next week for each day
            for (int i = 0; i < nWeek.length(); i++) {
                JSONObject day = nWeek.getJSONObject(i);
                PrecipitationType type = getPrecipType(day);

                // Dodgy hack - sometimes the API doesn't include visibility in daily forecasts, so we need to store a NaN if it's not legit
                // TODO: Find a more elegant way of doing this
                double visibility;
                try {
                    visibility = day.getDouble("visibility");
                } catch (JSONException e) {
                    visibility = Double.NaN;
                }

                WeatherData toSave = new WeatherData(
                        day.getLong("time"),
                        day.getDouble("temperatureMax"),
                        day.getDouble("temperatureMin"),
                        day.getDouble("pressure"),
                        day.getDouble("humidity"),
                        day.getDouble("precipIntensity"),
                        day.getDouble("precipProbability"),
                        visibility,
                        ForecastType.DAILY,
                        type,
                        day.getString("icon")
                );

                data.add(toSave);
            }
        } catch (MalformedURLException e) {
            throw new APIException("Malformed URL! " + e.getMessage());
        }

        return new ForecastWeatherPoint(latitude, longitude, System.currentTimeMillis() / 1000, data);
    }

    /**
     * Method for doing a request to the API
     *
     * @param url URL for request
     * @return JSON document
     * @throws IOException
     */
    private JSONObject doAPIRequest(URL url) throws APIException {
        // Fetch JSON file from DarkSky based on code from https://stackoverflow.com/a/1485730
        try {
            StringBuilder result = new StringBuilder();
            HttpURLConnection connectionToAPI = (HttpURLConnection) url.openConnection();
            connectionToAPI.setRequestMethod("GET");

            BufferedReader rd = new BufferedReader(new InputStreamReader(connectionToAPI.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            String resultFromAPI = result.toString();

            return new JSONObject(resultFromAPI);
        } catch (IOException e) {
            throw new APIException("Failed to connect to API server \n" + e.getMessage());
        }
    }

    /**
     * Fetch typical weather for a point-time combo using the API, caching it in the point
     *
     * @param point The point to fetch weather for
     * @param time  The time (rounded to the day) to fetch weather for
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
        List<String> icons = new ArrayList<>();

        //Create a calendar so we can scale back years without worrying about leap years etc
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time * 1000);

        //While the time on the calendar is after today
        while (c.getTime().after(new Date())) {
            // Scale back by a year
            c.add(Calendar.YEAR, -1);
        }

        //We're interested in the typical data, so take an average of the previous three years
        for (int i = 0; i < YEARS_FOR_TYPICAL; i++) {
            //Gets the current time on the calendar (which is set to the first corresponding date of year before now)
            long lookupTime = c.getTime().getTime() / 1000;

            //Go back a year (now we've extracted the epoch time, we can do this safely
            c.add(Calendar.YEAR, -1);

            try {
                URL totalLookupURL = new URL(String.format(TYPICAL_DATA_LOOKUP_URL, APIKey.getDarkSkyKey(), latitude, longitude, lookupTime));
                JSONObject result = doAPIRequest(totalLookupURL);

                //Now we've fetched the result, convert it into a JSON object

                JSONObject dailyResult = result.getJSONObject("daily")
                        .getJSONArray("data").getJSONObject(0);

                //Extract required information
                double highTempCelsius = dailyResult.getDouble("temperatureHigh");
                double lowTempCelsius = dailyResult.getDouble("temperatureLow");
                double pressure = dailyResult.getDouble("pressure");
                double humidity = dailyResult.getDouble("humidity");

                double precipIntensity = dailyResult.getDouble("precipIntensity");
                double precipProbability = dailyResult.getDouble("precipProbability");
                double visibility = dailyResult.getDouble("visibility");

                PrecipitationType type = getPrecipType(dailyResult);
                String icon = dailyResult.getString("icon");

                //Sum up all the data
                totalHighTempCelsius += highTempCelsius;
                totalLowTempCelsius += lowTempCelsius;
                totalPressure += pressure;
                totalHumidity += humidity;

                totalPrecipIntensity += precipIntensity;
                totalPrecipProbability += precipProbability;
                totalVisibility += visibility;

                precipitationTypes.add(type);
                icons.add(icon);
            } catch (MalformedURLException e) {
                throw new APIException("Malformed URL! " + e.getMessage());
            }
        }

        // Sum counts
        HashMap<PrecipitationType, Integer> precipCounts = new HashMap<>();

        for (PrecipitationType type : precipitationTypes) {
            precipCounts.put(type, precipCounts.getOrDefault(type, 0) + 1);
        }

        HashMap<String, Integer> iconCounts = new HashMap<>();
        for (String icon : icons) {
            iconCounts.put(icon, iconCounts.getOrDefault(icon, 0) + 1);
        }


        // Work out the Precip Type with the highest count ie. mode
        int bestPrecipCount = Integer.MIN_VALUE;
        PrecipitationType modePrecipType = null;

        for (Map.Entry<PrecipitationType, Integer> entry : precipCounts.entrySet()) {
            int count = entry.getValue();
            if (count > bestPrecipCount) {
                bestPrecipCount = count;
                modePrecipType = entry.getKey();
            }
        }

        // Work out the icon with the highest count ie. mode
        int bestIconCount = Integer.MIN_VALUE;
        String modeIcon = null;

        for (Map.Entry<String, Integer> entry : iconCounts.entrySet()) {
            int count = entry.getValue();
            if (count > bestIconCount) {
                bestIconCount = count;
                modeIcon = entry.getKey();
            }
        }

        // Create a datapoint from all the averages
        WeatherData typicalWeather = new WeatherData(time, totalHighTempCelsius / YEARS_FOR_TYPICAL,
                totalLowTempCelsius / YEARS_FOR_TYPICAL, totalPressure / YEARS_FOR_TYPICAL,
                totalHumidity / YEARS_FOR_TYPICAL, totalPrecipIntensity / YEARS_FOR_TYPICAL,
                totalPrecipProbability / YEARS_FOR_TYPICAL, totalVisibility / YEARS_FOR_TYPICAL,
                forecastType, modePrecipType, modeIcon);

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
        long currentTime = System.currentTimeMillis() / 1000;

        forecastCache.removeIf(x -> isStale(x, currentTime));
    }

    /**
     * @param latitude  Latitude of point being fetched in degrees
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

        for (ForecastWeatherPoint point : forecastCache) {
            double distance = Haversine.getDistance(point.getLatitude(), point.getLongitude(), latitude, longitude);
            if (distance < bestDistance) {
                bestPoint = point;
                bestDistance = distance;
            }
        }

        if (bestPoint == null) {
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

        for (TypicalWeatherPoint point : typicalWeatherPoints) {
            double distance = Haversine.getDistance(point.getLatitude(), point.getLongitude(), latitude, longitude);
            if (distance < bestDistance) {
                bestPoint = point;
                bestDistance = distance;
            }
        }

        if (bestPoint == null) {
            bestPoint = new TypicalWeatherPoint(latitude, longitude, new ArrayList<>());
            typicalWeatherPoints.add(bestPoint);
        }

        // Now we have a bestPoint, see if it has a point at the right time
        try {
            return bestPoint.getDataAboutTime(time);
        } catch (ForecastException e) {
            // It doesn't have a point at the right time, so fetch it, add it to the file and
            return fetchTypicalWeatherForPoint(bestPoint, time);
        }
    }

    public static NaiveAPI getInstance() {
        if (instance == null) {
            instance = new NaiveAPI();
        }
        return instance;
    }

    private NaiveAPI() {}
}
