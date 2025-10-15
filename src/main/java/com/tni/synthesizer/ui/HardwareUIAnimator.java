package com.tni.synthesizer.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * Animation controller for hardware-style UI
 * Simulates live audio data and VU meter activity
 */
public class HardwareUIAnimator {
    
    private Timer animationTimer;
    private Random random;
    private HardwareStyleUI.HardwareChannelStrip[] channels;
    private boolean isRunning = false;
    
    public HardwareUIAnimator() {
        this.random = new Random();
        this.animationTimer = new Timer(50, new AnimationUpdateListener()); // 20 FPS
    }
    
    public void setChannels(HardwareStyleUI.HardwareChannelStrip[] channels) {
        this.channels = channels;
    }
    
    public void start() {
        if (!isRunning && channels != null) {
            isRunning = true;
            animationTimer.start();
        }
    }
    
    public void stop() {
        if (isRunning) {
            isRunning = false;
            animationTimer.stop();
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    private class AnimationUpdateListener implements ActionListener {
        private int frameCount = 0;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (channels == null) return;
            
            frameCount++;
            
            for (int i = 0; i < channels.length; i++) {
                if (channels[i] != null) {
                    // Generate fake audio waveform data
                    float[] waveform = generateWaveform(i, frameCount);
                    channels[i].updateOscilloscope(waveform);
                    
                    // Generate VU meter levels
                    float vuLevel = generateVULevel(i, frameCount);
                    channels[i].updateVUMeter(vuLevel);
                }
            }
        }
        
        private float[] generateWaveform(int channelIndex, int frame) {
            float[] data = new float[64];
            
            // Create different waveform characteristics per channel
            float frequency = 0.1f + (channelIndex * 0.05f);
            float amplitude = 0.3f + (float) Math.sin(frame * 0.02f + channelIndex) * 0.2f;
            float phase = (float) (channelIndex * Math.PI / 4);
            
            for (int i = 0; i < data.length; i++) {
                float t = (frame + i) * frequency + phase;
                
                // Mix different waveforms for variety
                float sine = (float) Math.sin(t);
                float triangle = (float) (2.0 * Math.asin(Math.sin(t)) / Math.PI);
                float noise = (random.nextFloat() - 0.5f) * 0.1f;
                
                // Combine waveforms based on channel
                switch (channelIndex % 3) {
                    case 0: // Sine wave with some noise
                        data[i] = amplitude * (sine * 0.8f + noise);
                        break;
                    case 1: // Triangle wave with harmonics
                        data[i] = amplitude * (triangle * 0.7f + sine * 0.3f + noise);
                        break;
                    case 2: // Complex waveform
                        data[i] = amplitude * (sine * 0.5f + 
                                 (float) Math.sin(t * 2) * 0.3f + 
                                 (float) Math.sin(t * 3) * 0.2f + noise);
                        break;
                }
            }
            
            return data;
        }
        
        private float generateVULevel(int channelIndex, int frame) {
            // Create realistic VU meter behavior
            float baseLevel = 0.3f + (float) Math.sin(frame * 0.03f + channelIndex * 0.7f) * 0.25f;
            float peaks = (float) Math.sin(frame * 0.2f + channelIndex) * 0.3f;
            float randomVariation = (random.nextFloat() - 0.5f) * 0.1f;
            
            float level = Math.abs(baseLevel + peaks + randomVariation);
            
            // Occasional peak bursts
            if (random.nextFloat() < 0.02f) {
                level = Math.min(1.0f, level + 0.4f);
            }
            
            return Math.max(0.0f, Math.min(1.0f, level));
        }
    }
}