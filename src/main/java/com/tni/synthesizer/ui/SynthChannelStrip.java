package com.tni.synthesizer.ui;

import com.tni.synthesizer.generative.AdvancedAudioEngine;
import com.tni.synthesizer.generative.SynthChannel;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Synthesizer Channel Strip with hardware-style controls
 * Controls actual synthesizer parameters: filters, ADSR, pan, volume
 */
public class SynthChannelStrip extends JPanel {
    
    private final int channelNumber;
    private final int channelIndex;
    private AdvancedAudioEngine audioEngine;
    private HardwareStyleUI.VUMeter vuMeter;
    private List<HardwareStyleUI.RotaryKnob> knobs;
    
    // Control knobs
    private HardwareStyleUI.RotaryKnob volumeKnob;
    private HardwareStyleUI.RotaryKnob panKnob;
    private HardwareStyleUI.RotaryKnob attackKnob;
    private HardwareStyleUI.RotaryKnob decayKnob;
    private HardwareStyleUI.RotaryKnob sustainKnob;
    private HardwareStyleUI.RotaryKnob releaseKnob;
    private HardwareStyleUI.RotaryKnob cutoffKnob;
    private HardwareStyleUI.RotaryKnob resonanceKnob;
    
    // Controls
    private JComboBox<SynthChannel.Waveform> waveformCombo;
    private JButton muteButton;
    private JButton soloButton;
    private JCheckBox enabledCheckbox;
    
    public SynthChannelStrip(int channelNumber, int channelIndex) {
        this.channelNumber = channelNumber;
        this.channelIndex = channelIndex;
        this.knobs = new ArrayList<>();
        
        setBackground(HardwareStyleUI.CHANNEL_BACKGROUND);
        setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65), 1));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(140, 700));
        
        initializeComponents();
    }
    
    public void setAudioEngine(AdvancedAudioEngine audioEngine) {
        this.audioEngine = audioEngine;
    }
    
    private void initializeComponents() {
        // Top section with channel info and oscilloscope
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(HardwareStyleUI.CHANNEL_BACKGROUND);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Channel label and enable
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HardwareStyleUI.CHANNEL_BACKGROUND);
        
        JLabel channelLabel = new JLabel("CH " + channelNumber, SwingConstants.CENTER);
        channelLabel.setForeground(HardwareStyleUI.TEXT_COLOR);
        channelLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(channelLabel, BorderLayout.NORTH);
        
        enabledCheckbox = new JCheckBox("", true);
        enabledCheckbox.setBackground(HardwareStyleUI.CHANNEL_BACKGROUND);
        enabledCheckbox.setForeground(HardwareStyleUI.TEXT_COLOR);
        enabledCheckbox.addActionListener(e -> updateChannelEnabled());
        headerPanel.add(enabledCheckbox, BorderLayout.CENTER);
        
        topPanel.add(headerPanel, BorderLayout.NORTH);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Middle section with knobs
        JPanel knobPanel = new JPanel();
        knobPanel.setLayout(new BoxLayout(knobPanel, BoxLayout.Y_AXIS));
        knobPanel.setBackground(HardwareStyleUI.CHANNEL_BACKGROUND);
        knobPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        // Waveform selector
        JPanel wavePanel = new JPanel(new BorderLayout());
        wavePanel.setBackground(HardwareStyleUI.CHANNEL_BACKGROUND);
        wavePanel.setMaximumSize(new Dimension(130, 60));
        
        JLabel waveLabel = new JLabel("WAVE", SwingConstants.CENTER);
        waveLabel.setForeground(HardwareStyleUI.LABEL_COLOR);
        waveLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        wavePanel.add(waveLabel, BorderLayout.NORTH);
        
        waveformCombo = new JComboBox<>(SynthChannel.Waveform.values());
        waveformCombo.setBackground(HardwareStyleUI.KNOB_BASE);
        waveformCombo.setForeground(HardwareStyleUI.TEXT_COLOR);
        waveformCombo.addActionListener(e -> updateWaveform());
        wavePanel.add(waveformCombo, BorderLayout.CENTER);
        
        knobPanel.add(wavePanel);
        knobPanel.add(Box.createVerticalStrut(10));
        
        // Volume and Pan
        volumeKnob = addKnobSection(knobPanel, "VOLUME", 0.8f, this::updateVolume);
        panKnob = addKnobSection(knobPanel, "PAN", 0.5f, this::updatePan);
        
        // ADSR Controls
        knobPanel.add(createSectionLabel("ADSR"));
        attackKnob = addKnobSection(knobPanel, "ATTACK", 0.1f, this::updateAttack);
        decayKnob = addKnobSection(knobPanel, "DECAY", 0.3f, this::updateDecay);
        sustainKnob = addKnobSection(knobPanel, "SUSTAIN", 0.7f, this::updateSustain);
        releaseKnob = addKnobSection(knobPanel, "RELEASE", 0.5f, this::updateRelease);
        
        // Filter Controls
        knobPanel.add(createSectionLabel("FILTER"));
        cutoffKnob = addKnobSection(knobPanel, "CUTOFF", 0.8f, this::updateCutoff);
        resonanceKnob = addKnobSection(knobPanel, "RES", 0.2f, this::updateResonance);
        
        add(knobPanel, BorderLayout.CENTER);
        
        // Bottom section with controls
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(HardwareStyleUI.CHANNEL_BACKGROUND);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        
        // Mute/Solo buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 2, 2));
        buttonPanel.setBackground(HardwareStyleUI.CHANNEL_BACKGROUND);
        
        muteButton = createLEDButton("M", HardwareStyleUI.RED_LED);
        soloButton = createLEDButton("S", HardwareStyleUI.YELLOW_LED);
        
        buttonPanel.add(muteButton);
        buttonPanel.add(soloButton);
        bottomPanel.add(buttonPanel);
        
        // VU Meter
        vuMeter = new HardwareStyleUI.VUMeter();
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(vuMeter);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Oscilloscope will be fed real data from MIDI generator
    }
    
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(HardwareStyleUI.CYAN_ACCENT);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setMaximumSize(new Dimension(130, 20));
        return label;
    }
    
    private HardwareStyleUI.RotaryKnob addKnobSection(JPanel parent, String label, float initialValue, Runnable updateCallback) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(HardwareStyleUI.CHANNEL_BACKGROUND);
        section.setMaximumSize(new Dimension(130, 80));
        
        JLabel knobLabel = new JLabel(label, SwingConstants.CENTER);
        knobLabel.setForeground(HardwareStyleUI.LABEL_COLOR);
        knobLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        section.add(knobLabel, BorderLayout.NORTH);
        
        HardwareStyleUI.RotaryKnob knob = new HardwareStyleUI.RotaryKnob(initialValue);
        knob.addChangeListener(value -> updateCallback.run());
        knobs.add(knob);
        section.add(knob, BorderLayout.CENTER);
        
        parent.add(section);
        parent.add(Box.createVerticalStrut(5));
        
        return knob;
    }
    
    private JButton createLEDButton(String text, Color ledColor) {
        JButton button = new JButton(text) {
            private boolean isActive = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Button background
                g2d.setColor(isActive ? ledColor.darker() : HardwareStyleUI.KNOB_BASE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // LED indicator
                if (isActive) {
                    g2d.setColor(ledColor);
                    g2d.fillOval(getWidth() - 12, 2, 8, 8);
                }
                
                // Text
                g2d.setColor(HardwareStyleUI.TEXT_COLOR);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent()) / 2;
                g2d.drawString(getText(), textX, textY);
                
                g2d.dispose();
            }
            
            @Override
            public void setSelected(boolean selected) {
                this.isActive = selected;
                super.setSelected(selected);
                repaint();
            }
        };
        
        button.setPreferredSize(new Dimension(50, 25));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.addActionListener(e -> button.setSelected(!button.isSelected()));
        
        return button;
    }
    
    // Parameter update methods that interface with the synthesizer
    private void updateChannelEnabled() {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null) {
                channel.setEnabled(enabledCheckbox.isSelected());
            }
        }
    }
    
    private void updateWaveform() {
        if (audioEngine != null && waveformCombo.getSelectedItem() != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null) {
                SynthChannel.Waveform waveform = (SynthChannel.Waveform) waveformCombo.getSelectedItem();
                channel.setWaveform(waveform);
            }
        }
    }
    
    private void updateVolume() {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null) {
                float volume = volumeKnob.getValue();
                channel.setVolume(volume);
            }
        }
    }
    
    private void updatePan() {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null) {
                float pan = (panKnob.getValue() - 0.5f) * 2.0f; // Convert 0-1 to -1 to 1
                channel.setPan(pan);
            }
        }
    }
    
    private void updateAttack() {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null && channel.getEnvelope() != null) {
                float attack = attackKnob.getValue() * 2.0f; // 0-2 seconds
                channel.getEnvelope().setAttackTime(attack);
            }
        }
    }
    
    private void updateDecay() {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null && channel.getEnvelope() != null) {
                float decay = decayKnob.getValue() * 2.0f; // 0-2 seconds
                channel.getEnvelope().setDecayTime(decay);
            }
        }
    }
    
    private void updateSustain() {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null && channel.getEnvelope() != null) {
                float sustain = sustainKnob.getValue(); // 0-1 level
                channel.getEnvelope().setSustainLevel(sustain);
            }
        }
    }
    
    private void updateRelease() {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null && channel.getEnvelope() != null) {
                float release = releaseKnob.getValue() * 3.0f; // 0-3 seconds
                channel.getEnvelope().setReleaseTime(release);
            }
        }
    }
    
    private void updateCutoff() {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null && channel.getFilter() != null) {
                float cutoff = cutoffKnob.getValue() * 20000.0f; // 0-20kHz
                channel.getFilter().setCutoffFrequency(cutoff);
            }
        }
    }
    
    private void updateResonance() {
        if (audioEngine != null) {
            SynthChannel channel = audioEngine.getChannel(channelIndex);
            if (channel != null && channel.getFilter() != null) {
                float resonance = resonanceKnob.getValue() * 10.0f; // 0-10 Q factor
                channel.getFilter().setResonance(resonance);
            }
        }
    }
    
    // Methods for external control and feedback
    
    public void updateVUMeter(float level) {
        if (vuMeter != null) {
            vuMeter.setLevel(level);
        }
    }
    
    public boolean isMuted() {
        return muteButton.isSelected();
    }
    
    public boolean isSoloed() {
        return soloButton.isSelected();
    }
    
    public boolean isEnabled() {
        return enabledCheckbox.isSelected();
    }
    
    public int getChannelIndex() {
        return channelIndex;
    }
}