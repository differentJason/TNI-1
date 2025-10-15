package com.tni.synthesizer.generative;

import java.util.*;

/**
 * Pattern matrix for routing generated patterns to multiple synthesizer channels
 */
public class PatternMatrix {
    
    public static class Pattern {
        private final String name;
        private final List<Note> notes;
        private final int lengthInBeats;
        
        public Pattern(String name, int lengthInBeats) {
            this.name = name;
            this.lengthInBeats = lengthInBeats;
            this.notes = new ArrayList<>();
        }
        
        public void addNote(int beat, int midiNote, int velocity, int duration) {
            notes.add(new Note(beat, midiNote, velocity, duration));
        }
        
        public List<Note> getNotes() {
            return new ArrayList<>(notes);
        }
        
        public String getName() {
            return name;
        }
        
        public int getLengthInBeats() {
            return lengthInBeats;
        }
        
        public void clear() {
            notes.clear();
        }
    }
    
    public static class Note {
        private final int beat;
        private final int midiNote;
        private final int velocity;
        private final int duration;
        
        public Note(int beat, int midiNote, int velocity, int duration) {
            this.beat = beat;
            this.midiNote = midiNote;
            this.velocity = velocity;
            this.duration = duration;
        }
        
        public int getBeat() { return beat; }
        public int getMidiNote() { return midiNote; }
        public int getVelocity() { return velocity; }
        public int getDuration() { return duration; }
    }
    
    public static class ChannelAssignment {
        private final int channelIndex;
        private final String patternName;
        private final float volume;
        private final int transpose;
        private final boolean muted;
        
        public ChannelAssignment(int channelIndex, String patternName, float volume, int transpose, boolean muted) {
            this.channelIndex = channelIndex;
            this.patternName = patternName;
            this.volume = volume;
            this.transpose = transpose;
            this.muted = muted;
        }
        
        public int getChannelIndex() { return channelIndex; }
        public String getPatternName() { return patternName; }
        public float getVolume() { return volume; }
        public int getTranspose() { return transpose; }
        public boolean isMuted() { return muted; }
    }
    
    private final Map<String, Pattern> patterns;
    private final Map<Integer, List<ChannelAssignment>> channelAssignments;
    private final int maxChannels;
    private final int beatsPerMinute;
    private final int beatsPerPattern;
    
    // Playback state
    private int currentBeat;
    private boolean playing;
    private long lastBeatTime;
    private final long beatInterval; // milliseconds per beat
    
    public PatternMatrix(int maxChannels, int beatsPerMinute, int beatsPerPattern) {
        this.maxChannels = maxChannels;
        this.beatsPerMinute = beatsPerMinute;
        this.beatsPerPattern = beatsPerPattern;
        this.patterns = new HashMap<>();
        this.channelAssignments = new HashMap<>();
        this.currentBeat = 0;
        this.playing = false;
        this.lastBeatTime = 0;
        this.beatInterval = 60000 / beatsPerMinute; // Convert BPM to milliseconds per beat
        
        // Initialize channel assignments
        for (int i = 0; i < maxChannels; i++) {
            channelAssignments.put(i, new ArrayList<>());
        }
        
        // Create some default patterns
        createDefaultPatterns();
    }
    
    private void createDefaultPatterns() {
        // Kick drum pattern
        Pattern kick = new Pattern("Kick", 16);
        kick.addNote(0, 36, 127, 1);   // Beat 1
        kick.addNote(4, 36, 100, 1);   // Beat 5
        kick.addNote(8, 36, 127, 1);   // Beat 9
        kick.addNote(12, 36, 110, 1);  // Beat 13
        patterns.put("Kick", kick);
        
        // Snare pattern
        Pattern snare = new Pattern("Snare", 16);
        snare.addNote(4, 38, 120, 1);  // Beat 5
        snare.addNote(12, 38, 115, 1); // Beat 13
        patterns.put("Snare", snare);
        
        // Hi-hat pattern
        Pattern hihat = new Pattern("Hi-Hat", 16);
        for (int i = 0; i < 16; i += 2) {
            hihat.addNote(i, 42, 80 + (i % 4) * 10, 1);
        }
        patterns.put("Hi-Hat", hihat);
        
        // Bass line pattern
        Pattern bass = new Pattern("Bass", 16);
        bass.addNote(0, 48, 100, 4);   // C3
        bass.addNote(4, 55, 90, 2);    // G3
        bass.addNote(8, 48, 100, 4);   // C3
        bass.addNote(12, 52, 95, 2);   // E3
        patterns.put("Bass", bass);
        
        // Melody pattern
        Pattern melody = new Pattern("Melody", 16);
        melody.addNote(0, 72, 90, 2);  // C5
        melody.addNote(2, 74, 85, 2);  // D5
        melody.addNote(4, 76, 90, 2);  // E5
        melody.addNote(6, 74, 85, 2);  // D5
        melody.addNote(8, 72, 90, 4);  // C5
        melody.addNote(12, 69, 80, 4); // A4
        patterns.put("Melody", melody);
    }
    
    /**
     * Assign a pattern to a channel
     */
    public void assignPatternToChannel(int channelIndex, String patternName, float volume, int transpose) {
        if (channelIndex >= 0 && channelIndex < maxChannels && patterns.containsKey(patternName)) {
            List<ChannelAssignment> assignments = channelAssignments.get(channelIndex);
            assignments.clear(); // Clear existing assignments for this channel
            assignments.add(new ChannelAssignment(channelIndex, patternName, volume, transpose, false));
        }
    }
    
    /**
     * Add multiple pattern assignments to a channel (for layering)
     */
    public void addPatternToChannel(int channelIndex, String patternName, float volume, int transpose) {
        if (channelIndex >= 0 && channelIndex < maxChannels && patterns.containsKey(patternName)) {
            List<ChannelAssignment> assignments = channelAssignments.get(channelIndex);
            assignments.add(new ChannelAssignment(channelIndex, patternName, volume, transpose, false));
        }
    }
    
    /**
     * Clear all pattern assignments for a channel
     */
    public void clearChannel(int channelIndex) {
        if (channelIndex >= 0 && channelIndex < maxChannels) {
            channelAssignments.get(channelIndex).clear();
        }
    }
    
    /**
     * Mute/unmute a channel
     */
    public void muteChannel(int channelIndex, boolean muted) {
        if (channelIndex >= 0 && channelIndex < maxChannels) {
            List<ChannelAssignment> assignments = channelAssignments.get(channelIndex);
            for (int i = 0; i < assignments.size(); i++) {
                ChannelAssignment old = assignments.get(i);
                assignments.set(i, new ChannelAssignment(old.channelIndex, old.patternName, 
                                                       old.volume, old.transpose, muted));
            }
        }
    }
    
    /**
     * Update playback and return notes that should be triggered this frame
     */
    public Map<Integer, List<Note>> updatePlayback() {
        Map<Integer, List<Note>> channelNotes = new HashMap<>();
        
        if (!playing) {
            return channelNotes;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Check if it's time for the next beat
        if (currentTime - lastBeatTime >= beatInterval) {
            lastBeatTime = currentTime;
            
            // Get notes for current beat from all channel assignments
            for (Map.Entry<Integer, List<ChannelAssignment>> entry : channelAssignments.entrySet()) {
                int channelIndex = entry.getKey();
                List<ChannelAssignment> assignments = entry.getValue();
                
                List<Note> notesForChannel = new ArrayList<>();
                
                for (ChannelAssignment assignment : assignments) {
                    if (assignment.isMuted()) continue;
                    
                    Pattern pattern = patterns.get(assignment.getPatternName());
                    if (pattern == null) continue;
                    
                    // Find notes that should play on this beat
                    for (Note note : pattern.getNotes()) {
                        if (note.getBeat() == currentBeat) {
                            // Apply transpose and volume
                            int transposedNote = note.getMidiNote() + assignment.getTranspose();
                            int scaledVelocity = (int) (note.getVelocity() * assignment.getVolume());
                            scaledVelocity = Math.max(1, Math.min(127, scaledVelocity));
                            
                            Note processedNote = new Note(note.getBeat(), transposedNote, 
                                                        scaledVelocity, note.getDuration());
                            notesForChannel.add(processedNote);
                        }
                    }
                }
                
                if (!notesForChannel.isEmpty()) {
                    channelNotes.put(channelIndex, notesForChannel);
                }
            }
            
            // Advance to next beat
            currentBeat = (currentBeat + 1) % beatsPerPattern;
        }
        
        return channelNotes;
    }
    
    /**
     * Start playback
     */
    public void play() {
        playing = true;
        lastBeatTime = System.currentTimeMillis();
    }
    
    /**
     * Stop playback
     */
    public void stop() {
        playing = false;
        currentBeat = 0;
    }
    
    /**
     * Pause/resume playback
     */
    public void setPaused(boolean paused) {
        if (paused && playing) {
            playing = false;
        } else if (!paused && !playing) {
            playing = true;
            lastBeatTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Create a new pattern
     */
    public void createPattern(String name, int lengthInBeats) {
        patterns.put(name, new Pattern(name, lengthInBeats));
    }
    
    /**
     * Get a pattern by name
     */
    public Pattern getPattern(String name) {
        return patterns.get(name);
    }
    
    /**
     * Get all pattern names
     */
    public Set<String> getPatternNames() {
        return new HashSet<>(patterns.keySet());
    }
    
    /**
     * Generate a random pattern based on data input
     */
    public void generatePatternFromData(String patternName, float[] data, int lengthInBeats) {
        Pattern pattern = new Pattern(patternName, lengthInBeats);
        
        // Use data to generate notes
        for (int i = 0; i < Math.min(data.length, lengthInBeats); i++) {
            float value = data[i];
            
            // Map data value to musical parameters
            int beat = i;
            int midiNote = 60 + (int) (value * 24); // C4 to C6 range
            int velocity = 64 + (int) (Math.abs(value) * 63); // 64-127 range
            int duration = 1 + (int) (Math.abs(value) * 3); // 1-4 beats
            
            // Add note with some probability based on data value
            if (Math.abs(value) > 0.1f) { // Threshold to avoid too many notes
                pattern.addNote(beat, midiNote, velocity, duration);
            }
        }
        
        patterns.put(patternName, pattern);
    }
    
    // Getters
    public int getCurrentBeat() { return currentBeat; }
    public boolean isPlaying() { return playing; }
    public int getBeatsPerMinute() { return beatsPerMinute; }
    public int getBeatsPerPattern() { return beatsPerPattern; }
    public int getMaxChannels() { return maxChannels; }
    
    /**
     * Get channel assignments for a specific channel
     */
    public List<ChannelAssignment> getChannelAssignments(int channelIndex) {
        if (channelIndex >= 0 && channelIndex < maxChannels) {
            return new ArrayList<>(channelAssignments.get(channelIndex));
        }
        return new ArrayList<>();
    }
}