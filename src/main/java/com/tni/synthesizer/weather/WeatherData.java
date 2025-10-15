package com.tni.synthesizer.weather;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Data class representing weather information at a specific point in time
 */
public class WeatherData {
    private final double temperature;    // Temperature in Celsius
    private final double pressure;       // Atmospheric pressure in hPa
    private final double humidity;       // Humidity percentage (0-100)
    private final double windSpeed;      // Wind speed in m/s
    private final String description;    // Weather description (e.g., "clear sky", "light rain")
    private final String cityName;      // Name of the city
    private final long timestamp;        // Unix timestamp in milliseconds
    
    public WeatherData(double temperature, double pressure, double humidity, 
                      double windSpeed, String description, String cityName, long timestamp) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.description = description;
        this.cityName = cityName;
        this.timestamp = timestamp;
    }
    
    // Getters
    public double getTemperature() {
        return temperature;
    }
    
    public double getPressure() {
        return pressure;
    }
    
    public double getHumidity() {
        return humidity;
    }
    
    public double getWindSpeed() {
        return windSpeed;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCityName() {
        return cityName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public LocalDateTime getDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
    
    public String getFormattedDateTime() {
        return getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    /**
     * Get temperature in Fahrenheit
     */
    public double getTemperatureFahrenheit() {
        return (temperature * 9.0 / 5.0) + 32.0;
    }
    
    /**
     * Get wind speed in mph
     */
    public double getWindSpeedMph() {
        return windSpeed * 2.237;
    }
    
    /**
     * Get a normalized "weather intensity" value (0-100)
     * Combines multiple weather parameters into a single metric
     */
    public double getWeatherIntensity() {
        // Normalize each parameter to 0-100 range
        double tempNorm = Math.max(0, Math.min(100, (temperature + 10) * 2.5)); // -10°C to 30°C -> 0 to 100
        double pressureNorm = Math.max(0, Math.min(100, (pressure - 980) * 2.5)); // 980-1020 hPa -> 0 to 100
        double humidityNorm = humidity; // Already 0-100
        double windNorm = Math.min(100, windSpeed * 5); // 0-20 m/s -> 0 to 100
        
        // Weight the parameters (temperature and pressure are most important for music)
        return (tempNorm * 0.4) + (pressureNorm * 0.3) + (humidityNorm * 0.2) + (windNorm * 0.1);
    }
    
    /**
     * Determine musical key suggestion based on weather conditions
     */
    public String getSuggestedMusicalKey() {
        double intensity = getWeatherIntensity();
        
        if (intensity < 20) {
            return "C Minor"; // Cold, low pressure = melancholy
        } else if (intensity < 40) {
            return "A Minor"; // Cool conditions = minor keys
        } else if (intensity < 60) {
            return "C Major"; // Moderate conditions = balanced
        } else if (intensity < 80) {
            return "G Major"; // Warm conditions = bright keys
        } else {
            return "D Major"; // Hot, high intensity = very bright
        }
    }
    
    /**
     * Suggest tempo based on wind speed and weather intensity
     */
    public int getSuggestedTempo() {
        int baseTempo = 120;
        
        // Wind speed affects tempo
        int windTempo = (int) (windSpeed * 5);
        
        // Weather intensity affects tempo
        int intensityTempo = (int) (getWeatherIntensity() * 0.8);
        
        return Math.max(60, Math.min(180, baseTempo + windTempo + intensityTempo - 80));
    }
    
    @Override
    public String toString() {
        return String.format("%s: %.1f°C, %.0f%% humidity, %.1f m/s wind, %s [%s]",
            cityName, temperature, humidity, windSpeed, description, getFormattedDateTime());
    }
    
    /**
     * Create a detailed string representation for display
     */
    public String toDetailedString() {
        return String.format(
            "City: %s\n" +
            "Time: %s\n" +
            "Temperature: %.1f°C (%.1f°F)\n" +
            "Pressure: %.1f hPa\n" +
            "Humidity: %.0f%%\n" +
            "Wind Speed: %.1f m/s (%.1f mph)\n" +
            "Conditions: %s\n" +
            "Weather Intensity: %.1f/100\n" +
            "Suggested Key: %s\n" +
            "Suggested Tempo: %d BPM",
            cityName, getFormattedDateTime(), temperature, getTemperatureFahrenheit(),
            pressure, humidity, windSpeed, getWindSpeedMph(), description,
            getWeatherIntensity(), getSuggestedMusicalKey(), getSuggestedTempo());
    }
}