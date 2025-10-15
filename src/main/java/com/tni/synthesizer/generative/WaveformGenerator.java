package com.tni.synthesizer.generative;

import java.util.Random;

/**
 * Waveform generator that produces different waveform types
 */
public class WaveformGenerator {
    
    private final int sampleRate;
    private final Random random;
    
    public WaveformGenerator(int sampleRate) {
        this.sampleRate = sampleRate;
        this.random = new Random();
    }
    
    /**
     * Generate a single sample of the specified waveform
     * @param waveform The waveform type to generate
     * @param phase Current phase (0.0 to 1.0)
     * @return Sample value (-1.0 to 1.0)
     */
    public float generateSample(SynthChannel.Waveform waveform, double phase) {
        switch (waveform) {
            case SINE:
                return (float) Math.sin(phase * 2.0 * Math.PI);
                
            case SAWTOOTH:
                return (float) (2.0 * (phase - Math.floor(phase + 0.5)));
                
            case SQUARE:
                return phase < 0.5 ? 1.0f : -1.0f;
                
            case TRIANGLE:
                double t = phase * 4.0;
                if (t < 1.0) {
                    return (float) t;
                } else if (t < 3.0) {
                    return (float) (2.0 - t);
                } else {
                    return (float) (t - 4.0);
                }
                
            case NOISE:
                return (random.nextFloat() * 2.0f) - 1.0f;
                
            default:
                return 0.0f;
        }
    }
    
    /**
     * Generate multiple samples into a buffer
     */
    public void generateSamples(SynthChannel.Waveform waveform, float[] buffer, 
                               double startPhase, double phaseIncrement) {
        double phase = startPhase;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = generateSample(waveform, phase);
            phase += phaseIncrement;
            if (phase >= 1.0) {
                phase -= 1.0;
            }
        }
    }
    
    /**
     * Calculate phase increment for a given frequency
     */
    public double calculatePhaseIncrement(float frequency) {
        return frequency / sampleRate;
    }
}