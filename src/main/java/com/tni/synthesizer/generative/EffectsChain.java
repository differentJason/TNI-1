package com.tni.synthesizer.generative;

import java.util.Arrays;

/**
 * Master effects chain with multiple effects that can be applied to the final audio output
 */
public class EffectsChain {
    
    public enum EffectType {
        REVERB("Reverb"),
        DELAY("Delay"),
        CHORUS("Chorus"),
        DISTORTION("Distortion"),
        COMPRESSOR("Compressor"),
        EQ("Equalizer");
        
        private final String displayName;
        
        EffectType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final int sampleRate;
    
    // Reverb parameters
    private boolean reverbEnabled = false;
    private float reverbMix = 0.3f;      // 0.0 to 1.0
    private float reverbDecay = 0.5f;    // 0.1 to 0.9
    private float[] reverbBuffer;
    private int reverbPosition;
    
    // Delay parameters
    private boolean delayEnabled = false;
    private float delayTime = 0.25f;     // 0.1 to 2.0 seconds
    private float delayFeedback = 0.4f;  // 0.0 to 0.9
    private float delayMix = 0.3f;       // 0.0 to 1.0
    private float[] delayBuffer;
    private int delayPosition;
    
    // Chorus parameters
    private boolean chorusEnabled = false;
    private float chorusRate = 2.0f;     // 0.5 to 10.0 Hz
    private float chorusDepth = 0.5f;    // 0.1 to 1.0
    private float chorusMix = 0.4f;      // 0.0 to 1.0
    private LFO chorusLFO;
    private float[] chorusBuffer;
    private int chorusPosition;
    
    // Distortion parameters
    private boolean distortionEnabled = false;
    private float distortionDrive = 2.0f;  // 1.0 to 10.0
    private float distortionMix = 1.0f;    // 0.0 to 1.0
    
    // Compressor parameters
    private boolean compressorEnabled = false;
    private float compressorThreshold = -12.0f;  // -30.0 to 0.0 dB
    private float compressorRatio = 4.0f;        // 1.0 to 20.0
    private float compressorAttack = 0.003f;     // 0.001 to 0.1 seconds
    private float compressorRelease = 0.1f;      // 0.01 to 1.0 seconds
    private float compressorGain = 0.0f;         // Current gain reduction
    
    // EQ parameters
    private boolean eqEnabled = false;
    private ResonantFilter lowShelf;
    private ResonantFilter midPeak;
    private ResonantFilter highShelf;
    
    public EffectsChain(int sampleRate) {
        this.sampleRate = sampleRate;
        
        // Initialize effect buffers
        initializeBuffers();
        
        // Initialize EQ filters
        lowShelf = new ResonantFilter(sampleRate);
        lowShelf.setCutoffFrequency(200.0f);
        lowShelf.setFilterType(ResonantFilter.FilterType.LOW_PASS);
        
        midPeak = new ResonantFilter(sampleRate);
        midPeak.setCutoffFrequency(1000.0f);
        midPeak.setFilterType(ResonantFilter.FilterType.BAND_PASS);
        
        highShelf = new ResonantFilter(sampleRate);
        highShelf.setCutoffFrequency(5000.0f);
        highShelf.setFilterType(ResonantFilter.FilterType.HIGH_PASS);
        
        // Initialize chorus LFO
        chorusLFO = new LFO(sampleRate);
        chorusLFO.setFrequency(chorusRate);
        chorusLFO.setWaveform(LFO.LFOWaveform.SINE);
    }
    
    private void initializeBuffers() {
        // Reverb buffer (up to 2 seconds)
        int reverbBufferSize = (int) (sampleRate * 2.0f);
        reverbBuffer = new float[reverbBufferSize];
        reverbPosition = 0;
        
        // Delay buffer (up to 2 seconds)
        int delayBufferSize = (int) (sampleRate * 2.0f);
        delayBuffer = new float[delayBufferSize];
        delayPosition = 0;
        
        // Chorus buffer (50ms max delay)
        int chorusBufferSize = (int) (sampleRate * 0.05f);
        chorusBuffer = new float[chorusBufferSize];
        chorusPosition = 0;
    }
    
    /**
     * Process audio buffer through the effects chain
     */
    public void process(float[] buffer) {
        // Process each effect in order
        if (distortionEnabled) {
            applyDistortion(buffer);
        }
        
        if (compressorEnabled) {
            applyCompressor(buffer);
        }
        
        if (eqEnabled) {
            applyEqualizer(buffer);
        }
        
        if (chorusEnabled) {
            applyChorus(buffer);
        }
        
        if (delayEnabled) {
            applyDelay(buffer);
        }
        
        if (reverbEnabled) {
            applyReverb(buffer);
        }
    }
    
    private void applyDistortion(float[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            float input = buffer[i];
            
            // Soft clipping distortion
            float driven = input * distortionDrive;
            float distorted = (float) Math.tanh(driven);
            
            // Mix with original signal
            buffer[i] = input * (1.0f - distortionMix) + distorted * distortionMix;
        }
    }
    
    private void applyCompressor(float[] buffer) {
        float attackCoeff = (float) Math.exp(-1.0f / (compressorAttack * sampleRate));
        float releaseCoeff = (float) Math.exp(-1.0f / (compressorRelease * sampleRate));
        
        for (int i = 0; i < buffer.length; i++) {
            float input = buffer[i];
            float inputLevel = Math.abs(input);
            
            // Convert threshold from dB to linear
            float thresholdLinear = (float) Math.pow(10.0f, compressorThreshold / 20.0f);
            
            if (inputLevel > thresholdLinear) {
                // Calculate desired gain reduction
                float overThreshold = inputLevel / thresholdLinear;
                float desiredGain = 1.0f / (1.0f + (overThreshold - 1.0f) * (compressorRatio - 1.0f) / compressorRatio);
                
                // Apply attack/release
                if (desiredGain < compressorGain) {
                    compressorGain = desiredGain + (compressorGain - desiredGain) * attackCoeff;
                } else {
                    compressorGain = desiredGain + (compressorGain - desiredGain) * releaseCoeff;
                }
            } else {
                // Release towards 1.0
                compressorGain = 1.0f + (compressorGain - 1.0f) * releaseCoeff;
            }
            
            buffer[i] = input * compressorGain;
        }
    }
    
    private void applyEqualizer(float[] buffer) {
        // Simple 3-band EQ using cascaded filters
        lowShelf.process(buffer);
        midPeak.process(buffer);
        highShelf.process(buffer);
    }
    
    private void applyChorus(float[] buffer) {
        float baseDelay = 0.02f; // 20ms base delay
        chorusLFO.setFrequency(chorusRate);
        
        for (int i = 0; i < buffer.length; i++) {
            float input = buffer[i];
            
            // Get LFO value for modulation
            float lfoValue = chorusLFO.getNextValue();
            float delayMs = baseDelay + (chorusDepth * 0.01f * lfoValue);
            int delaySamples = (int) (delayMs * sampleRate);
            
            // Read from chorus buffer with interpolation
            int readPos = (chorusPosition - delaySamples + chorusBuffer.length) % chorusBuffer.length;
            float delayed = chorusBuffer[readPos];
            
            // Write current sample to buffer
            chorusBuffer[chorusPosition] = input;
            chorusPosition = (chorusPosition + 1) % chorusBuffer.length;
            
            // Mix with original
            buffer[i] = input * (1.0f - chorusMix) + delayed * chorusMix;
        }
    }
    
    private void applyDelay(float[] buffer) {
        int delaySamples = (int) (delayTime * sampleRate);
        
        for (int i = 0; i < buffer.length; i++) {
            float input = buffer[i];
            
            // Read delayed sample
            int readPos = (delayPosition - delaySamples + delayBuffer.length) % delayBuffer.length;
            float delayed = delayBuffer[readPos];
            
            // Write input + feedback to delay buffer
            delayBuffer[delayPosition] = input + delayed * delayFeedback;
            delayPosition = (delayPosition + 1) % delayBuffer.length;
            
            // Mix with original
            buffer[i] = input * (1.0f - delayMix) + delayed * delayMix;
        }
    }
    
    private void applyReverb(float[] buffer) {
        // Simple reverb using multiple delay lines with different lengths
        int[] delayLengths = {
            (int) (0.03f * sampleRate),  // 30ms
            (int) (0.07f * sampleRate),  // 70ms
            (int) (0.15f * sampleRate),  // 150ms
            (int) (0.31f * sampleRate)   // 310ms
        };
        
        for (int i = 0; i < buffer.length; i++) {
            float input = buffer[i];
            float reverbSum = 0.0f;
            
            // Sum multiple delay taps
            for (int delayLength : delayLengths) {
                int readPos = (reverbPosition - delayLength + reverbBuffer.length) % reverbBuffer.length;
                reverbSum += reverbBuffer[readPos] * 0.25f; // Divide by number of taps
            }
            
            // Apply decay and write to buffer
            reverbBuffer[reverbPosition] = input + reverbSum * reverbDecay;
            reverbPosition = (reverbPosition + 1) % reverbBuffer.length;
            
            // Mix with original
            buffer[i] = input * (1.0f - reverbMix) + reverbSum * reverbMix;
        }
    }
    
    /**
     * Clear all effect buffers
     */
    public void clearBuffers() {
        Arrays.fill(reverbBuffer, 0.0f);
        Arrays.fill(delayBuffer, 0.0f);
        Arrays.fill(chorusBuffer, 0.0f);
        reverbPosition = 0;
        delayPosition = 0;
        chorusPosition = 0;
        compressorGain = 1.0f;
        
        // Reset EQ filters
        lowShelf.reset();
        midPeak.reset();
        highShelf.reset();
    }
    
    // Effect enable/disable methods
    public boolean isEffectEnabled(EffectType effect) {
        switch (effect) {
            case REVERB: return reverbEnabled;
            case DELAY: return delayEnabled;
            case CHORUS: return chorusEnabled;
            case DISTORTION: return distortionEnabled;
            case COMPRESSOR: return compressorEnabled;
            case EQ: return eqEnabled;
            default: return false;
        }
    }
    
    public void setEffectEnabled(EffectType effect, boolean enabled) {
        switch (effect) {
            case REVERB: reverbEnabled = enabled; break;
            case DELAY: delayEnabled = enabled; break;
            case CHORUS: chorusEnabled = enabled; break;
            case DISTORTION: distortionEnabled = enabled; break;
            case COMPRESSOR: compressorEnabled = enabled; break;
            case EQ: eqEnabled = enabled; break;
        }
    }
    
    // Getter and setter methods for effect parameters
    public float getReverbMix() { return reverbMix; }
    public void setReverbMix(float mix) { this.reverbMix = Math.max(0.0f, Math.min(1.0f, mix)); }
    
    public float getReverbDecay() { return reverbDecay; }
    public void setReverbDecay(float decay) { this.reverbDecay = Math.max(0.1f, Math.min(0.9f, decay)); }
    
    public float getDelayTime() { return delayTime; }
    public void setDelayTime(float time) { this.delayTime = Math.max(0.1f, Math.min(2.0f, time)); }
    
    public float getDelayFeedback() { return delayFeedback; }
    public void setDelayFeedback(float feedback) { this.delayFeedback = Math.max(0.0f, Math.min(0.9f, feedback)); }
    
    public float getDelayMix() { return delayMix; }
    public void setDelayMix(float mix) { this.delayMix = Math.max(0.0f, Math.min(1.0f, mix)); }
    
    public float getChorusRate() { return chorusRate; }
    public void setChorusRate(float rate) { 
        this.chorusRate = Math.max(0.5f, Math.min(10.0f, rate));
        chorusLFO.setFrequency(this.chorusRate);
    }
    
    public float getChorusDepth() { return chorusDepth; }
    public void setChorusDepth(float depth) { this.chorusDepth = Math.max(0.1f, Math.min(1.0f, depth)); }
    
    public float getChorusMix() { return chorusMix; }
    public void setChorusMix(float mix) { this.chorusMix = Math.max(0.0f, Math.min(1.0f, mix)); }
    
    public float getDistortionDrive() { return distortionDrive; }
    public void setDistortionDrive(float drive) { this.distortionDrive = Math.max(1.0f, Math.min(10.0f, drive)); }
    
    public float getDistortionMix() { return distortionMix; }
    public void setDistortionMix(float mix) { this.distortionMix = Math.max(0.0f, Math.min(1.0f, mix)); }
    
    public float getCompressorThreshold() { return compressorThreshold; }
    public void setCompressorThreshold(float threshold) { this.compressorThreshold = Math.max(-30.0f, Math.min(0.0f, threshold)); }
    
    public float getCompressorRatio() { return compressorRatio; }
    public void setCompressorRatio(float ratio) { this.compressorRatio = Math.max(1.0f, Math.min(20.0f, ratio)); }
    
    public ResonantFilter getLowShelfFilter() { return lowShelf; }
    public ResonantFilter getMidPeakFilter() { return midPeak; }
    public ResonantFilter getHighShelfFilter() { return highShelf; }
}