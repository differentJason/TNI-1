package com.tni.synthesizer.generative;

import com.tni.synthesizer.weather.WeatherService;
import com.tni.synthesizer.weather.WeatherData;
import com.tni.synthesizer.weather.WeatherServiceException;
import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Data-to-MIDI Generator that converts numerical data arrays into musical MIDI sequences.
 * Analyzes data patterns to determine appropriate chord progressions, rhythms, and melodies.
 */
public class DataToMidiGenerator extends JFrame {
    private Synthesizer midiSynth;
    private Sequence sequence;
    private Sequencer sequencer;
    private WeatherService weatherService;
    
    // Chord progressions for different data patterns
    private static final int[][] MAJOR_PROGRESSION = {
        {60, 64, 67}, {65, 69, 72}, {67, 71, 74}, {60, 64, 67} // C, F, G, C
    };
    private static final int[][] MINOR_PROGRESSION = {
        {60, 63, 67}, {65, 68, 72}, {67, 70, 74}, {60, 63, 67} // Cm, Fm, Gm, Cm
    };
    private static final int[][] BLUES_PROGRESSION = {
        {60, 64, 67}, {60, 64, 67}, {65, 69, 72}, {60, 64, 67} // I, I, IV, I
    };
    
    // Scale definitions
    private static final int[] MAJOR_SCALE = {0, 2, 4, 5, 7, 9, 11};
    private static final int[] MINOR_SCALE = {0, 2, 3, 5, 7, 8, 10};
    private static final int[] PENTATONIC_SCALE = {0, 2, 4, 7, 9};
    
    // UI Components
    private JTextArea dataInput;
    private JComboBox<String> styleCombo;
    private JSlider tempoSlider;
    private JCheckBox bassLineCheck, drumsCheck, loopCheck;
    private JButton generateButton, playButton, stopButton, saveButton;
    private JTextField cityField, apiKeyField;
    private JComboBox<WeatherService.WeatherParameter> weatherParamCombo;
    private JButton fetchWeatherButton;
    private JLabel statusLabel;
    
    // Current settings
    private int currentTempo = 120;
    private String currentStyle = "Major";
    private boolean includeBassLine = true;
    private boolean includeDrums = false;
    private boolean enableLooping = true;
    
    public DataToMidiGenerator() throws MidiUnavailableException {
        setupMidi();
        setupWeatherService();
        setupUI();
    }
    
    /**
     * Initialize weather service
     */
    private void setupWeatherService() {
        weatherService = new WeatherService();
    }
    
    /**
     * Initialize MIDI system
     */
    private void setupMidi() throws MidiUnavailableException {
        midiSynth = MidiSystem.getSynthesizer();
        midiSynth.open();
        
        sequencer = MidiSystem.getSequencer();
        sequencer.open();
        
        // Set up instruments
        try {
            Soundbank soundbank = midiSynth.getDefaultSoundbank();
            if (soundbank != null) {
                midiSynth.loadAllInstruments(soundbank);
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load soundbank");
        }
    }
    
    /**
     * Create the user interface
     */
    private void setupUI() {
        setTitle("Data to MIDI Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Input panel
        add(createInputPanel(), BorderLayout.CENTER);
        
        // Control panel
        add(createControlPanel(), BorderLayout.EAST);
        
        // Button panel
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Create data input panel
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Data Input"));
        
        dataInput = new JTextArea(10, 40);
        dataInput.setText("1.0, 2.5, 3.2, 2.8, 4.1, 3.7, 5.2, 4.8, 3.9, 2.1, 1.5, 2.3\n" +
                         "Enter comma-separated numbers, or multiple lines for multi-dimensional data");
        dataInput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(dataInput);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Sample data buttons
        JPanel samplePanel = new JPanel(new FlowLayout());
        JButton sampleButton1 = new JButton("Pitcher/Catcher");
        JButton sampleButton2 = new JButton("Sample Weather");
        JButton sampleButton3 = new JButton("Random Walk");
        
        sampleButton1.addActionListener(e -> loadSampleData("baseball"));
        sampleButton2.addActionListener(e -> loadSampleData("weather"));
        sampleButton3.addActionListener(e -> loadSampleData("random"));
        
        samplePanel.add(sampleButton1);
        samplePanel.add(sampleButton2);
        samplePanel.add(sampleButton3);
        
        // Weather API section
        JPanel weatherPanel = createWeatherPanel();
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(samplePanel, BorderLayout.NORTH);
        bottomPanel.add(weatherPanel, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create weather API panel
     */
    private JPanel createWeatherPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Real Weather Data"));
        panel.setBackground(new Color(240, 248, 255)); // Light blue background
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // City input
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1;
        cityField = new JTextField("New York", 12);
        cityField.setToolTipText("Enter city name (e.g., 'London', 'Tokyo', 'New York')");
        panel.add(cityField, gbc);
        
        // Weather parameter selection
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Parameter:"), gbc);
        gbc.gridx = 3;
        weatherParamCombo = new JComboBox<>(WeatherService.WeatherParameter.values());
        weatherParamCombo.setSelectedItem(WeatherService.WeatherParameter.TEMPERATURE);
        panel.add(weatherParamCombo, gbc);
        
        // API Key input
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("API Key:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        apiKeyField = new JTextField(20);
        apiKeyField.setToolTipText("Enter your OpenWeatherMap API key (get free key at openweathermap.org)");
        panel.add(apiKeyField, gbc);
        
        // Fetch button
        gbc.gridx = 3; gbc.gridwidth = 1;
        fetchWeatherButton = new JButton("Fetch Live Weather");
        fetchWeatherButton.setBackground(new Color(70, 130, 180));
        fetchWeatherButton.setForeground(Color.WHITE);
        fetchWeatherButton.addActionListener(e -> fetchRealWeatherData());
        panel.add(fetchWeatherButton, gbc);
        
        return panel;
    }
    
    /**
     * Create control panel
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Generation Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Style selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Musical Style:"), gbc);
        gbc.gridy = 1;
        styleCombo = new JComboBox<>(new String[]{"Major", "Minor", "Blues"});
        styleCombo.addActionListener(e -> currentStyle = (String) styleCombo.getSelectedItem());
        panel.add(styleCombo, gbc);
        
        // Tempo control
        gbc.gridy = 2;
        panel.add(new JLabel("Tempo (BPM):"), gbc);
        gbc.gridy = 3;
        tempoSlider = new JSlider(60, 180, currentTempo);
        tempoSlider.setMajorTickSpacing(30);
        tempoSlider.setPaintTicks(true);
        tempoSlider.setPaintLabels(true);
        tempoSlider.addChangeListener(e -> currentTempo = tempoSlider.getValue());
        panel.add(tempoSlider, gbc);
        
        // Options
        gbc.gridy = 4;
        bassLineCheck = new JCheckBox("Include Bass Line", includeBassLine);
        bassLineCheck.addActionListener(e -> includeBassLine = bassLineCheck.isSelected());
        panel.add(bassLineCheck, gbc);
        
        gbc.gridy = 5;
        drumsCheck = new JCheckBox("Include Drums", includeDrums);
        drumsCheck.addActionListener(e -> includeDrums = drumsCheck.isSelected());
        panel.add(drumsCheck, gbc);
        
        gbc.gridy = 6;
        loopCheck = new JCheckBox("Loop Continuously", enableLooping);
        loopCheck.addActionListener(e -> enableLooping = loopCheck.isSelected());
        panel.add(loopCheck, gbc);
        
        return panel;
    }
    
    /**
     * Create button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        generateButton = new JButton("Generate MIDI");
        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        saveButton = new JButton("Save MIDI");
        
        generateButton.addActionListener(e -> generateMidiFromData());
        playButton.addActionListener(e -> playSequence());
        stopButton.addActionListener(e -> stopSequence());
        saveButton.addActionListener(e -> saveMidiFile());
        
        // Initially disable play/save buttons
        playButton.setEnabled(false);
        saveButton.setEnabled(false);
        
        panel.add(generateButton);
        panel.add(playButton);
        panel.add(stopButton);
        panel.add(saveButton);
        
        // Status label
        statusLabel = new JLabel("Ready - Enter data and click Generate MIDI");
        panel.add(statusLabel);
        
        return panel;
    }
    
    /**
     * Load sample data patterns
     */
    private void loadSampleData(String type) {
        String sampleData = "";
        
        switch (type) {
            case "baseball":
                sampleData = generateBaseballStatistics();
                break;
            case "weather":
                sampleData = "22.5, 24.1, 26.3, 28.7, 25.4, 23.8, 21.2, 19.6, 18.3, 20.1, 22.9, 25.6, 27.8, 29.2, 26.7, 24.3";
                break;
            case "random":
                // Generate random walk data
                Random rand = new Random();
                StringBuilder sb = new StringBuilder();
                double value = 50.0;
                for (int i = 0; i < 20; i++) {
                    value += (rand.nextDouble() - 0.5) * 10;
                    if (i > 0) sb.append(", ");
                    sb.append(String.format("%.1f", value));
                }
                sampleData = sb.toString();
                break;
        }
        
        dataInput.setText(sampleData);
    }
    
    /**
     * Generate historical baseball pitcher and catcher statistics data
     * Selects a random historical year and generates realistic baseball stats
     * focusing on the dynamic relationship between pitchers and catchers
     */
    private String generateBaseballStatistics() {
        Random rand = new Random();
        
        // Select a random historical year with famous pitcher-catcher combinations
        String[] historicalSeasons = {
            "1954 - NY Giants (Sal Maglie/Wes Westrum)", 
            "1961 - NY Yankees (Whitey Ford/Elston Howard)",
            "1975 - Cincinnati Reds (Don Gullett/Johnny Bench)", 
            "1986 - NY Mets (Dwight Gooden/Gary Carter)",
            "1995 - Atlanta Braves (Greg Maddux/Javy Lopez)", 
            "1998 - NY Yankees (David Wells/Jorge Posada)",
            "2001 - Arizona D-backs (Randy Johnson/Damian Miller)", 
            "2004 - Boston Red Sox (Pedro Martinez/Jason Varitek)",
            "2016 - Chicago Cubs (Jake Arrieta/David Ross)", 
            "2019 - Houston Astros (Gerrit Cole/Martin Maldonado)",
            "2021 - Atlanta Braves (Max Fried/Travis d'Arnaud)"
        };
        
        String selectedSeason = historicalSeasons[rand.nextInt(historicalSeasons.length)];
        
        StringBuilder sb = new StringBuilder();
        
        // Generate game-by-game pitcher-catcher performance metrics
        // Combining ERA, WHIP, batting avg, caught stealing %, etc.
        for (int game = 1; game <= 20; game++) {
            if (game > 1) sb.append(", ");
            
            double gameMetric;
            switch (game % 4) {
                case 0: // Pitcher ERA for this game (scaled to 200-600 range)
                    gameMetric = (2.0 + rand.nextDouble() * 4.0) * 100; // 200-600
                    break;
                case 1: // Catcher batting average (scaled to 150-350 range)
                    gameMetric = (0.150 + rand.nextDouble() * 0.200) * 1000; // 150-350
                    break;
                case 2: // Combined pitcher-catcher chemistry metric (100-300 range)
                    // Simulates how well they work together (strikeouts, passed balls, etc.)
                    gameMetric = 100 + rand.nextDouble() * 200; // 100-300
                    break;
                case 3: // Defensive efficiency rating (50-150 range)
                    // Caught stealing %, pickoffs, pitch framing, etc.
                    gameMetric = 50 + rand.nextDouble() * 100; // 50-150
                    break;
                default:
                    gameMetric = 100 + rand.nextDouble() * 100;
            }
            
            sb.append(String.format("%.1f", gameMetric));
        }
        
        // Update status to show what season's data was generated
        statusLabel.setText("Generated " + selectedSeason + " pitcher/catcher statistics");
        
        return sb.toString();
    }
    
    /**
     * Fetch real weather data from API
     */
    private void fetchRealWeatherData() {
        String cityName = cityField.getText().trim();
        String apiKey = apiKeyField.getText().trim();
        WeatherService.WeatherParameter selectedParam = (WeatherService.WeatherParameter) weatherParamCombo.getSelectedItem();
        
        if (cityName.isEmpty()) {
            statusLabel.setText("Error: Please enter a city name");
            return;
        }
        
        // Update weather service with API key if provided
        if (!apiKey.isEmpty()) {
            try {
                weatherService.close();
            } catch (Exception e) {
                // Ignore close errors
            }
            weatherService = new WeatherService(apiKey);
        }
        
        // Show loading status
        statusLabel.setText("Fetching weather data for " + cityName + "...");
        fetchWeatherButton.setEnabled(false);
        
        // Fetch weather data in background thread
        SwingWorker<List<WeatherData>, Void> worker = new SwingWorker<List<WeatherData>, Void>() {
            @Override
            protected List<WeatherData> doInBackground() throws Exception {
                try {
                    // Try to get forecast data (more data points for better music)
                    return weatherService.getWeatherForecast(cityName);
                } catch (WeatherServiceException e) {
                    if (e.getMessage().contains("API key")) {
                        // If API key issue, fall back to sample data
                        statusLabel.setText("Using sample weather data (set API key for live data)");
                        return weatherService.generateSampleWeatherData();
                    } else {
                        throw e;
                    }
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<WeatherData> weatherDataList = get();
                    
                    // Convert weather data to numerical array
                    double[] weatherNumbers = weatherService.weatherToNumericalData(weatherDataList, selectedParam);
                    
                    // Format as comma-separated string
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < weatherNumbers.length; i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(String.format("%.1f", weatherNumbers[i]));
                    }
                    
                    // Set the data in the input field
                    dataInput.setText(sb.toString());
                    
                    // Show success status with weather info
                    if (!weatherDataList.isEmpty()) {
                        WeatherData firstReading = weatherDataList.get(0);
                        statusLabel.setText(String.format("Loaded %d weather readings from %s (%s: %.1f%s)", 
                            weatherDataList.size(), 
                            firstReading.getCityName(),
                            selectedParam.getDisplayName(),
                            weatherNumbers[0],
                            getParameterUnit(selectedParam)));
                        
                        // Auto-suggest musical settings based on weather
                        suggestMusicalSettings(firstReading);
                    }
                    
                } catch (Exception e) {
                    statusLabel.setText("Error fetching weather: " + e.getMessage());
                    System.err.println("Weather fetch error: " + e.getMessage());
                } finally {
                    fetchWeatherButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Get unit string for weather parameter
     */
    private String getParameterUnit(WeatherService.WeatherParameter param) {
        switch (param) {
            case TEMPERATURE: return "°C";
            case PRESSURE: return " hPa";
            case HUMIDITY: return "%";
            case WIND_SPEED: return " m/s";
            case COMBINED: return "";
            default: return "";
        }
    }
    
    /**
     * Suggest musical settings based on weather data
     */
    private void suggestMusicalSettings(WeatherData weatherData) {
        // Auto-suggest tempo based on weather
        int suggestedTempo = weatherData.getSuggestedTempo();
        tempoSlider.setValue(suggestedTempo);
        
        // Auto-suggest style based on weather intensity
        String suggestedKey = weatherData.getSuggestedMusicalKey();
        if (suggestedKey.contains("Minor")) {
            styleCombo.setSelectedItem("Minor");
        } else {
            styleCombo.setSelectedItem("Major");
        }
        
        // Show weather details in a tooltip or status
        statusLabel.setToolTipText(weatherData.toDetailedString());
    }
    
    /**
     * Generate MIDI sequence from input data
     */
    private void generateMidiFromData() {
        try {
            String inputText = dataInput.getText().trim();
            if (inputText.isEmpty()) {
                statusLabel.setText("Error: No data entered");
                return;
            }
            
            // Parse data
            double[] dataArray = parseDataInput(inputText);
            if (dataArray.length == 0) {
                statusLabel.setText("Error: Could not parse data");
                return;
            }
            
            // Generate sequence
            generateSequenceFromData(dataArray);
            
            // Enable play/save buttons
            playButton.setEnabled(true);
            saveButton.setEnabled(true);
            
            statusLabel.setText(String.format("Generated MIDI sequence from %d data points", dataArray.length));
            
        } catch (Exception e) {
            statusLabel.setText("Error generating MIDI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Parse input data from text
     */
    private double[] parseDataInput(String input) {
        List<Double> values = new ArrayList<>();
        
        // Split by lines and commas
        String[] lines = input.split("\\n");
        for (String line : lines) {
            if (line.trim().isEmpty() || line.contains("Enter comma-separated")) continue;
            
            String[] parts = line.split("[,\\s]+");
            for (String part : parts) {
                try {
                    double value = Double.parseDouble(part.trim());
                    values.add(value);
                } catch (NumberFormatException e) {
                    // Skip invalid numbers
                }
            }
        }
        
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * Generate MIDI sequence from data array
     */
    public void generateSequenceFromData(double[] dataArray) {
        try {
            // Create new MIDI sequence
            sequence = new Sequence(Sequence.PPQ, 480); // 480 ticks per quarter note
            Track track = sequence.createTrack();
            
            // Analyze data characteristics
            DataAnalysis analysis = analyzeData(dataArray);
            
            // Choose chord progression based on data trend and style
            int[][] chordProgression = getChordProgression(analysis);
            
            // Generate MIDI events
            long currentTick = 0;
            int beatsPerDataPoint = 1; // Each data point = 1 beat
            
            // Set tempo
            MetaMessage tempoMessage = new MetaMessage();
            int microsecondsPerQuarter = 60000000 / currentTempo;
            byte[] tempoData = new byte[3];
            tempoData[0] = (byte) ((microsecondsPerQuarter >> 16) & 0xFF);
            tempoData[1] = (byte) ((microsecondsPerQuarter >> 8) & 0xFF);
            tempoData[2] = (byte) (microsecondsPerQuarter & 0xFF);
            tempoMessage.setMessage(0x51, tempoData, 3);
            track.add(new MidiEvent(tempoMessage, 0));
            
            for (int i = 0; i < dataArray.length; i++) {
                double normalizedValue = (dataArray[i] - analysis.min) / (analysis.max - analysis.min);
                
                // Map data to musical parameters
                int velocity = (int)(normalizedValue * 80) + 47; // 47-127 velocity range
                int chordIndex = i % chordProgression.length;
                int[] chord = chordProgression[chordIndex];
                
                // Add chord notes (channel 0)
                for (int note : chord) {
                    addNoteEvent(track, 0, note, velocity, currentTick, 480 * beatsPerDataPoint);
                }
                
                // Add melody based on data value (channel 1)
                int[] scale = getScale();
                int scaleIndex = (int)(normalizedValue * (scale.length - 1));
                int melodyNote = chord[0] + 12 + scale[scaleIndex]; // Octave above root + scale note
                addNoteEvent(track, 1, melodyNote, velocity, currentTick, 240); // Shorter melody notes
                
                // Add bass line if enabled (channel 2)
                if (includeBassLine && i > 0) {
                    double slope = dataArray[i] - dataArray[i-1];
                    int bassNote = chord[0] - 12; // Octave below root
                    if (slope > 0) bassNote += 2; // Add tension for rising data
                    if (slope < 0) bassNote -= 1; // Add movement for falling data
                    
                    int bassVelocity = (int)(Math.abs(slope / (analysis.max - analysis.min)) * 60) + 50;
                    addNoteEvent(track, 2, bassNote, bassVelocity, currentTick, 480 * beatsPerDataPoint);
                }
                
                // Add drums if enabled (channel 9 - standard drum channel)
                if (includeDrums) {
                    // Kick drum on strong beats
                    if (i % 4 == 0) {
                        addNoteEvent(track, 9, 36, 100, currentTick, 120); // Kick
                    }
                    // Snare on off-beats
                    if (i % 4 == 2) {
                        addNoteEvent(track, 9, 38, 80, currentTick, 120); // Snare
                    }
                    // Hi-hat based on data variation
                    if (normalizedValue > 0.5) {
                        addNoteEvent(track, 9, 42, 60, currentTick, 120); // Closed hi-hat
                    }
                }
                
                currentTick += 480 * beatsPerDataPoint;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating MIDI sequence", e);
        }
    }
    
    /**
     * Add a note event to the track
     */
    private void addNoteEvent(Track track, int channel, int note, int velocity, long tick, long duration) {
        try {
            // Note on
            ShortMessage noteOn = new ShortMessage();
            noteOn.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
            track.add(new MidiEvent(noteOn, tick));
            
            // Note off
            ShortMessage noteOff = new ShortMessage();
            noteOff.setMessage(ShortMessage.NOTE_OFF, channel, note, 0);
            track.add(new MidiEvent(noteOff, tick + duration));
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get chord progression based on analysis and style
     */
    private int[][] getChordProgression(DataAnalysis analysis) {
        switch (currentStyle) {
            case "Minor":
                return MINOR_PROGRESSION;
            case "Blues":
                return BLUES_PROGRESSION;
            default:
                return MAJOR_PROGRESSION;
        }
    }
    
    /**
     * Get scale based on current style
     */
    private int[] getScale() {
        switch (currentStyle) {
            case "Minor":
                return MINOR_SCALE;
            case "Blues":
                return PENTATONIC_SCALE;
            default:
                return MAJOR_SCALE;
        }
    }
    
    /**
     * Analyze data characteristics
     */
    private DataAnalysis analyzeData(double[] data) {
        DataAnalysis analysis = new DataAnalysis();
        analysis.min = Arrays.stream(data).min().orElse(0);
        analysis.max = Arrays.stream(data).max().orElse(1);
        
        // Note: Additional analysis metrics could be added here if needed for future features
        
        return analysis;
    }
    
    /**
     * Play the generated sequence
     */
    private void playSequence() {
        if (sequence == null) {
            statusLabel.setText("No sequence to play - generate MIDI first");
            return;
        }
        
        try {
            sequencer.setSequence(sequence);
            
            // Set looping based on checkbox
            if (enableLooping) {
                sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
                statusLabel.setText("Playing MIDI sequence (looping indefinitely)...");
            } else {
                sequencer.setLoopCount(0); // Play once
                statusLabel.setText("Playing MIDI sequence (single play)...");
            }
            
            sequencer.start();
        } catch (InvalidMidiDataException e) {
            statusLabel.setText("Error playing sequence: " + e.getMessage());
        }
    }
    
    /**
     * Stop playback
     */
    private void stopSequence() {
        if (sequencer.isRunning()) {
            sequencer.stop();
            if (enableLooping) {
                statusLabel.setText("Looped playback stopped");
            } else {
                statusLabel.setText("Playback stopped");
            }
        }
    }
    
    /**
     * Save MIDI file
     */
    private void saveMidiFile() {
        if (sequence == null) {
            statusLabel.setText("No sequence to save - generate MIDI first");
            return;
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("generated_music.mid"));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                MidiSystem.write(sequence, 1, file);
                statusLabel.setText("MIDI file saved: " + file.getName());
            } catch (Exception e) {
                statusLabel.setText("Error saving file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Clean shutdown
     */
    public void shutdown() {
        if (sequencer != null && sequencer.isRunning()) {
            sequencer.stop();
            sequencer.close();
        }
        if (midiSynth != null) {
            midiSynth.close();
        }
        if (weatherService != null) {
            try {
                weatherService.close();
            } catch (Exception e) {
                System.err.println("Error closing weather service: " + e.getMessage());
            }
        }
    }
    
    /**
     * Data analysis helper class
     */
    private static class DataAnalysis {
        double min, max;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                DataToMidiGenerator generator = new DataToMidiGenerator();
                generator.setVisible(true);
                
                // Add shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(generator::shutdown));
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error initializing MIDI system: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}