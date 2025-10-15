---
layout: page
title: Getting Started
permalink: /getting-started/
---

# Getting Started with TNI Synthesizer Suite

This guide will help you install, configure, and start using TNI Synthesizer Suite for data-driven music generation.

## System Requirements

### Minimum Requirements
- **Java**: Version 11 or higher
- **Operating System**: Windows 10, macOS 10.14, or Linux with GUI support
- **Memory**: 512 MB RAM available
- **Storage**: 50 MB free disk space

### Recommended Requirements
- **Java**: Version 17 or higher
- **Memory**: 1 GB RAM available
- **Audio**: MIDI-compatible audio system or soundcard
- **Internet**: For weather API integration (optional)

## Installation Methods

### Method 1: Pre-built Release (Recommended)

1. **Download the latest release**
   - Visit the [GitHub Releases page](https://github.com/differentJason/TNI-1/releases)
   - Download the `tni-synthesizer-1.0.0.jar` file

2. **Verify Java installation**
   ```bash
   java -version
   ```
   You should see Java 11 or higher

3. **Run the application**
   ```bash
   java -jar tni-synthesizer-1.0.0.jar
   ```

### Method 2: Build from Source

1. **Clone the repository**
   ```bash
   git clone https://github.com/differentJason/TNI-1.git
   cd TNI-1
   ```

2. **Install Maven** (if not already installed)
   - **macOS**: `brew install maven`
   - **Windows**: Download from [Maven website](https://maven.apache.org/download.cgi)
   - **Linux**: `sudo apt install maven` or equivalent

3. **Build the project**
   ```bash
   mvn clean compile
   ```

4. **Run the application**
   ```bash
   mvn exec:java
   ```

## First Launch

When you first launch TNI Synthesizer Suite, you'll see the main launcher window:

### 1. Choose Your Module
- **Simple MIDI Synthesizer**: Interactive piano keyboard with instruments
- **Data-to-MIDI Generator**: Convert numerical data to music
- **Advanced Features**: Coming soon (JSyn-based synthesizers)

### 2. Test Audio Output
1. Click "Simple MIDI Synthesizer"
2. Click any piano key to test audio
3. If no sound: Check system volume and MIDI settings

### 3. Verify Installation
- All buttons should be clickable
- No error messages should appear
- Audio should play when keys are pressed

## Quick Start Tutorial

### Playing Your First Notes

1. **Launch the MIDI Synthesizer**
   - Click "Simple MIDI Synthesizer" from the main menu
   - The piano keyboard interface will open

2. **Select an Instrument**
   - Use the "Instrument" dropdown to choose from 16 MIDI instruments
   - Try "Acoustic Grand Piano" for a familiar sound

3. **Play Notes**
   - Click piano keys with your mouse
   - Use computer keyboard keys (A, S, D, F, etc.) for quick playing
   - Adjust octave with the octave controls

4. **Try Scales and Chords**
   - Click "Play C Major Scale" to hear an ascending scale
   - Click "Play C Major Chord" to hear a chord

### Your First Data-to-Music Generation

1. **Open the Data Generator**
   - Return to main menu and click "Data-to-MIDI Generator"

2. **Load Sample Data**
   - Click "Stock Data" for stock price sample data
   - Click "Sample Weather" for weather pattern data
   - Click "Random Walk" for generated random data

3. **Generate Music**
   - Click "Generate MIDI" to create music from the data
   - Adjust tempo, style, and options as desired

4. **Play and Loop**
   - Click "Play" to hear your generated composition
   - The music will loop continuously until you click "Stop"

## Common First-Run Issues

### No Audio Output
**Problem**: No sound when clicking piano keys

**Solutions**:
1. Check system volume levels
2. Verify Java has audio permissions
3. Try different audio output devices
4. Restart the application
5. On macOS: Grant microphone permissions if prompted

### Application Won't Start
**Problem**: Double-clicking JAR file doesn't work

**Solutions**:
1. Use command line: `java -jar tni-synthesizer-1.0.0.jar`
2. Check Java installation: `java -version`
3. Install/update Java if needed
4. Try running as administrator (Windows)

### Memory Issues
**Problem**: Application runs slowly or crashes

**Solutions**:
1. Increase Java heap size: `java -Xmx1g -jar tni-synthesizer-1.0.0.jar`
2. Close other applications to free memory
3. Check available system memory

## Next Steps

Once you have TNI Synthesizer Suite running:

1. **[Explore the User Guide](user-guide.html)** - Learn all features in detail
2. **[Set up Weather API](weather-api.html)** - Connect to live weather data
3. **[Try Advanced Features](troubleshooting.html)** - Troubleshoot any issues

## Development Setup (Optional)

For developers who want to modify or extend the application:

### IDE Setup
1. **Import Maven Project**
   - Open IntelliJ IDEA, Eclipse, or VS Code
   - Import the project as a Maven project

2. **Configure Java**
   - Set project JDK to Java 11 or higher
   - Ensure Maven is properly configured

3. **Run from IDE**
   - Main class: `com.tni.synthesizer.SimpleSynthesizerApplication`
   - No additional JVM arguments needed

### Building Distribution
```bash
# Create executable JAR with dependencies
mvn clean package

# JAR file will be in target/ directory
ls target/tni-synthesizer-*.jar
```

---

**Ready to make music?** Continue to the **[User Guide](user-guide.html)** for detailed feature documentation.