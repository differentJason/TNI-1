package com.tni.synthesizer.generative;

/**
 * Individual synthesizer voice for polyphonic synthesis
 * Each voice can play one note at a time with independent envelope and modulation
 */
public class SynthVoice {
    
    public enum VoiceState {
        IDLE("Idle"),
        ACTIVE("Active"),
        RELEASE("Release");
        
        private final String displayName;
        
        VoiceState(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final int sampleRate;
    private final int voiceNumber;
    
    // Voice parameters
    private VoiceState state;
    private int midiNote;
    private int velocity;
    private float frequency;
    private float amplitude;
    
    // Audio generation
    private final WaveformGenerator waveformGenerator;
    private final ADSREnvelope envelope;
    private final LFO pitchLFO;
    private final LFO amplitudeLFO;
    private final ResonantFilter filter;
    
    // Oscillator state
    private double phase;
    private SynthChannel.Waveform waveform;
    
    // Modulation parameters
    private float pitchBend = 0.0f;      // -1.0 to +1.0 (semitones)
    private float modWheel = 0.0f;       // 0.0 to 1.0
    private float aftertouch = 0.0f;     // 0.0 to 1.0
    
    // Voice management
    private long noteOnTime;
    private boolean sustainPedal = false;
    
    public SynthVoice(int voiceNumber, int sampleRate) {
        this.voiceNumber = voiceNumber;
        this.sampleRate = sampleRate;
        this.state = VoiceState.IDLE;
        
        // Initialize audio components
        this.waveformGenerator = new WaveformGenerator(sampleRate);
        this.envelope = new ADSREnvelope(sampleRate);
        this.pitchLFO = new LFO(sampleRate);
        this.amplitudeLFO = new LFO(sampleRate);
        this.filter = new ResonantFilter(sampleRate);
        
        // Default settings
        this.waveform = SynthChannel.Waveform.SAWTOOTH;
        this.phase = 0.0;
        
        // Configure LFOs
        pitchLFO.setFrequency(5.0f);
        pitchLFO.setDepth(0.1f);
        pitchLFO.setWaveform(LFO.LFOWaveform.SINE);
        pitchLFO.setTarget(LFO.LFOTarget.FREQUENCY);
        
        amplitudeLFO.setFrequency(3.0f);
        amplitudeLFO.setDepth(0.2f);
        amplitudeLFO.setWaveform(LFO.LFOWaveform.TRIANGLE);
        amplitudeLFO.setTarget(LFO.LFOTarget.AMPLITUDE);
        
        // Configure filter
        filter.setFilterType(ResonantFilter.FilterType.LOW_PASS);
        filter.setCutoffFrequency(8000.0f);
        filter.setResonance(1.0f);
    }
    
    /**
     * Trigger a note on this voice
     */
    public void noteOn(int midiNote, int velocity) {
        this.midiNote = midiNote;
        this.velocity = velocity;
        this.frequency = midiNoteToFrequency(midiNote);
        this.amplitude = velocity / 127.0f;
        this.state = VoiceState.ACTIVE;
        this.noteOnTime = System.currentTimeMillis();
        
        // Trigger envelope
        envelope.noteOn();
        
        // Reset oscillator phase for consistent attack
        phase = 0.0;
    }
    
    /**
     * Release the note on this voice
     */
    public void noteOff() {
        if (state == VoiceState.ACTIVE) {
            if (!sustainPedal) {
                envelope.noteOff();
                state = VoiceState.RELEASE;
            }
        }
    }
    
    /**
     * Force the voice to stop immediately
     */
    public void killVoice() {
        state = VoiceState.IDLE;
        envelope.reset();
        phase = 0.0;
    }
    
    /**
     * Generate the next audio sample
     */
    public float getNextSample() {
        if (state == VoiceState.IDLE) {
            return 0.0f;
        }
        
        // Get envelope value
        float envelopeLevel = envelope.getNextValue();
        
        // Check if envelope has finished
        if (state == VoiceState.RELEASE && envelope.isFinished()) {
            state = VoiceState.IDLE;
            return 0.0f;
        }
        
        // Calculate current frequency with modulation
        float currentFreq = frequency;
        
        // Apply pitch bend
        if (pitchBend != 0.0f) {
            float pitchFactor = (float) Math.pow(2.0, pitchBend / 12.0);
            currentFreq *= pitchFactor;
        }
        
        // Apply pitch LFO
        float pitchLFOValue = pitchLFO.getNextValue();
        if (modWheel > 0.0f) {
            float pitchModDepth = pitchLFO.getDepth() * modWheel;
            float pitchModFactor = (float) Math.pow(2.0, (pitchLFOValue * pitchModDepth) / 12.0);
            currentFreq *= pitchModFactor;
        }
        
        // Generate waveform sample
        float sample = waveformGenerator.generateSample(waveform, phase);
        
        // Apply amplitude envelope
        sample *= envelopeLevel * amplitude;
        
        // Apply amplitude LFO
        float ampLFOValue = amplitudeLFO.getNextValue();
        if (aftertouch > 0.0f) {
            float ampMod = 1.0f + (ampLFOValue * amplitudeLFO.getDepth() * aftertouch);
            sample *= Math.max(0.0f, ampMod);
        }
        
        // Apply filter
        sample = filter.process(sample);
        
        // Advance phase
        phase += currentFreq / sampleRate;
        if (phase >= 1.0) {
            phase -= 1.0;
        }
        
        return sample;
    }
    
    /**
     * Process a buffer of samples
     */
    public void processBuffer(float[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = getNextSample();
        }
    }
    
    /**
     * Convert MIDI note number to frequency in Hz
     */
    private float midiNoteToFrequency(int midiNote) {
        // A4 (MIDI note 69) = 440 Hz
        return (float) (440.0 * Math.pow(2.0, (midiNote - 69) / 12.0));
    }
    
    /**
     * Check if this voice is available for assignment
     */
    public boolean isAvailable() {
        return state == VoiceState.IDLE;
    }
    
    /**
     * Check if this voice is playing the specified MIDI note
     */
    public boolean isPlayingNote(int midiNote) {
        return (state == VoiceState.ACTIVE || state == VoiceState.RELEASE) && this.midiNote == midiNote;
    }
    
    /**
     * Get voice priority for voice stealing (lower number = higher priority to steal)
     */
    public int getStealPriority() {
        switch (state) {
            case IDLE:
                return 0; // Highest priority - voice is free
            case RELEASE:
                return 1; // Medium priority - voice is releasing
            case ACTIVE:
                // Lower priority for newer notes, higher for older notes
                long age = System.currentTimeMillis() - noteOnTime;
                return (int) Math.min(1000, 100 + age / 10);
            default:
                return 1000; // Lowest priority
        }
    }
    
    // Control methods
    public void setPitchBend(float pitchBend) {
        this.pitchBend = Math.max(-2.0f, Math.min(2.0f, pitchBend)); // ±2 semitones
    }
    
    public void setModWheel(float modWheel) {
        this.modWheel = Math.max(0.0f, Math.min(1.0f, modWheel));
    }
    
    public void setAftertouch(float aftertouch) {
        this.aftertouch = Math.max(0.0f, Math.min(1.0f, aftertouch));
    }
    
    public void setSustainPedal(boolean sustainPedal) {
        this.sustainPedal = sustainPedal;
        
        // If sustain pedal is released and voice is in release state, actually release it
        if (!sustainPedal && state == VoiceState.RELEASE) {
            envelope.noteOff();
        }
    }
    
    public void setWaveform(SynthChannel.Waveform waveform) {
        this.waveform = waveform;
    }
    
    // Component getters for external configuration
    public ADSREnvelope getEnvelope() { return envelope; }
    public LFO getPitchLFO() { return pitchLFO; }
    public LFO getAmplitudeLFO() { return amplitudeLFO; }
    public ResonantFilter getFilter() { return filter; }
    
    // Status getters
    public VoiceState getState() { return state; }
    public int getMidiNote() { return midiNote; }
    public int getVelocity() { return velocity; }
    public float getFrequency() { return frequency; }
    public int getVoiceNumber() { return voiceNumber; }
    public SynthChannel.Waveform getWaveform() { return waveform; }
    
    /**
     * Reset voice to initial state
     */
    public void reset() {
        state = VoiceState.IDLE;
        midiNote = 0;
        velocity = 0;
        frequency = 0.0f;
        amplitude = 0.0f;
        phase = 0.0;
        pitchBend = 0.0f;
        modWheel = 0.0f;
        aftertouch = 0.0f;
        sustainPedal = false;
        
        envelope.reset();
        pitchLFO.reset();
        amplitudeLFO.reset();
        filter.reset();
    }
}