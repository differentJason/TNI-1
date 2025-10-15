package com.tni.synthesizer.generative;

import java.util.*;

/**
 * Individual synthesizer channel with waveform generator, envelope, LFO, and filter
 */
public class SynthChannel {
    
    public enum Waveform {
        SINE("Sine"),
        SAWTOOTH("Sawtooth"),
        SQUARE("Square"),
        TRIANGLE("Triangle"),
        NOISE("Noise");
        
        private final String displayName;
        
        Waveform(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Channel properties
    private final int channelId;
    private final int sampleRate;
    private boolean enabled;
    private float volume;
    private float pan; // -1.0 (left) to 1.0 (right)
    
    // Oscillator
    private Waveform waveform;
    private float detune; // In cents (-1200 to +1200)
    
    // Modulation
    private LFO lfo;
    private boolean lfoEnabled;
    
    // Envelope
    private ADSREnvelope envelope;
    
    // Filter
    private ResonantFilter filter;
    private boolean filterEnabled;
    
    // Voice management
    private List<SynthVoice> activeVoices;
    private final int maxVoices = 8;
    

    
    public SynthChannel(int channelId, int sampleRate) {
        this.channelId = channelId;
        this.sampleRate = sampleRate;
        this.enabled = true;
        this.volume = 0.8f;
        this.pan = 0.0f;
        
        // Initialize components
        this.waveform = Waveform.SINE;
        this.detune = 0.0f;
        
        this.lfo = new LFO(sampleRate);
        this.lfoEnabled = false;
        
        this.envelope = new ADSREnvelope(sampleRate);
        
        this.filter = new ResonantFilter(sampleRate);
        this.filterEnabled = false;
        
        this.activeVoices = new ArrayList<>();
    }
    
    /**
     * Generate audio samples for this channel
     */
    public float[] generateSamples(int bufferSize) {
        float[] output = new float[bufferSize];
        
        if (!enabled) {
            return output; // Return silent buffer
        }
        
        // Process each active voice
        for (int i = activeVoices.size() - 1; i >= 0; i--) {
            SynthVoice voice = activeVoices.get(i);
            
            // Generate samples from voice
            for (int sample = 0; sample < bufferSize; sample++) {
                output[sample] += voice.getNextSample();
            }
            
            // Remove idle voices
            if (!voice.isAvailable() && voice.getState() == SynthVoice.VoiceState.IDLE) {
                activeVoices.remove(i);
            }
        }
        
        // Apply master volume
        for (int i = 0; i < bufferSize; i++) {
            output[i] *= volume;
        }
        
        return output;
    }
    
    /**
     * Start playing a note
     */
    public void noteOn(float frequency, float velocity) {
        // Convert frequency to MIDI note
        int midiNote = (int) Math.round(12 * Math.log(frequency / 440.0) / Math.log(2.0)) + 69;
        int velocityInt = (int) (velocity * 127);
        
        // Remove old voice with same MIDI note if exists
        activeVoices.removeIf(voice -> voice.getMidiNote() == midiNote);
        
        // Create new voice if not at max capacity
        if (activeVoices.size() < maxVoices) {
            SynthVoice voice = new SynthVoice(activeVoices.size(), sampleRate);
            voice.setWaveform(waveform);
            voice.noteOn(midiNote, velocityInt);
            activeVoices.add(voice);
        }
    }
    
    /**
     * Stop playing a note
     */
    public void noteOff(float frequency) {
        // Convert frequency to MIDI note
        int midiNote = (int) Math.round(12 * Math.log(frequency / 440.0) / Math.log(2.0)) + 69;
        
        for (SynthVoice voice : activeVoices) {
            if (voice.getMidiNote() == midiNote) {
                voice.noteOff();
            }
        }
    }
    
    /**
     * Stop all notes on this channel
     */
    public void allNotesOff() {
        for (SynthVoice voice : activeVoices) {
            voice.noteOff();
        }
    }
    
    // Getters and setters
    public int getChannelId() {
        return channelId;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            allNotesOff();
        }
    }
    
    public float getVolume() {
        return volume;
    }
    
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    public float getPan() {
        return pan;
    }
    
    public void setPan(float pan) {
        this.pan = Math.max(-1.0f, Math.min(1.0f, pan));
    }
    
    public Waveform getWaveform() {
        return waveform;
    }
    
    public void setWaveform(Waveform waveform) {
        this.waveform = waveform;
    }
    
    public float getDetune() {
        return detune;
    }
    
    public void setDetune(float detune) {
        this.detune = Math.max(-1200.0f, Math.min(1200.0f, detune));
    }
    
    public LFO getLFO() {
        return lfo;
    }
    
    public boolean isLFOEnabled() {
        return lfoEnabled;
    }
    
    public void setLFOEnabled(boolean enabled) {
        this.lfoEnabled = enabled;
    }
    
    public ADSREnvelope getEnvelope() {
        return envelope;
    }
    
    public ResonantFilter getFilter() {
        return filter;
    }
    
    public boolean isFilterEnabled() {
        return filterEnabled;
    }
    
    public void setFilterEnabled(boolean enabled) {
        this.filterEnabled = enabled;
    }
    
    public int getActiveVoicesCount() {
        return activeVoices.size();
    }
}