package com.tni.synthesizer.generative;

import com.tni.synthesizer.weather.WeatherService;
import com.tni.synthesizer.weather.WeatherData;
import com.tni.synthesizer.weather.WeatherServiceException;
import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import com.tni.synthesizer.ui.HardwareStyleUI;
import com.tni.synthesizer.ui.SynthChannelStrip;
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
    
    // Advanced synthesizer components
    private AdvancedAudioEngine audioEngine;
    private PatternMatrix patternMatrix;
    private ImageToMusicSynthesizer imageToMusicSynth;
    private DAWIntegrationManager dawManager;
    
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
    
    // Advanced synthesizer UI components
    private JComboBox<SynthChannel.Waveform>[] channelWaveforms;
    private JSlider[] channelVolumes, channelPans;
    private JCheckBox[] channelEnabled;
    private SynthChannelStrip[] channelStrips; // Store references for oscilloscope updates

    private JLabel imageStatusLabel;
    
    // Effects UI components - removed (unused)
    
    // Current settings
    private int currentTempo = 120;
    private String currentStyle = "Major";
    private boolean includeBassLine = true;
    private boolean includeDrums = false;
    private boolean enableLooping = true;
    
    public DataToMidiGenerator() throws MidiUnavailableException {
        setupMidi();
        setupWeatherService();
        setupAdvancedSynthesizer();
        setupUI();
    }
    
    /**
     * Initialize advanced synthesizer components
     */
    private void setupAdvancedSynthesizer() {
        try {
            audioEngine = new AdvancedAudioEngine();
            patternMatrix = audioEngine.getPatternMatrix();
            dawManager = new DAWIntegrationManager();
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize advanced audio engine: " + e.getMessage());
            // Continue with basic MIDI functionality
        }
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
        setTitle("TNI Synthesizer Suite - Data to Music Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Main content panel with tabs
        JTabbedPane mainTabs = new JTabbedPane();
        
        // Combined synthesizer and data input tab
        JPanel mainSynthPanel = new JPanel(new BorderLayout());
        
        // Synthesizer controls at top
        if (audioEngine != null) {
            mainSynthPanel.add(createSynthesizerPanel(), BorderLayout.CENTER);
        }
        
        // Data input section at bottom - smaller and horizontal
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(createCompactInputPanel(), BorderLayout.CENTER);
        bottomPanel.add(createControlPanel(), BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Data Input"));
        
        mainSynthPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainTabs.addTab("Synthesizer", mainSynthPanel);
        
        // Other tabs
        if (audioEngine != null) {
            mainTabs.addTab("Image to Music", createImageToMusicPanel());
            mainTabs.addTab("DAW Integration", createDAWIntegrationPanel());
        }
        
        add(mainTabs, BorderLayout.CENTER);
        
        // Button panel
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    /**
     * Create compact data input panel for bottom of synthesizer view
     */
    private JPanel createCompactInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Smaller, more compact text area
        dataInput = new JTextArea(3, 60);  // Reduced height, increased width
        dataInput.setText("1.0, 2.5, 3.2, 2.8, 4.1, 3.7, 5.2, 4.8, 3.9, 2.1, 1.5, 2.3\n" +
                         "Enter data (each line feeds channels: 1 line→all 8, 2 lines→4 each, etc.)");
        dataInput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));  // Smaller font
        
        JScrollPane scrollPane = new JScrollPane(dataInput);
        scrollPane.setPreferredSize(new Dimension(600, 80));  // Compact size
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Compact sample buttons in one row
        JPanel samplePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton sampleButton1 = new JButton("Baseball");
        JButton sampleButton2 = new JButton("Weather");
        JButton sampleButton3 = new JButton("Random");
        
        // Smaller buttons
        sampleButton1.setPreferredSize(new Dimension(80, 25));
        sampleButton2.setPreferredSize(new Dimension(80, 25));
        sampleButton3.setPreferredSize(new Dimension(80, 25));
        
        sampleButton1.addActionListener(e -> loadSampleData("baseball"));
        sampleButton2.addActionListener(e -> loadSampleData("weather"));
        sampleButton3.addActionListener(e -> loadSampleData("random"));
        
        samplePanel.add(sampleButton1);
        samplePanel.add(sampleButton2);
        samplePanel.add(sampleButton3);
        
        // Add test button for multi-line data
        JButton multiLineTestBtn = new JButton("Multi-Line Test");
        multiLineTestBtn.setPreferredSize(new Dimension(100, 25));
        multiLineTestBtn.addActionListener(e -> loadMultiLineTestData());
        samplePanel.add(multiLineTestBtn);
        
        // Status labels in compact format
        imageStatusLabel = new JLabel("No image loaded");
        imageStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Status:"));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(new JLabel("Image:"));
        statusPanel.add(imageStatusLabel);
        
        panel.add(samplePanel, BorderLayout.WEST);
        panel.add(statusPanel, BorderLayout.EAST);
        
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
     * Load multi-line test data to demonstrate channel distribution
     */
    private void loadMultiLineTestData() {
        StringBuilder sb = new StringBuilder();
        sb.append("1.0, 2.5, 3.2, 2.8, 4.1, 3.7, 2.9\n"); // Line 1 - Bass frequencies
        sb.append("5.2, 4.8, 6.1, 5.5, 4.9, 6.3, 5.8\n"); // Line 2 - Mid frequencies  
        sb.append("7.1, 8.3, 7.7, 8.9, 7.4, 8.6, 7.9\n"); // Line 3 - High frequencies
        sb.append("2.3, 1.8, 3.1, 2.7, 1.9, 3.4, 2.6");   // Line 4 - Rhythm pattern
        
        dataInput.setText(sb.toString());
        statusLabel.setText("Loaded multi-line test data - each line feeds different channels");
    }
    

    
    /**
     * Create synthesizer control panel with hardware-style interface
     */
    private JPanel createSynthesizerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HardwareStyleUI.PANEL_BACKGROUND);
        
        // Create horizontal layout for vertical channel strips
        JPanel channelContainer = new JPanel();
        channelContainer.setLayout(new BoxLayout(channelContainer, BoxLayout.X_AXIS));
        channelContainer.setBackground(HardwareStyleUI.PANEL_BACKGROUND);
        channelContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        int maxChannels = audioEngine != null ? audioEngine.getMaxChannels() : 8;
        
        // Initialize arrays
        @SuppressWarnings("unchecked")
        JComboBox<SynthChannel.Waveform>[] waveforms = new JComboBox[maxChannels];
        channelWaveforms = waveforms;
        channelVolumes = new JSlider[maxChannels]; // Keep for compatibility, but won't use
        channelPans = new JSlider[maxChannels]; // Keep for compatibility, but won't use
        channelEnabled = new JCheckBox[maxChannels];
        channelStrips = new SynthChannelStrip[maxChannels]; // Store channel strip references
        
        // Create vertical channel strips
        for (int i = 0; i < maxChannels; i++) {
            SynthChannelStrip channelStrip = new SynthChannelStrip(i + 1, i);
            channelStrip.setAudioEngine(audioEngine); // Connect to audio engine
            channelStrips[i] = channelStrip; // Store reference for oscilloscope updates
            channelContainer.add(channelStrip);
            if (i < maxChannels - 1) {
                channelContainer.add(Box.createHorizontalStrut(5));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(channelContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(HardwareStyleUI.PANEL_BACKGROUND);
        scrollPane.getViewport().setBackground(HardwareStyleUI.PANEL_BACKGROUND);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Pattern matrix controls at bottom
        JPanel patternPanel = createPatternMatrixPanel();
        panel.add(patternPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create individual channel control row
     */
    private JPanel createChannelControlRow(int channelIndex) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBorder(BorderFactory.createTitledBorder("Channel " + (channelIndex + 1)));
        
        // Enable checkbox
        channelEnabled[channelIndex] = new JCheckBox("Enable", true);
        channelEnabled[channelIndex].addActionListener(e -> updateChannelEnabled(channelIndex));
        row.add(channelEnabled[channelIndex]);
        
        // Waveform selector
        row.add(new JLabel("Wave:"));
        channelWaveforms[channelIndex] = new JComboBox<>(SynthChannel.Waveform.values());
        channelWaveforms[channelIndex].addActionListener(e -> updateChannelWaveform(channelIndex));
        row.add(channelWaveforms[channelIndex]);
        
        // Volume control
        row.add(new JLabel("Vol:"));
        channelVolumes[channelIndex] = new JSlider(0, 100, 80);
        channelVolumes[channelIndex].setPreferredSize(new Dimension(100, 25));
        channelVolumes[channelIndex].addChangeListener(e -> updateChannelVolume(channelIndex));
        row.add(channelVolumes[channelIndex]);
        
        // Pan control
        row.add(new JLabel("Pan:"));
        channelPans[channelIndex] = new JSlider(-100, 100, 0);
        channelPans[channelIndex].setPreferredSize(new Dimension(100, 25));
        channelPans[channelIndex].addChangeListener(e -> updateChannelPan(channelIndex));
        row.add(channelPans[channelIndex]);
        
        return row;
    }
    
    /**
     * Create pattern matrix control panel
     */
    private JPanel createPatternMatrixPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Pattern Matrix"));
        
        JPanel controls = new JPanel(new FlowLayout());
        JButton playPatternButton = new JButton("Play Patterns");
        JButton stopPatternButton = new JButton("Stop Patterns");
        
        playPatternButton.addActionListener(e -> {
            if (patternMatrix != null) {
                patternMatrix.play();
            }
        });
        
        stopPatternButton.addActionListener(e -> {
            if (patternMatrix != null) {
                patternMatrix.stop();
            }
        });
        
        controls.add(playPatternButton);
        controls.add(stopPatternButton);
        
        panel.add(controls, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Effects panel methods removed - functionality not working
    
    // Channel update methods
    private void updateChannelEnabled(int channelIndex) {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null) {
                channel.setEnabled(channelEnabled[channelIndex].isSelected());
            }
        }
    }
    
    private void updateChannelWaveform(int channelIndex) {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null) {
                SynthChannel.Waveform waveform = (SynthChannel.Waveform) channelWaveforms[channelIndex].getSelectedItem();
                channel.setWaveform(waveform);
            }
        }
    }
    
    private void updateChannelVolume(int channelIndex) {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null) {
                float volume = channelVolumes[channelIndex].getValue() / 100.0f;
                channel.setVolume(volume);
            }
        }
    }
    
    private void updateChannelPan(int channelIndex) {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null) {
                float pan = channelPans[channelIndex].getValue() / 100.0f;
                channel.setPan(pan);
            }
        }
    }
    
    // updateEffectEnabled method removed - effects functionality not working
    
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
            
            // Parse data by lines for channel distribution
            double[][] dataLines = parseDataInputByLines(inputText);
            if (dataLines.length == 0) {
                statusLabel.setText("Error: Could not parse data");
                return;
            }
            
            // Generate improved sequence with varied patterns
            double[] flatData = parseDataInput(inputText);
            generateImprovedSequenceFromData(flatData, dataLines);
            
            // Enable play/save buttons
            playButton.setEnabled(true);
            saveButton.setEnabled(true);
            
            statusLabel.setText(String.format("Generated MIDI from %d lines, %d total points", 
                dataLines.length, flatData.length));
            
        } catch (Exception e) {
            statusLabel.setText("Error generating MIDI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate much more interesting and varied MIDI sequence
     */
    private void generateImprovedSequenceFromData(double[] dataArray, double[][] dataLines) {
        try {
            // Create new MIDI sequence
            sequence = new Sequence(Sequence.PPQ, 480);
            Track track = sequence.createTrack();
            
            // Analyze data characteristics
            DataAnalysis analysis = analyzeData(dataArray);
            
            // Set tempo based on data volatility
            int dynamicTempo = calculateDynamicTempo(analysis);
            setTempoMessage(track, dynamicTempo);
            
            long currentTick = 0;
            int beatsPerSection = 4; // 4 beats per section
            
            for (int i = 0; i < dataArray.length; i++) {
                // Generate varied musical content for each data point
                currentTick = generateMusicalSection(track, dataArray, dataLines, analysis, i, currentTick, beatsPerSection);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private int calculateDynamicTempo(DataAnalysis analysis) {
        // Base tempo on data range and variance
        double range = Math.abs(analysis.max - analysis.min);
        double volatility = range / Math.max(1.0, Math.abs(analysis.max));
        int tempo = (int)(90 + volatility * 60); // 90-150 BPM range
        return Math.max(80, Math.min(160, tempo));
    }
    
    private void setTempoMessage(Track track, int tempo) throws InvalidMidiDataException {
        MetaMessage tempoMessage = new MetaMessage();
        int microsecondsPerQuarter = 60000000 / tempo;
        byte[] tempoData = new byte[3];
        tempoData[0] = (byte) ((microsecondsPerQuarter >> 16) & 0xFF);
        tempoData[1] = (byte) ((microsecondsPerQuarter >> 8) & 0xFF);
        tempoData[2] = (byte) (microsecondsPerQuarter & 0xFF);
        tempoMessage.setMessage(0x51, tempoData, 3);
        track.add(new MidiEvent(tempoMessage, 0));
    }
    
    private long generateMusicalSection(Track track, double[] dataArray, double[][] dataLines, 
                                      DataAnalysis analysis, int dataIndex, long currentTick, int beatsPerSection) 
                                      throws InvalidMidiDataException {
        
        double currentValue = dataArray[dataIndex];
        double normalizedValue = (currentValue - analysis.min) / (analysis.max - analysis.min);
        
        // Determine musical style based on data characteristics
        MusicStyle style = chooseMusicStyle(normalizedValue, dataIndex);
        
        // Generate bass line (Channel 0)
        generateBassLine(track, style, normalizedValue, currentTick, beatsPerSection);
        
        // Generate chords (Channels 1-2)
        generateChords(track, style, normalizedValue, dataIndex, currentTick, beatsPerSection);
        
        // Generate melody (Channel 3)
        generateMelody(track, style, normalizedValue, dataIndex, currentTick, beatsPerSection);
        
        // Generate percussion (Channel 9 - standard MIDI drums)
        generateDrums(track, style, normalizedValue, currentTick, beatsPerSection);
        
        // Generate additional textural elements based on data lines
        if (dataLines.length > 1) {
            generateTextureFromLines(track, dataLines, dataIndex, normalizedValue, currentTick, beatsPerSection);
        }
        
        return currentTick + (480 * beatsPerSection); // Move to next section
    }
    
    private MusicStyle chooseMusicStyle(double normalizedValue, int dataIndex) {
        // Change style based on data characteristics and position
        if (normalizedValue < 0.3) {
            return MusicStyle.AMBIENT;
        } else if (normalizedValue > 0.7) {
            return MusicStyle.ENERGETIC;
        } else if (dataIndex % 8 < 4) {
            return MusicStyle.RHYTHMIC;
        } else {
            return MusicStyle.MELODIC;
        }
    }
    
    enum MusicStyle {
        AMBIENT, ENERGETIC, RHYTHMIC, MELODIC
    }
    
    private void generateBassLine(Track track, MusicStyle style, double normalizedValue, long currentTick, int beatsPerSection) throws InvalidMidiDataException {
        int[] bassNotes = getBassNotes(style, normalizedValue);
        int velocity = (int)(60 + normalizedValue * 40); // 60-100 velocity
        
        for (int beat = 0; beat < beatsPerSection; beat++) {
            if (shouldPlayBassNote(style, beat)) {
                int note = bassNotes[beat % bassNotes.length];
                long tick = currentTick + (beat * 480);
                int duration = getBassNoteDuration(style);
                addNoteEvent(track, 0, note, velocity, tick, duration);
            }
        }
    }
    
    private void generateChords(Track track, MusicStyle style, double normalizedValue, int dataIndex, long currentTick, int beatsPerSection) throws InvalidMidiDataException {
        int[][] chords = getChordsForStyle(style, normalizedValue);
        int velocity = (int)(50 + normalizedValue * 50); // 50-100 velocity
        
        for (int beat = 0; beat < beatsPerSection; beat += 2) { // Play chords every 2 beats
            int[] chord = chords[(dataIndex + beat) % chords.length];
            long tick = currentTick + (beat * 480);
            
            for (int note : chord) {
                addNoteEvent(track, 1, note, velocity, tick, 480 * 2); // 2 beat duration
            }
        }
    }
    
    private void generateMelody(Track track, MusicStyle style, double normalizedValue, int dataIndex, long currentTick, int beatsPerSection) throws InvalidMidiDataException {
        int[] scale = getScaleForStyle(style);
        int velocity = (int)(70 + normalizedValue * 30); // 70-100 velocity
        
        // Generate melody pattern based on style
        int[] melodyPattern = getMelodyPattern(style, normalizedValue);
        
        for (int i = 0; i < melodyPattern.length && i < beatsPerSection * 2; i++) { // 8th notes
            if (melodyPattern[i] > 0) {
                int scaleIndex = (int)(normalizedValue * (scale.length - 1)) + (i % 3);
                scaleIndex = Math.max(0, Math.min(scale.length - 1, scaleIndex));
                int note = 60 + scale[scaleIndex] + (int)(normalizedValue * 12); // C4 + scale + octave variation
                
                long tick = currentTick + (i * 240); // 8th note intervals
                addNoteEvent(track, 3, note, velocity, tick, 240);
            }
        }
    }
    
    private void generateDrums(Track track, MusicStyle style, double normalizedValue, long currentTick, int beatsPerSection) throws InvalidMidiDataException {
        int velocity = (int)(80 + normalizedValue * 47); // 80-127 velocity
        
        for (int beat = 0; beat < beatsPerSection; beat++) {
            long tick = currentTick + (beat * 480);
            
            // Kick drum pattern
            if (shouldPlayKick(style, beat)) {
                addNoteEvent(track, 9, 36, velocity, tick, 120); // Kick drum
            }
            
            // Snare drum pattern
            if (shouldPlaySnare(style, beat)) {
                addNoteEvent(track, 9, 38, velocity - 10, tick, 120); // Snare drum
            }
            
            // Hi-hat pattern
            if (shouldPlayHiHat(style, beat, normalizedValue)) {
                addNoteEvent(track, 9, 42, velocity - 20, tick, 60); // Hi-hat
            }
        }
    }
    
    private void generateTextureFromLines(Track track, double[][] dataLines, int dataIndex, double normalizedValue, long currentTick, int beatsPerSection) throws InvalidMidiDataException {
        // Use different lines for different textural elements
        for (int lineIndex = 0; lineIndex < Math.min(dataLines.length, 4); lineIndex++) {
            double[] line = dataLines[lineIndex];
            if (line.length > dataIndex) {
                double lineValue = line[dataIndex % line.length];
                double lineNormalized = Math.abs(lineValue) / 10.0; // Normalize to 0-1 range roughly
                
                // Generate arpeggios or sustained notes based on line data
                generateArpeggio(track, 4 + lineIndex, lineNormalized, currentTick, beatsPerSection);
            }
        }
    }
    
    private void generateArpeggio(Track track, int channel, double normalizedValue, long currentTick, int beatsPerSection) throws InvalidMidiDataException {
        int[] arpeggioNotes = {60, 64, 67, 72}; // C major arpeggio
        int velocity = (int)(40 + normalizedValue * 40); // Soft arpeggios
        
        for (int i = 0; i < 8 && i < beatsPerSection * 2; i++) { // 8th note arpeggios
            int note = arpeggioNotes[i % arpeggioNotes.length] + (int)(normalizedValue * 12);
            long tick = currentTick + (i * 240);
            addNoteEvent(track, channel, note, velocity, tick, 240);
        }
    }
    
    // Helper methods for musical patterns
    private int[] getBassNotes(MusicStyle style, double normalizedValue) {
        switch (style) {
            case AMBIENT:   return new int[]{36, 43}; // C, G
            case ENERGETIC: return new int[]{36, 41, 43, 38}; // C, F, G, D
            case RHYTHMIC:  return new int[]{36, 36, 43, 41}; // More repetitive
            default:        return new int[]{36, 43, 48, 43}; // C, G, C5, G
        }
    }
    
    private boolean shouldPlayBassNote(MusicStyle style, int beat) {
        switch (style) {
            case AMBIENT:   return beat % 2 == 0; // Half notes
            case ENERGETIC: return true; // Every beat
            case RHYTHMIC:  return beat % 2 == 0 || beat == 3; // Syncopated
            default:        return beat % 2 == 0; // Half notes
        }
    }
    
    private int getBassNoteDuration(MusicStyle style) {
        switch (style) {
            case AMBIENT:   return 960; // Half note
            case ENERGETIC: return 240; // Quarter note
            case RHYTHMIC:  return 360; // Dotted quarter
            default:        return 480; // Quarter note
        }
    }
    
    private int[][] getChordsForStyle(MusicStyle style, double normalizedValue) {
        // Different chord progressions for different styles
        switch (style) {
            case AMBIENT:   return new int[][]{{60,64,67}, {65,69,72}, {67,71,74}}; // C, F, G
            case ENERGETIC: return new int[][]{{60,64,67}, {62,65,69}, {64,67,71}, {65,69,72}}; // C, Dm, Em, F
            case RHYTHMIC:  return new int[][]{{60,64,67}, {67,71,74}}; // C, G (simple)
            default:        return new int[][]{{60,64,67}, {65,69,72}, {67,71,74}, {60,64,67}}; // C, F, G, C
        }
    }
    
    private int[] getScaleForStyle(MusicStyle style) {
        switch (style) {
            case AMBIENT:   return new int[]{0, 2, 4, 7, 9}; // Pentatonic
            case ENERGETIC: return new int[]{0, 2, 4, 5, 7, 9, 11}; // Major scale
            case RHYTHMIC:  return new int[]{0, 3, 5, 7, 10}; // Minor pentatonic
            default:        return new int[]{0, 2, 4, 7, 9}; // Pentatonic
        }
    }
    
    private int[] getMelodyPattern(MusicStyle style, double normalizedValue) {
        // 1 = play note, 0 = rest
        switch (style) {
            case AMBIENT:   return new int[]{1, 0, 1, 0, 1, 0, 0, 0};
            case ENERGETIC: return new int[]{1, 1, 0, 1, 1, 0, 1, 1};
            case RHYTHMIC:  return new int[]{1, 0, 1, 1, 0, 1, 0, 1};
            default:        return new int[]{1, 0, 1, 0, 1, 1, 0, 0};
        }
    }
    
    private boolean shouldPlayKick(MusicStyle style, int beat) {
        switch (style) {
            case ENERGETIC: return beat % 2 == 0; // Every other beat
            case RHYTHMIC:  return beat == 0 || beat == 3; // 1 and 4
            default:        return beat == 0; // Just on 1
        }
    }
    
    private boolean shouldPlaySnare(MusicStyle style, int beat) {
        switch (style) {
            case ENERGETIC: return beat == 1 || beat == 3; // 2 and 4
            case RHYTHMIC:  return beat == 1 || beat == 3; // 2 and 4
            default:        return beat == 2; // Just on 3
        }
    }
    
    private boolean shouldPlayHiHat(MusicStyle style, int beat, double normalizedValue) {
        switch (style) {
            case AMBIENT:   return normalizedValue > 0.6 && beat % 2 == 0;
            case ENERGETIC: return true; // Every beat
            case RHYTHMIC:  return true; // Every beat
            default:        return beat % 2 == 0; // Half notes
        }
    }
    
    /**
     * Parse input data from text, preserving line structure for channel distribution
     */
    private double[][] parseDataInputByLines(String input) {
        List<List<Double>> lineValues = new ArrayList<>();
        
        // Split by lines
        String[] lines = input.split("\\n");
        for (String line : lines) {
            if (line.trim().isEmpty() || line.contains("Enter comma-separated")) continue;
            
            List<Double> values = new ArrayList<>();
            String[] parts = line.split("[,\\s]+");
            for (String part : parts) {
                try {
                    double value = Double.parseDouble(part.trim());
                    values.add(value);
                } catch (NumberFormatException e) {
                    // Skip invalid numbers
                }
            }
            if (!values.isEmpty()) {
                lineValues.add(values);
            }
        }
        
        // Convert to double array
        double[][] result = new double[lineValues.size()][];
        for (int i = 0; i < lineValues.size(); i++) {
            List<Double> line = lineValues.get(i);
            result[i] = line.stream().mapToDouble(Double::doubleValue).toArray();
        }
        
        return result;
    }
    
    /**
     * Parse input data from text (legacy method for backwards compatibility)
     */
    private double[] parseDataInput(String input) {
        double[][] lines = parseDataInputByLines(input);
        List<Double> allValues = new ArrayList<>();
        
        for (double[] line : lines) {
            for (double value : line) {
                allValues.add(value);
            }
        }
        
        return allValues.stream().mapToDouble(Double::doubleValue).toArray();
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
            // Ensure all values are in valid MIDI range
            int safeChannel = Math.max(0, Math.min(15, channel));
            int safeNote = Math.max(0, Math.min(127, note));
            int safeVelocity = Math.max(0, Math.min(127, velocity));
            
            // Note on
            ShortMessage noteOn = new ShortMessage();
            noteOn.setMessage(ShortMessage.NOTE_ON, safeChannel, safeNote, safeVelocity);
            track.add(new MidiEvent(noteOn, tick));
            
            // Note off
            ShortMessage noteOff = new ShortMessage();
            noteOff.setMessage(ShortMessage.NOTE_OFF, safeChannel, safeNote, 0);
            track.add(new MidiEvent(noteOff, tick + duration));
        } catch (InvalidMidiDataException e) {
            System.err.println("Error creating MIDI event: " + e.getMessage() + 
                             " (channel=" + channel + ", note=" + note + ", velocity=" + velocity + ")");
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
     * Create Image to Music panel
     */
    private JPanel createImageToMusicPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Image to Music Conversion"));
        
        // Create interactive image panel
        InteractiveImagePanel imagePanel = new InteractiveImagePanel();
        imagePanel.setPreferredSize(new Dimension(500, 350));
        
        // Image loading panel
        JPanel loadPanel = new JPanel(new FlowLayout());
        JButton loadImageBtn = new JButton("Load Image");
        JLabel imageStatusLabel = new JLabel("No image loaded");
        JButton playRegionBtn = new JButton("Play Selected Area");
        playRegionBtn.setEnabled(false);
        
        loadImageBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "bmp", "gif"));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    if (imageToMusicSynth == null) {
                        imageToMusicSynth = new ImageToMusicSynthesizer();
                    }
                    
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(selectedFile);
                    
                    if (image != null) {
                        // Load image into both synthesizer and interactive panel
                        imageToMusicSynth.loadImage(image);
                        imagePanel.setImage(image);
                        imageStatusLabel.setText("Loaded: " + selectedFile.getName() + " - Select an area to play");
                        playRegionBtn.setEnabled(true);
                        
                        // Clear any existing listeners and set up new one
                        imagePanel.clearAreaSelectionListeners();
                        InteractiveImagePanel.AreaSelectionListener areaListener = (area, region) -> {
                            if (region != null && imageToMusicSynth != null) {
                                imageToMusicSynth.setSelectedRegion(area, region);
                                imageStatusLabel.setText("Area selected: " + area.width + "x" + area.height + " px - Ready to play");
                                
                                // Auto-generate music from new selection for immediate feedback
                                SwingUtilities.invokeLater(() -> {
                                    try {
                                        javax.sound.midi.Sequence sequence = imageToMusicSynth.generateMusicFromImage();
                                        if (sequence != null && sequencer != null) {
                                            sequencer.setSequence(sequence);
                                            statusLabel.setText("Music updated from selected area - ready to play");
                                        }
                                    } catch (Exception musicEx) {
                                        System.err.println("Error updating music from selection: " + musicEx.getMessage());
                                    }
                                });
                            }
                        };
                        
                        imagePanel.addAreaSelectionListener(areaListener);
                        
                    } else {
                        imageStatusLabel.setText("Failed to load image");
                    }
                } catch (Exception ex) {
                    imageStatusLabel.setText("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        
        // Play region button functionality
        playRegionBtn.addActionListener(e -> {
            try {
                if (imageToMusicSynth != null) {
                    javax.sound.midi.Sequence sequence = imageToMusicSynth.generateMusicFromImage();
                    if (sequence != null && sequencer != null) {
                        sequencer.setSequence(sequence);
                        sequencer.start();
                        statusLabel.setText("Playing music from selected image area");
                    }
                }
            } catch (Exception ex) {
                statusLabel.setText("Error playing region: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        loadPanel.add(loadImageBtn);
        loadPanel.add(playRegionBtn);
        loadPanel.add(imageStatusLabel);
        
        // Image analysis controls
        JPanel controlsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Image Analysis Settings"));
        
        // Brightness mapping
        controlsPanel.add(new JLabel("Brightness → Volume:"));
        JSlider brightnessSlider = new JSlider(0, 100, 80);
        brightnessSlider.addChangeListener(e -> {
            if (imageToMusicSynth != null) {
                imageToMusicSynth.setBrightnessMapping(brightnessSlider.getValue() / 100.0f);
            }
        });
        controlsPanel.add(brightnessSlider);
        
        // Color mapping
        controlsPanel.add(new JLabel("Color → Pitch:"));
        JSlider colorSlider = new JSlider(0, 100, 60);
        colorSlider.addChangeListener(e -> {
            if (imageToMusicSynth != null) {
                imageToMusicSynth.setColorMapping(colorSlider.getValue() / 100.0f);
            }
        });
        controlsPanel.add(colorSlider);
        
        // Saturation mapping
        controlsPanel.add(new JLabel("Saturation → Intensity:"));
        JSlider saturationSlider = new JSlider(0, 100, 70);
        saturationSlider.addChangeListener(e -> {
            if (imageToMusicSynth != null) {
                imageToMusicSynth.setSaturationMapping(saturationSlider.getValue() / 100.0f);
            }
        });
        controlsPanel.add(saturationSlider);
        
        // Create left side with image display
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(loadPanel, BorderLayout.NORTH);
        leftPanel.add(imagePanel, BorderLayout.CENTER);
        
        // Create instructions panel
        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.setBorder(BorderFactory.createTitledBorder("Instructions"));
        JTextArea instructions = new JTextArea(
            "1. Load an image using the 'Load Image' button below\n" +
            "2. Drag to select different areas of the image\n" +
            "3. Yellow highlight shows the selected area\n" +
            "4. Click 'Play Selected Area' to hear that region\n" +
            "5. Double-click image to select entire image\n" +
            "6. Adjust mapping controls to change the sound"
        );
        instructions.setEditable(false);
        instructions.setFont(instructions.getFont().deriveFont(12f));
        instructions.setBackground(panel.getBackground());
        instructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        instructionsPanel.add(instructions, BorderLayout.CENTER);
        
        // Create right side with controls and instructions
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(instructionsPanel, BorderLayout.NORTH);
        rightPanel.add(controlsPanel, BorderLayout.CENTER);
        
        // Split the panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(520);
        splitPane.setResizeWeight(0.6);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create DAW Integration panel
     */
    private JPanel createDAWIntegrationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("DAW Integration"));
        
        // MIDI Output Section
        JPanel midiPanel = new JPanel(new GridBagLayout());
        midiPanel.setBorder(BorderFactory.createTitledBorder("MIDI Output"));
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // MIDI Device Selection
        gbc.gridx = 0; gbc.gridy = 0;
        midiPanel.add(new JLabel("MIDI Device:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<String> midiDeviceCombo = new JComboBox<>();
        midiPanel.add(midiDeviceCombo, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        JButton refreshMidiBtn = new JButton("Refresh");
        midiPanel.add(refreshMidiBtn, gbc);
        
        // MIDI Channel
        gbc.gridx = 0; gbc.gridy = 1;
        midiPanel.add(new JLabel("MIDI Channel:"), gbc);
        
        gbc.gridx = 1;
        JSpinner midiChannelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 16, 1));
        midiPanel.add(midiChannelSpinner, gbc);
        
        // MIDI Connect button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton midiConnectBtn = new JButton("Connect MIDI");
        midiPanel.add(midiConnectBtn, gbc);
        
        // Audio Output Section
        JPanel audioPanel = new JPanel(new GridBagLayout());
        audioPanel.setBorder(BorderFactory.createTitledBorder("Audio Output"));
        gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // Audio Device Selection
        gbc.gridx = 0; gbc.gridy = 0;
        audioPanel.add(new JLabel("Audio Device:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<String> audioDeviceCombo = new JComboBox<>();
        audioPanel.add(audioDeviceCombo, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        JButton refreshAudioBtn = new JButton("Refresh");
        audioPanel.add(refreshAudioBtn, gbc);
        
        // Buffer Size
        gbc.gridx = 0; gbc.gridy = 1;
        audioPanel.add(new JLabel("Buffer Size:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<Integer> bufferSizeCombo = new JComboBox<>(new Integer[]{64, 128, 256, 512, 1024, 2048});
        bufferSizeCombo.setSelectedItem(512);
        audioPanel.add(bufferSizeCombo, gbc);
        
        // Sample Rate
        gbc.gridx = 0; gbc.gridy = 2;
        audioPanel.add(new JLabel("Sample Rate:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<Integer> sampleRateCombo = new JComboBox<>(new Integer[]{22050, 44100, 48000, 88200, 96000});
        sampleRateCombo.setSelectedItem(44100);
        audioPanel.add(sampleRateCombo, gbc);
        
        // Audio Connect button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton audioConnectBtn = new JButton("Connect Audio");
        audioPanel.add(audioConnectBtn, gbc);
        
        // Status and Control Section
        JPanel statusPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status & Control"));
        
        JLabel midiStatusLabel = new JLabel("MIDI: Disconnected");
        JLabel audioStatusLabel = new JLabel("Audio: Disconnected");
        
        JButton startStreamingBtn = new JButton("Start Audio Streaming");
        JButton stopStreamingBtn = new JButton("Stop Audio Streaming");
        stopStreamingBtn.setEnabled(false);
        
        statusPanel.add(midiStatusLabel);
        statusPanel.add(audioStatusLabel);
        statusPanel.add(startStreamingBtn);
        statusPanel.add(stopStreamingBtn);
        
        // Event Handlers
        refreshMidiBtn.addActionListener(e -> {
            if (dawManager != null) {
                dawManager.refreshAvailableDevices();
                midiDeviceCombo.removeAllItems();
                for (javax.sound.midi.MidiDevice.Info info : dawManager.getAvailableMidiDevices()) {
                    midiDeviceCombo.addItem(info.getName());
                }
            }
        });
        
        refreshAudioBtn.addActionListener(e -> {
            if (dawManager != null) {
                dawManager.refreshAvailableDevices();
                audioDeviceCombo.removeAllItems();
                for (javax.sound.sampled.Mixer.Info info : dawManager.getAvailableAudioDevices()) {
                    audioDeviceCombo.addItem(info.getName());
                }
            }
        });
        
        midiConnectBtn.addActionListener(e -> {
            if (dawManager != null && midiDeviceCombo.getSelectedIndex() >= 0) {
                try {
                    java.util.List<javax.sound.midi.MidiDevice.Info> devices = dawManager.getAvailableMidiDevices();
                    if (midiDeviceCombo.getSelectedIndex() < devices.size()) {
                        javax.sound.midi.MidiDevice.Info selectedDevice = devices.get(midiDeviceCombo.getSelectedIndex());
                        
                        // Set MIDI channel
                        int channel = (Integer) midiChannelSpinner.getValue() - 1; // Convert to 0-based
                        dawManager.setMidiChannel(channel);
                        
                        if (dawManager.setupMidiOutput(selectedDevice)) {
                            midiStatusLabel.setText("MIDI: Connected to " + selectedDevice.getName());
                            midiConnectBtn.setText("Disconnect MIDI");
                        } else {
                            midiStatusLabel.setText("MIDI: Connection failed");
                        }
                    }
                } catch (Exception ex) {
                    midiStatusLabel.setText("MIDI: Error - " + ex.getMessage());
                }
            } else {
                // Disconnect
                if (dawManager != null) {
                    dawManager.closeMidiOutput();
                    midiStatusLabel.setText("MIDI: Disconnected");
                    midiConnectBtn.setText("Connect MIDI");
                }
            }
        });
        
        audioConnectBtn.addActionListener(e -> {
            if (dawManager != null && audioDeviceCombo.getSelectedIndex() >= 0) {
                try {
                    java.util.List<javax.sound.sampled.Mixer.Info> devices = dawManager.getAvailableAudioDevices();
                    if (audioDeviceCombo.getSelectedIndex() < devices.size()) {
                        javax.sound.sampled.Mixer.Info selectedDevice = devices.get(audioDeviceCombo.getSelectedIndex());
                        
                        // Set audio parameters
                        dawManager.setBufferSize((Integer) bufferSizeCombo.getSelectedItem());
                        dawManager.setSampleRate((Integer) sampleRateCombo.getSelectedItem());
                        
                        if (dawManager.setupAudioOutput(selectedDevice)) {
                            audioStatusLabel.setText("Audio: Connected to " + selectedDevice.getName());
                            audioConnectBtn.setText("Disconnect Audio");
                            startStreamingBtn.setEnabled(true);
                        } else {
                            audioStatusLabel.setText("Audio: Connection failed");
                        }
                    }
                } catch (Exception ex) {
                    audioStatusLabel.setText("Audio: Error - " + ex.getMessage());
                }
            } else {
                // Disconnect
                if (dawManager != null) {
                    dawManager.closeAudioOutput();
                    audioStatusLabel.setText("Audio: Disconnected");
                    audioConnectBtn.setText("Connect Audio");
                    startStreamingBtn.setEnabled(false);
                    stopStreamingBtn.setEnabled(false);
                }
            }
        });
        
        startStreamingBtn.addActionListener(e -> {
            if (dawManager != null && audioEngine != null) {
                dawManager.startAudioStreaming(audioEngine);
                startStreamingBtn.setEnabled(false);
                stopStreamingBtn.setEnabled(true);
                audioStatusLabel.setText(audioStatusLabel.getText() + " (Streaming)");
            }
        });
        
        stopStreamingBtn.addActionListener(e -> {
            if (dawManager != null) {
                dawManager.stopAudioStreaming();
                startStreamingBtn.setEnabled(true);
                stopStreamingBtn.setEnabled(false);
                String status = audioStatusLabel.getText();
                if (status.contains(" (Streaming)")) {
                    audioStatusLabel.setText(status.replace(" (Streaming)", ""));
                }
            }
        });
        
        // Initialize device lists
        refreshMidiBtn.doClick();
        refreshAudioBtn.doClick();
        
        // Layout
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        topPanel.add(midiPanel);
        topPanel.add(audioPanel);
        
        panel.add(topPanel, BorderLayout.CENTER);
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Hardware mixer panel method removed - functionality moved to synthesizer tab
    
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
        if (audioEngine != null) {
            audioEngine.stop();
        }
        if (patternMatrix != null && patternMatrix.isPlaying()) {
            patternMatrix.stop();
        }
        if (weatherService != null) {
            try {
                weatherService.close();
            } catch (Exception e) {
                System.err.println("Error closing weather service: " + e.getMessage());
            }
        }
        if (dawManager != null) {
            dawManager.shutdown();
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