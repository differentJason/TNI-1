package com.tni.synthesizer.generative;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.*;
import java.util.List;

/**
 * Manages MIDI and audio output routing for DAW integration
 * Provides MIDI CC mapping and real-time audio streaming capabilities
 */
public class DAWIntegrationManager {
    
    // MIDI Output Management
    private MidiDevice midiOutputDevice;
    private Receiver midiReceiver;
    private List<MidiDevice.Info> availableMidiDevices;
    private boolean midiOutputEnabled = false;
    
    // Audio Output Management
    private SourceDataLine audioOutputLine;
    private AudioFormat audioFormat;
    private List<Mixer.Info> availableAudioDevices;
    private boolean audioOutputEnabled = false;
    
    // MIDI CC Mapping for synthesizer parameters
    private Map<Integer, String> ccMappings;
    private Map<String, Integer> parameterToCC;
    
    // Audio streaming
    private Thread audioStreamThread;
    private volatile boolean isStreaming = false;
    private final Object audioLock = new Object();
    
    // Configuration
    private int midiChannel = 0; // MIDI channel 1 (0-based)
    private int bufferSize = 512;
    private int sampleRate = 44100;
    
    public DAWIntegrationManager() {
        initializeMidiCCMappings();
        refreshAvailableDevices();
    }
    
    /**
     * Initialize MIDI CC mappings for synthesizer parameters
     */
    private void initializeMidiCCMappings() {
        ccMappings = new HashMap<>();
        parameterToCC = new HashMap<>();
        
        // Standard MIDI CC assignments for synthesizer parameters
        addCCMapping(1, "ModWheel");           // CC1 - Modulation Wheel
        addCCMapping(7, "Volume");             // CC7 - Channel Volume
        addCCMapping(10, "Pan");               // CC10 - Pan
        addCCMapping(11, "Expression");        // CC11 - Expression
        addCCMapping(12, "Effect1");           // CC12 - Effect Control 1
        addCCMapping(13, "Effect2");           // CC13 - Effect Control 2
        addCCMapping(71, "FilterResonance");   // CC71 - Resonance/Timbre
        addCCMapping(72, "ReleaseTime");       // CC72 - Release Time
        addCCMapping(73, "AttackTime");        // CC73 - Attack Time
        addCCMapping(74, "FilterCutoff");      // CC74 - Brightness/Cutoff
        addCCMapping(75, "DecayTime");         // CC75 - Decay Time
        addCCMapping(76, "VibratoRate");       // CC76 - Vibrato Rate
        addCCMapping(77, "VibratoDepth");      // CC77 - Vibrato Depth
        addCCMapping(78, "VibratoDelay");      // CC78 - Vibrato Delay
        addCCMapping(91, "ReverbSend");        // CC91 - Reverb Send Level
        addCCMapping(92, "TremoloDepth");      // CC92 - Tremolo Depth
        addCCMapping(93, "ChorusSend");        // CC93 - Chorus Send Level
        addCCMapping(94, "DelaySend");         // CC94 - Delay Send Level
    }
    
    private void addCCMapping(int ccNumber, String parameterName) {
        ccMappings.put(ccNumber, parameterName);
        parameterToCC.put(parameterName, ccNumber);
    }
    
    /**
     * Refresh available MIDI and audio devices
     */
    public void refreshAvailableDevices() {
        refreshMidiDevices();
        refreshAudioDevices();
    }
    
    private void refreshMidiDevices() {
        availableMidiDevices = new ArrayList<>();
        MidiDevice.Info[] midiInfos = MidiSystem.getMidiDeviceInfo();
        
        for (MidiDevice.Info info : midiInfos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                if (device.getMaxReceivers() != 0) { // Can receive MIDI data
                    availableMidiDevices.add(info);
                }
            } catch (MidiUnavailableException e) {
                System.err.println("MIDI device unavailable: " + info.getName());
            }
        }
    }
    
    private void refreshAudioDevices() {
        availableAudioDevices = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            
            // Check if mixer supports audio output
            for (Line.Info lineInfo : lineInfos) {
                if (lineInfo instanceof DataLine.Info) {
                    DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
                    if (SourceDataLine.class.isAssignableFrom(dataLineInfo.getLineClass())) {
                        availableAudioDevices.add(info);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Set up MIDI output to specified device
     */
    public boolean setupMidiOutput(MidiDevice.Info deviceInfo) {
        try {
            // Close existing connection if any
            closeMidiOutput();
            
            midiOutputDevice = MidiSystem.getMidiDevice(deviceInfo);
            midiOutputDevice.open();
            midiReceiver = midiOutputDevice.getReceiver();
            midiOutputEnabled = true;
            
            System.out.println("MIDI output connected to: " + deviceInfo.getName());
            return true;
            
        } catch (MidiUnavailableException e) {
            System.err.println("Failed to setup MIDI output: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Set up audio output to specified device
     */
    public boolean setupAudioOutput(Mixer.Info mixerInfo) {
        try {
            // Close existing connection if any
            closeAudioOutput();
            
            audioFormat = new AudioFormat(sampleRate, 16, 2, true, false);
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            audioOutputLine = (SourceDataLine) mixer.getLine(dataLineInfo);
            audioOutputLine.open(audioFormat, bufferSize * 4); // Buffer for stereo 16-bit
            audioOutputLine.start();
            audioOutputEnabled = true;
            
            System.out.println("Audio output connected to: " + mixerInfo.getName());
            return true;
            
        } catch (LineUnavailableException e) {
            System.err.println("Failed to setup audio output: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Start real-time audio streaming
     */
    public void startAudioStreaming(AdvancedAudioEngine audioEngine) {
        if (!audioOutputEnabled || isStreaming) {
            return;
        }
        
        isStreaming = true;
        audioStreamThread = new Thread(() -> {
            byte[] buffer = new byte[bufferSize * 4]; // Stereo 16-bit
            float[] leftChannel = new float[bufferSize];
            float[] rightChannel = new float[bufferSize];
            
            while (isStreaming && audioOutputLine != null) {
                try {
                    synchronized (audioLock) {
                        // Clear buffers
                        Arrays.fill(leftChannel, 0.0f);
                        Arrays.fill(rightChannel, 0.0f);
                        
                        // Generate audio from synthesizer engine
                        if (audioEngine != null) {
                            // This would need to be implemented in AdvancedAudioEngine
                            // audioEngine.generateStereoSamples(leftChannel, rightChannel);
                        }
                        
                        // Convert float samples to byte buffer
                        convertToByteBuffer(leftChannel, rightChannel, buffer);
                        
                        // Write to audio output
                        if (audioOutputLine.isOpen()) {
                            audioOutputLine.write(buffer, 0, buffer.length);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Audio streaming error: " + e.getMessage());
                    break;
                }
            }
        }, "DAW-Audio-Stream");
        
        audioStreamThread.setDaemon(true);
        audioStreamThread.start();
    }
    
    /**
     * Stop audio streaming
     */
    public void stopAudioStreaming() {
        isStreaming = false;
        if (audioStreamThread != null) {
            try {
                audioStreamThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Send MIDI note on message
     */
    public void sendMidiNoteOn(int note, int velocity) {
        if (midiOutputEnabled && midiReceiver != null) {
            try {
                ShortMessage message = new ShortMessage();
                message.setMessage(ShortMessage.NOTE_ON, midiChannel, note, velocity);
                midiReceiver.send(message, -1);
            } catch (InvalidMidiDataException e) {
                System.err.println("MIDI note on error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Send MIDI note off message
     */
    public void sendMidiNoteOff(int note) {
        if (midiOutputEnabled && midiReceiver != null) {
            try {
                ShortMessage message = new ShortMessage();
                message.setMessage(ShortMessage.NOTE_OFF, midiChannel, note, 0);
                midiReceiver.send(message, -1);
            } catch (InvalidMidiDataException e) {
                System.err.println("MIDI note off error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Send MIDI Control Change message
     */
    public void sendMidiCC(int ccNumber, int value) {
        if (midiOutputEnabled && midiReceiver != null) {
            try {
                ShortMessage message = new ShortMessage();
                message.setMessage(ShortMessage.CONTROL_CHANGE, midiChannel, ccNumber, value);
                midiReceiver.send(message, -1);
            } catch (InvalidMidiDataException e) {
                System.err.println("MIDI CC error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Send MIDI CC for synthesizer parameter
     */
    public void sendParameterAsCC(String parameterName, float value) {
        Integer ccNumber = parameterToCC.get(parameterName);
        if (ccNumber != null) {
            int midiValue = Math.round(value * 127); // Convert 0.0-1.0 to 0-127
            midiValue = Math.max(0, Math.min(127, midiValue));
            sendMidiCC(ccNumber, midiValue);
        }
    }
    
    /**
     * Send MIDI Program Change
     */
    public void sendMidiProgramChange(int program) {
        if (midiOutputEnabled && midiReceiver != null) {
            try {
                ShortMessage message = new ShortMessage();
                message.setMessage(ShortMessage.PROGRAM_CHANGE, midiChannel, program, 0);
                midiReceiver.send(message, -1);
            } catch (InvalidMidiDataException e) {
                System.err.println("MIDI program change error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Convert float samples to byte buffer for audio output
     */
    private void convertToByteBuffer(float[] left, float[] right, byte[] buffer) {
        int bufferIndex = 0;
        
        for (int i = 0; i < left.length && i < right.length; i++) {
            // Convert float (-1.0 to 1.0) to 16-bit signed integer
            short leftSample = (short) (Math.max(-1.0f, Math.min(1.0f, left[i])) * 32767);
            short rightSample = (short) (Math.max(-1.0f, Math.min(1.0f, right[i])) * 32767);
            
            // Little-endian 16-bit stereo
            if (bufferIndex < buffer.length - 3) {
                buffer[bufferIndex++] = (byte) (leftSample & 0xFF);
                buffer[bufferIndex++] = (byte) ((leftSample >> 8) & 0xFF);
                buffer[bufferIndex++] = (byte) (rightSample & 0xFF);
                buffer[bufferIndex++] = (byte) ((rightSample >> 8) & 0xFF);
            }
        }
    }
    
    /**
     * Close MIDI output connection
     */
    public void closeMidiOutput() {
        if (midiReceiver != null) {
            midiReceiver.close();
            midiReceiver = null;
        }
        if (midiOutputDevice != null && midiOutputDevice.isOpen()) {
            midiOutputDevice.close();
            midiOutputDevice = null;
        }
        midiOutputEnabled = false;
    }
    
    /**
     * Close audio output connection
     */
    public void closeAudioOutput() {
        stopAudioStreaming();
        if (audioOutputLine != null && audioOutputLine.isOpen()) {
            audioOutputLine.stop();
            audioOutputLine.close();
            audioOutputLine = null;
        }
        audioOutputEnabled = false;
    }
    
    /**
     * Shutdown all connections
     */
    public void shutdown() {
        closeMidiOutput();
        closeAudioOutput();
    }
    
    // Getters
    public List<MidiDevice.Info> getAvailableMidiDevices() {
        return new ArrayList<>(availableMidiDevices);
    }
    
    public List<Mixer.Info> getAvailableAudioDevices() {
        return new ArrayList<>(availableAudioDevices);
    }
    
    public boolean isMidiOutputEnabled() {
        return midiOutputEnabled;
    }
    
    public boolean isAudioOutputEnabled() {
        return audioOutputEnabled;
    }
    
    public boolean isAudioStreaming() {
        return isStreaming;
    }
    
    public Map<Integer, String> getCCMappings() {
        return new HashMap<>(ccMappings);
    }
    
    public int getMidiChannel() {
        return midiChannel;
    }
    
    public void setMidiChannel(int channel) {
        this.midiChannel = Math.max(0, Math.min(15, channel));
    }
    
    public int getBufferSize() {
        return bufferSize;
    }
    
    public void setBufferSize(int size) {
        this.bufferSize = Math.max(64, Math.min(4096, size));
    }
    
    public int getSampleRate() {
        return sampleRate;
    }
    
    public void setSampleRate(int rate) {
        this.sampleRate = rate;
    }
}