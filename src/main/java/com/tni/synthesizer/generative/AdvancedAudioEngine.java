package com.tni.synthesizer.generative;

import javax.sound.sampled.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced audio synthesis engine with multiple channels, waveform generators,
 * modulation capabilities, filters, and effects processing
 */
public class AdvancedAudioEngine {
    
    // Audio configuration
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 1024;
    private static final int CHANNELS = 2; // Stereo
    private static final int BITS_PER_SAMPLE = 16;
    
    // Audio system
    private AudioFormat audioFormat;
    private SourceDataLine audioLine;
    private boolean isRunning;
    private Thread audioThread;
    
    // Sound channels (up to 8 channels)
    private final int MAX_CHANNELS = 8;
    private List<SynthChannel> synthChannels;
    
    // Master effects chain
    private EffectsChain masterEffects;
    
    // Pattern matrix for routing
    private PatternMatrix patternMatrix;
    
    // Active notes tracking
    private Map<Integer, Set<ActiveNote>> channelNotes;
    
    public AdvancedAudioEngine() throws LineUnavailableException {
        initializeAudio();
        initializeChannels();
        initializeEffects();
        initializePatternMatrix();
        
        channelNotes = new ConcurrentHashMap<>();
        for (int i = 0; i < MAX_CHANNELS; i++) {
            channelNotes.put(i, ConcurrentHashMap.newKeySet());
        }
    }
    
    private void initializeAudio() throws LineUnavailableException {
        audioFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            SAMPLE_RATE,
            BITS_PER_SAMPLE,
            CHANNELS,
            CHANNELS * (BITS_PER_SAMPLE / 8),
            SAMPLE_RATE,
            false
        );
        
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        audioLine = (SourceDataLine) AudioSystem.getLine(info);
        audioLine.open(audioFormat, BUFFER_SIZE * 4);
    }
    
    private void initializeChannels() {
        synthChannels = new ArrayList<>();
        for (int i = 0; i < MAX_CHANNELS; i++) {
            synthChannels.add(new SynthChannel(i, SAMPLE_RATE));
        }
    }
    
    private void initializeEffects() {
        masterEffects = new EffectsChain(SAMPLE_RATE);
    }
    
    private void initializePatternMatrix() {
        patternMatrix = new PatternMatrix(MAX_CHANNELS, 120, 16); // 120 BPM, 16 beats per pattern
    }
    
    /**
     * Start the audio engine
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            audioLine.start();
            audioThread = new Thread(this::audioLoop, "AdvancedAudioEngine");
            audioThread.setDaemon(true);
            audioThread.start();
        }
    }
    
    /**
     * Stop the audio engine
     */
    public void stop() {
        isRunning = false;
        if (audioThread != null) {
            try {
                audioThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
        }
    }
    
    /**
     * Main audio processing loop
     */
    private void audioLoop() {
        byte[] buffer = new byte[BUFFER_SIZE * CHANNELS * (BITS_PER_SAMPLE / 8)];
        float[] leftChannel = new float[BUFFER_SIZE];
        float[] rightChannel = new float[BUFFER_SIZE];
        
        while (isRunning) {
            try {
                // Clear buffers
                Arrays.fill(leftChannel, 0.0f);
                Arrays.fill(rightChannel, 0.0f);
                
                // Mix all synthesizer channels
                for (int ch = 0; ch < MAX_CHANNELS; ch++) {
                    SynthChannel channel = synthChannels.get(ch);
                    if (channel.isEnabled()) {
                        float[] channelOutput = channel.generateSamples(BUFFER_SIZE);
                        
                        // Add to mix with panning
                        float panLeft = (1.0f - channel.getPan()) * channel.getVolume();
                        float panRight = (1.0f + channel.getPan()) * channel.getVolume();
                        
                        for (int i = 0; i < BUFFER_SIZE; i++) {
                            leftChannel[i] += channelOutput[i] * panLeft;
                            rightChannel[i] += channelOutput[i] * panRight;
                        }
                    }
                }
                
                // Apply master effects to left channel, then right channel
                masterEffects.process(leftChannel);
                masterEffects.process(rightChannel);
                
                // Convert to byte buffer and output
                convertToByteBuffer(leftChannel, rightChannel, buffer);
                audioLine.write(buffer, 0, buffer.length);
                
            } catch (Exception e) {
                System.err.println("Audio engine error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Convert float samples to byte buffer for audio output
     */
    private void convertToByteBuffer(float[] left, float[] right, byte[] buffer) {
        for (int i = 0, bufferIndex = 0; i < BUFFER_SIZE; i++) {
            // Clamp and convert left channel
            short leftSample = (short) Math.max(-32768, Math.min(32767, left[i] * 32767));
            buffer[bufferIndex++] = (byte) (leftSample & 0xFF);
            buffer[bufferIndex++] = (byte) ((leftSample >> 8) & 0xFF);
            
            // Clamp and convert right channel
            short rightSample = (short) Math.max(-32768, Math.min(32767, right[i] * 32767));
            buffer[bufferIndex++] = (byte) (rightSample & 0xFF);
            buffer[bufferIndex++] = (byte) ((rightSample >> 8) & 0xFF);
        }
    }
    
    /**
     * Trigger a note on a specific channel
     */
    public void noteOn(int channel, float frequency, float velocity) {
        if (channel >= 0 && channel < MAX_CHANNELS) {
            ActiveNote note = new ActiveNote(channel, frequency);
            channelNotes.get(channel).add(note);
            synthChannels.get(channel).noteOn(frequency, velocity);
        }
    }
    
    /**
     * Release a note on a specific channel
     */
    public void noteOff(int channel, float frequency) {
        if (channel >= 0 && channel < MAX_CHANNELS) {
            synthChannels.get(channel).noteOff(frequency);
            // Remove from active notes
            channelNotes.get(channel).removeIf(note -> 
                Math.abs(note.frequency - frequency) < 0.1f);
        }
    }
    
    /**
     * Get synthesizer channel for configuration
     */
    public SynthChannel getChannel(int channel) {
        if (channel >= 0 && channel < MAX_CHANNELS) {
            return synthChannels.get(channel);
        }
        return null;
    }
    
    /**
     * Get master effects chain
     */
    public EffectsChain getMasterEffects() {
        return masterEffects;
    }
    
    /**
     * Get pattern matrix for routing configuration
     */
    public PatternMatrix getPatternMatrix() {
        return patternMatrix;
    }
    
    /**
     * Get maximum number of channels
     */
    public int getMaxChannels() {
        return MAX_CHANNELS;
    }
    
    /**
     * Get sample rate
     */
    public int getSampleRate() {
        return SAMPLE_RATE;
    }
    
    /**
     * Active note tracking
     */
    private static class ActiveNote {
        final float frequency;
        
        ActiveNote(int channelIndex, float frequency) {
            this.frequency = frequency;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ActiveNote)) return false;
            ActiveNote other = (ActiveNote) obj;
            return Math.abs(frequency - other.frequency) < 0.1f;
        }
        
        @Override
        public int hashCode() {
            return Float.hashCode(frequency);
        }
    }
}