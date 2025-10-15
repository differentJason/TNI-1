package com.tni.synthesizer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hardware-style UI with professional mixer aesthetics
 * Features vertical channel strips, oscilloscopes, and knob controls
 */
public class HardwareStyleUI {
    
    // Hardware color scheme - made public for use in other classes
    public static final Color PANEL_BACKGROUND = new Color(45, 45, 50);
    public static final Color CHANNEL_BACKGROUND = new Color(38, 40, 45);
    public static final Color KNOB_BASE = new Color(60, 65, 70);
    public static final Color KNOB_HIGHLIGHT = new Color(85, 90, 95);
    public static final Color CYAN_ACCENT = new Color(0, 200, 255);
    public static final Color GREEN_LED = new Color(0, 255, 100);
    public static final Color RED_LED = new Color(255, 50, 50);
    public static final Color YELLOW_LED = new Color(255, 255, 0);
    public static final Color TEXT_COLOR = new Color(200, 200, 200);
    public static final Color LABEL_COLOR = new Color(160, 160, 160);
    
    public static class HardwareChannelStrip extends JPanel {
        private final String channelName;
        private final int channelNumber;
        private MiniOscilloscope oscilloscope;
        private List<RotaryKnob> knobs;
        private JButton muteButton;
        private JButton soloButton;
        private VUMeter vuMeter;
        
        public HardwareChannelStrip(String name, int number) {
            this.channelName = name;
            this.channelNumber = number;
            this.knobs = new ArrayList<>();
            
            setBackground(CHANNEL_BACKGROUND);
            setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65), 1));
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(120, 600));
            
            initializeComponents();
        }
        
        private void initializeComponents() {
            // Top section with oscilloscope
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(CHANNEL_BACKGROUND);
            topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            // Channel label
            JLabel channelLabel = new JLabel(channelName, SwingConstants.CENTER);
            channelLabel.setForeground(TEXT_COLOR);
            channelLabel.setFont(new Font("Arial", Font.BOLD, 10));
            topPanel.add(channelLabel, BorderLayout.NORTH);
            
            // Mini oscilloscope
            oscilloscope = new MiniOscilloscope();
            topPanel.add(oscilloscope, BorderLayout.CENTER);
            
            add(topPanel, BorderLayout.NORTH);
            
            // Middle section with knobs
            JPanel knobPanel = new JPanel();
            knobPanel.setLayout(new BoxLayout(knobPanel, BoxLayout.Y_AXIS));
            knobPanel.setBackground(CHANNEL_BACKGROUND);
            knobPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
            
            // Add various knobs
            addKnobSection(knobPanel, "GAIN", 0.7f);
            addKnobSection(knobPanel, "HIGH", 0.5f);
            addKnobSection(knobPanel, "MID", 0.5f);
            addKnobSection(knobPanel, "LOW", 0.5f);
            addKnobSection(knobPanel, "AUX", 0.3f);
            
            add(knobPanel, BorderLayout.CENTER);
            
            // Bottom section with controls
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
            bottomPanel.setBackground(CHANNEL_BACKGROUND);
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
            
            // Mute/Solo buttons
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 2, 2));
            buttonPanel.setBackground(CHANNEL_BACKGROUND);
            
            muteButton = createLEDButton("M", RED_LED);
            soloButton = createLEDButton("S", YELLOW_LED);
            
            buttonPanel.add(muteButton);
            buttonPanel.add(soloButton);
            bottomPanel.add(buttonPanel);
            
            // VU Meter
            vuMeter = new VUMeter();
            bottomPanel.add(Box.createVerticalStrut(5));
            bottomPanel.add(vuMeter);
            
            // Channel number
            JLabel numberLabel = new JLabel(String.valueOf(channelNumber), SwingConstants.CENTER);
            numberLabel.setForeground(TEXT_COLOR);
            numberLabel.setFont(new Font("Arial", Font.BOLD, 14));
            bottomPanel.add(Box.createVerticalStrut(5));
            bottomPanel.add(numberLabel);
            
            add(bottomPanel, BorderLayout.SOUTH);
        }
        
        private void addKnobSection(JPanel parent, String label, float initialValue) {
            JPanel section = new JPanel(new BorderLayout());
            section.setBackground(CHANNEL_BACKGROUND);
            section.setMaximumSize(new Dimension(110, 80));
            
            JLabel knobLabel = new JLabel(label, SwingConstants.CENTER);
            knobLabel.setForeground(LABEL_COLOR);
            knobLabel.setFont(new Font("Arial", Font.PLAIN, 9));
            section.add(knobLabel, BorderLayout.NORTH);
            
            RotaryKnob knob = new RotaryKnob(initialValue);
            knobs.add(knob);
            section.add(knob, BorderLayout.CENTER);
            
            parent.add(section);
            parent.add(Box.createVerticalStrut(5));
        }
        
        private JButton createLEDButton(String text, Color ledColor) {
            JButton button = new JButton(text) {
                private boolean isActive = false;
                
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Button background
                    g2d.setColor(isActive ? ledColor.darker() : KNOB_BASE);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    
                    // LED indicator
                    if (isActive) {
                        g2d.setColor(ledColor);
                        g2d.fillOval(getWidth() - 12, 2, 8, 8);
                    }
                    
                    // Text
                    g2d.setColor(TEXT_COLOR);
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
            
            button.setPreferredSize(new Dimension(45, 25));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(e -> button.setSelected(!button.isSelected()));
            
            return button;
        }
        
        public void updateOscilloscope(float[] audioData) {
            if (oscilloscope != null) {
                oscilloscope.updateWaveform(audioData);
            }
        }
        
        public void updateVUMeter(float level) {
            if (vuMeter != null) {
                vuMeter.setLevel(level);
            }
        }
    }
    
    /**
     * Professional rotary knob component
     */
    public static class RotaryKnob extends JComponent {
        private float value;
        private float minValue = 0.0f;
        private float maxValue = 1.0f;
        private boolean isDragging = false;
        private int lastMouseY;
        private List<ChangeListener> listeners = new ArrayList<>();
        
        public interface ChangeListener {
            void valueChanged(float newValue);
        }
        
        public RotaryKnob(float initialValue) {
            this.value = Math.max(minValue, Math.min(maxValue, initialValue));
            setPreferredSize(new Dimension(50, 50));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    isDragging = true;
                    lastMouseY = e.getY();
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    isDragging = false;
                }
            });
            
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging) {
                        int deltaY = lastMouseY - e.getY();
                        float deltaValue = deltaY * 0.01f;
                        setValue(value + deltaValue);
                        lastMouseY = e.getY();
                    }
                }
            });
        }
        
        public void setValue(float newValue) {
            float oldValue = this.value;
            this.value = Math.max(minValue, Math.min(maxValue, newValue));
            if (oldValue != this.value) {
                repaint();
                notifyListeners();
            }
        }
        
        public float getValue() {
            return value;
        }
        
        public void addChangeListener(ChangeListener listener) {
            listeners.add(listener);
        }
        
        private void notifyListeners() {
            for (ChangeListener listener : listeners) {
                listener.valueChanged(value);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            
            // Knob base
            g2d.setColor(KNOB_BASE);
            g2d.fillOval(x, y, size, size);
            
            // Knob highlight
            g2d.setColor(KNOB_HIGHLIGHT);
            g2d.drawOval(x, y, size, size);
            g2d.drawOval(x + 1, y + 1, size - 2, size - 2);
            
            // Value indicator
            double angle = -140 + (value * 280); // -140 to +140 degrees
            double radians = Math.toRadians(angle);
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            int indicatorLength = size / 3;
            
            int endX = (int) (centerX + Math.cos(radians) * indicatorLength);
            int endY = (int) (centerY + Math.sin(radians) * indicatorLength);
            
            g2d.setColor(CYAN_ACCENT);
            g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(centerX, centerY, endX, endY);
            
            // Center dot
            g2d.setColor(CYAN_ACCENT);
            g2d.fillOval(centerX - 2, centerY - 2, 4, 4);
            
            g2d.dispose();
        }
    }
    
    /**
     * Mini oscilloscope for each channel
     */
    public static class MiniOscilloscope extends JPanel {
        private float[] waveformData = new float[64];
        
        public MiniOscilloscope() {
            setPreferredSize(new Dimension(100, 60));
            setBackground(new Color(20, 25, 30));
            setBorder(BorderFactory.createLoweredBevelBorder());
        }
        
        public void updateWaveform(float[] data) {
            if (data != null && data.length > 0) {
                // Downsample to fit our display
                int step = Math.max(1, data.length / waveformData.length);
                for (int i = 0; i < waveformData.length && i * step < data.length; i++) {
                    waveformData[i] = data[i * step];
                }
                repaint();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth() - 4;
            int height = getHeight() - 4;
            int centerY = height / 2 + 2;
            
            // Grid lines
            g2d.setColor(new Color(40, 50, 60));
            g2d.drawLine(2, centerY, width + 2, centerY); // Center line
            
            // Waveform
            if (waveformData.length > 1) {
                g2d.setColor(CYAN_ACCENT);
                g2d.setStroke(new BasicStroke(1.5f));
                
                for (int i = 0; i < waveformData.length - 1; i++) {
                    int x1 = 2 + (i * width) / (waveformData.length - 1);
                    int y1 = centerY - (int) (waveformData[i] * height / 4);
                    int x2 = 2 + ((i + 1) * width) / (waveformData.length - 1);
                    int y2 = centerY - (int) (waveformData[i + 1] * height / 4);
                    
                    y1 = Math.max(2, Math.min(height + 2, y1));
                    y2 = Math.max(2, Math.min(height + 2, y2));
                    
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
            
            g2d.dispose();
        }
    }
    
    /**
     * VU Meter component
     */
    public static class VUMeter extends JPanel {
        private float level = 0.0f;
        private final int NUM_SEGMENTS = 12;
        
        public VUMeter() {
            setPreferredSize(new Dimension(20, 150));
            setBackground(CHANNEL_BACKGROUND);
        }
        
        public void setLevel(float level) {
            this.level = Math.max(0.0f, Math.min(1.0f, level));
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            
            int width = getWidth() - 4;
            int height = getHeight() - 4;
            int segmentHeight = height / NUM_SEGMENTS;
            
            for (int i = 0; i < NUM_SEGMENTS; i++) {
                int y = height - (i + 1) * segmentHeight + 2;
                float segmentThreshold = (float) i / NUM_SEGMENTS;
                
                Color segmentColor;
                if (i < NUM_SEGMENTS * 0.7) {
                    segmentColor = GREEN_LED;
                } else if (i < NUM_SEGMENTS * 0.9) {
                    segmentColor = YELLOW_LED;
                } else {
                    segmentColor = RED_LED;
                }
                
                if (level > segmentThreshold) {
                    g2d.setColor(segmentColor);
                } else {
                    g2d.setColor(segmentColor.darker().darker());
                }
                
                g2d.fillRect(2, y, width, segmentHeight - 1);
            }
            
            g2d.dispose();
        }
    }
    
    /**
     * Create the main hardware-style mixer panel
     */
    public static JPanel createHardwareMixerPanel(int numberOfChannels) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(PANEL_BACKGROUND);
        
        // Channel strips panel
        JPanel channelPanel = new JPanel();
        channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.X_AXIS));
        channelPanel.setBackground(PANEL_BACKGROUND);
        channelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create channel strips
        for (int i = 1; i <= numberOfChannels; i++) {
            HardwareChannelStrip channel = new HardwareChannelStrip("AUX " + i, i);
            channelPanel.add(channel);
            if (i < numberOfChannels) {
                channelPanel.add(Box.createHorizontalStrut(5));
            }
        }
        
        // Add master section
        HardwareChannelStrip masterChannel = new HardwareChannelStrip("MAIN", 0);
        channelPanel.add(Box.createHorizontalStrut(10));
        channelPanel.add(masterChannel);
        
        JScrollPane scrollPane = new JScrollPane(channelPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(PANEL_BACKGROUND);
        scrollPane.getViewport().setBackground(PANEL_BACKGROUND);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
}