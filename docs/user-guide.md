---
layout: page
title: User Guide
permalink: /user-guide/
---

# TNI Synthesizer Suite User Guide

Complete documentation for all features and capabilities of TNI Synthesizer Suite.

## Main Interface Overview

The TNI Synthesizer Suite launcher provides access to different synthesizer modules:

### Main Menu Options

**Simple MIDI Synthesizer**
- Interactive piano keyboard interface
- Real-time MIDI playback
- Multiple instrument selection
- Scale and chord generation

**Data-to-MIDI Generator**
- Convert numerical data to music
- Weather API integration
- Customizable musical parameters
- Looping playback support

## Simple MIDI Synthesizer

### Piano Keyboard Interface

The piano keyboard features:
- **88 Keys**: Full piano range from A0 to C8
- **Visual Feedback**: Keys highlight when pressed
- **Mouse and Keyboard Input**: Click keys or use computer keyboard
- **Octave Controls**: Navigate different octave ranges

### Keyboard Shortcuts

**Piano Keys (mapped to computer keyboard)**:
- `A` = C note
- `W` = C# note  
- `S` = D note
- `E` = D# note
- `D` = E note
- `F` = F note
- And so on following piano layout...

**Octave Controls**:
- `+` or `=`: Increase octave
- `-`: Decrease octave

### Instrument Selection

Choose from 16 General MIDI instruments:

1. **Acoustic Grand Piano** - Classic piano sound
2. **Bright Acoustic Piano** - Brighter piano tone
3. **Electric Grand Piano** - Electric piano sound
4. **Honky-tonk Piano** - Ragtime-style piano
5. **Rhodes Piano** - Electric Rhodes sound
6. **Chorused Piano** - Piano with chorus effect
7. **Harpsichord** - Baroque harpsichord
8. **Clavinet** - Funk clavinet sound
9. **Acoustic Guitar (nylon)** - Classical guitar
10. **Acoustic Guitar (steel)** - Steel-string guitar
11. **Electric Guitar (jazz)** - Clean electric guitar
12. **Electric Guitar (clean)** - Clean electric tone
13. **Electric Guitar (muted)** - Palm-muted electric
14. **Overdriven Guitar** - Distorted electric guitar
15. **Distortion Guitar** - Heavy distortion
16. **Guitar Harmonics** - Harmonic guitar tones

### Musical Functions

**Scale Playback**
- Click "Play C Major Scale" to hear ascending C major scale
- Demonstrates melodic note progression
- Useful for testing instrument sounds

**Chord Playback**
- Click "Play C Major Chord" to hear C major triad
- Plays multiple notes simultaneously
- Demonstrates harmonic capabilities

## Data-to-MIDI Generator

### Data Input Methods

**Manual Data Entry**
- Enter comma-separated numbers in the text area
- Supports multiple lines for multi-dimensional data
- Example: `22.5, 24.1, 26.3, 28.7, 25.4`

**Sample Data Options**
- **Stock Data**: Simulated stock price movements
- **Sample Weather**: Pre-generated weather patterns
- **Random Walk**: Algorithmic random data generation

**Live Weather Data**
- Real-time weather API integration
- Fetch data from any city worldwide
- Multiple weather parameters available

### Weather API Integration

**Setup Requirements**
1. Free OpenWeatherMap API key
2. Internet connection
3. Valid city name

**Weather Parameters**
- **Temperature**: Creates melodies from temperature variations
- **Pressure**: Uses atmospheric pressure changes
- **Humidity**: Converts humidity levels to musical patterns
- **Wind Speed**: Maps wind intensity to musical dynamics
- **Combined**: Uses all parameters for complex compositions

**API Configuration**
1. Enter city name (e.g., "London", "Tokyo", "New York")
2. Select desired weather parameter
3. Enter OpenWeatherMap API key
4. Click "Fetch Live Weather"

### Musical Generation Settings

**Style Selection**
- **Major**: Bright, happy-sounding progressions
- **Minor**: Darker, more melancholic progressions
- **Blues**: Blues-based chord progressions

**Tempo Control**
- Range: 60-180 BPM
- Default: 120 BPM
- Affects playback speed of generated sequences

**Musical Options**
- **Include Bass Line**: Adds bass accompaniment
- **Include Drums**: Adds drum track (basic patterns)
- **Loop Continuously**: Enables infinite looping playback

### Data Analysis and Conversion

The system analyzes input data for:

**Statistical Properties**
- Minimum and maximum values
- Mean (average) values
- Volatility (variation)
- Trend direction (positive/negative)

**Musical Mapping**
- **Notes**: Data values mapped to musical notes
- **Chords**: Chord selection based on data trends
- **Rhythm**: Timing patterns from data sequences
- **Dynamics**: Volume changes from data variation

### Smart Musical Suggestions

Based on data analysis, the system suggests:

**Musical Key**
- Major keys for positive trends
- Minor keys for negative trends or high volatility
- Blues progressions for erratic data

**Tempo**
- Faster tempo for volatile/dynamic data
- Slower tempo for stable data
- Wind speed influences tempo suggestions

**Style Recommendations**
- Bright styles for high values
- Dark styles for low values
- Complex styles for multi-dimensional data

## Playback Controls

### Basic Controls

**Generate MIDI**
- Analyzes input data
- Creates MIDI sequence
- Enables playback controls

**Play Button**
- Starts sequence playback
- Loops continuously if enabled
- Shows playback status

**Stop Button**
- Stops current playback
- Resets playback position
- Clears looping state

**Save MIDI**
- Exports generated sequence to MIDI file
- Standard .mid format
- Compatible with other music software

### Looping Features

**Continuous Looping**
- Check "Loop Continuously" for infinite playback
- Seamless transitions between loop cycles
- Perfect for ambient music generation
- Stop button required to end playback

## Advanced Features

### Multi-Dimensional Data

**Input Format**
```
# Weather data with multiple parameters
22.5, 65, 1013.2    # Temperature, Humidity, Pressure
24.1, 62, 1012.8
26.3, 58, 1014.1
```

**Processing**
- Each dimension creates separate musical layers
- Harmonized based on statistical relationships
- More complex resulting compositions

### Data Preprocessing

**Automatic Scaling**
- Data values normalized to musical ranges
- Outliers handled gracefully
- Maintains relative relationships

**Pattern Recognition**
- Identifies recurring patterns
- Creates musical themes from patterns
- Develops variations on recognized themes

## Troubleshooting

### Common Issues

**No Audio Output**
- Verify system audio settings
- Check Java audio permissions
- Try different instruments
- Restart application

**Weather API Errors**
- Verify API key is correct
- Check internet connection
- Try different city names
- Use sample data as fallback

**Performance Issues**
- Close unnecessary applications
- Increase Java heap memory
- Use smaller data sets
- Disable drums/bass for better performance

### Error Messages

**"No sequence to play"**
- Generate MIDI sequence first
- Check that data was loaded properly

**"Invalid API key"**
- Verify OpenWeatherMap API key
- Check for extra spaces or characters

**"City not found"**
- Try different city name formats
- Use major city names
- Check spelling

## Tips for Best Results

### Data Selection
1. **Use meaningful data ranges**: Avoid all zeros or identical values
2. **Consider data resolution**: More data points create longer compositions
3. **Try different parameters**: Each weather parameter creates unique music
4. **Experiment with cities**: Different climates produce different musical styles

### Musical Settings
1. **Match style to data**: Use minor keys for declining trends
2. **Adjust tempo thoughtfully**: Faster tempo for dynamic data
3. **Enable looping**: Perfect for background/ambient music
4. **Try bass and drums**: Adds richness to simple melodies

### Creative Workflow
1. **Start with sample data**: Learn the interface
2. **Try weather from different cities**: Explore global musical variations
3. **Save interesting sequences**: Export MIDI files for further editing
4. **Combine with other tools**: Import MIDI into DAWs for enhancement

---

**Need help with specific features?** Check the **[Troubleshooting Guide](troubleshooting.html)** or **[Weather API Documentation](weather-api.html)**.