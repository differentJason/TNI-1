---
layout: page
title: Weather API Integration
permalink: /weather-api/
---

# Weather API Integration Guide

Learn how to connect TNI Synthesizer Suite to live weather data for real-time music generation.

## Overview

The Weather API integration transforms real meteorological data into musical compositions. Using OpenWeatherMap's API, you can fetch current weather conditions or forecasts from any city worldwide and convert them into unique musical sequences.

## Setup Process

### Step 1: Get OpenWeatherMap API Key

1. **Create Account**
   - Visit [OpenWeatherMap](https://openweathermap.org/api)
   - Click "Sign Up" and create a free account
   - Verify your email address

2. **Access API Keys**
   - Log into your OpenWeatherMap account
   - Navigate to "My API Keys" section
   - Copy your default API key
   - **Note**: New keys may take up to 2 hours to activate

3. **API Key Limits (Free Tier)**
   - 1,000 API calls per day
   - 60 calls per minute
   - Current weather and 5-day forecast access

### Step 2: Configure TNI Synthesizer Suite

1. **Open Data-to-MIDI Generator**
   - Launch TNI Synthesizer Suite
   - Click "Data-to-MIDI Generator"

2. **Locate Weather Section**
   - Find "Real Weather Data" panel (light blue background)
   - Contains city input, parameter selection, and API key fields

3. **Enter Configuration**
   - **City**: Enter target city name
   - **Parameter**: Choose weather data type
   - **API Key**: Paste your OpenWeatherMap key

## Weather Parameters

### Temperature
- **Data Source**: Air temperature in Celsius
- **Musical Mapping**: Higher temperatures → higher pitches
- **Range**: Typically -40°C to +50°C
- **Best For**: Melodic composition, seasonal variations

### Atmospheric Pressure
- **Data Source**: Barometric pressure in hectopascals (hPa)
- **Musical Mapping**: Pressure changes → chord progressions
- **Range**: Typically 980-1050 hPa
- **Best For**: Harmonic progressions, weather system tracking

### Humidity
- **Data Source**: Relative humidity percentage
- **Musical Mapping**: Humidity levels → musical dynamics
- **Range**: 0-100%
- **Best For**: Ambient textures, atmospheric music

### Wind Speed
- **Data Source**: Wind velocity in meters per second
- **Musical Mapping**: Wind speed → tempo and rhythm intensity
- **Range**: 0-40+ m/s
- **Best For**: Rhythmic patterns, dynamic compositions

### Combined Weather Index
- **Data Source**: Weighted combination of all parameters
- **Musical Mapping**: Complex multi-layered compositions
- **Calculation**: Temperature × 0.4 + Pressure/50 + Humidity × 0.3 + WindSpeed × 2
- **Best For**: Rich, complex musical arrangements

## City Name Formats

### Supported Formats
- **Simple Names**: "London", "Paris", "Tokyo"
- **City, Country**: "New York, US", "London, UK"
- **City, State, Country**: "Austin, TX, US"

### Troubleshooting City Names
If "City not found" error occurs:
- Try different name formats
- Use major city names within regions
- Check spelling carefully
- Try nearby major cities

### Popular Cities for Musical Exploration
- **Tropical**: "Miami", "Singapore", "Rio de Janeiro"
- **Arctic**: "Reykjavik", "Anchorage", "Murmansk"
- **Desert**: "Phoenix", "Dubai", "Alice Springs"
- **Monsoon**: "Mumbai", "Bangkok", "Dhaka"
- **Mountainous**: "Denver", "Zurich", "Cusco"

## Musical Interpretation

### Automatic Suggestions

**Musical Key Selection**
- **Sunny/Warm Weather** → Major keys (C Major, G Major, D Major)
- **Stormy/Cold Weather** → Minor keys (A Minor, D Minor, E Minor)
- **Mild Weather** → Balanced progressions

**Tempo Recommendations**
- **Calm Weather** → Slower tempo (60-100 BPM)
- **Windy Weather** → Faster tempo (120-160 BPM)
- **Storm Systems** → Variable tempo with dynamic changes

### Weather-to-Music Examples

**Sunny Summer Day (High Temperature, Low Humidity)**
```
Temperature: 28°C → Bright major scale passages
Humidity: 45% → Clear, crisp note articulation
Wind: 3 m/s → Gentle, flowing rhythm
Result: Uplifting, cheerful composition
```

**Approaching Storm (Dropping Pressure, Rising Wind)**
```
Pressure: 995 hPa → Dark, minor chord progressions
Wind Speed: 12 m/s → Intense, driving rhythms
Temperature: 22°C → Mid-range melodic content
Result: Dramatic, building musical tension
```

**Foggy Morning (High Humidity, Low Visibility)**
```
Humidity: 92% → Soft, ambient textures
Temperature: 8°C → Lower register melodies
Wind: 1 m/s → Slow, gentle pacing
Result: Mysterious, atmospheric composition
```

## Advanced Features

### Forecast Data Integration

**5-Day Forecast**
- Provides 40 data points (3-hour intervals)
- Creates longer musical compositions
- Shows weather evolution over time
- Perfect for extended ambient pieces

**Data Processing**
- Smoothing algorithms reduce noise
- Trend analysis identifies patterns
- Statistical normalization ensures musical coherence

### Real-Time Updates

**Live Weather Monitoring**
- Fetch current conditions repeatedly
- Create evolving soundscapes
- Perfect for installations or live performance

**Automatic Refresh**
- Set up periodic data fetching
- Continuously evolving compositions
- Reflects real-time weather changes

## Integration Workflow

### Basic Workflow
1. **Select City** → Choose location of interest
2. **Choose Parameter** → Pick weather aspect for musical focus
3. **Fetch Data** → Download live weather information
4. **Generate Music** → Convert weather data to MIDI
5. **Play & Loop** → Enjoy continuous weather-based music

### Advanced Workflow
1. **Weather Analysis** → Study forecast patterns
2. **Parameter Selection** → Choose parameter matching desired mood
3. **Musical Configuration** → Adjust tempo, key, instruments
4. **Generation & Refinement** → Create and refine composition
5. **Export & Share** → Save MIDI for further production

## API Error Handling

### Common Error Messages

**"Invalid API Key"**
- **Cause**: Incorrect or expired API key
- **Solution**: Verify key, wait for activation, or regenerate

**"City not found"**
- **Cause**: Unrecognized city name format
- **Solution**: Try alternative city names or nearby locations

**"API request failed"**
- **Cause**: Network issues or API service problems
- **Solution**: Check internet connection, try again later

**"Using sample weather data"**
- **Cause**: No API key provided or API unavailable
- **Solution**: Normal fallback behavior, provides realistic sample data

### Fallback Behavior

When API calls fail, the system automatically:
1. **Generates Sample Data**: Realistic weather patterns
2. **Maintains Functionality**: All features remain available
3. **Provides Feedback**: Clear status messages
4. **Enables Retry**: Easy to attempt API connection again

## Privacy and Usage

### Data Privacy
- **No Storage**: Weather data not saved permanently
- **Session Only**: API key used only during current session
- **Direct Connection**: Data fetched directly from OpenWeatherMap
- **No Tracking**: No user behavior tracking or analytics

### API Usage Guidelines
- **Rate Limits**: Respect API call limits
- **Key Security**: Don't share API keys publicly
- **Terms of Service**: Follow OpenWeatherMap terms
- **Attribution**: Credit OpenWeatherMap when sharing compositions

## Troubleshooting

### Performance Issues
- **Slow Response**: Check internet connection speed
- **Timeout Errors**: Try again with stable connection
- **Memory Usage**: Large forecast data may require more RAM

### Musical Quality
- **Repetitive Patterns**: Try different weather parameters
- **Harsh Sounds**: Adjust tempo and style settings
- **Lack of Variation**: Use Combined parameter for complexity

### Best Practices
1. **Test with Sample Data First**: Learn interface before using API
2. **Start with Single Parameter**: Master one parameter before combining
3. **Experiment with Cities**: Different climates produce different music
4. **Save Interesting Results**: Export MIDI files for later use

---

**Ready to make music from weather?** Get your free API key from [OpenWeatherMap](https://openweathermap.org/api) and start exploring global weather patterns through music!