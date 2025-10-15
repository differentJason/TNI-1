# Weather API Integration Guide

## Overview
The TNI Synthesizer Suite now includes **real-time weather API integration** that converts live weather data into musical sequences!

## Features
- **Live Weather Data**: Fetch current weather and 5-day forecasts from any city worldwide
- **Multiple Weather Parameters**: Convert temperature, pressure, humidity, wind speed, or combined weather metrics to music
- **Smart Musical Suggestions**: Automatically suggests musical keys and tempo based on weather conditions
- **Looping Support**: Generated weather-based sequences loop indefinitely until stopped

## Getting Started

### 1. Get a Free API Key
1. Visit [OpenWeatherMap](https://openweathermap.org/api)
2. Sign up for a free account
3. Go to "API Keys" section
4. Copy your API key

### 2. Using Weather Data in the Application
1. Launch the TNI Synthesizer Suite
2. Click "Data-to-MIDI Generator"
3. In the "Real Weather Data" section:
   - **City**: Enter any city name (e.g., "London", "Tokyo", "New York")
   - **Parameter**: Choose which weather aspect to convert to music:
     - **Temperature**: Creates melodies based on temperature variations
     - **Pressure**: Uses atmospheric pressure changes for musical patterns
     - **Humidity**: Converts humidity levels to musical sequences
     - **Wind Speed**: Maps wind speed to musical intensity
     - **Combined**: Uses all weather parameters for complex compositions
   - **API Key**: Paste your OpenWeatherMap API key
4. Click "Fetch Live Weather" to download real weather data
5. Click "Generate MIDI" to create music from the weather data
6. Click "Play" to hear your weather-based composition (loops automatically)

### 3. Weather-to-Music Examples

**Sunny Day (High Temperature)**
- Results in bright, major-key compositions
- Faster tempos (warm weather = energetic music)
- Higher musical pitches

**Stormy Weather (High Wind Speed, Changing Pressure)**
- Creates dramatic, dynamic musical sequences
- Minor keys for intense weather
- Rapid tempo changes following weather patterns

**Calm, Humid Evening**
- Gentle, flowing melodies
- Moderate tempo
- Smooth transitions between notes

## Sample Weather Data
If you don't have an API key yet, you can still explore weather-based music generation:
- Click "Sample Weather" to load realistic weather pattern data
- The system will generate synthetic weather data that simulates real conditions
- Perfect for testing and demonstration purposes

## Advanced Features

### Automatic Musical Suggestions
The system analyzes weather conditions and automatically suggests:
- **Musical Key**: Minor keys for stormy/cold weather, major keys for pleasant conditions
- **Tempo**: Faster for windy/dynamic weather, slower for calm conditions
- **Style**: Musical progressions that match the weather mood

### Weather Analysis
Hover over the status label after fetching weather data to see detailed weather information including:
- Current conditions and forecasts
- Weather intensity metrics
- Suggested musical parameters

## Tips for Best Results
1. **Try Different Cities**: Different climates create different musical styles
2. **Experiment with Parameters**: Each weather parameter creates unique musical patterns
3. **Use Forecast Data**: The API fetches multiple time points, creating longer musical sequences
4. **Combine with Other Features**: Use weather data as a starting point, then adjust tempo, instruments, and add bass lines or drums

## Troubleshooting
- **"Invalid API Key"**: Check that your OpenWeatherMap API key is correct and active
- **"City not found"**: Try different city name formats (e.g., "New York" vs "New York City")
- **"Using sample weather data"**: This appears when no API key is provided - sample data will be used instead

## Privacy & Data Usage
- Weather data is fetched directly from OpenWeatherMap
- No personal data is stored or transmitted
- API calls are made only when you click "Fetch Live Weather"
- Your API key is only used for the current session

---

**Start creating music from the weather around you!** 🌤️🎵