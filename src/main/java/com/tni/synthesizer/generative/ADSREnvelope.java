package com.tni.synthesizer.generative;

/**
 * ADSR (Attack, Decay, Sustain, Release) Envelope Generator
 */
public class ADSREnvelope {
    
    public enum EnvelopeStage {
        ATTACK,
        DECAY,
        SUSTAIN,
        RELEASE,
        FINISHED
    }
    
    private final int sampleRate;
    
    // Envelope parameters
    private float attackTime;   // Attack time in seconds
    private float decayTime;    // Decay time in seconds
    private float sustainLevel; // Sustain level (0.0 to 1.0)
    private float releaseTime;  // Release time in seconds
    
    // State variables
    private EnvelopeStage currentStage;
    private float currentLevel;
    private boolean noteReleased;
    
    // Calculated increments for each stage
    private float attackIncrement;
    private float decayIncrement;
    private float releaseIncrement;
    
    public ADSREnvelope(int sampleRate) {
        this.sampleRate = sampleRate;
        
        // Default ADSR settings
        this.attackTime = 0.01f;   // 10ms
        this.decayTime = 0.2f;     // 200ms
        this.sustainLevel = 0.7f;  // 70%
        this.releaseTime = 0.5f;   // 500ms
        
        reset();
        calculateIncrements();
    }
    
    /**
     * Start the envelope (note on)
     */
    public void noteOn() {
        currentStage = EnvelopeStage.ATTACK;
        currentLevel = 0.0f;
        noteReleased = false;
        calculateIncrements();
    }
    
    /**
     * Release the envelope (note off)
     */
    public void noteOff() {
        if (!noteReleased) {
            noteReleased = true;
            currentStage = EnvelopeStage.RELEASE;
            // Release starts from current level, not from sustain level
        }
    }
    
    /**
     * Reset envelope to initial state
     */
    public void reset() {
        currentStage = EnvelopeStage.FINISHED;
        currentLevel = 0.0f;
        noteReleased = false;
    }
    
    /**
     * Get the next envelope value
     * @return Envelope level (0.0 to 1.0)
     */
    public float getNextValue() {
        if (currentStage == EnvelopeStage.FINISHED) {
            return 0.0f;
        }
        
        switch (currentStage) {
            case ATTACK:
                currentLevel += attackIncrement;
                if (currentLevel >= 1.0f) {
                    currentLevel = 1.0f;
                    currentStage = EnvelopeStage.DECAY;
                }
                break;
                
            case DECAY:
                currentLevel -= decayIncrement;
                if (currentLevel <= sustainLevel) {
                    currentLevel = sustainLevel;
                    currentStage = EnvelopeStage.SUSTAIN;
                }
                break;
                
            case SUSTAIN:
                currentLevel = sustainLevel;
                // Stay in sustain until note off
                break;
                
            case RELEASE:
                currentLevel -= releaseIncrement;
                if (currentLevel <= 0.0f) {
                    currentLevel = 0.0f;
                    currentStage = EnvelopeStage.FINISHED;
                }
                break;
                
            case FINISHED:
                currentLevel = 0.0f;
                break;
        }
        
        return Math.max(0.0f, Math.min(1.0f, currentLevel));
    }
    
    /**
     * Check if envelope has finished
     */
    public boolean isFinished() {
        return currentStage == EnvelopeStage.FINISHED;
    }
    
    /**
     * Check if envelope is in release stage
     */
    public boolean isReleasing() {
        return currentStage == EnvelopeStage.RELEASE;
    }
    
    private void calculateIncrements() {
        // Calculate per-sample increments for each stage
        attackIncrement = attackTime > 0 ? 1.0f / (attackTime * sampleRate) : 1.0f;
        decayIncrement = decayTime > 0 ? (1.0f - sustainLevel) / (decayTime * sampleRate) : (1.0f - sustainLevel);
        releaseIncrement = releaseTime > 0 ? 1.0f / (releaseTime * sampleRate) : 1.0f;
    }
    
    // Getters and setters
    public float getAttackTime() {
        return attackTime;
    }
    
    public void setAttackTime(float attackTime) {
        this.attackTime = Math.max(0.001f, Math.min(5.0f, attackTime));
        calculateIncrements();
    }
    
    public float getDecayTime() {
        return decayTime;
    }
    
    public void setDecayTime(float decayTime) {
        this.decayTime = Math.max(0.001f, Math.min(5.0f, decayTime));
        calculateIncrements();
    }
    
    public float getSustainLevel() {
        return sustainLevel;
    }
    
    public void setSustainLevel(float sustainLevel) {
        this.sustainLevel = Math.max(0.0f, Math.min(1.0f, sustainLevel));
        calculateIncrements();
    }
    
    public float getReleaseTime() {
        return releaseTime;
    }
    
    public void setReleaseTime(float releaseTime) {
        this.releaseTime = Math.max(0.001f, Math.min(10.0f, releaseTime));
        calculateIncrements();
    }
    
    public EnvelopeStage getCurrentStage() {
        return currentStage;
    }
    
    public float getCurrentLevel() {
        return currentLevel;
    }
}