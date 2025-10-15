package com.tni.synthesizer.weather;

/**
 * Exception thrown when weather service operations fail
 */
public class WeatherServiceException extends Exception {
    
    public WeatherServiceException(String message) {
        super(message);
    }
    
    public WeatherServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WeatherServiceException(Throwable cause) {
        super(cause);
    }
}