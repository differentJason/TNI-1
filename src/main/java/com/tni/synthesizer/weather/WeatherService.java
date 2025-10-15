package com.tni.synthesizer.weather;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for fetching real weather data from OpenWeatherMap API
 */
public class WeatherService {
    
    // OpenWeatherMap API configuration
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5";
    private static final String FORECAST_URL = BASE_URL + "/forecast";
    private static final String CURRENT_URL = BASE_URL + "/weather";
    
    // Demo API key - users should replace with their own
    private static final String DEFAULT_API_KEY = "demo_key";
    
    private final String apiKey;
    private final Gson gson;
    private final CloseableHttpClient httpClient;
    
    public WeatherService() {
        this(DEFAULT_API_KEY);
    }
    
    public WeatherService(String apiKey) {
        this.apiKey = apiKey;
        this.gson = new Gson();
        this.httpClient = HttpClients.createDefault();
    }
    
    /**
     * Fetch current weather data for a city
     */
    public WeatherData getCurrentWeather(String cityName) throws WeatherServiceException {
        try {
            String url = String.format("%s?q=%s&appid=%s&units=metric", 
                CURRENT_URL, cityName, apiKey);
            
            String jsonResponse = makeHttpRequest(url);
            JsonObject weatherObj = gson.fromJson(jsonResponse, JsonObject.class);
            
            return parseCurrentWeatherData(weatherObj);
            
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to fetch current weather for " + cityName, e);
        }
    }
    
    /**
     * Fetch 5-day weather forecast (3-hour intervals)
     */
    public List<WeatherData> getWeatherForecast(String cityName) throws WeatherServiceException {
        try {
            String url = String.format("%s?q=%s&appid=%s&units=metric", 
                FORECAST_URL, cityName, apiKey);
            
            String jsonResponse = makeHttpRequest(url);
            JsonObject forecastObj = gson.fromJson(jsonResponse, JsonObject.class);
            
            return parseForecastData(forecastObj);
            
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to fetch forecast for " + cityName, e);
        }
    }
    
    /**
     * Generate sample weather data for demo purposes when API key is not available
     */
    public List<WeatherData> generateSampleWeatherData() {
        List<WeatherData> sampleData = new ArrayList<>();
        
        // Simulate 24 hours of weather data (hourly readings)
        double baseTemp = 20.0;
        double basePressure = 1013.25;
        double baseHumidity = 60.0;
        double baseWindSpeed = 5.0;
        
        for (int hour = 0; hour < 24; hour++) {
            // Simulate daily temperature cycle
            double tempVariation = 8 * Math.sin((hour - 6) * Math.PI / 12); // Peak at 2 PM
            double temperature = baseTemp + tempVariation + (Math.random() - 0.5) * 2;
            
            // Simulate pressure variations
            double pressure = basePressure + (Math.random() - 0.5) * 20;
            
            // Simulate humidity variations (inverse relationship with temperature)
            double humidity = baseHumidity - tempVariation * 2 + (Math.random() - 0.5) * 10;
            humidity = Math.max(30, Math.min(95, humidity)); // Clamp between 30-95%
            
            // Simulate wind speed variations
            double windSpeed = baseWindSpeed + (Math.random() - 0.5) * 3;
            windSpeed = Math.max(0, windSpeed);
            
            // Add some weather events
            String description = "clear sky";
            if (hour >= 18 || hour <= 6) {
                if (Math.random() < 0.3) description = "few clouds";
            } else {
                if (Math.random() < 0.2) description = "scattered clouds";
                if (Math.random() < 0.1) description = "light rain";
            }
            
            sampleData.add(new WeatherData(
                temperature, pressure, humidity, windSpeed, description, 
                "Sample City", System.currentTimeMillis() + hour * 3600000L
            ));
        }
        
        return sampleData;
    }
    
    /**
     * Convert weather data to numerical array for music generation
     */
    public double[] weatherToNumericalData(List<WeatherData> weatherDataList, WeatherParameter parameter) {
        return weatherDataList.stream()
            .mapToDouble(data -> {
                switch (parameter) {
                    case TEMPERATURE:
                        return data.getTemperature();
                    case PRESSURE:
                        return data.getPressure() / 10; // Scale down for better musical range
                    case HUMIDITY:
                        return data.getHumidity();
                    case WIND_SPEED:
                        return data.getWindSpeed() * 10; // Scale up for better musical range
                    case COMBINED:
                        // Create a combined metric that represents overall weather "intensity"
                        return (data.getTemperature() * 0.4) + 
                               (data.getPressure() / 50) + 
                               (data.getHumidity() * 0.3) + 
                               (data.getWindSpeed() * 2);
                    default:
                        return data.getTemperature();
                }
            })
            .toArray();
    }
    
    private String makeHttpRequest(String url) throws IOException, WeatherServiceException {
        if (DEFAULT_API_KEY.equals(apiKey)) {
            throw new WeatherServiceException("Please set a valid OpenWeatherMap API key");
        }
        
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/json");
        
        try (ClassicHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            HttpEntity entity = response.getEntity();
            
            String responseBody;
            try {
                responseBody = EntityUtils.toString(entity);
            } catch (ParseException e) {
                throw new WeatherServiceException("Failed to parse HTTP response", e);
            }
            
            if (statusCode == 200) {
                return responseBody;
            } else if (statusCode == 401) {
                throw new WeatherServiceException("Invalid API key");
            } else if (statusCode == 404) {
                throw new WeatherServiceException("City not found");
            } else {
                throw new WeatherServiceException("API request failed with status: " + statusCode);
            }
        }
    }
    
    private WeatherData parseCurrentWeatherData(JsonObject weatherObj) {
        JsonObject main = weatherObj.getAsJsonObject("main");
        JsonObject wind = weatherObj.getAsJsonObject("wind");
        JsonArray weatherArray = weatherObj.getAsJsonArray("weather");
        
        double temperature = main.get("temp").getAsDouble();
        double pressure = main.get("pressure").getAsDouble();
        double humidity = main.get("humidity").getAsDouble();
        double windSpeed = wind != null ? wind.get("speed").getAsDouble() : 0.0;
        
        String description = weatherArray.size() > 0 ? 
            weatherArray.get(0).getAsJsonObject().get("description").getAsString() : "unknown";
        
        String cityName = weatherObj.get("name").getAsString();
        long timestamp = weatherObj.get("dt").getAsLong() * 1000L;
        
        return new WeatherData(temperature, pressure, humidity, windSpeed, description, cityName, timestamp);
    }
    
    private List<WeatherData> parseForecastData(JsonObject forecastObj) {
        List<WeatherData> weatherDataList = new ArrayList<>();
        JsonArray list = forecastObj.getAsJsonArray("list");
        JsonObject city = forecastObj.getAsJsonObject("city");
        String cityName = city.get("name").getAsString();
        
        for (int i = 0; i < list.size(); i++) {
            JsonObject item = list.get(i).getAsJsonObject();
            JsonObject main = item.getAsJsonObject("main");
            JsonObject wind = item.getAsJsonObject("wind");
            JsonArray weather = item.getAsJsonArray("weather");
            
            double temperature = main.get("temp").getAsDouble();
            double pressure = main.get("pressure").getAsDouble();
            double humidity = main.get("humidity").getAsDouble();
            double windSpeed = wind != null ? wind.get("speed").getAsDouble() : 0.0;
            
            String description = weather.size() > 0 ? 
                weather.get(0).getAsJsonObject().get("description").getAsString() : "unknown";
            
            long timestamp = item.get("dt").getAsLong() * 1000L;
            
            weatherDataList.add(new WeatherData(
                temperature, pressure, humidity, windSpeed, description, cityName, timestamp
            ));
        }
        
        return weatherDataList;
    }
    
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
    
    // Weather parameters that can be used for music generation
    public enum WeatherParameter {
        TEMPERATURE("Temperature (°C)"),
        PRESSURE("Pressure (hPa)"),
        HUMIDITY("Humidity (%)"),
        WIND_SPEED("Wind Speed (m/s)"),
        COMBINED("Combined Weather Index");
        
        private final String displayName;
        
        WeatherParameter(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}