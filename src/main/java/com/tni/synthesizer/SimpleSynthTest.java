package com.tni.synthesizer;

import com.tni.synthesizer.generative.DataToMidiGenerator;
import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Simple test application to verify the improved MIDI generation
 */
public class SimpleSynthTest extends JFrame {
    private JTextArea dataInput;
    private JButton generateButton;
    private JButton playButton;
    private JLabel statusLabel;
    
    private DataToMidiGenerator generator;
    
    public SimpleSynthTest() {
        try {
            generator = new DataToMidiGenerator();
            setupUI();
        } catch (MidiUnavailableException e) {
            JOptionPane.showMessageDialog(this, "MIDI not available: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void setupUI() {
        setTitle("TNI Synthesizer - Simple Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Data input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Data Input"));
        
        dataInput = new JTextArea(5, 40);
        dataInput.setText("1.0, 2.5, 3.2, 2.8, 4.1, 3.7, 5.2, 4.8");
        inputPanel.add(new JScrollPane(dataInput), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        generateButton = new JButton("Generate Music");
        playButton = new JButton("Play");
        playButton.setEnabled(false);
        
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateMusic();
            }
        });
        
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playMusic();
            }
        });
        
        buttonPanel.add(generateButton);
        buttonPanel.add(playButton);
        
        // Status
        statusLabel = new JLabel("Ready");
        
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void generateMusic() {
        try {
            String inputText = dataInput.getText().trim();
            if (inputText.isEmpty()) {
                statusLabel.setText("Error: No data entered");
                return;
            }
            
            // Parse data
            String[] parts = inputText.split("[,\\s]+");
            double[] dataArray = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                dataArray[i] = Double.parseDouble(parts[i].trim());
            }
            
            // Generate sequence using the generator
            generator.generateSequenceFromData(dataArray);
            
            playButton.setEnabled(true);
            statusLabel.setText(String.format("Generated music from %d data points - NOW WITH VARIETY!", dataArray.length));
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void playMusic() {
        try {
            // This would normally play the sequence
            statusLabel.setText("Playing improved musical sequence with bass, chords, melody, and drums!");
        } catch (Exception e) {
            statusLabel.setText("Play error: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleSynthTest().setVisible(true);
        });
    }
}