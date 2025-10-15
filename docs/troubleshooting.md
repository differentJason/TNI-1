---
layout: page
title: Troubleshooting
permalink: /troubleshooting/
---

# Troubleshooting Guide

Common issues and solutions for TNI Synthesizer Suite.

## Installation Issues

### Java Version Problems

**Issue**: "Unsupported Java version" or won't start
**Solution**:
```bash
# Check Java version
java -version

# Should show Java 11 or higher
# If not, install newer Java version
```

**Issue**: Application crashes on startup
**Solution**:
1. Try running with more memory: `java -Xmx1g -jar tni-synthesizer.jar`
2. Check Java installation: `java -version`
3. Try running from command line to see error messages

### Maven Build Issues

**Issue**: `mvn: command not found`
**Solution**:
- **macOS**: `brew install maven`
- **Windows**: Download from [Maven website](https://maven.apache.org/)
- **Linux**: `sudo apt install maven`

**Issue**: Compilation errors
**Solution**:
1. Clean build: `mvn clean compile`
2. Check Java version in IDE settings
3. Refresh Maven dependencies

## Audio Issues

### No Sound Output

**Issue**: No audio when playing notes or sequences
**Solutions**:

1. **Check System Audio**
   - Verify system volume is up
   - Test other audio applications
   - Check audio output device selection

2. **Java Audio Permissions**
   - On macOS: Grant microphone permissions to Java
   - On Windows: Check Windows audio permissions
   - Try running as administrator

3. **MIDI System Issues**
   ```bash
   # Test Java MIDI system
   java -cp . TestMIDI
   ```

4. **Audio Driver Problems**
   - Update audio drivers
   - Try different audio output devices
   - Restart audio services

### Audio Latency or Glitches

**Issue**: Delayed or choppy audio playback
**Solutions**:
1. Close other audio applications
2. Increase Java heap size
3. Check system performance
4. Try different audio buffer settings

## Weather API Issues

### API Key Problems

**Issue**: "Invalid API key" error
**Solutions**:
1. **Verify API Key**
   - Check for extra spaces or characters
   - Ensure key is from OpenWeatherMap
   - Wait up to 2 hours for new keys to activate

2. **Test API Key**
   ```bash
   # Test API key manually
   curl "https://api.openweathermap.org/data/2.5/weather?q=London&appid=YOUR_API_KEY"
   ```

3. **Check Account Status**
   - Verify OpenWeatherMap account is active
   - Check API call limits haven't been exceeded

### Network Connection Issues

**Issue**: "Failed to fetch weather data"
**Solutions**:
1. **Check Internet Connection**
   - Test other websites/applications
   - Try different network connection
   - Check firewall settings

2. **Proxy/Corporate Network**
   - Configure Java proxy settings
   - Contact IT department for API access
   - Try from different network location

3. **API Service Status**
   - Check [OpenWeatherMap status page](https://status.openweathermap.org/)
   - Try again later if service is down

### City Name Issues

**Issue**: "City not found" error
**Solutions**:
1. **Try Different Formats**
   - "London" instead of "London, England"
   - "New York" instead of "NYC"
   - Use major city names

2. **Alternative Cities**
   - Try nearby major cities
   - Use capital cities
   - Check spelling carefully

## Performance Issues

### Slow Application Performance

**Issue**: Application responds slowly or freezes
**Solutions**:
1. **Increase Memory**
   ```bash
   # Run with more memory
   java -Xmx2g -jar tni-synthesizer.jar
   ```

2. **Close Other Applications**
   - Free up system memory
   - Close unnecessary programs
   - Check system resource usage

3. **Reduce Data Complexity**
   - Use smaller datasets
   - Disable drums/bass for better performance
   - Lower tempo settings

### Memory Issues

**Issue**: "OutOfMemoryError" or crashes
**Solutions**:
1. **Increase Heap Size**
   ```bash
   java -Xmx1g -Xms512m -jar tni-synthesizer.jar
   ```

2. **System Requirements**
   - Ensure sufficient RAM available
   - Check virtual memory settings
   - Close memory-intensive applications

## Data Generation Issues

### No Music Generated

**Issue**: "Generate MIDI" produces no output
**Solutions**:
1. **Check Input Data**
   - Ensure data is properly formatted
   - Use comma-separated numbers
   - Try sample data first

2. **Verify Settings**
   - Check tempo and style settings
   - Ensure valid musical parameters
   - Try different generation options

### Poor Quality Music

**Issue**: Generated music sounds harsh or repetitive
**Solutions**:
1. **Adjust Musical Settings**
   - Try different musical styles
   - Adjust tempo to match data
   - Enable/disable bass and drums

2. **Data Quality**
   - Use data with good variation
   - Avoid all zeros or identical values
   - Try different weather parameters

3. **Parameter Tuning**
   - Experiment with different cities
   - Try combined weather parameters
   - Use longer data sequences

## User Interface Issues

### Window Display Problems

**Issue**: Windows too large/small or UI elements missing
**Solutions**:
1. **Display Settings**
   - Check system display scaling
   - Try different screen resolutions
   - Reset window positions

2. **Java UI Settings**
   ```bash
   # Try with different look and feel
   java -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel -jar tni-synthesizer.jar
   ```

### Button/Control Issues

**Issue**: Buttons don't respond or controls are disabled
**Solutions**:
1. **Wait for Operations**
   - Some operations take time to complete
   - Wait for background processes to finish
   - Check status messages

2. **Restart Application**
   - Close and reopen application
   - Clear any stuck operations

## Error Messages

### Common Error Messages and Solutions

**"No sequence to play - generate MIDI first"**
- Click "Generate MIDI" before "Play"
- Ensure data is loaded properly

**"Error: No data entered"**
- Enter numerical data in the text area
- Use sample data buttons for testing

**"Using sample weather data (set API key for live data)"**
- This is normal behavior when no API key is provided
- Sample data will be used instead of live weather

**"Compilation failure" (when building from source)**
- Check Java version (needs Java 11+)
- Run `mvn clean compile` to clean build
- Check Maven installation

## Getting Help

### Before Seeking Help
1. **Check this troubleshooting guide**
2. **Try the solutions listed above**
3. **Test with sample data first**
4. **Note exact error messages**

### Getting Support
1. **GitHub Issues**: Report bugs at [GitHub Issues](https://github.com/differentJason/TNI-1/issues)
2. **Include Details**:
   - Operating system and version
   - Java version (`java -version`)
   - Exact error messages
   - Steps to reproduce the problem

### Diagnostic Information
When reporting issues, please include:
```bash
# System information
java -version
mvn -version  # if building from source

# Operating system
uname -a  # Linux/macOS
systeminfo  # Windows
```

## Advanced Troubleshooting

### Debug Mode
Run with debug output:
```bash
java -Djava.util.logging.level=FINE -jar tni-synthesizer.jar
```

### Reset Configuration
If all else fails:
1. Close application completely
2. Clear any cached data
3. Restart with fresh configuration

### Testing Components Individually
1. Test MIDI system first with Simple MIDI Synthesizer
2. Test data input with sample data
3. Test weather API with simple city names
4. Combine features gradually

---

**Still having issues?** Create a detailed issue report at [GitHub Issues](https://github.com/differentJason/TNI-1/issues) with your system information and error messages.