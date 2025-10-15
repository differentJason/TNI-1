package com.tni.synthesizer.generative;

/**
 * Low Frequency Oscillator for modulation
 */
public class LFO {
    
    public enum LFOWaveform {
        SINE("Sine"),
        TRIANGLE("Triangle"),
        SQUARE("Square"),
        SAWTOOTH("Sawtooth"),
        RANDOM("Sample & Hold");
        
        private final String displayName;
        
        LFOWaveform(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum LFOTarget {
        AMPLITUDE("Amplitude"),
        FREQUENCY("Frequency"),
        FILTER_CUTOFF("Filter Cutoff"),
        FILTER_RESONANCE("Filter Resonance");
        
        private final String displayName;
        
        LFOTarget(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final int sampleRate;
    
    // LFO parameters
    private float frequency; // LFO frequency in Hz (0.01 to 20.0)
    private float depth;     // Modulation depth (0.0 to 1.0)
    private LFOWaveform waveform;
    private LFOTarget target;
    
    // Internal state
    private double phase;
    private float lastRandomValue;
    private int randomHoldCounter;
    private int randomHoldInterval;
    
    public LFO(int sampleRate) {
        this.sampleRate = sampleRate;
        
        // Default settings
        this.frequency = 1.0f; // 1 Hz
        this.depth = 0.5f;
        this.waveform = LFOWaveform.SINE;
        this.target = LFOTarget.AMPLITUDE;
        
        this.phase = 0.0;
        this.lastRandomValue = 0.0f;
        this.randomHoldCounter = 0;
        updateRandomHoldInterval();
    }
    
    /**
     * Generate the next LFO modulation value
     * @return Modulation value (-depth to +depth)
     */
    public float getNextValue() {
        float lfoValue;
        
        if (waveform == LFOWaveform.RANDOM) {
            // Sample and hold behavior
            if (randomHoldCounter <= 0) {
                lastRandomValue = (float) (Math.random() * 2.0 - 1.0);
                randomHoldCounter = randomHoldInterval;
            }
            randomHoldCounter--;
            lfoValue = lastRandomValue;
        } else {
            // Regular waveform generation
            switch (waveform) {
                case SINE:
                    lfoValue = (float) Math.sin(phase * 2.0 * Math.PI);
                    break;
                case TRIANGLE:
                    double t = phase * 4.0;
                    if (t < 1.0) {
                        lfoValue = (float) t;
                    } else if (t < 3.0) {
                        lfoValue = (float) (2.0 - t);
                    } else {
                        lfoValue = (float) (t - 4.0);
                    }
                    break;
                case SQUARE:
                    lfoValue = phase < 0.5 ? 1.0f : -1.0f;
                    break;
                case SAWTOOTH:
                    lfoValue = (float) (2.0 * (phase - Math.floor(phase + 0.5)));
                    break;
                default:
                    lfoValue = 0.0f;
            }
            
            // Advance phase
            phase += frequency / sampleRate;
            if (phase >= 1.0) {
                phase -= 1.0;
            }
        }
        
        return lfoValue * depth;
    }
    
    /**
     * Reset LFO phase to start of cycle
     */
    public void reset() {
        phase = 0.0;
        randomHoldCounter = 0;
    }
    
    private void updateRandomHoldInterval() {
        // Update interval based on frequency (higher freq = shorter holds)
        randomHoldInterval = Math.max(1, (int) (sampleRate / (frequency * 10)));
    }
    
    // Getters and setters
    public float getFrequency() {
        return frequency;
    }
    
    public void setFrequency(float frequency) {
        this.frequency = Math.max(0.01f, Math.min(20.0f, frequency));
        updateRandomHoldInterval();
    }
    
    public float getDepth() {
        return depth;
    }
    
    public void setDepth(float depth) {
        this.depth = Math.max(0.0f, Math.min(1.0f, depth));
    }
    
    public LFOWaveform getWaveform() {
        return waveform;
    }
    
    public void setWaveform(LFOWaveform waveform) {
        this.waveform = waveform;
        updateRandomHoldInterval();
    }
    
    public LFOTarget getTarget() {
        return target;
    }
    
    public void setTarget(LFOTarget target) {
        this.target = target;
    }
}