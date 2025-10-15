---
layout: page
title: API Reference
permalink: /api-reference/
---

# API Reference

Technical documentation for developers working with TNI Synthesizer Suite.

## Application Architecture

### Main Classes

**SimpleSynthesizerApplication**
- Main launcher application
- Entry point for the synthesizer suite
- Provides navigation between modules

**SimpleMidiSynthesizer**
- Interactive MIDI keyboard synthesizer
- Real-time note playback
- Instrument selection and control

**DataToMidiGenerator**
- Data-to-music conversion engine
- Weather API integration
- MIDI sequence generation

### Package Structure
```
com.tni.synthesizer/
├── SimpleSynthesizerApplication.java    # Main launcher
├── SimpleMidiSynthesizer.java           # MIDI keyboard
├── generative/
│   └── DataToMidiGenerator.java         # Data conversion
└── weather/
    ├── WeatherService.java              # API client
    ├── WeatherData.java                 # Data model
    └── WeatherServiceException.java     # Exception handling
```

## Weather API Integration

### WeatherService Class

**Constructor**
```java
WeatherService()                    // Uses default demo API key
WeatherService(String apiKey)       // Uses provided API key
```

**Methods**
```java
// Fetch current weather for a city
WeatherData getCurrentWeather(String cityName) throws WeatherServiceException

// Fetch 5-day forecast (3-hour intervals)
List<WeatherData> getWeatherForecast(String cityName) throws WeatherServiceException

// Generate sample weather data for demo
List<WeatherData> generateSampleWeatherData()

// Convert weather data to numerical array
double[] weatherToNumericalData(List<WeatherData> weatherDataList, WeatherParameter parameter)

// Clean shutdown
void close() throws IOException
```

**Weather Parameters**
```java
enum WeatherParameter {
    TEMPERATURE("Temperature (°C)"),
    PRESSURE("Pressure (hPa)"),
    HUMIDITY("Humidity (%)"),
    WIND_SPEED("Wind Speed (m/s)"),
    COMBINED("Combined Weather Index")
}
```

### WeatherData Class

**Properties**
- `double temperature` - Temperature in Celsius
- `double pressure` - Atmospheric pressure in hPa
- `double humidity` - Humidity percentage (0-100)
- `double windSpeed` - Wind speed in m/s
- `String description` - Weather description
- `String cityName` - City name
- `long timestamp` - Unix timestamp in milliseconds

**Methods**
```java
// Basic getters
double getTemperature()
double getPressure()
double getHumidity()
double getWindSpeed()
String getDescription()
String getCityName()
long getTimestamp()

// Calculated values
double getTemperatureFahrenheit()      // Temperature in Fahrenheit
double getWindSpeedMph()               // Wind speed in mph
double getWeatherIntensity()           // Normalized 0-100 intensity
LocalDateTime getDateTime()            // Timestamp as LocalDateTime
String getFormattedDateTime()          // Formatted date/time string

// Musical suggestions
String getSuggestedMusicalKey()        // Suggested key based on weather
int getSuggestedTempo()               // Suggested tempo (60-180 BPM)

// String representations
String toString()                      // Brief weather summary
String toDetailedString()             // Detailed weather information
```

## MIDI Generation

### Data Analysis

**Statistical Analysis**
The system analyzes input data for:
- Minimum and maximum values
- Mean (average) values
- Standard deviation (volatility)
- Trend direction (positive/negative slope)

**Musical Mapping Algorithm**
```java
// Pseudo-code for data-to-music mapping
for (double dataPoint : inputData) {
    // Normalize data to musical range
    int midiNote = scaleToMidiRange(dataPoint, minValue, maxValue);
    
    // Determine chord based on data trend
    int[] chord = selectChord(trend, volatility);
    
    // Set timing based on data position
    long timing = calculateTiming(index, tempo);
    
    // Add to MIDI sequence
    addNoteToSequence(midiNote, chord, timing);
}
```

### MIDI Sequence Generation

**Supported Features**
- Multiple instrument tracks
- Chord progressions based on data patterns
- Bass line generation
- Basic drum patterns
- Tempo and key customization

**File Formats**
- Standard MIDI Format 1
- Compatible with most DAWs and music software
- Exports with all track information preserved

## Configuration

### System Requirements
- Java 11 or higher
- Maven 3.6+ (for building from source)
- 512 MB RAM minimum, 1 GB recommended
- Internet connection (for weather API)

### Dependencies

**Core Dependencies**
```xml
<!-- MIDI and audio -->
javax.sound.midi (built-in Java)
javax.swing (built-in Java)

<!-- Weather API -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>

<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.1</version>
</dependency>
```

**Build Dependencies**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>11</source>
        <target>11</target>
    </configuration>
</plugin>
```

## Extending the Application

### Adding New Weather Parameters

1. **Extend WeatherParameter enum**
```java
enum WeatherParameter {
    // Existing parameters...
    NEW_PARAMETER("Display Name");
}
```

2. **Update data conversion logic**
```java
case NEW_PARAMETER:
    return data.getNewParameter() * scalingFactor;
```

3. **Add UI controls** in `createWeatherPanel()`

### Adding New Musical Styles

1. **Define chord progressions**
```java
private static final int[][] NEW_PROGRESSION = {
    {note1, note2, note3}, {note4, note5, note6}, // etc.
};
```

2. **Update style selection logic**
```java
case "new_style":
    return NEW_PROGRESSION;
```

3. **Add to UI dropdown** in style combo box

### Custom Data Sources

1. **Implement data fetching**
```java
public class CustomDataService {
    public List<Double> fetchData(String source) {
        // Implement data fetching logic
        return dataList;
    }
}
```

2. **Integrate with DataToMidiGenerator**
```java
// Add custom data loading method
private void loadCustomData(String source) {
    CustomDataService service = new CustomDataService();
    List<Double> data = service.fetchData(source);
    // Convert to comma-separated string and set in dataInput
}
```

## Error Handling

### Exception Types

**WeatherServiceException**
- Thrown for weather API related errors
- Includes specific error messages for different failure types
- Provides guidance for resolution

**MidiUnavailableException**
- Java built-in exception for MIDI system problems
- Usually indicates audio system issues
- Handled gracefully with user-friendly messages

### Best Practices

**API Rate Limiting**
```java
// Implement rate limiting for API calls
private long lastApiCall = 0;
private static final long MIN_API_INTERVAL = 1000; // 1 second

public void makeApiCall() {
    long now = System.currentTimeMillis();
    if (now - lastApiCall < MIN_API_INTERVAL) {
        Thread.sleep(MIN_API_INTERVAL - (now - lastApiCall));
    }
    // Make API call
    lastApiCall = System.currentTimeMillis();
}
```

**Resource Management**
```java
// Always clean up resources
try (WeatherService service = new WeatherService(apiKey)) {
    // Use service
} catch (Exception e) {
    // Handle errors
} // Automatic cleanup
```

## Testing

### Unit Testing Framework
```java
@Test
public void testWeatherDataConversion() {
    WeatherData data = new WeatherData(25.0, 1013.25, 60.0, 5.0, "clear", "Test City", System.currentTimeMillis());
    assertEquals(25.0, data.getTemperature(), 0.1);
    assertTrue(data.getWeatherIntensity() > 0);
}
```

### Integration Testing
```java
@Test
public void testWeatherServiceIntegration() {
    WeatherService service = new WeatherService("demo_key");
    assertThrows(WeatherServiceException.class, () -> {
        service.getCurrentWeather("TestCity");
    });
}
```

## Performance considerations

### Memory Usage
- Weather data: ~1KB per data point
- MIDI sequences: ~10KB for typical composition
- UI components: ~50MB base memory usage

### Optimization Tips
1. **Limit data size**: Use reasonable data ranges (100-1000 points)
2. **Cache weather data**: Avoid repeated API calls
3. **Efficient MIDI generation**: Reuse MIDI resources
4. **Background processing**: Use SwingWorker for long operations

---

**For advanced development topics**, see the source code documentation and JavaDoc comments in the repository.