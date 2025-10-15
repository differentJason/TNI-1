# TNI Synthesizer Suite

A comprehensive Java-based synthesizer with MIDI capabilities, data-to-music generation, and real-time weather API integration for creative music composition.

## 🎵 Features

- **Interactive MIDI Synthesizer**: Full 88-key piano interface with 16 instruments
- **Data-to-Music Generation**: Convert numerical data arrays into structured musical compositions  
- **Real-Time Weather Integration**: Transform live weather data from any city into unique musical sequences
- **Looping Compositions**: Generated sequences loop indefinitely for ambient music creation
- **Smart Musical Suggestions**: AI-powered recommendations for keys, tempo, and style based on data patterns

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.6+ (for building from source)
- OpenWeatherMap API key (free, for weather features)

### Running the Application

**Option 1: Pre-built JAR (Recommended)**
```bash
# Download latest release from GitHub Releases
java -jar tni-synthesizer-1.0.0.jar
```

**Option 2: Build from Source**
```bash
git clone https://github.com/differentJason/TNI-1.git
cd TNI-1
mvn clean compile
mvn exec:java
```

**Option 3: Build JAR Package**
```bash
# Create standalone executable JAR
mvn clean package
java -jar target/tni-synthesizer-1.0.0.jar
```

## 📖 Documentation

**Complete documentation is available at: [https://differentjason.github.io/TNI-1/](https://differentjason.github.io/TNI-1/)**

- **[Getting Started Guide](https://differentjason.github.io/TNI-1/getting-started/)** - Installation and setup
- **[User Manual](https://differentjason.github.io/TNI-1/user-guide/)** - Complete feature documentation  
- **[Weather API Setup](https://differentjason.github.io/TNI-1/weather-api/)** - Connect to live weather data
- **[Troubleshooting](https://differentjason.github.io/TNI-1/troubleshooting/)** - Common issues and solutions
- **[API Reference](https://differentjason.github.io/TNI-1/api-reference/)** - Technical documentation

## 🎹 What You Can Do

### Transform Data into Music
- Convert stock prices, sensor readings, or any numerical data into melodies
- Automatic chord progression generation based on data patterns
- Customizable tempo, key, and musical style

### Create Weather-Based Compositions
- Fetch live weather data from OpenWeatherMap API
- Convert temperature, pressure, humidity, and wind into musical parameters
- Generate unique compositions for any city in the world

### Play Music Interactively
- Use the full piano keyboard interface
- Switch between 16 different MIDI instruments
- Generate scales and chords in multiple keys

## 🌟 Recent Updates

- **Real Weather API Integration**: Connect to OpenWeatherMap for live weather-based music generation
- **Infinite Looping**: Generated sequences now loop continuously until stopped
- **Enhanced UI**: Improved interface with better weather parameter controls
- **Smart Suggestions**: Automatic musical parameter recommendations based on data analysis

## 🛠️ Technical Details

### Built With
- **Java**: Core application framework
- **Java MIDI System**: Audio synthesis and playback
- **Maven**: Build and dependency management
- **Gson**: JSON parsing for weather data
- **Apache HttpClient**: API communication
- **Swing**: User interface framework

### Architecture
```
com.tni.synthesizer/
├── SimpleSynthesizerApplication.java    # Main launcher
├── SimpleMidiSynthesizer.java           # Interactive MIDI keyboard
├── generative/
│   └── DataToMidiGenerator.java         # Data-to-music conversion
└── weather/
    ├── WeatherService.java              # Weather API client
    ├── WeatherData.java                 # Weather data model
    └── WeatherServiceException.java     # Exception handling
```

## 💡 Use Cases

- **Data Sonification**: Make numerical data audible and intuitive
- **Ambient Music Creation**: Generate endless background music from weather patterns
- **Educational Tool**: Learn about data patterns through musical representation
- **Creative Composition**: Use data as inspiration for musical ideas
- **Live Performance**: Real-time music generation for concerts and installations

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- OpenWeatherMap API for weather data
- Java MIDI system for audio capabilities
- Maven ecosystem for build management

---

**[📚 View Full Documentation →](https://differentjason.github.io/TNI-1/)**
