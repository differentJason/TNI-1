package com.tni.synthesizer.generative;

/**
 * Resonant filter with low-pass, high-pass, and band-pass modes
 */
public class ResonantFilter {
    
    public enum FilterType {
        LOW_PASS("Low Pass"),
        HIGH_PASS("High Pass"),
        BAND_PASS("Band Pass"),
        NOTCH("Notch");
        
        private final String displayName;
        
        FilterType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final int sampleRate;
    
    // Filter parameters
    private float cutoffFrequency; // 20 Hz to 20000 Hz
    private float resonance;       // 0.1 to 10.0 (Q factor)
    private FilterType filterType;
    
    // Filter state variables (for biquad implementation)
    private float x1, x2; // Input delay line
    private float y1, y2; // Output delay line
    
    // Biquad coefficients
    private float a0, a1, a2; // Numerator coefficients
    private float b1, b2;     // Denominator coefficients (b0 is normalized to 1)
    
    public ResonantFilter(int sampleRate) {
        this.sampleRate = sampleRate;
        
        // Default settings
        this.cutoffFrequency = 1000.0f; // 1 kHz
        this.resonance = 1.0f;          // Q = 1 (no resonance)
        this.filterType = FilterType.LOW_PASS;
        
        // Initialize state
        reset();
        calculateCoefficients();
    }
    
    /**
     * Process a single sample through the filter
     */
    public float process(float input) {
        // Biquad filter implementation: y[n] = a0*x[n] + a1*x[n-1] + a2*x[n-2] - b1*y[n-1] - b2*y[n-2]
        float output = a0 * input + a1 * x1 + a2 * x2 - b1 * y1 - b2 * y2;
        
        // Shift delay lines
        x2 = x1;
        x1 = input;
        y2 = y1;
        y1 = output;
        
        return output;
    }
    
    /**
     * Process a buffer of samples
     */
    public void process(float[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = process(buffer[i]);
        }
    }
    
    /**
     * Reset filter state
     */
    public void reset() {
        x1 = x2 = 0.0f;
        y1 = y2 = 0.0f;
    }
    
    /**
     * Calculate biquad coefficients based on current settings
     */
    private void calculateCoefficients() {
        // Normalize cutoff frequency
        float normalizedFreq = cutoffFrequency / (sampleRate * 0.5f);
        normalizedFreq = Math.max(0.001f, Math.min(0.99f, normalizedFreq));
        
        // Calculate intermediate values
        float omega = (float) (Math.PI * normalizedFreq);
        float sin = (float) Math.sin(omega);
        float cos = (float) Math.cos(omega);
        float alpha = sin / (2.0f * resonance);
        
        float b0; // Will be used for normalization
        
        switch (filterType) {
            case LOW_PASS:
                b0 = 1.0f + alpha;
                a0 = ((1.0f - cos) / 2.0f) / b0;
                a1 = (1.0f - cos) / b0;
                a2 = ((1.0f - cos) / 2.0f) / b0;
                b1 = (-2.0f * cos) / b0;
                b2 = (1.0f - alpha) / b0;
                break;
                
            case HIGH_PASS:
                b0 = 1.0f + alpha;
                a0 = ((1.0f + cos) / 2.0f) / b0;
                a1 = -(1.0f + cos) / b0;
                a2 = ((1.0f + cos) / 2.0f) / b0;
                b1 = (-2.0f * cos) / b0;
                b2 = (1.0f - alpha) / b0;
                break;
                
            case BAND_PASS:
                b0 = 1.0f + alpha;
                a0 = alpha / b0;
                a1 = 0.0f / b0;
                a2 = -alpha / b0;
                b1 = (-2.0f * cos) / b0;
                b2 = (1.0f - alpha) / b0;
                break;
                
            case NOTCH:
                b0 = 1.0f + alpha;
                a0 = 1.0f / b0;
                a1 = (-2.0f * cos) / b0;
                a2 = 1.0f / b0;
                b1 = (-2.0f * cos) / b0;
                b2 = (1.0f - alpha) / b0;
                break;
                
            default:
                // Pass-through (no filtering)
                a0 = 1.0f; a1 = 0.0f; a2 = 0.0f;
                b1 = 0.0f; b2 = 0.0f;
                break;
        }
    }
    
    // Getters and setters
    public float getCutoffFrequency() {
        return cutoffFrequency;
    }
    
    public void setCutoffFrequency(float cutoffFrequency) {
        this.cutoffFrequency = Math.max(20.0f, Math.min(20000.0f, cutoffFrequency));
        calculateCoefficients();
    }
    
    public float getResonance() {
        return resonance;
    }
    
    public void setResonance(float resonance) {
        this.resonance = Math.max(0.1f, Math.min(10.0f, resonance));
        calculateCoefficients();
    }
    
    public FilterType getFilterType() {
        return filterType;
    }
    
    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
        calculateCoefficients();
    }
}