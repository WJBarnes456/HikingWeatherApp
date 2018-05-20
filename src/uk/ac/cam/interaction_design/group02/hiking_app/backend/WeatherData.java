package uk.ac.cam.interaction_design.group02.hiking_app.backend;


/**
 * Immutable class representing a piece of weather data
 */
public class WeatherData {
    private long timeForData;

    private double highTempCelsius;
    private double lowTempCelsius;

    private double pressure;
    private double humidity;

    private double precipitationIntensity;
    private double precipitationProbability;

    private double visibility;
    private String icon;

    private ForecastType forecastType;
    private PrecipitationType precipitationType;

    public WeatherData(long timeForData, double highTempCelsius, double lowTempCelsius, double pressure, double humidity,
                       double precipitationIntensity, double precipitationProbability,
                       double visibility, ForecastType forecastType, PrecipitationType precipitationType, String icon) {
        this.highTempCelsius = highTempCelsius;
        this.lowTempCelsius = lowTempCelsius;
        this.pressure = pressure;
        this.humidity = humidity;

        this.precipitationIntensity = precipitationIntensity;
        this.precipitationProbability = precipitationProbability;
        this.visibility = visibility;
        this.forecastType = forecastType;
        this.precipitationType = precipitationType;
        this.timeForData = timeForData;

        this.icon = icon;
    }

    public long getTimeForData() {
        return timeForData;
    }

    private double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    private double celsiusToFahrenheit(double celsius) {
        return 9*celsius/5 + 32;
    }

    // Average temperature getters

    public double getAvgTemperatureKelvin() {
        return celsiusToKelvin(getAvgTemperatureCelsius());
    }

    public double getAvgTemperatureCelsius() {
        return (highTempCelsius + lowTempCelsius)/2;
    }

    public double getAvgTemperatureFahrenheit() {
        return celsiusToFahrenheit(getAvgTemperatureCelsius());
    }

    // High temperature getters

    public double getHighTemperatureKelvin() {
        return celsiusToKelvin(getHighTemperatureCelsius());
    }

    public double getHighTemperatureCelsius() {
        return highTempCelsius;
    }

    public double getHighTemperatureFahrenheit() {
        return celsiusToFahrenheit(getHighTemperatureCelsius());
    }

    // Low temperature getters

    public double getLowTemperatureKelvin() {
        return celsiusToKelvin(getLowTemperatureCelsius());
    }

    public double getLowTemperatureCelsius() {
        return lowTempCelsius;
    }

    public double getLowTemperatureFahrenheit() {
        return celsiusToFahrenheit(getLowTemperatureCelsius());
    }

    public double getPressure() {
        return pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPrecipitationProbability() {
        return precipitationProbability;
    }

    public double getPrecipitationIntensity() {
        return precipitationIntensity;
    }

    public ForecastType getForecastType() {
        return forecastType;
    }

    public PrecipitationType getPrecipitationType() {
        return precipitationType;
    }

    public double getVisibility() {
        return visibility;
    }

    public String getIcon() {return icon;}
}
