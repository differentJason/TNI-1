package com.tni.synthesizer.generative;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts images to musical data by analyzing pixel values, colors, and patterns
 */
public class ImageToMusicSynthesizer {
    
    // Instance variables for real-time image processing
    private BufferedImage currentImage;
    private BufferedImage currentRegion;
    private Rectangle selectedArea;
    private float brightnessMapping = 0.8f;
    private float colorMapping = 0.6f;
    private float saturationMapping = 0.7f;
    
    // Enhanced MIDI data structure to hold trimmed overflow for ADSR
    private static class EnhancedMidiData {
        public int value;           // Trimmed value within MIDI range
        public int overflow;        // Overflow to be used for ADSR decay
        public float decayFactor;   // Calculated decay factor from overflow
        
        public EnhancedMidiData(int originalValue) {
            if (originalValue >= 0 && originalValue <= 127) {
                // Value is already in valid MIDI range
                this.value = originalValue;
                this.overflow = 0;
                this.decayFactor = 1.0f;
            } else if (originalValue > 127) {
                // Apply user's trimming approach: 11723 → 117
                String valueStr = String.valueOf(originalValue);
                String trimmed = "";
                
                // Build the largest valid MIDI value from left digits
                for (int i = 1; i <= valueStr.length(); i++) {
                    String candidate = valueStr.substring(0, i);
                    int candidateValue = Integer.parseInt(candidate);
                    if (candidateValue <= 127) {
                        trimmed = candidate;
                    } else {
                        break;
                    }
                }
                
                if (trimmed.isEmpty()) {
                    // Use first digit if nothing else works
                    this.value = Math.min(127, Character.getNumericValue(valueStr.charAt(0)));
                    this.overflow = originalValue - this.value;
                } else {
                    this.value = Integer.parseInt(trimmed);
                    this.overflow = originalValue - this.value;
                }
                
                // Use overflow to create decay factor for ADSR
                this.decayFactor = Math.max(0.1f, Math.min(2.0f, 1.0f + (this.overflow % 1000) / 1000.0f));
            } else {
                // Negative values become 0 with decay based on magnitude
                this.value = 0;
                this.overflow = Math.abs(originalValue);
                this.decayFactor = Math.max(0.1f, Math.min(2.0f, 1.0f + (this.overflow % 1000) / 1000.0f));
            }
        }
    }
    
    /**
     * Extract musical data from an image file
     */
    public static double[] imageToNumericalData(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        return imageToNumericalData(image);
    }
    
    /**
     * Extract musical data from a BufferedImage
     */
    public static double[] imageToNumericalData(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        
        List<Double> dataPoints = new ArrayList<>();
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Sample the image in a grid pattern to extract meaningful data
        int sampleWidth = Math.min(width, 32);  // Maximum 32 samples horizontally
        int sampleHeight = Math.min(height, 16); // Maximum 16 samples vertically
        
        int stepX = width / sampleWidth;
        int stepY = height / sampleHeight;
        
        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                if (x < width && y < height) {
                    int rgb = image.getRGB(x, y);
                    
                    // Extract color components
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    
                    // Convert to musical data points
                    // Use brightness as primary musical parameter
                    double brightness = (0.299 * red + 0.587 * green + 0.114 * blue);
                    
                    // Use color saturation as secondary parameter
                    int max = Math.max(red, Math.max(green, blue));
                    int min = Math.min(red, Math.min(green, blue));
                    double saturation = max > 0 ? ((double)(max - min) / max) * 100 : 0;
                    
                    // Use hue as tertiary parameter
                    double hue = calculateHue(red, green, blue);
                    
                    // Combine into meaningful musical values
                    dataPoints.add(brightness);           // Brightness -> Volume/Velocity
                    dataPoints.add(saturation);          // Saturation -> Timbre/Filter
                    dataPoints.add(hue);                 // Hue -> Pitch/Note
                }
            }
        }
        
        return dataPoints.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * Calculate hue from RGB values
     */
    private static double calculateHue(int red, int green, int blue) {
        double r = red / 255.0;
        double g = green / 255.0;
        double b = blue / 255.0;
        
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double delta = max - min;
        
        if (delta == 0) {
            return 0; // Gray
        }
        
        double hue = 0;
        if (max == r) {
            hue = ((g - b) / delta) % 6;
        } else if (max == g) {
            hue = (b - r) / delta + 2;
        } else {
            hue = (r - g) / delta + 4;
        }
        
        hue *= 60;
        if (hue < 0) hue += 360;
        
        return hue; // 0-360 degrees
    }
    
    /**
     * Extract edge patterns from image for rhythm generation
     */
    public static double[] extractEdgePatterns(BufferedImage image) {
        List<Double> edgeData = new ArrayList<>();
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Simple edge detection using pixel differences
        for (int y = 1; y < height - 1; y += 4) { // Sample every 4th row
            for (int x = 1; x < width - 1; x += 4) { // Sample every 4th column
                int centerPixel = image.getRGB(x, y);
                int rightPixel = image.getRGB(x + 1, y);
                int bottomPixel = image.getRGB(x, y + 1);
                
                // Calculate edge strength
                double horizontalEdge = getPixelDifference(centerPixel, rightPixel);
                double verticalEdge = getPixelDifference(centerPixel, bottomPixel);
                double edgeStrength = Math.sqrt(horizontalEdge * horizontalEdge + verticalEdge * verticalEdge);
                
                edgeData.add(edgeStrength);
            }
        }
        
        return edgeData.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * Calculate difference between two pixels
     */
    private static double getPixelDifference(int pixel1, int pixel2) {
        int r1 = (pixel1 >> 16) & 0xFF;
        int g1 = (pixel1 >> 8) & 0xFF;
        int b1 = pixel1 & 0xFF;
        
        int r2 = (pixel2 >> 16) & 0xFF;
        int g2 = (pixel2 >> 8) & 0xFF;
        int b2 = pixel2 & 0xFF;
        
        return Math.sqrt(Math.pow(r2 - r1, 2) + Math.pow(g2 - g1, 2) + Math.pow(b2 - b1, 2));
    }
    
    /**
     * Analyze image composition for musical structure
     */
    public static ImageAnalysis analyzeImageComposition(BufferedImage image) {
        ImageAnalysis analysis = new ImageAnalysis();
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        double totalBrightness = 0;
        double totalSaturation = 0;
        int pixelCount = 0;
        
        // Analyze overall image characteristics
        for (int y = 0; y < height; y += 2) { // Sample every other pixel for performance
            for (int x = 0; x < width; x += 2) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                
                double brightness = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0;
                totalBrightness += brightness;
                
                int max = Math.max(red, Math.max(green, blue));
                int min = Math.min(red, Math.min(green, blue));
                double saturation = max > 0 ? (double)(max - min) / max : 0;
                totalSaturation += saturation;
                
                pixelCount++;
            }
        }
        
        analysis.averageBrightness = totalBrightness / pixelCount;
        analysis.averageSaturation = totalSaturation / pixelCount;
        analysis.complexity = calculateComplexity(image);
        
        // Suggest musical parameters based on analysis
        analysis.suggestedTempo = (int)(60 + analysis.complexity * 60); // 60-120 BPM
        analysis.suggestedKey = analysis.averageBrightness > 0.5 ? "Major" : "Minor";
        
        return analysis;
    }
    
    /**
     * Calculate image complexity based on color variance
     */
    private static double calculateComplexity(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        double totalVariance = 0;
        int sampleCount = 0;
        
        // Calculate local variance in small regions
        for (int y = 0; y < height - 4; y += 5) {
            for (int x = 0; x < width - 4; x += 5) {
                double regionVariance = calculateRegionVariance(image, x, y, 5, 5);
                totalVariance += regionVariance;
                sampleCount++;
            }
        }
        
        return Math.min(1.0, totalVariance / sampleCount / 10000.0); // Normalize to 0-1
    }
    
    /**
     * Calculate variance in a small region of the image
     */
    private static double calculateRegionVariance(BufferedImage image, int startX, int startY, int width, int height) {
        List<Double> brightness = new ArrayList<>();
        
        for (int y = startY; y < Math.min(startY + height, image.getHeight()); y++) {
            for (int x = startX; x < Math.min(startX + width, image.getWidth()); x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                double pixelBrightness = (0.299 * red + 0.587 * green + 0.114 * blue);
                brightness.add(pixelBrightness);
            }
        }
        
        if (brightness.isEmpty()) return 0;
        
        double mean = brightness.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = brightness.stream()
            .mapToDouble(b -> Math.pow(b - mean, 2))
            .average().orElse(0);
            
        return variance;
    }
    
    // Instance methods for real-time image processing
    
    /**
     * Load an image for processing
     */
    public void loadImage(BufferedImage image) {
        this.currentImage = image;
        this.currentRegion = image; // Default to entire image
        this.selectedArea = null;
    }
    
    /**
     * Set the selected region of the image for processing
     */
    public void setSelectedRegion(Rectangle area, BufferedImage regionImage) {
        this.selectedArea = area;
        this.currentRegion = regionImage;
    }
    
    /**
     * Generate MIDI sequence from the currently selected region
     */
    public javax.sound.midi.Sequence generateMusicFromImage() {
        BufferedImage sourceImage = currentRegion != null ? currentRegion : currentImage;
        if (sourceImage == null) {
            return null;
        }
        
        try {
            // Create a new MIDI sequence
            javax.sound.midi.Sequence sequence = new javax.sound.midi.Sequence(
                javax.sound.midi.Sequence.PPQ, 480);
            javax.sound.midi.Track track = sequence.createTrack();
            
            // Get image data from selected region
            double[] imageData = imageToNumericalData(sourceImage);
            ImageAnalysis analysis = analyzeImageComposition(sourceImage);
            
            // Convert image data to MIDI notes with improved variation
            long tick = 0;
            int[] pentatonicScale = {60, 62, 65, 67, 69, 72, 74, 77, 79, 81, 84}; // Pentatonic scale
            int baseDuration = 120; // Base note duration
            
            // Create multiple passes for richer harmonies
            for (int pass = 0; pass < Math.min(3, imageData.length / 8); pass++) {
                tick = 0; // Reset for each pass
                
                for (int i = pass; i < imageData.length; i += (1 + pass)) {
                    double value = imageData[i];
                    double nextValue = (i + 1 < imageData.length) ? imageData[i + 1] : value;
                    
                    // Use pentatonic scale for more musical results
                    int scaleIndex = (int) (value * (pentatonicScale.length - 1) * brightnessMapping);
                    scaleIndex = Math.max(0, Math.min(pentatonicScale.length - 1, scaleIndex));
                    int note = pentatonicScale[scaleIndex];
                    
                    // Add octave variation based on image position
                    int octaveShift = ((i * 12) / imageData.length) - 6; // -6 to +6 semitones
                    note += octaveShift;
                    note = Math.max(24, Math.min(108, note));
                    
                    // Dynamic velocity based on image data variation
                    int velocity = (int) (40 + (Math.abs(nextValue - value) * 300 * saturationMapping));
                    velocity = Math.max(20, Math.min(127, velocity));
                    
                    // Variable note duration based on color mapping and image data
                    int duration = (int) (baseDuration * (0.5 + value * colorMapping));
                    if (Math.abs(nextValue - value) > 0.1) {
                        duration = (int) (duration * 1.5); // Longer notes for dramatic changes
                    }
                    
                    // Skip very quiet notes to create rhythm
                    if (velocity > 30) {
                        // Use enhanced MIDI event creation with decay handling
                        MidiEventWithDecay noteOnWithDecay = createEnhancedMidiEvent(
                            javax.sound.midi.ShortMessage.NOTE_ON, pass % 2, note, velocity, tick);
                        
                        if (noteOnWithDecay != null && noteOnWithDecay.event != null) {
                            track.add(noteOnWithDecay.event);
                            
                            // Adjust duration based on decay factor from overflow values
                            int adjustedDuration = (int) (duration * noteOnWithDecay.decayFactor);
                            
                            // Add note off with adjusted timing
                            MidiEventWithDecay noteOffWithDecay = createEnhancedMidiEvent(
                                javax.sound.midi.ShortMessage.NOTE_OFF, pass % 2, note, 0, tick + adjustedDuration);
                            
                            if (noteOffWithDecay != null && noteOffWithDecay.event != null) {
                                track.add(noteOffWithDecay.event);
                            }
                        }
                    }
                    
                    // Add some rhythmic variation
                    if (value > 0.7) {
                        tick += duration; // Normal timing
                    } else if (value > 0.3) {
                        tick += duration + (baseDuration / 4); // Slight delay
                    } else {
                        tick += duration / 2; // Faster rhythm for dark areas
                    }
                }
            }
            
            // Add some harmonic bass notes for foundation
            tick = 0;
            for (int i = 0; i < imageData.length; i += 8) {
                double value = imageData[i];
                int bassNote = 36 + (int) (value * 24); // Bass range
                bassNote = Math.max(24, Math.min(60, bassNote));
                
                int velocity = (int) (30 + value * 50);
                int duration = baseDuration * 4; // Longer bass notes
                
                MidiEventWithDecay bassOnWithDecay = createEnhancedMidiEvent(
                    javax.sound.midi.ShortMessage.NOTE_ON, 2, bassNote, velocity, tick);
                if (bassOnWithDecay != null && bassOnWithDecay.event != null) {
                    track.add(bassOnWithDecay.event);
                    
                    // Adjust bass note duration based on decay factor
                    int adjustedDuration = (int) (duration * bassOnWithDecay.decayFactor);
                    
                    MidiEventWithDecay bassOffWithDecay = createEnhancedMidiEvent(
                        javax.sound.midi.ShortMessage.NOTE_OFF, 2, bassNote, 0, tick + adjustedDuration);
                    if (bassOffWithDecay != null && bassOffWithDecay.event != null) {
                        track.add(bassOffWithDecay.event);
                    }
                }
                
                tick += duration;
            }
            
            return sequence;
            
        } catch (Exception e) {
            System.err.println("Error generating music from image: " + e.getMessage());
            return null;
        }
    }
    

    
    /**
     * Enhanced version that returns both MIDI event and decay information
     */
    private MidiEventWithDecay createEnhancedMidiEvent(int command, int channel, 
                                                      int data1, int data2, long tick) {
        try {
            EnhancedMidiData processedData1 = new EnhancedMidiData(data1);
            EnhancedMidiData processedData2 = new EnhancedMidiData(data2);
            
            javax.sound.midi.ShortMessage message = new javax.sound.midi.ShortMessage();
            message.setMessage(command, channel, processedData1.value, processedData2.value);
            
            javax.sound.midi.MidiEvent event = new javax.sound.midi.MidiEvent(message, tick);
            
            // Calculate combined decay factor from both data values
            float combinedDecay = (processedData1.decayFactor + processedData2.decayFactor) / 2.0f;
            
            return new MidiEventWithDecay(event, combinedDecay, processedData2.overflow);
            
        } catch (Exception e) {
            // Fallback to safe values
            return createSafeMidiEventWithDecay(command, channel, data1, data2, tick);
        }
    }
    
    /**
     * Create safe MIDI event with decay when normal creation fails
     */
    private MidiEventWithDecay createSafeMidiEventWithDecay(int command, int channel, 
                                                           int data1, int data2, long tick) {
        try {
            javax.sound.midi.ShortMessage safeMessage = new javax.sound.midi.ShortMessage();
            safeMessage.setMessage(command, Math.max(0, Math.min(15, channel)), 
                                 Math.max(0, Math.min(127, data1)), 
                                 Math.max(0, Math.min(127, data2)));
            
            javax.sound.midi.MidiEvent event = new javax.sound.midi.MidiEvent(safeMessage, tick);
            return new MidiEventWithDecay(event, 1.0f, 0); // Default decay
            
        } catch (Exception e) {
            System.err.println("Failed to create safe MIDI event: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Helper class to bundle MIDI event with decay information
     */
    private static class MidiEventWithDecay {
        public final javax.sound.midi.MidiEvent event;
        public final float decayFactor;
        
        public MidiEventWithDecay(javax.sound.midi.MidiEvent event, float decayFactor, int overflowValue) {
            this.event = event;
            this.decayFactor = decayFactor;
            // Note: overflowValue could be used for future ADSR envelope modifications
        }
    }
    
    // Getters and setters for mapping parameters
    
    public void setBrightnessMapping(float brightness) {
        this.brightnessMapping = Math.max(0.0f, Math.min(1.0f, brightness));
    }
    
    public void setColorMapping(float color) {
        this.colorMapping = Math.max(0.0f, Math.min(1.0f, color));
    }
    
    public void setSaturationMapping(float saturation) {
        this.saturationMapping = Math.max(0.0f, Math.min(1.0f, saturation));
    }
    
    public float getBrightnessMapping() {
        return brightnessMapping;
    }
    
    public float getColorMapping() {
        return colorMapping;
    }
    
    public float getSaturationMapping() {
        return saturationMapping;
    }
    
    public BufferedImage getCurrentImage() {
        return currentImage;
    }
    
    public BufferedImage getCurrentRegion() {
        return currentRegion;
    }
    
    public Rectangle getSelectedArea() {
        return selectedArea;
    }
    
    /**
     * Image analysis results
     */
    public static class ImageAnalysis {
        public double averageBrightness;
        public double averageSaturation;
        public double complexity;
        public int suggestedTempo;
        public String suggestedKey;
        
        @Override
        public String toString() {
            return String.format("Image Analysis: Brightness=%.2f, Saturation=%.2f, Complexity=%.2f, Suggested: %s key at %d BPM",
                averageBrightness, averageSaturation, complexity, suggestedKey, suggestedTempo);
        }
    }
}
